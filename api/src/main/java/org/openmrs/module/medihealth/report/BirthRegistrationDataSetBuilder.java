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
 * Builds the "Birth Registration" section of the NHMIS Monthly Summary Form (form rows 91-93), from
 * the new "Birth Registration" encounter type (see {@code birth_registration-medihealth.json}). Row
 * 91 (children registered) is simply a count of these encounters by sex; rows 92-93 (birth
 * certificate issued/collected) are boolean flags on the same encounter.
 */
@Component("medihealth.BirthRegistrationDataSetBuilder")
public class BirthRegistrationDataSetBuilder {
	
	private static final String CERTIFICATE_ISSUED_CONCEPT = "a24ce531-dae1-49ca-bc5b-495a97c384fa";
	
	private static final String CERTIFICATE_COLLECTED_CONCEPT = "5d66fc5f-1071-41d8-8ea6-c562e470e777";
	
	public SqlDataSetDefinition build() {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("NHMIS - Birth Registration");
		dsd.setDescription("Form rows 91-93: children under 1 year registered, birth certificate issued/collected");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.setSqlQuery(buildSql());
		return dsd;
	}
	
	private String buildSql() {
		String issuedTrue = "o_issued.value_coded = " + BooleanObsIndicatorFactory.TRUE_CONCEPT_ID_SQL;
		String collectedTrue = "o_collected.value_coded = " + BooleanObsIndicatorFactory.TRUE_CONCEPT_ID_SQL;
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) AS registered_total");
		sql.append(",\n  SUM(CASE WHEN sex = 'Male' THEN 1 ELSE 0 END) AS registered_male");
		sql.append(",\n  SUM(CASE WHEN sex = 'Female' THEN 1 ELSE 0 END) AS registered_female");
		sql.append(",\n  SUM(CASE WHEN ").append(issuedTrue).append(" AND sex = 'Male' THEN 1 ELSE 0 END)")
		        .append(" AS certificate_issued_male");
		sql.append(",\n  SUM(CASE WHEN ").append(issuedTrue).append(" AND sex = 'Female' THEN 1 ELSE 0 END)")
		        .append(" AS certificate_issued_female");
		sql.append(",\n  SUM(CASE WHEN ").append(collectedTrue).append(" AND sex = 'Male' THEN 1 ELSE 0 END)")
		        .append(" AS certificate_collected_male");
		sql.append(",\n  SUM(CASE WHEN ").append(collectedTrue).append(" AND sex = 'Female' THEN 1 ELSE 0 END)")
		        .append(" AS certificate_collected_female");
		
		sql.append("\nFROM (\n");
		sql.append("  SELECT ev.encounter_id, ").append(AgeSexBands.SEX_CASE_SQL).append(" AS sex\n");
		sql.append("  FROM (\n");
		sql.append("    SELECT e.encounter_id, e.patient_id\n");
		sql.append("    FROM encounter e\n");
		sql.append("    JOIN encounter_type et ON et.encounter_type_id = e.encounter_type\n");
		sql.append("    WHERE e.voided = 0 AND et.name = 'Birth Registration'\n");
		sql.append("      AND e.encounter_datetime BETWEEN :startDate AND :endDate\n");
		sql.append("  ) ev\n");
		sql.append("  JOIN person p ON p.person_id = ev.patient_id AND p.voided = 0\n");
		sql.append(") x\n");
		sql.append("LEFT JOIN obs o_issued ON o_issued.encounter_id = x.encounter_id AND o_issued.voided = 0\n");
		sql.append("  AND o_issued.concept_id = (SELECT concept_id FROM concept WHERE uuid = '")
		        .append(CERTIFICATE_ISSUED_CONCEPT).append("')\n");
		sql.append("LEFT JOIN obs o_collected ON o_collected.encounter_id = x.encounter_id AND o_collected.voided = 0\n");
		sql.append("  AND o_collected.concept_id = (SELECT concept_id FROM concept WHERE uuid = '")
		        .append(CERTIFICATE_COLLECTED_CONCEPT).append("')");
		return sql.toString();
	}
}
