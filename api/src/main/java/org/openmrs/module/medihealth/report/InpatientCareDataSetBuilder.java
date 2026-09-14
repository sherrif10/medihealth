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
 * Builds the "Inpatient Care (IPC)" section of the NHMIS Monthly Summary Form (rows 3-4): Patients
 * admitted and Inpatient discharges, each broken down by sex and age band, using this distro's
 * existing "Admission" and "Discharge" encounter types.
 */
@Component("medihealth.InpatientCareDataSetBuilder")
public class InpatientCareDataSetBuilder {
	
	public SqlDataSetDefinition build() {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("NHMIS - Inpatient Care");
		dsd.setDescription("Form rows 3-4: Patients admitted and Inpatient discharges by sex and age band");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.setSqlQuery(buildSql());
		return dsd;
	}
	
	private String buildSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT\n");
		sql.append("  SUM(CASE WHEN encounter_type_name = 'Admission' THEN 1 ELSE 0 END) AS patients_admitted_total,\n");
		sql.append("  SUM(CASE WHEN encounter_type_name = 'Discharge' THEN 1 ELSE 0 END) AS inpatient_discharges_total");
		AgeSexColumnFactory.appendColumns(sql, "patients_admitted", "encounter_type_name = 'Admission'");
		AgeSexColumnFactory.appendColumns(sql, "inpatient_discharges", "encounter_type_name = 'Discharge'");
		sql.append("\nFROM (\n");
		sql.append("  SELECT ").append(AgeSexBands.SEX_CASE_SQL).append(" AS sex,\n");
		sql.append("    ").append(AgeSexBands.INPATIENT_AGE_BAND_SQL).append(" AS age_band,\n");
		sql.append("    ev.encounter_type_name AS encounter_type_name\n");
		sql.append("  FROM (\n");
		sql.append("    SELECT e.patient_id, e.encounter_datetime AS event_date, et.name AS encounter_type_name\n");
		sql.append("    FROM encounter e\n");
		sql.append("    JOIN encounter_type et ON et.encounter_type_id = e.encounter_type\n");
		sql.append("    WHERE e.voided = 0 AND et.name IN ('Admission', 'Discharge')\n");
		sql.append("      AND e.encounter_datetime BETWEEN :startDate AND :endDate\n");
		sql.append("  ) ev\n");
		sql.append("  JOIN person p ON p.person_id = ev.patient_id AND p.voided = 0\n");
		sql.append(") x");
		return sql.toString();
	}
}
