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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;

/**
 * SQL building blocks for the NHMIS Monthly Summary Form sections that count clinical events (rows
 * 94-180). Each section is a set of {@link NhmisBlock}s; each block counts over one "event" source,
 * and every event source returns the same three columns: {@code event_id}, {@code patient_id} and
 * {@code event_date}.
 * <p>
 * Event sources available:
 * <ul>
 * <li>encounters of one type ({@link #encounters})</li>
 * <li>diagnoses: visit-note diagnoses and conditions ({@link #diagnoses}, {@link #firstDiagnoses})</li>
 * <li>obs answers and lab results ({@link #obsAnswers}, {@link #obsResults}, {@link #obsTrue},
 * {@link #obsAtLeast})</li>
 * <li>drug orders, matched by name ({@link #drugOrders})</li>
 * </ul>
 * Drugs are matched by regular expressions over the drug name, the drug concept's names and the
 * dosage form rather than by uuid, because the same medicine exists under many concepts and drugs
 * on a live formulary (brand names, strengths, duplicates); a new pharmacy item with a recognisable
 * name is then counted without a code change.
 * <p>
 * The reporting module strips anything after {@code --} as an SQL comment, so no fragment here may
 * contain two hyphens in a row.
 */
public final class NhmisSql {
	
	/** Exclusive end of the reporting period: the day after endDate, so the whole last day counts. */
	public static final String PERIOD_END = "DATE_ADD(DATE(:endDate), INTERVAL 1 DAY)";
	
	public static final String TRUE_ID = BooleanObsIndicatorFactory.TRUE_CONCEPT_ID_SQL;
	
	/** CIEL "Positive" and the local "+" answer used by the older qualitative lab tests. */
	public static final List<String> POSITIVE_RESULTS = Arrays.asList("703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "9982fc9a-575b-4af7-9407-1c12653f357b");
	
	/** Lab answers that mean the test was not actually done, so the person was not "tested". */
	public static final List<String> NOT_PERFORMED = Arrays.asList("160352AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "160414AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final String INJECTABLE = "inj|vial|ampoule|intraven|intramusc|parenteral";
	
	public static final String RECTAL = "rect|suppos";
	
	private NhmisSql() {
	}
	
	public static SqlDataSetDefinition dataSet(String name, String description, NhmisBlock... blocks) {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName(name);
		dsd.setDescription(description);
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		StringBuilder sql = new StringBuilder("SELECT *\nFROM ");
		for (int i = 0; i < blocks.length; i++) {
			if (i > 0) {
				sql.append("\nCROSS JOIN ");
			}
			sql.append(blocks[i].toSql()).append(" b").append(i);
		}
		dsd.setSqlQuery(sql.toString());
		return dsd;
	}
	
	public static String inPeriod(String column) {
		return column + " >= :startDate AND " + column + " < " + PERIOD_END;
	}
	
	public static String conceptId(String uuid) {
		return "(SELECT concept_id FROM concept WHERE uuid = '" + uuid + "')";
	}
	
	public static String conceptIds(Collection<String> uuids) {
		StringBuilder in = new StringBuilder();
		for (String u : uuids) {
			in.append(in.length() == 0 ? "" : ", ").append('\'').append(u).append('\'');
		}
		return "(SELECT concept_id FROM concept WHERE uuid IN (" + in + "))";
	}
	
	public static String union(String... sources) {
		StringBuilder sql = new StringBuilder();
		for (String s : sources) {
			sql.append(sql.length() == 0 ? "" : "\n  UNION ALL ").append(s);
		}
		return sql.toString();
	}
	
	// ---- event sources -------------------------------------------------------------------------
	
	/** Encounters of the given type in the period; {@code event_id} is the encounter_id. */
	public static String encounters(String encounterTypeUuid) {
		return "SELECT e.encounter_id AS event_id, e.patient_id, e.encounter_datetime AS event_date FROM encounter e "
		        + "WHERE e.voided = 0 AND e.encounter_type = (SELECT encounter_type_id FROM encounter_type WHERE uuid = '"
		        + encounterTypeUuid + "') AND " + inPeriod("e.encounter_datetime");
	}
	
	/** Visit-note diagnoses and conditions with one of the given concepts, recorded in the period. */
	public static String diagnoses(Collection<String> uuids) {
		String ids = conceptIds(uuids);
		return "SELECT CONCAT('d', ed.diagnosis_id) AS event_id, ed.patient_id, e.encounter_datetime AS event_date "
		        + "FROM encounter_diagnosis ed JOIN encounter e ON e.encounter_id = ed.encounter_id AND e.voided = 0 "
		        + "WHERE ed.voided = 0 AND ed.diagnosis_coded IN " + ids + " AND " + inPeriod("e.encounter_datetime")
		        + "\n  UNION ALL SELECT CONCAT('c', c.condition_id), c.patient_id, COALESCE(c.onset_date, c.date_created) "
		        + "FROM conditions c WHERE c.voided = 0 AND c.condition_coded IN " + ids + " AND "
		        + inPeriod("COALESCE(c.onset_date, c.date_created)");
	}
	
	/**
	 * Patients whose first ever diagnosis (or condition onset) among the given concepts falls in
	 * the period, i.e. new cases of a long-term condition rather than everyone seen for it this
	 * month.
	 */
	public static String firstDiagnoses(Collection<String> uuids) {
		String ids = conceptIds(uuids);
		return "SELECT CONCAT('p', f.patient_id) AS event_id, f.patient_id, f.event_date FROM (SELECT a.patient_id, "
		        + "MIN(a.event_date) AS event_date FROM (SELECT ed.patient_id, e.encounter_datetime AS event_date "
		        + "FROM encounter_diagnosis ed JOIN encounter e ON e.encounter_id = ed.encounter_id AND e.voided = 0 "
		        + "WHERE ed.voided = 0 AND ed.diagnosis_coded IN " + ids
		        + " UNION ALL SELECT c.patient_id, COALESCE(c.onset_date, c.date_created) FROM conditions c "
		        + "WHERE c.voided = 0 AND c.condition_coded IN " + ids + ") a GROUP BY a.patient_id) f WHERE "
		        + inPeriod("f.event_date");
	}
	
	/** Any obs in the period whose coded answer is one of the given concepts (e.g. a complaint). */
	public static String obsAnswers(Collection<String> answerUuids) {
		return "SELECT CONCAT('o', o.obs_id) AS event_id, o.person_id AS patient_id, o.obs_datetime AS event_date "
		        + "FROM obs o WHERE o.voided = 0 AND o.value_coded IN " + conceptIds(answerUuids) + " AND "
		        + inPeriod("o.obs_datetime");
	}
	
	/**
	 * Obs of the given questions (usually lab tests) in the period. With {@code answerUuids} null,
	 * every result counts except "not performed"; otherwise only those answers count.
	 */
	public static String obsResults(Collection<String> questionUuids, Collection<String> answerUuids) {
		String answer = answerUuids == null ? "(o.value_coded IS NULL OR o.value_coded NOT IN " + conceptIds(NOT_PERFORMED)
		        + ")" : "o.value_coded IN " + conceptIds(answerUuids);
		return "SELECT CONCAT('o', o.obs_id) AS event_id, o.person_id AS patient_id, o.obs_datetime AS event_date "
		        + "FROM obs o WHERE o.voided = 0 AND o.concept_id IN " + conceptIds(questionUuids) + " AND " + answer
		        + " AND " + inPeriod("o.obs_datetime");
	}
	
	/** Boolean obs of the given concept recorded true in the period. */
	public static String obsTrue(String conceptUuid) {
		return "SELECT CONCAT('o', o.obs_id) AS event_id, o.person_id AS patient_id, o.obs_datetime AS event_date "
		        + "FROM obs o WHERE o.voided = 0 AND o.concept_id = " + conceptId(conceptUuid) + " AND o.value_coded = "
		        + TRUE_ID + " AND " + inPeriod("o.obs_datetime");
	}
	
	/** Numeric obs of the given concept at or above {@code min} in the period. */
	public static String obsAtLeast(String conceptUuid, String min) {
		return "SELECT CONCAT('o', o.obs_id) AS event_id, o.person_id AS patient_id, o.obs_datetime AS event_date "
		        + "FROM obs o WHERE o.voided = 0 AND o.concept_id = " + conceptId(conceptUuid) + " AND o.value_numeric >= "
		        + min + " AND " + inPeriod("o.obs_datetime");
	}
	
	/** Drug orders placed in the period for a drug matching {@code match}. */
	public static String drugOrders(DrugMatch match) {
		return "SELECT CONCAT('r', o.order_id) AS event_id, o.patient_id, o.date_activated AS event_date "
		        + "FROM orders o JOIN drug_order dro ON dro.order_id = o.order_id LEFT JOIN drug d ON d.drug_id = dro.drug_inventory_id "
		        + "WHERE o.voided = 0 AND o.order_action <> 'DISCONTINUE' AND " + inPeriod("o.date_activated") + " AND "
		        + match.sql("o", "dro", "d");
	}
	
	// ---- per-event checks, for NhmisBlock.with(...): ev = the event row, p = its person --------
	
	/** 1 if the encounter {@code encounterIdExpr} has the boolean obs recorded true. */
	public static String hasTrue(String encounterIdExpr, String conceptUuid) {
		return "EXISTS (SELECT 1 FROM obs zo WHERE zo.encounter_id = " + encounterIdExpr + " AND zo.voided = 0 "
		        + "AND zo.concept_id = " + conceptId(conceptUuid) + " AND zo.value_coded = " + TRUE_ID + ")";
	}
	
	/**
	 * 1 if the encounter {@code encounterIdExpr} has the coded obs answered with {@code answerUuid}
	 * .
	 */
	public static String hasAnswer(String encounterIdExpr, String questionUuid, String answerUuid) {
		return "EXISTS (SELECT 1 FROM obs zo WHERE zo.encounter_id = " + encounterIdExpr + " AND zo.voided = 0 "
		        + "AND zo.concept_id = " + conceptId(questionUuid) + " AND zo.value_coded = " + conceptId(answerUuid) + ")";
	}
	
	/** 1 if the encounter {@code encounterIdExpr} has any obs of the question. */
	public static String hasObs(String encounterIdExpr, String questionUuid) {
		return "EXISTS (SELECT 1 FROM obs zo WHERE zo.encounter_id = " + encounterIdExpr + " AND zo.voided = 0 "
		        + "AND zo.concept_id = " + conceptId(questionUuid) + ")";
	}
	
	/** Sum of the numeric obs of the concept in the encounter {@code encounterIdExpr} (0 if none). */
	public static String numericSum(String encounterIdExpr, String conceptUuid) {
		return "(SELECT COALESCE(SUM(zo.value_numeric), 0) FROM obs zo WHERE zo.encounter_id = " + encounterIdExpr
		        + " AND zo.voided = 0 AND zo.concept_id = " + conceptId(conceptUuid) + ")";
	}
	
	/** 1 if the patient had one of the diagnoses/conditions recorded in the period. */
	public static String hadDiagnosis(String patientExpr, Collection<String> uuids) {
		return "EXISTS (SELECT 1 FROM (" + diagnoses(uuids) + ") zd WHERE zd.patient_id = " + patientExpr + ")";
	}
	
	/**
	 * 1 if the patient had one of the diagnoses/conditions recorded at any time up to the period
	 * end.
	 */
	public static String everDiagnosed(String patientExpr, Collection<String> uuids) {
		String ids = conceptIds(uuids);
		return "(EXISTS (SELECT 1 FROM encounter_diagnosis zed WHERE zed.patient_id = " + patientExpr
		        + " AND zed.voided = 0 AND zed.diagnosis_coded IN " + ids + " AND zed.date_created < " + PERIOD_END
		        + ") OR EXISTS (SELECT 1 FROM conditions zc WHERE zc.patient_id = " + patientExpr
		        + " AND zc.voided = 0 AND zc.condition_coded IN " + ids + " AND zc.date_created < " + PERIOD_END + "))";
	}
	
	/** 1 if the patient has a result of the tests in the period (see {@link #obsResults}). */
	public static String hadResult(String patientExpr, Collection<String> questionUuids, Collection<String> answerUuids) {
		return "EXISTS (SELECT 1 FROM (" + obsResults(questionUuids, answerUuids) + ") zr WHERE zr.patient_id = "
		        + patientExpr + ")";
	}
	
	/** 1 if the patient has one of the answers to the tests at any time up to the period end. */
	public static String everResult(String patientExpr, Collection<String> questionUuids, Collection<String> answerUuids) {
		return "EXISTS (SELECT 1 FROM obs zo WHERE zo.person_id = " + patientExpr
		        + " AND zo.voided = 0 AND zo.concept_id IN " + conceptIds(questionUuids) + " AND zo.value_coded IN "
		        + conceptIds(answerUuids) + " AND zo.obs_datetime < " + PERIOD_END + ")";
	}
	
	/** 1 if the patient had a matching drug order placed in the period. */
	public static String hadDrug(String patientExpr, DrugMatch match) {
		return "EXISTS (SELECT 1 FROM orders zo JOIN drug_order zdo ON zdo.order_id = zo.order_id "
		        + "LEFT JOIN drug zdr ON zdr.drug_id = zdo.drug_inventory_id WHERE zo.patient_id = " + patientExpr
		        + " AND zo.voided = 0 AND zo.order_action <> 'DISCONTINUE' AND " + inPeriod("zo.date_activated") + " AND "
		        + match.sql("zo", "zdo", "zdr") + ")";
	}
	
	/** 1 if any encounter in the period has the coded obs answered with {@code answerUuid}. */
	public static String hadAnswer(String patientExpr, String questionUuid, String answerUuid) {
		return "EXISTS (SELECT 1 FROM obs zo WHERE zo.person_id = " + patientExpr
		        + " AND zo.voided = 0 AND zo.concept_id = " + conceptId(questionUuid) + " AND zo.value_coded = "
		        + conceptId(answerUuid) + " AND " + inPeriod("zo.obs_datetime") + ")";
	}
	
	/**
	 * 1 if the woman was pregnant at the event: she had an Antenatal Care Visit in the 280 days up
	 * to the event day.
	 */
	public static String pregnant(String patientExpr, String dateExpr) {
		return "EXISTS (SELECT 1 FROM encounter zpe WHERE zpe.patient_id = " + patientExpr + " AND zpe.voided = 0 "
		        + "AND zpe.encounter_type = (SELECT encounter_type_id FROM encounter_type WHERE uuid = '"
		        + NhmisConcepts.ANC_ENCOUNTER_TYPE + "') AND zpe.encounter_datetime >= DATE_SUB(DATE(" + dateExpr
		        + "), INTERVAL 280 DAY) AND zpe.encounter_datetime < DATE_ADD(DATE(" + dateExpr + "), INTERVAL 1 DAY))";
	}
	
	/**
	 * A drug name test: every regex in {@code all} must match, and {@code none} (if set) must not,
	 * against the lower-cased drug name, non-coded drug text, drug concept names and dosage form.
	 */
	public static final class DrugMatch {
		
		private final List<String> all = new ArrayList<String>();
		
		private String none;
		
		public static DrugMatch named(String... regexes) {
			DrugMatch m = new DrugMatch();
			m.all.addAll(Arrays.asList(regexes));
			return m;
		}
		
		public DrugMatch except(String regex) {
			this.none = regex;
			return this;
		}
		
		String sql(String order, String drugOrder, String drug) {
			String text = "LOWER(CONCAT_WS(' ', " + drug + ".name, " + drugOrder + ".drug_non_coded, "
			        + "(SELECT GROUP_CONCAT(zn.name SEPARATOR ' ') FROM concept_name zn WHERE zn.concept_id = " + order
			        + ".concept_id AND zn.voided = 0), (SELECT GROUP_CONCAT(zf.name SEPARATOR ' ') FROM concept_name zf "
			        + "WHERE zf.concept_id = " + drug + ".dosage_form AND zf.voided = 0)))";
			StringBuilder sql = new StringBuilder("(");
			for (int i = 0; i < all.size(); i++) {
				sql.append(i == 0 ? "" : " AND ").append(text).append(" REGEXP '").append(all.get(i)).append('\'');
			}
			if (none != null) {
				sql.append(" AND ").append(text).append(" NOT REGEXP '").append(none).append('\'');
			}
			return sql.append(')').toString();
		}
	}
}
