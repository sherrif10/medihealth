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

import java.util.Date;

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

/**
 * Builds the "Mortality (Deaths)" section of the NHMIS Monthly Summary Form (rows 5-9): deaths by
 * age/sex, maternal mortality, and the three cause-of-death breakdowns. All rows key off the core
 * {@code Person.dead}/{@code Person.deathDate}/{@code Person.causeOfDeath} fields, so no new
 * clinical form is needed for this section - only the answer concepts recorded against causeOfDeath
 * (see {@link CauseOfDeathConcepts}).
 */
@Component("medihealth.MortalityDataSetBuilder")
public class MortalityDataSetBuilder {
	
	public SqlDataSetDefinition build() {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("NHMIS - Mortality");
		dsd.setDescription("Form rows 5-9: deaths by age/sex, maternal mortality, and causes of maternal/neonatal/under-5 death");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.setSqlQuery(buildSql());
		return dsd;
	}
	
	private String buildSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT\n");
		sql.append("  COUNT(*) AS deaths_total");
		// Row 5: all deaths by sex/age band
		AgeSexColumnFactory.appendColumns(sql, "deaths", null);
		// Row 6: maternal mortality, female only, by 10-19yrs / >=20yrs, cause in the maternal set
		String maternalCauseFilter = "sex = 'Female' AND cause_of_death_uuid IN ("
		        + CauseOfDeathConcepts.quotedInClause(CauseOfDeathConcepts.MATERNAL_CAUSES) + ")";
		sql.append(",\n  SUM(CASE WHEN ").append(maternalCauseFilter)
		        .append(" AND age_at_death_years < 20 THEN 1 ELSE 0 END) AS maternal_deaths_10_19yrs");
		sql.append(",\n  SUM(CASE WHEN ").append(maternalCauseFilter)
		        .append(" AND age_at_death_years >= 20 THEN 1 ELSE 0 END) AS maternal_deaths_gte20yrs");
		// Row 7: confirmed maternal deaths by individual cause
		appendCauseColumns(sql, "maternal_death", "sex = 'Female'", CauseOfDeathConcepts.MATERNAL_CAUSES, new String[] {
		        "postpartum_haemorrhage", "sepsis", "obstructed_labour", "abortion", "malaria", "anaemia", "hiv", "other" });
		// Row 8: confirmed neonatal deaths (age at death <= 28 days) by individual cause
		appendCauseColumns(sql, "neonatal_death", "age_at_death_days <= 28", CauseOfDeathConcepts.NEONATAL_CAUSES,
		    new String[] { "prematurity", "neonatal_tetanus", "congenital_malformation", "other" });
		// Row 9: confirmed under-5 deaths (age at death < 5 years) by individual cause
		appendCauseColumns(sql, "under5_death", "age_at_death_years < 5", CauseOfDeathConcepts.UNDER5_CAUSES, new String[] {
		        "malaria", "pneumonia", "malnutrition", "other" });
		
		sql.append("\nFROM (\n");
		sql.append("  SELECT ").append(AgeSexBands.SEX_CASE_SQL).append(" AS sex,\n");
		sql.append("    ").append(AgeSexBands.INPATIENT_AGE_BAND_SQL).append(" AS age_band,\n");
		sql.append("    TIMESTAMPDIFF(YEAR, p.birthdate, ev.event_date) AS age_at_death_years,\n");
		sql.append("    DATEDIFF(ev.event_date, p.birthdate) AS age_at_death_days,\n");
		sql.append("    c.uuid AS cause_of_death_uuid\n");
		sql.append("  FROM (\n");
		sql.append("    SELECT person_id, death_date AS event_date, cause_of_death AS cause_of_death_concept_id\n");
		sql.append("    FROM person\n");
		sql.append("    WHERE dead = 1 AND voided = 0 AND death_date BETWEEN :startDate AND :endDate\n");
		sql.append("  ) ev\n");
		sql.append("  JOIN person p ON p.person_id = ev.person_id\n");
		sql.append("  LEFT JOIN concept c ON c.concept_id = ev.cause_of_death_concept_id\n");
		sql.append(") x");
		return sql.toString();
	}
	
	/**
	 * Appends one SUM(...) column per cause in {@code causeUuids}, each counting rows matching
	 * {@code baseFilter} and that specific cause, plus one "other"-labelled combined column isn't
	 * needed since "Other" is itself one of the passed-in causes/labels.
	 */
	private void appendCauseColumns(StringBuilder sql, String rowPrefix, String baseFilter, String[] causeUuids,
	        String[] causeLabels) {
		for (int i = 0; i < causeUuids.length; i++) {
			sql.append(",\n  SUM(CASE WHEN ").append(baseFilter).append(" AND cause_of_death_uuid = '")
			        .append(causeUuids[i]).append("' THEN 1 ELSE 0 END) AS ").append(rowPrefix).append('_')
			        .append(causeLabels[i]);
		}
	}
}
