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
 * One row per day: total units dispensed and total revenue across all medications, so "how much was
 * made today" is a single number instead of having to sum
 * {@link PharmacySalesByMedicationDataSetBuilder}'s detail rows by hand. Same medication
 * definition, bill-status filter, and per-unit-price multiplication as that dataset - see its class
 * comment for the reasoning.
 */
@Component("medihealth.PharmacySalesDailyTotalsDataSetBuilder")
public class PharmacySalesDailyTotalsDataSetBuilder {
	
	public SqlDataSetDefinition build() {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("Pharmacy Sales - Daily Totals");
		dsd.setDescription("One row per day: total quantity dispensed and total revenue, across all medications");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.setSqlQuery(buildSql());
		return dsd;
	}
	
	private String buildSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DATE(cb.date_created) AS sale_date,\n");
		sql.append("  SUM(cbli.quantity) AS total_quantity_dispensed,\n");
		sql.append("  SUM(cbli.quantity * cbli.price) AS total_revenue\n");
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
		sql.append("GROUP BY DATE(cb.date_created)\n");
		sql.append("ORDER BY sale_date");
		return sql.toString();
	}
}
