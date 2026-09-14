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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

/**
 * Builds the "Maternal Health (Ante-Natal Care)" section of the NHMIS Monthly Summary Form (form
 * rows 10-33), from the new "Antenatal Care Visit" encounter type and its boolean/numeric obs (see
 * {@code anc_visit-medihealth.json} and {@code anc_concepts-medihealth.csv}).
 * <p>
 * Row 10 (attendance by age band) and row 11 (first-visit split by gestational age) need custom
 * SQL; rows 12-33 are all "count visits where flag concept X was recorded true", handled
 * generically by {@link BooleanObsIndicatorFactory}.
 */
@Component("medihealth.AncDataSetBuilder")
public class AncDataSetBuilder {
	
	private static final String ANC_FIRST_VISIT_CONCEPT = "ecfb800d-9e90-441f-91ce-e4c7050ae4ae";
	
	private static final String GESTATIONAL_AGE_CONCEPT = "77a646a6-14b6-4981-888b-cea0d71d728b";
	
	/** Rows 12-33: each a simple "count of ANC visits where this flag was recorded true". */
	private static final List<BooleanObsColumn> FLAG_COLUMNS = Arrays.asList(new BooleanObsColumn("anc_4th_visit",
	        "0a08c905-a918-470b-af6b-04d5dce35fce"), new BooleanObsColumn("anc_8th_visit",
	        "d4862ab8-dfb1-45d6-891e-f689b3bb8b43"), new BooleanObsColumn("anc_counselled_fgm",
	        "37d4396b-7263-44c8-888f-d5ba54447f14"), new BooleanObsColumn("anc_counselled_fp",
	        "318d7c15-e1d9-4408-bee6-7c36121aad73"), new BooleanObsColumn("anc_counselled_nutrition",
	        "8e5f0345-0139-4173-8a37-674a7e88b93e"), new BooleanObsColumn("anc_syphilis_test_done",
	        "7e0cd826-6557-4789-8a94-0795e1c49451"), new BooleanObsColumn("anc_syphilis_test_positive",
	        "482b870d-55f9-4bb5-94cb-3e27568d823e"), new BooleanObsColumn("anc_syphilis_case_treated",
	        "932f8332-1b2b-4d14-a54a-0ec0c73125f7"), new BooleanObsColumn("anc_hepb_test_done",
	        "71628a1e-447e-4897-aaa2-10fc57c96fe2"), new BooleanObsColumn("anc_hepb_test_positive",
	        "0639c9d7-6a66-4aa0-a42e-7193761e7e22"), new BooleanObsColumn("anc_hepb_referred",
	        "142518fd-5d32-44bc-b789-fcb430cd0d64"), new BooleanObsColumn("anc_hepc_test_done",
	        "0988bebc-0245-4fc5-84c1-2c7a922e698d"), new BooleanObsColumn("anc_hepc_test_positive",
	        "bde5a07e-2085-48fb-bbd9-fb41046b662f"), new BooleanObsColumn("anc_hepc_referred",
	        "9cd39926-22be-471c-bd39-f3956185fadf"), new BooleanObsColumn("anc_ipt1_given",
	        "6a08714c-f7dd-417a-8fc1-21f1765e593c"), new BooleanObsColumn("anc_ipt2_given",
	        "2dd8ed00-cd49-4fb6-bbfb-3119c1d62eec"), new BooleanObsColumn("anc_ipt3_given",
	        "c6f5c199-7b16-4f1f-b155-c80efab78444"), new BooleanObsColumn("anc_ipt_gt3_given",
	        "d8519f7b-dd2e-40d0-9051-4a841a2c6cd7"), new BooleanObsColumn("anc_llin_given",
	        "442cfd7b-5cc7-4712-9deb-0e93ac3bc4b0"), new BooleanObsColumn("anc_haematinics_given",
	        "a38b5486-d4fa-4cb8-8a7f-b7e9faab25d3"), new BooleanObsColumn("anc_severe_anaemia",
	        "34a576f9-4c8a-4572-8cf9-41e1f3729962"), new BooleanObsColumn("anc_proteinuria",
	        "d3f6f591-8886-4a60-b1e5-fedb62a9899f"));
	
	public SqlDataSetDefinition build() {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("NHMIS - Antenatal Care");
		dsd.setDescription("Form rows 10-33: ANC attendance, first-visit gestational age, counselling, screening, "
		        + "malaria prevention and supplementation/findings");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.setSqlQuery(buildSql());
		return dsd;
	}
	
	private String buildSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) AS anc_attendance_total");
		for (String band : Arrays.asList("10-14yrs", "15-19yrs", "20-35yrs", "35-49yrs", ">=50yrs")) {
			sql.append(",\n  SUM(CASE WHEN age_band = '").append(band).append("' THEN 1 ELSE 0 END) AS anc_attendance_")
			        .append(band.toLowerCase().replace(">=", "gte").replace("-", "_"));
		}
		sql.append(",\n  SUM(CASE WHEN o_first.value_coded = ").append(BooleanObsIndicatorFactory.TRUE_CONCEPT_ID_SQL)
		        .append(" AND o_ga.value_numeric < 20 THEN 1 ELSE 0 END) AS anc_first_visit_ga_lt20wks");
		sql.append(",\n  SUM(CASE WHEN o_first.value_coded = ").append(BooleanObsIndicatorFactory.TRUE_CONCEPT_ID_SQL)
		        .append(" AND o_ga.value_numeric >= 20 THEN 1 ELSE 0 END) AS anc_first_visit_ga_gte20wks");
		BooleanObsIndicatorFactory.appendSelectColumns(sql, FLAG_COLUMNS);
		
		sql.append("\nFROM (\n");
		sql.append("  SELECT ev.encounter_id, ").append(AgeSexBands.ANC_AGE_BAND_SQL).append(" AS age_band\n");
		sql.append("  FROM (\n");
		sql.append("    SELECT e.encounter_id, e.patient_id, e.encounter_datetime AS event_date\n");
		sql.append("    FROM encounter e\n");
		sql.append("    JOIN encounter_type et ON et.encounter_type_id = e.encounter_type\n");
		sql.append("    WHERE e.voided = 0 AND et.name = 'Antenatal Care Visit'\n");
		sql.append("      AND e.encounter_datetime BETWEEN :startDate AND :endDate\n");
		sql.append("  ) ev\n");
		sql.append("  JOIN person p ON p.person_id = ev.patient_id AND p.voided = 0\n");
		sql.append(") x\n");
		sql.append("LEFT JOIN obs o_first ON o_first.encounter_id = x.encounter_id AND o_first.voided = 0\n");
		sql.append("  AND o_first.concept_id = (SELECT concept_id FROM concept WHERE uuid = '")
		        .append(ANC_FIRST_VISIT_CONCEPT).append("')\n");
		sql.append("LEFT JOIN obs o_ga ON o_ga.encounter_id = x.encounter_id AND o_ga.voided = 0\n");
		sql.append("  AND o_ga.concept_id = (SELECT concept_id FROM concept WHERE uuid = '").append(GESTATIONAL_AGE_CONCEPT)
		        .append("')");
		BooleanObsIndicatorFactory.appendJoins(sql, "x", FLAG_COLUMNS);
		return sql.toString();
	}
}
