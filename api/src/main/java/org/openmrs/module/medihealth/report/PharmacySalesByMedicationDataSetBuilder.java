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
 * One row per (day, medication): how many units were dispensed and how much revenue they made, for
 * auditing what actually moved out of the pharmacy on a given day.
 * <p>
 * "Medication" here means every {@code cashier_billable_service} whose Service Type is the
 * "Pharmacy" concept - this is how medications are already distinguished from non-medication
 * billable services (consultations, Ambulance, lab tests, etc.) in this instance's existing data;
 * every drug-named billable service checked was consistently tagged this way, and no non-medication
 * one was. Resolved by concept name rather than a hard-coded UUID, since the concept dictionary
 * UUID for "Pharmacy" is not guaranteed to be the same across instances.
 * <p>
 * Only POSTED and PAID bills count as a real sale; PENDING (not yet finalised), CANCELLED, and
 * ADJUSTED (superseded by a later adjustment) bills are excluded. Revenue is quantity x unit price,
 * since {@code cashier_bill_line_item} stores a per-unit price, not a pre-multiplied line total -
 * confirm this multiplication against a real test bill before relying on the totals (see testing
 * steps).
 */
@Component("medihealth.PharmacySalesByMedicationDataSetBuilder")
public class PharmacySalesByMedicationDataSetBuilder {
	
	public SqlDataSetDefinition build() {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("Pharmacy Sales - By Medication");
		dsd.setDescription("One row per day per medication: quantity dispensed and revenue");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.setSqlQuery(buildSql());
		return dsd;
	}
	
	private String buildSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DATE(cb.date_created) AS sale_date,\n");
		sql.append("  cbs.name AS medication_name,\n");
		sql.append("  SUM(cbli.quantity) AS quantity_dispensed,\n");
		sql.append("  SUM(cbli.quantity * cbli.price) AS revenue\n");
		sql.append("FROM cashier_bill_line_item cbli\n");
		sql.append("JOIN cashier_bill cb ON cb.bill_id = cbli.bill_id AND cb.voided = 0\n");
		sql.append("JOIN cashier_billable_service cbs ON cbs.service_id = cbli.service_id AND cbs.voided = 0\n");
		sql.append("WHERE cbli.voided = 0\n");
		sql.append("  AND cb.status IN (1, 2)\n"); // POSTED, PAID - see BillStatus enum ordinals
		sql.append("  AND cbs.service_type = (\n");
		sql.append("    SELECT concept_id FROM concept_name\n");
		sql.append("    WHERE name = 'Pharmacy' AND voided = 0\n");
		sql.append("    ORDER BY locale_preferred DESC LIMIT 1\n");
		sql.append("  )\n");
		sql.append("  AND cb.date_created BETWEEN :startDate AND :endDate\n");
		sql.append("GROUP BY DATE(cb.date_created), cbs.name\n");
		sql.append("ORDER BY sale_date, medication_name");
		return sql.toString();
	}
}
