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
 * Builds the Tetanus Diphtheria, EPI antigen, and AEFI sections of the NHMIS Monthly Summary Form
 * (form rows 63-90), from the new "Immunization Record" encounter type (see
 * {@code immunization_record-medihealth.json}).
 * <p>
 * Every dose given is its own obs row (concept "Vaccine administered", value_coded = the specific
 * vaccine/dose concept), up to 5 per encounter - so unlike most other sections here, this query is
 * driven off {@code obs} directly rather than off one row per encounter, which naturally handles
 * 0-5 doses per visit without needing per-concept LEFT JOINs. Rows 65-87 (standard EPI antigens)
 * split by recipient age (&lt;1yr / &gt;=1yr, computed against the encounter date) and delivery
 * location ("Mobile Clinic" location = Outreach, everything else = Fixed, per confirmed mapping).
 * Row 83 (Fully Immunized) splits by location only. Rows 63-64 (TD) split by dose number (1-5, read
 * off a sibling obs in the same obs group) and pregnancy status, using dedicated
 * TD-Pregnant/TD-Non-Pregnant answer concepts rather than trying to infer pregnancy status, which
 * isn't otherwise captured on this encounter.
 */
@Component("medihealth.ImmunizationDataSetBuilder")
public class ImmunizationDataSetBuilder {
	
	private static final String VACCINE_ADMINISTERED_CONCEPT = "cc2f3a5c-ea29-4709-bf70-7c516dbf3719";
	
	private static final String TD_DOSE_NUMBER_CONCEPT = "f05e8ce6-2fef-4be8-b578-8e3726ba1bde";
	
	private static final String FULLY_IMMUNIZED_UNDER1_CONCEPT = "450c0e78-72fe-4156-82b7-b34177e52e9e";
	
	private static final String TD_PREGNANT_CONCEPT = "c5f21be9-02e6-4924-9d93-33c0a12ab4bf";
	
	private static final String TD_NON_PREGNANT_CONCEPT = "f3448db1-1bc9-4622-ba08-e1192e4eaaf9";
	
	/** Rows 65-82, 84-87: standard EPI antigens, each split by age band x delivery location. */
	private static final List<VaccineDoseColumn> ANTIGEN_COLUMNS = Arrays.asList(new VaccineDoseColumn("opv0",
	        "46bfb919-6b32-4040-b089-5f2077b890a4"), new VaccineDoseColumn("hepb0", "08ab1510-aee0-4687-9c12-5db506470fa7"),
	    new VaccineDoseColumn("bcg", "a4e8b674-eee4-41c2-a473-2eadc38f443b"), new VaccineDoseColumn("opv1",
	            "c0290e1e-0f66-4e3f-aeb7-552e8cdfd67a"), new VaccineDoseColumn("penta1",
	            "610e5a89-02ba-43ab-8814-39eb3257db08"), new VaccineDoseColumn("pcv1",
	            "57845954-da0e-46eb-b713-54b8fc55a853"), new VaccineDoseColumn("rota1",
	            "080dfc3f-b1d4-4dbe-9233-970a675f618d"), new VaccineDoseColumn("opv2",
	            "5c7b4af8-f774-4028-9e92-24e78eee0f72"), new VaccineDoseColumn("penta2",
	            "50e945fd-f828-43c4-bf13-e4b555c5b852"), new VaccineDoseColumn("pcv2",
	            "287d2d7b-f9c5-4568-9c98-cf182f0d6f55"), new VaccineDoseColumn("rota2",
	            "1f40e138-d2b6-4881-bc5e-d45fd5ebe25e"), new VaccineDoseColumn("opv3",
	            "078dc96b-01f1-49da-b70a-dd20dcb5ee7f"), new VaccineDoseColumn("penta3",
	            "5738a0e8-3b28-444a-9b2f-9f9e3786ba07"), new VaccineDoseColumn("pcv3",
	            "1e86622d-fc24-4d72-ad8e-3623b2ee0b7a"), new VaccineDoseColumn("rota3",
	            "ff329793-7ded-40ab-a51a-f8477a52d519"),
	    new VaccineDoseColumn("ipv", "412201a9-af61-4edf-9bf0-19510819e314"), new VaccineDoseColumn("vitamin_a",
	            "37fc0998-dbb6-468b-aab4-fe06973a70be"), new VaccineDoseColumn("measles1",
	            "4f4de487-c10a-49e0-b88e-946cdc8b42fb"), new VaccineDoseColumn("yellow_fever",
	            "400ad32a-1d5d-46cf-ae0d-fb683c4f1f6a"), new VaccineDoseColumn("measles2",
	            "4797e3eb-5710-4deb-982e-40b0af717142"), new VaccineDoseColumn("men_a",
	            "2e993c4f-94bd-46f2-b5e2-b7cd828dfad7"),
	    new VaccineDoseColumn("hpv", "668fdecc-eba6-45d7-a096-b123a78de299"));
	
	/** Row 88: AEFI cases reported, by severity. Row 90: outcome of serious cases investigated. */
	private static final List<CodedObsColumn> AEFI_COLUMNS = Arrays.asList(new CodedObsColumn("aefi_reported_non_serious",
	        "d506ce85-f1c4-4f2d-af8a-9ef7a15ea5b4", "7fd1111d-dc22-4b05-9c94-ee7eda9fbacc"), new CodedObsColumn(
	        "aefi_reported_serious", "d506ce85-f1c4-4f2d-af8a-9ef7a15ea5b4", "af4d704c-348e-4613-89d6-82cfccebbc45"),
	    new CodedObsColumn("aefi_outcome_alive", "5b798926-731d-4c9d-9057-1b2c1a00cbea",
	            "84bcce14-a4b5-4390-b2e6-bcf8cee1bc62"), new CodedObsColumn("aefi_outcome_dead",
	            "5b798926-731d-4c9d-9057-1b2c1a00cbea", "18d3a3b9-f896-4513-8aa6-eb794ff2c596"));
	
	/** Row 89: serious AEFI case investigated (a simple per-encounter flag). */
	private static final List<BooleanObsColumn> AEFI_FLAG_COLUMNS = Arrays.asList(new BooleanObsColumn(
	        "aefi_serious_investigated", "3dbae94a-3eee-4cb0-a0e1-a349817eabcc"));
	
	public SqlDataSetDefinition build() {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("NHMIS - Immunization");
		dsd.setDescription("Form rows 63-90: Tetanus Diphtheria, EPI antigens by age/location, AEFI reporting");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.setSqlQuery(buildSql());
		return dsd;
	}
	
	/**
	 * Doses and AEFI questions live at different grains (many doses can share one encounter), so
	 * they are aggregated in two independent single-row subqueries and combined with a cross join -
	 * joining them at a shared grain would multiply AEFI counts by however many doses each
	 * encounter had.
	 */
	private String buildSql() {
		return "SELECT * FROM (\n" + buildDoseAggregateSql() + "\n) doses,\n(\n" + buildAefiAggregateSql() + "\n) aefi";
	}
	
	private String buildDoseAggregateSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) AS doses_total");
		for (String dose : Arrays.asList("1", "2", "3", "4", "5")) {
			sql.append(",\n  SUM(CASE WHEN vaccine_uuid = '").append(TD_PREGNANT_CONCEPT).append("' AND td_dose_number = ")
			        .append(dose).append(" THEN 1 ELSE 0 END) AS td_pregnant_dose").append(dose);
		}
		for (String dose : Arrays.asList("1", "2", "3", "4", "5")) {
			sql.append(",\n  SUM(CASE WHEN vaccine_uuid = '").append(TD_NON_PREGNANT_CONCEPT)
			        .append("' AND td_dose_number = ").append(dose).append(" THEN 1 ELSE 0 END) AS td_non_pregnant_dose")
			        .append(dose);
		}
		VaccineDoseIndicatorFactory.appendSelectColumns(sql, ANTIGEN_COLUMNS);
		sql.append(",\n  SUM(CASE WHEN vaccine_uuid = '").append(FULLY_IMMUNIZED_UNDER1_CONCEPT)
		        .append("' AND location_band = 'Fixed' THEN 1 ELSE 0 END) AS fully_immunized_lt1yr_fixed");
		sql.append(",\n  SUM(CASE WHEN vaccine_uuid = '").append(FULLY_IMMUNIZED_UNDER1_CONCEPT)
		        .append("' AND location_band = 'Outreach' THEN 1 ELSE 0 END) AS fully_immunized_lt1yr_outreach");
		
		sql.append("\nFROM (\n");
		sql.append("  SELECT o.encounter_id, vc.uuid AS vaccine_uuid,\n");
		sql.append("    CASE WHEN TIMESTAMPDIFF(YEAR, p.birthdate, e.encounter_datetime) < 1 THEN '")
		        .append(VaccineDoseIndicatorFactory.UNDER_1YR).append("' ELSE '")
		        .append(VaccineDoseIndicatorFactory.OVER_1YR).append("' END AS age_band,\n");
		sql.append("    CASE WHEN l.name = 'Mobile Clinic' THEN '").append(VaccineDoseIndicatorFactory.OUTREACH)
		        .append("' ELSE '").append(VaccineDoseIndicatorFactory.FIXED).append("' END AS location_band,\n");
		sql.append("    td.value_numeric AS td_dose_number\n");
		sql.append("  FROM obs o\n");
		sql.append("  JOIN concept vc ON vc.concept_id = o.value_coded\n");
		sql.append("  JOIN encounter e ON e.encounter_id = o.encounter_id\n");
		sql.append("  JOIN encounter_type et ON et.encounter_type_id = e.encounter_type\n");
		sql.append("  JOIN person p ON p.person_id = e.patient_id AND p.voided = 0\n");
		sql.append("  JOIN location l ON l.location_id = e.location_id\n");
		sql.append("  LEFT JOIN obs td ON td.obs_group_id = o.obs_group_id AND td.voided = 0\n");
		sql.append("    AND td.concept_id = (SELECT concept_id FROM concept WHERE uuid = '").append(TD_DOSE_NUMBER_CONCEPT)
		        .append("')\n");
		sql.append("  WHERE o.voided = 0 AND e.voided = 0 AND et.name = 'Immunization Record'\n");
		sql.append("    AND o.concept_id = (SELECT concept_id FROM concept WHERE uuid = '")
		        .append(VACCINE_ADMINISTERED_CONCEPT).append("')\n");
		sql.append("    AND e.encounter_datetime BETWEEN :startDate AND :endDate\n");
		sql.append("  ) x");
		return sql.toString();
	}
	
	private String buildAefiAggregateSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) AS aefi_encounters_total");
		CodedObsIndicatorFactory.appendSelectColumns(sql, "y", AEFI_COLUMNS);
		BooleanObsIndicatorFactory.appendSelectColumns(sql, AEFI_FLAG_COLUMNS);
		sql.append("\nFROM (\n");
		sql.append("  SELECT e.encounter_id\n");
		sql.append("  FROM encounter e\n");
		sql.append("  JOIN encounter_type et ON et.encounter_type_id = e.encounter_type\n");
		sql.append("  WHERE e.voided = 0 AND et.name = 'Immunization Record'\n");
		sql.append("    AND e.encounter_datetime BETWEEN :startDate AND :endDate\n");
		sql.append("  ) y");
		CodedObsIndicatorFactory.appendJoins(sql, "y", AEFI_COLUMNS);
		BooleanObsIndicatorFactory.appendJoins(sql, "y", AEFI_FLAG_COLUMNS);
		return sql.toString();
	}
}
