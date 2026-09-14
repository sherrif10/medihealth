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
 * Builds the "Newborn Health" sections of the NHMIS Monthly Summary Form (form rows 48-62: outcome
 * of pregnancy, immediate newborn care, complications), from the new "Newborn Care" encounter type
 * (see {@code newborn_care-medihealth.json}).
 * <p>
 * Newborn data is recorded as obs against the mother's patient record rather than a separate
 * newborn patient - this phase does not add newborn patient registration, so "Newborn sex" and
 * "Birth weight" are their own obs rather than read off a distinct newborn person. Row 48 (live
 * births by sex/weight) and row 49 (live births to HIV-positive mothers) combine several of those
 * obs per encounter and need custom SQL; rows 50-62 are single boolean flags, handled generically
 * by {@link BooleanObsIndicatorFactory}.
 */
@Component("medihealth.NewbornDataSetBuilder")
public class NewbornDataSetBuilder {
	
	private static final String LIVE_BIRTH_CONCEPT = "cfa3f623-97aa-466d-bc50-d81b88c60ace";
	
	private static final String NEWBORN_MALE_CONCEPT = "2d356f5b-44f0-4058-aa6a-76cf129e7184";
	
	private static final String NEWBORN_FEMALE_CONCEPT = "476b9008-048b-4995-9dac-b5c0ed86a125";
	
	private static final String BIRTH_WEIGHT_CONCEPT = "94c22c21-7111-410d-9a58-24d539d69d84";
	
	private static final String MOTHER_HIV_POSITIVE_CONCEPT = "355c2a5b-6c41-4aab-9da7-ec9ed5214f4c";
	
	/** Rows 50-62: single boolean flags on the Newborn Care encounter. */
	private static final List<BooleanObsColumn> FLAG_COLUMNS = Arrays.asList(new BooleanObsColumn("still_birth_macerated",
	        "05cbbef0-2317-4daa-a97b-331740758199"), new BooleanObsColumn("still_birth_fresh",
	        "cffee1a2-c544-425a-a1c2-c3710bd760cf"), new BooleanObsColumn("cord_clamped_after_1min",
	        "f63361ed-1058-4986-b77b-eb8371f6b01d"), new BooleanObsColumn("chx_gel_applied",
	        "cc6a853c-0801-4124-8c27-dec8fa670269"), new BooleanObsColumn("put_to_breast_skin_to_skin",
	        "543a1adb-f4ae-4c33-a3f1-8096f896fb2c"), new BooleanObsColumn("temperature_taken_1hr",
	        "cd37e23e-b35d-479d-aed7-46d42fae3fc8"), new BooleanObsColumn("not_breathing_at_birth",
	        "831d3c9d-84ac-4e04-997c-e4d104e86764"), new BooleanObsColumn("successfully_resuscitated",
	        "0ee60cbd-f764-422e-a7d8-00cc7a8f98e5"), new BooleanObsColumn("newborn_danger_signs",
	        "86ad7e48-4f32-427b-ba98-d94233159a36"), new BooleanObsColumn("danger_signs_antibiotics_referred",
	        "28fe8af0-cefa-4a27-9ffd-77819bfc151b"), new BooleanObsColumn("neonatal_tetanus",
	        "e971c467-a13a-4abb-bb1a-c96b37d8e656"), new BooleanObsColumn("neonatal_jaundice",
	        "c5068009-9c38-441d-bb62-28b21506b1a7"), new BooleanObsColumn("lbw_admitted_kmc",
	        "af59030e-481e-4512-8382-9e8bb4f45c28"), new BooleanObsColumn("lbw_discharged_kmc",
	        "35ea50e8-bfb0-4d21-b648-235ec6c07ed0"));
	
	public SqlDataSetDefinition build() {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("NHMIS - Newborn Health");
		dsd.setDescription("Form rows 48-62: outcome of pregnancy, immediate newborn care, and complications");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.setSqlQuery(buildSql());
		return dsd;
	}
	
	private String buildSql() {
		String t = BooleanObsIndicatorFactory.TRUE_CONCEPT_ID_SQL;
		String live = "o_live.value_coded = " + t;
		String male = "o_male.value_coded = " + t;
		String female = "o_female.value_coded = " + t;
		String hiv = "o_hiv.value_coded = " + t;
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) AS newborn_encounters_total");
		sql.append(",\n  SUM(CASE WHEN ").append(live).append(" AND ").append(male)
		        .append(" AND o_weight.value_numeric < 2.5 THEN 1 ELSE 0 END) AS live_births_male_lt2_5kg");
		sql.append(",\n  SUM(CASE WHEN ").append(live).append(" AND ").append(male)
		        .append(" AND o_weight.value_numeric >= 2.5 THEN 1 ELSE 0 END) AS live_births_male_gte2_5kg");
		sql.append(",\n  SUM(CASE WHEN ").append(live).append(" AND ").append(female)
		        .append(" AND o_weight.value_numeric < 2.5 THEN 1 ELSE 0 END) AS live_births_female_lt2_5kg");
		sql.append(",\n  SUM(CASE WHEN ").append(live).append(" AND ").append(female)
		        .append(" AND o_weight.value_numeric >= 2.5 THEN 1 ELSE 0 END) AS live_births_female_gte2_5kg");
		sql.append(",\n  SUM(CASE WHEN ").append(live).append(" AND ").append(hiv)
		        .append(" THEN 1 ELSE 0 END) AS live_births_to_hiv_positive_mothers");
		BooleanObsIndicatorFactory.appendSelectColumns(sql, FLAG_COLUMNS);
		
		sql.append("\nFROM (\n");
		sql.append("  SELECT e.encounter_id\n");
		sql.append("  FROM encounter e\n");
		sql.append("  JOIN encounter_type et ON et.encounter_type_id = e.encounter_type\n");
		sql.append("  WHERE e.voided = 0 AND et.name = 'Newborn Care'\n");
		sql.append("    AND e.encounter_datetime BETWEEN :startDate AND :endDate\n");
		sql.append(") ev\n");
		sql.append(leftJoinObs("o_live", LIVE_BIRTH_CONCEPT));
		sql.append(leftJoinObs("o_male", NEWBORN_MALE_CONCEPT));
		sql.append(leftJoinObs("o_female", NEWBORN_FEMALE_CONCEPT));
		sql.append(leftJoinObs("o_weight", BIRTH_WEIGHT_CONCEPT));
		sql.append(leftJoinObs("o_hiv", MOTHER_HIV_POSITIVE_CONCEPT));
		BooleanObsIndicatorFactory.appendJoins(sql, "ev", FLAG_COLUMNS);
		return sql.toString();
	}
	
	private String leftJoinObs(String alias, String conceptUuid) {
		return "LEFT JOIN obs " + alias + " ON " + alias + ".encounter_id = ev.encounter_id AND " + alias
		        + ".voided = 0 AND " + alias + ".concept_id = (SELECT concept_id FROM concept WHERE uuid = '" + conceptUuid
		        + "')\n";
	}
}
