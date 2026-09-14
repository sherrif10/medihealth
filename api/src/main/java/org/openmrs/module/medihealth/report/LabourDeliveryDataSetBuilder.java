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
 * Builds the "Maternal Health (Labour and Delivery)" section of the NHMIS Monthly Summary Form
 * (form rows 34-46), from the new "Labour and Delivery" encounter type and its boolean obs (see
 * {@code labour_delivery-medihealth.json} and {@code labour_delivery_concepts-medihealth.csv}).
 * <p>
 * Row 39 (deliveries by adolescent mother) needs no new concept - it is derived directly from the
 * mother's age at the delivery encounter. Every other row is "count deliveries where flag concept X
 * was recorded true", handled generically by {@link BooleanObsIndicatorFactory}.
 */
@Component("medihealth.LabourDeliveryDataSetBuilder")
public class LabourDeliveryDataSetBuilder {
	
	private static final List<BooleanObsColumn> FLAG_COLUMNS = Arrays.asList(new BooleanObsColumn(
	        "decision_seeking_care_lt24h", "6916fc76-c947-42e5-9577-3a025281c7b1"), new BooleanObsColumn(
	        "transportation_in", "49fc0f59-cba9-47c3-9388-718e3d6165a9"), new BooleanObsColumn("delivery_svd",
	        "ad62e5dd-4d33-49f3-b564-f0f0c71ea852"), new BooleanObsColumn("delivery_assisted",
	        "f78b8796-ed6f-422c-a8d8-f6ca7fdc1dd4"), new BooleanObsColumn("delivery_caesarean",
	        "48a99ac4-f02a-466d-a8bf-f76e42d6193e"), new BooleanObsColumn("preterm_birth",
	        "83be66e0-2476-4ddd-b8af-6fbb00ac73f1"), new BooleanObsColumn("delivery_complications_mother",
	        "b647706f-b788-4153-86be-1a70e7644197"), new BooleanObsColumn("delivery_partograph_used",
	        "2b762309-edfb-4dcc-abac-26ac43088355"), new BooleanObsColumn("delivery_sba",
	        "5a7fd030-3117-4e4f-8598-121ef4244541"), new BooleanObsColumn("uterotonic_oxytocin",
	        "5ca0532b-16f2-4aba-beed-a2f375637a75"), new BooleanObsColumn("uterotonic_misoprostol",
	        "d93eef7a-8729-47ad-9be3-de82cd9479eb"), new BooleanObsColumn("eclampsia_mgso4",
	        "64c20c97-3d5b-4c94-802c-6c87372293da"), new BooleanObsColumn("abortion_induced",
	        "cb57c268-ce6c-4eac-9d21-dade59f199ae"), new BooleanObsColumn("abortion_spontaneous",
	        "b425df3f-865f-4701-a33d-0a8d93aef984"), new BooleanObsColumn("post_abortion_care",
	        "bbada042-30c9-4019-85b3-413dd0381a51"), new BooleanObsColumn("unsafe_abortion_complications",
	        "7952557e-86cb-4e8f-ad54-b56c6f77868b"));
	
	public SqlDataSetDefinition build() {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("NHMIS - Labour and Delivery");
		dsd.setDescription("Form rows 34-46: admission, delivery mode/complications, medication, abortion/PAC");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.setSqlQuery(buildSql());
		return dsd;
	}
	
	private String buildSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) AS deliveries_total");
		sql.append(",\n  SUM(CASE WHEN age_years BETWEEN 10 AND 19 THEN 1 ELSE 0 END) AS deliveries_adolescent_mother");
		BooleanObsIndicatorFactory.appendSelectColumns(sql, FLAG_COLUMNS);
		
		sql.append("\nFROM (\n");
		sql.append("  SELECT ev.encounter_id, TIMESTAMPDIFF(YEAR, p.birthdate, ev.event_date) AS age_years\n");
		sql.append("  FROM (\n");
		sql.append("    SELECT e.encounter_id, e.patient_id, e.encounter_datetime AS event_date\n");
		sql.append("    FROM encounter e\n");
		sql.append("    JOIN encounter_type et ON et.encounter_type_id = e.encounter_type\n");
		sql.append("    WHERE e.voided = 0 AND et.name = 'Labour and Delivery'\n");
		sql.append("      AND e.encounter_datetime BETWEEN :startDate AND :endDate\n");
		sql.append("  ) ev\n");
		sql.append("  JOIN person p ON p.person_id = ev.patient_id AND p.voided = 0\n");
		sql.append(") x");
		BooleanObsIndicatorFactory.appendJoins(sql, "x", FLAG_COLUMNS);
		return sql.toString();
	}
}
