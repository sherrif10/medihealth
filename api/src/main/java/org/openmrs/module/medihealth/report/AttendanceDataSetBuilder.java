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
 * Builds the "Health Facility Attendance" section of the NHMIS Monthly Summary Form (rows 1-2):
 * General Attendance and Out-patient Attendance, each broken down by sex and age band.
 * <p>
 * "General Attendance" is modelled as every OpenMRS Visit started in the reporting period
 * (regardless of visit type), and "Out-patient Attendance" as the subset of those visits whose
 * visit type is "OPD Visit" (an existing visit type in this distro's configuration). Both are
 * attendance/event counts, not unique-patient counts, since a patient visiting twice in a month
 * should count twice on this form - confirm this matches how your facilities currently tally these
 * two rows by hand before relying on it for compliance submissions.
 */
@Component("medihealth.AttendanceDataSetBuilder")
public class AttendanceDataSetBuilder {
	
	public SqlDataSetDefinition build() {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("NHMIS - Health Facility Attendance");
		dsd.setDescription("Form rows 1-2: General Attendance and Out-patient Attendance by sex and age band");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.setSqlQuery(buildSql());
		return dsd;
	}
	
	private String buildSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) AS general_attendance_total,\n");
		sql.append("  SUM(CASE WHEN visit_type_name = 'OPD Visit' THEN 1 ELSE 0 END) AS outpatient_attendance_total");
		AgeSexColumnFactory.appendColumns(sql, "general_attendance", null);
		AgeSexColumnFactory.appendColumns(sql, "outpatient_attendance", "visit_type_name = 'OPD Visit'");
		sql.append("\nFROM (\n");
		sql.append("  SELECT ").append(AgeSexBands.SEX_CASE_SQL).append(" AS sex,\n");
		sql.append("    ").append(AgeSexBands.ATTENDANCE_AGE_BAND_SQL).append(" AS age_band,\n");
		sql.append("    ev.visit_type_name AS visit_type_name\n");
		sql.append("  FROM (\n");
		sql.append("    SELECT v.patient_id, v.date_started AS event_date, vt.name AS visit_type_name\n");
		sql.append("    FROM visit v\n");
		sql.append("    JOIN visit_type vt ON vt.visit_type_id = v.visit_type_id\n");
		sql.append("    WHERE v.voided = 0 AND v.date_started BETWEEN :startDate AND :endDate\n");
		sql.append("  ) ev\n");
		sql.append("  JOIN person p ON p.person_id = ev.patient_id AND p.voided = 0\n");
		sql.append(") x");
		return sql.toString();
	}
}
