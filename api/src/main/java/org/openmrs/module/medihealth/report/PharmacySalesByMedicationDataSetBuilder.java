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
 * since {@code cashier_bill_line_item} stores a per-unit price, not a pre-multiplied line total.
 * <p>
 * {@code cashier_billable_service} does not have a {@code voided} column on this server - a
 * migration changeset in the billing module (id
 * "openmrs.billing-001-20251014-migrate-billable-services-to-metadata") renamed it to
 * {@code retired} (along with {@code voided_by}/{@code date_voided}/{@code void_reason} to their
 * retired-pattern equivalents) before this instance's schema was ever created, even though the
 * module's own initial {@code createTable} still lists the old names - the running SQL failed with
 * "Unknown column 'cbs.voided'" until this was caught from the actual evaluation error and traced
 * through every changeset touching that table, not just the first one. {@code cashier_bill.status}
 * went through the same kind of migration: originally an int matching the {@code BillStatus} enum
 * ordinals, a later changeset added a new varchar column, backfilled it from the int values
 * (including the literal "ADJUSTED" mapping visible in that changeset's own migration SQL), dropped
 * the int column, and renamed the varchar one into its place - so {@code status} is compared
 * against the enum's name strings here, not its ordinals.
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
		sql.append("JOIN cashier_billable_service cbs ON cbs.service_id = cbli.service_id AND cbs.retired = 0\n");
		sql.append("WHERE cbli.voided = 0\n");
		sql.append("  AND cb.status IN ('POSTED', 'PAID')\n");
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
