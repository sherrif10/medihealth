/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.medihealth.report;

import static org.openmrs.module.medihealth.report.NhmisSql.conceptIds;
import static org.openmrs.module.medihealth.report.NhmisSql.inPeriod;

import java.util.Arrays;
import java.util.List;

/**
 * The SQL of the weekly MRDT and ACT report: malaria rapid tests done (and how many were positive)
 * and ACT prescriptions by the patient's age, for one health facility or all PHCs, over the chosen
 * period.
 * <p>
 * No double counting:
 * <ul>
 * <li>MRDT: one per patient per day. The lab can save the same result more than once, and a test is
 * recorded once whether it comes from the lab or the OPD Malaria RDT form. A day with any positive
 * result counts as positive. "Not performed" answers are not tests.</li>
 * <li>ACT: one per patient over the period, in the age category of their first ACT in the period.
 * Prescriptions count, not dispensing (dispensing is not recorded for ACT).</li>
 * </ul>
 * Each row is a category and its total, in the order of the reporting sheet.
 */
public final class MrdtActWeekly {
	
	private MrdtActWeekly() {
	}
	
	/** Categories of the report, in order: the label shown and the condition on the ACT patient. */
	public enum ActCategory {
		ACT1("ACT 1 (under 5 years)", "a.age_m < 60"),
		ACT2("ACT 2 (5 to under 10 years)", "a.age_m >= 60 AND a.age_y < 10"),
		ACT3("ACT 3 (10 to under 15 years)", "a.age_y >= 10 AND a.age_y < 15"),
		ACT4("ACT 4 (15 years and above)", "a.age_y >= 15"),
		AGE_UNKNOWN("ACT (age not recorded)", "a.age_m IS NULL");
		
		public final String label;
		
		final String condition;
		
		ActCategory(String label, String condition) {
			this.label = label;
			this.condition = condition;
		}
		
		/** The category of a patient aged {@code ageInMonths} on the day of their first ACT. */
		public static ActCategory of(Integer ageInMonths) {
			if (ageInMonths == null || ageInMonths < 0) {
				return AGE_UNKNOWN;
			}
			if (ageInMonths < 60) {
				return ACT1;
			}
			if (ageInMonths < 120) {
				return ACT2;
			}
			if (ageInMonths < 180) {
				return ACT3;
			}
			return ACT4;
		}
	}
	
	public static final String MRDT_TESTED = "MRDT tests done";
	
	public static final String MRDT_POSITIVE = "MRDT positive";
	
	public static final String MRDT_NEGATIVE = "MRDT negative";
	
	public static final String ACT_TOTAL = "ACT total";
	
	/**
	 * "Procedure not performed" and "not performed, no test materials": answers that are not tests.
	 */
	static final List<String> NOT_PERFORMED = Arrays.asList("160414AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "160352AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/**
	 * The location condition: the chosen facility (and rooms under it), or every PHC (and rooms
	 * under them).
	 */
	static String facility(String locationIdExpr, boolean perFacility) {
		if (perFacility) {
			return NhmisSql.atFacility(locationIdExpr);
		}
		return locationIdExpr + " IN (SELECT zl.location_id FROM location zl "
		        + "LEFT JOIN location zl2 ON zl2.location_id = zl.parent_location "
		        + "LEFT JOIN location zl3 ON zl3.location_id = zl2.parent_location "
		        + "WHERE zl.name LIKE '%PHC%' OR zl2.name LIKE '%PHC%' OR zl3.name LIKE '%PHC%')";
	}
	
	/**
	 * One row per patient and day with an MRDT result: positive = 1 when any result that day was
	 * positive.
	 */
	static String mrdtPatientDays(boolean perFacility) {
		return "SELECT o.person_id, DATE(o.obs_datetime) AS test_day, MAX(o.value_coded IN "
		        + conceptIds(NhmisConcepts.MALARIA_RDT_POSITIVE) + ") AS positive FROM obs o "
		        + "JOIN person p ON p.person_id = o.person_id AND p.voided = 0 " + "WHERE o.voided = 0 AND o.concept_id IN "
		        + conceptIds(NhmisConcepts.MALARIA_RDT) + " AND (o.value_coded IS NULL OR o.value_coded NOT IN "
		        + conceptIds(NOT_PERFORMED) + ") AND " + inPeriod("o.obs_datetime") + " AND "
		        + facility("o.location_id", perFacility) + " GROUP BY o.person_id, DATE(o.obs_datetime)";
	}
	
	/**
	 * One row per patient prescribed an ACT in the period, with their age on the day of the first
	 * one.
	 */
	static String actPatients(boolean perFacility) {
		return "SELECT f.patient_id, TIMESTAMPDIFF(MONTH, p.birthdate, f.first_act) AS age_m, "
		        + "TIMESTAMPDIFF(YEAR, p.birthdate, f.first_act) AS age_y FROM (SELECT o.patient_id, MIN(o.date_activated) AS first_act "
		        + "FROM orders o JOIN drug_order dro ON dro.order_id = o.order_id "
		        + "LEFT JOIN drug d ON d.drug_id = dro.drug_inventory_id "
		        + "WHERE o.voided = 0 AND o.order_action <> 'DISCONTINUE' AND " + inPeriod("o.date_activated") + " AND "
		        + facility(NhmisSql.encounterLocation("o.encounter_id"), perFacility) + " AND "
		        + NhmisConcepts.ACT.sql("o", "dro", "d") + " GROUP BY o.patient_id) f "
		        + "JOIN person p ON p.person_id = f.patient_id AND p.voided = 0";
	}
	
	/** The report rows: category, total; MRDT first, then ACT by age and the ACT total. */
	public static String sql(boolean perFacility) {
		String mrdt = "(" + mrdtPatientDays(perFacility) + ") m";
		String act = "(" + actPatients(perFacility) + ") a";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT 1 AS sort_order, '").append(MRDT_TESTED).append("' AS category, COUNT(*) AS total FROM ")
		        .append(mrdt);
		sql.append("\nUNION ALL SELECT 2, '").append(MRDT_POSITIVE).append("', COALESCE(SUM(m.positive), 0) FROM ")
		        .append(mrdt);
		sql.append("\nUNION ALL SELECT 3, '").append(MRDT_NEGATIVE)
		        .append("', COUNT(*) - COALESCE(SUM(m.positive), 0) FROM ").append(mrdt);
		int order = 4;
		for (ActCategory category : ActCategory.values()) {
			sql.append("\nUNION ALL SELECT ").append(order++).append(", '").append(category.label)
			        .append("', COUNT(CASE WHEN ").append(category.condition).append(" THEN 1 END) FROM ").append(act);
		}
		sql.append("\nUNION ALL SELECT ").append(order).append(", '").append(ACT_TOTAL).append("', COUNT(*) FROM ")
		        .append(act);
		return "SELECT category, total FROM (\n" + sql + "\n) r ORDER BY sort_order";
	}
}
