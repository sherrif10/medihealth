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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The IDSR 002 (weekly) and IDSR 003 (monthly) notification forms: which disease is on which row of
 * the official Excel template, and the SQL that counts cases and deaths per row, measure and age
 * band.
 * <p>
 * Counts are of patients (each patient once per row, measure and age band). Age bands are those of
 * the form: 0-28 days, 1-11 months, 12-59 months, 5-9, 10-19, 20-40 and over 40 years, at the date
 * of the diagnosis or death.
 * <p>
 * Measures: IDSR 002 counts suspected cases (every diagnosis), lab-confirmed cases (a diagnosis
 * recorded as confirmed, or for malaria a positive RDT or microscopy result) and deaths (the
 * patient's cause of death). IDSR 003 counts out-patient and in-patient cases (in-patient when the
 * diagnosis was recorded in a hospitalization visit or a visit with an admission) and deaths.
 */
public enum IdsrForm {
	
	WEEKLY_002("Health facility_002", "LGA Level_002"),
	
	MONTHLY_003("Health facility_003", "LGA Level_003");
	
	public static final String SUSPECTED = "suspected";
	
	public static final String CONFIRMED = "confirmed";
	
	public static final String OUTPATIENT = "outpatient";
	
	public static final String INPATIENT = "inpatient";
	
	public static final String DEATHS = "deaths";
	
	/** Age bands in the order of the form's columns. */
	public static final List<String> AGE_BANDS = Collections.unmodifiableList(Arrays.asList("0-28d", "1-11m", "12-59m",
	    "5-9y", "10-19y", "20-40y", "over40y"));
	
	private final String facilitySheet;
	
	private final String lgaSheet;
	
	IdsrForm(String facilitySheet, String lgaSheet) {
		this.facilitySheet = facilitySheet;
		this.lgaSheet = lgaSheet;
	}
	
	public String getSheet(boolean perFacility) {
		return perFacility ? facilitySheet : lgaSheet;
	}
	
	/** Row keys mapped to their 1-based Excel row on the template. */
	public Map<String, Integer> getRows() {
		Map<String, Integer> rows = new LinkedHashMap<String, Integer>();
		for (Row row : rows()) {
			rows.put(row.key, row.excelRow);
		}
		return rows;
	}
	
	List<Row> rows() {
		return this == WEEKLY_002 ? WEEKLY_ROWS : MONTHLY_ROWS;
	}
	
	// ---- rows ---------------------------------------------------------------------------------
	
	/** One disease row: the diagnoses it counts and who it is limited to. */
	static final class Row {
		
		final String key;
		
		final int excelRow;
		
		final List<String> diagnoses;
		
		/** Extra cause-of-death concepts (the "... (cause of death)" answers) counted as deaths. */
		List<String> deathCauses = Collections.emptyList();
		
		boolean underFive;
		
		boolean pregnantWomen;
		
		/** New cases only: the patient's first ever diagnosis among {@link #diagnoses}. */
		boolean newCasesOnly;
		
		/** Lab confirmation by a positive malaria RDT or microscopy result. */
		boolean malariaTests;
		
		/** Extra SQL condition on the diagnosis (alias {@code ed}), OR-ed with the diagnosis list. */
		String alsoMatches;
		
		/** Counted from an observation question instead of diagnoses (AEFI). */
		String obsQuestion;
		
		Row(String key, int excelRow, List<String> diagnoses) {
			this.key = key;
			this.excelRow = excelRow;
			this.diagnoses = diagnoses;
		}
	}
	
	private static Row row(String key, int excelRow, List<String> diagnoses) {
		return new Row(key, excelRow, diagnoses);
	}
	
	private static final List<Row> WEEKLY_ROWS = new ArrayList<Row>();
	
	private static final List<Row> MONTHLY_ROWS = new ArrayList<Row>();
	
	static {
		WEEKLY_ROWS.add(row("acute_viral_hepatitis", 14, IdsrConcepts.ACUTE_VIRAL_HEPATITIS));
		Row dehydration = row("diarrhoea_with_dehydration", 15, IdsrConcepts.DIARRHOEA_WITH_DEHYDRATION);
		dehydration.underFive = true;
		// diarrhoea recorded together with dehydration in the same encounter
		dehydration.alsoMatches = "(ed.diagnosis_coded IN " + conceptIds(NhmisConcepts.DIARRHOEA)
		        + " AND EXISTS (SELECT 1 FROM encounter_diagnosis zdh WHERE zdh.encounter_id = ed.encounter_id"
		        + " AND zdh.voided = 0 AND zdh.diagnosis_coded IN " + conceptIds(IdsrConcepts.DEHYDRATION) + "))";
		WEEKLY_ROWS.add(dehydration);
		WEEKLY_ROWS.add(row("diarrhoea_with_blood", 16, IdsrConcepts.DIARRHOEA_WITH_BLOOD));
		WEEKLY_ROWS.add(row("hat", 17, IdsrConcepts.AFRICAN_TRYPANOSOMIASIS));
		Row malaria = row("malaria", 18, NhmisConcepts.MALARIA);
		malaria.malariaTests = true;
		WEEKLY_ROWS.add(malaria);
		WEEKLY_ROWS.add(row("malaria_severe", 19, NhmisConcepts.SEVERE_MALARIA));
		Row malariaPregnant = row("malaria_pregnant", 20, NhmisConcepts.MALARIA);
		malariaPregnant.pregnantWomen = true;
		malariaPregnant.malariaTests = true;
		WEEKLY_ROWS.add(malariaPregnant);
		WEEKLY_ROWS.add(row("non_neonatal_tetanus", 21, IdsrConcepts.NON_NEONATAL_TETANUS));
		Row hiv = row("new_hiv", 22, IdsrConcepts.HIV);
		hiv.newCasesOnly = true;
		hiv.deathCauses = IdsrConcepts.HIV_DEATH_CAUSES;
		WEEKLY_ROWS.add(hiv);
		WEEKLY_ROWS.add(row("schistosomiasis", 23, IdsrConcepts.SCHISTOSOMIASIS));
		WEEKLY_ROWS.add(row("snake_bite", 24, NhmisConcepts.SNAKE_BITE));
		WEEKLY_ROWS.add(row("soil_transmitted_helminths", 25, IdsrConcepts.SOIL_TRANSMITTED_HELMINTHS));
		WEEKLY_ROWS.add(row("typhoid", 26, IdsrConcepts.TYPHOID));
		
		MONTHLY_ROWS.add(row("sti_vaginal_discharge", 15, IdsrConcepts.VAGINAL_DISCHARGE));
		MONTHLY_ROWS.add(row("sti_genital_ulcer", 16, IdsrConcepts.GENITAL_ULCER));
		MONTHLY_ROWS.add(row("sti_urethral_discharge", 17, IdsrConcepts.URETHRAL_DISCHARGE));
		MONTHLY_ROWS.add(row("sti_other", 18, IdsrConcepts.OTHER_STIS));
		Row aefi = row("aefi", 20, Collections.<String> emptyList());
		aefi.obsQuestion = IdsrConcepts.AEFI_QUESTION;
		MONTHLY_ROWS.add(aefi);
		Row diabetes = row("diabetes_new", 21, NhmisConcepts.DIABETES);
		diabetes.newCasesOnly = true;
		MONTHLY_ROWS.add(diabetes);
		MONTHLY_ROWS.add(row("hypertension", 22, NhmisConcepts.HYPERTENSION));
		MONTHLY_ROWS.add(row("injuries_rta", 23, IdsrConcepts.ROAD_TRAFFIC_INJURY));
		Row malnutrition = row("malnutrition_under5", 24, IdsrConcepts.MALNUTRITION);
		malnutrition.underFive = true;
		malnutrition.deathCauses = IdsrConcepts.MALNUTRITION_DEATH_CAUSES;
		MONTHLY_ROWS.add(malnutrition);
		MONTHLY_ROWS.add(row("mns_disorders", 25, IdsrConcepts.MNS_DISORDERS));
		Row pneumonia = row("severe_pneumonia_under5", 26, NhmisConcepts.PNEUMONIA);
		pneumonia.underFive = true;
		MONTHLY_ROWS.add(pneumonia);
		Row sickleCell = row("sickle_cell_new", 27, NhmisConcepts.SICKLE_CELL);
		sickleCell.newCasesOnly = true;
		MONTHLY_ROWS.add(sickleCell);
	}
	
	// ---- SQL ----------------------------------------------------------------------------------
	
	/**
	 * Counts per row key, measure and age band. {@code perFacility}: limited to the
	 * {@code :location} parameter (and locations under it); otherwise every PHC.
	 */
	public String cellsSql(boolean perFacility) {
		List<String> events = new ArrayList<String>();
		for (Row row : rows()) {
			if (row.obsQuestion != null) {
				events.add(obsEvents(row, perFacility));
				continue;
			}
			if (row.diagnoses.isEmpty() && row.alsoMatches == null) {
				continue; // no concept for this row yet; it stays at 0
			}
			if (this == WEEKLY_002) {
				events.add(diagnosisEvents(row, perFacility, "'" + SUSPECTED + "'", null));
				events.add(diagnosisEvents(row, perFacility, "'" + CONFIRMED + "'", "ed.certainty = 'CONFIRMED'"));
				if (row.malariaTests) {
					events.add(malariaTestEvents(row, perFacility));
				}
			} else {
				events.add(diagnosisEvents(row, perFacility, "CASE WHEN " + inpatient("e") + " THEN '" + INPATIENT
				        + "' ELSE '" + OUTPATIENT + "' END", null));
			}
			events.add(deathEvents(row, perFacility));
		}
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT x.row_key, x.measure, ").append(ageBand("zp.birthdate", "x.event_date"))
		        .append(" AS age_band,\n");
		sql.append("  COUNT(DISTINCT x.patient_id) AS patients\nFROM (\n");
		for (int i = 0; i < events.size(); i++) {
			sql.append(i == 0 ? "  " : "  UNION ALL\n  ").append(events.get(i)).append("\n");
		}
		sql.append(") x\nJOIN person zp ON zp.person_id = x.patient_id AND zp.voided = 0\n");
		sql.append("WHERE zp.birthdate IS NOT NULL\nGROUP BY x.row_key, x.measure, age_band");
		return sql.toString();
	}
	
	/**
	 * Facility totals for the form header: PHCs in scope and PHCs with any encounter in the period.
	 */
	public String headerSql(boolean perFacility) {
		return "SELECT (SELECT COUNT(*) FROM location zh WHERE zh.retired = 0 AND zh.name LIKE '%PHC%') AS total_hfs,\n"
		        + "  (SELECT COUNT(DISTINCT e.location_id) FROM encounter e\n"
		        + "   JOIN location zl ON zl.location_id = e.location_id AND zl.retired = 0 AND zl.name LIKE '%PHC%'\n"
		        + "   WHERE e.voided = 0 AND " + inPeriod("e.encounter_datetime") + " AND "
		        + facility("e.location_id", perFacility) + ") AS reporting_hfs";
	}
	
	private static String facility(String locationIdExpr, boolean perFacility) {
		if (perFacility) {
			return NhmisSql.atFacility(locationIdExpr);
		}
		return locationIdExpr
		        + " IN (SELECT zph.location_id FROM location zph WHERE zph.retired = 0 AND zph.name LIKE '%PHC%')";
	}
	
	private static String patientFilters(Row row, String patientExpr, String dateExpr) {
		StringBuilder sql = new StringBuilder();
		if (row.underFive) {
			sql.append(" AND EXISTS (SELECT 1 FROM person zu WHERE zu.person_id = ").append(patientExpr)
			        .append(" AND TIMESTAMPDIFF(MONTH, zu.birthdate, ").append(dateExpr).append(") < 60)");
		}
		if (row.pregnantWomen) {
			sql.append(" AND EXISTS (SELECT 1 FROM person zw WHERE zw.person_id = ").append(patientExpr)
			        .append(" AND zw.gender = 'F') AND ").append(NhmisSql.pregnant(patientExpr, dateExpr));
		}
		if (row.newCasesOnly) {
			String ids = conceptIds(row.diagnoses);
			sql.append(
			    " AND NOT EXISTS (SELECT 1 FROM encounter_diagnosis zn JOIN encounter zne ON zne.encounter_id = zn.encounter_id")
			        .append(" AND zne.voided = 0 WHERE zn.patient_id = ").append(patientExpr).append(" AND zn.voided = 0")
			        .append(" AND zn.diagnosis_coded IN ").append(ids).append(" AND zne.encounter_datetime < ")
			        .append(dateExpr).append(") AND NOT EXISTS (SELECT 1 FROM conditions zc WHERE zc.patient_id = ")
			        .append(patientExpr).append(" AND zc.voided = 0 AND zc.condition_coded IN ").append(ids)
			        .append(" AND COALESCE(zc.onset_date, zc.date_created) < DATE(").append(dateExpr).append("))");
		}
		return sql.toString();
	}
	
	private static String diagnosisMatch(Row row) {
		List<String> parts = new ArrayList<String>();
		if (!row.diagnoses.isEmpty()) {
			parts.add("ed.diagnosis_coded IN " + conceptIds(row.diagnoses));
		}
		if (row.alsoMatches != null) {
			parts.add(row.alsoMatches);
		}
		return parts.size() == 1 ? parts.get(0) : "(" + join(parts, " OR ") + ")";
	}
	
	private static String diagnosisEvents(Row row, boolean perFacility, String measureExpr, String extra) {
		return "SELECT '" + row.key + "' AS row_key, " + measureExpr
		        + " AS measure, ed.patient_id, e.encounter_datetime AS event_date"
		        + " FROM encounter_diagnosis ed JOIN encounter e ON e.encounter_id = ed.encounter_id AND e.voided = 0"
		        + " WHERE ed.voided = 0 AND " + diagnosisMatch(row) + (extra == null ? "" : " AND " + extra) + " AND "
		        + inPeriod("e.encounter_datetime") + " AND " + facility("e.location_id", perFacility)
		        + patientFilters(row, "ed.patient_id", "e.encounter_datetime");
	}
	
	private static String malariaTestEvents(Row row, boolean perFacility) {
		List<String> tests = new ArrayList<String>(NhmisConcepts.MALARIA_RDT);
		tests.addAll(NhmisConcepts.MALARIA_MICROSCOPY);
		List<String> positive = new ArrayList<String>(NhmisConcepts.MALARIA_RDT_POSITIVE);
		positive.addAll(NhmisConcepts.MALARIA_MICROSCOPY_POSITIVE);
		return "SELECT '" + row.key + "', '" + CONFIRMED + "', o.person_id, o.obs_datetime FROM obs o"
		        + " WHERE o.voided = 0 AND o.concept_id IN " + conceptIds(tests) + " AND o.value_coded IN "
		        + conceptIds(positive) + " AND " + inPeriod("o.obs_datetime") + " AND "
		        + facility("o.location_id", perFacility) + patientFilters(row, "o.person_id", "o.obs_datetime");
	}
	
	private static String deathEvents(Row row, boolean perFacility) {
		List<String> causes = new ArrayList<String>(row.diagnoses);
		causes.addAll(row.deathCauses);
		if (causes.isEmpty()) {
			causes.add("00000000-0000-0000-0000-000000000000");
		}
		return "SELECT '" + row.key + "', '" + DEATHS + "', d.person_id, d.death_date FROM person d"
		        + " WHERE d.voided = 0 AND d.dead = 1 AND d.cause_of_death IN " + conceptIds(causes) + " AND "
		        + inPeriod("d.death_date") + " AND "
		        + facility(NhmisSql.patientLocationAt("d.person_id", "d.death_date"), perFacility)
		        + patientFilters(row, "d.person_id", "d.death_date");
	}
	
	private static String obsEvents(Row row, boolean perFacility) {
		return "SELECT '" + row.key + "', '" + OUTPATIENT + "', o.person_id, o.obs_datetime FROM obs o"
		        + " WHERE o.voided = 0 AND o.concept_id = " + NhmisSql.conceptId(row.obsQuestion) + " AND "
		        + inPeriod("o.obs_datetime") + " AND " + facility("o.location_id", perFacility);
	}
	
	/** True when the encounter belongs to a hospitalization visit or a visit with an admission. */
	private static String inpatient(String encounterAlias) {
		return "EXISTS (SELECT 1 FROM visit zv JOIN visit_type zvt ON zvt.visit_type_id = zv.visit_type_id WHERE zv.visit_id = "
		        + encounterAlias
		        + ".visit_id AND (LOWER(zvt.name) LIKE '%hospital%' OR LOWER(zvt.name) LIKE '%inpatient%'"
		        + " OR EXISTS (SELECT 1 FROM encounter za JOIN encounter_type zat ON zat.encounter_type_id = za.encounter_type"
		        + " WHERE za.visit_id = zv.visit_id AND za.voided = 0 AND zat.name = 'Admission')))";
	}
	
	static String ageBand(String birthdate, String at) {
		return "CASE WHEN DATEDIFF(" + at + ", " + birthdate + ") <= 28 THEN '0-28d'" + " WHEN TIMESTAMPDIFF(MONTH, "
		        + birthdate + ", " + at + ") < 12 THEN '1-11m'" + " WHEN TIMESTAMPDIFF(MONTH, " + birthdate + ", " + at
		        + ") < 60 THEN '12-59m'" + " WHEN TIMESTAMPDIFF(YEAR, " + birthdate + ", " + at + ") < 10 THEN '5-9y'"
		        + " WHEN TIMESTAMPDIFF(YEAR, " + birthdate + ", " + at + ") < 20 THEN '10-19y'"
		        + " WHEN TIMESTAMPDIFF(YEAR, " + birthdate + ", " + at + ") <= 40 THEN '20-40y'" + " ELSE 'over40y' END";
	}
	
	private static String join(Collection<String> parts, String separator) {
		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			sb.append(sb.length() == 0 ? "" : separator).append(part);
		}
		return sb.toString();
	}
}
