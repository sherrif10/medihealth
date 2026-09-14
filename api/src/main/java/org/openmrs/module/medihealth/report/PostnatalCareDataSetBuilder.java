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
 * Builds the "Maternal Health (Post-Natal Care)" section of the NHMIS Monthly Summary Form (form
 * row 47: postnatal clinic visits for mothers and newborns, by day band since delivery). Staff tick
 * which of the 8 day-band/mother-or-newborn combinations a visit matches at data entry time, the
 * same way the paper register is filled - see {@code postnatal_visit-medihealth.json}.
 */
@Component("medihealth.PostnatalCareDataSetBuilder")
public class PostnatalCareDataSetBuilder {
	
	private static final List<BooleanObsColumn> FLAG_COLUMNS = Arrays.asList(new BooleanObsColumn("pnc_mother_day1",
	        "9b7b3582-5d01-462b-a708-25ad06958495"), new BooleanObsColumn("pnc_mother_2to3days",
	        "4537999f-44f1-4c03-8a78-92ebfe2246e0"), new BooleanObsColumn("pnc_mother_4to7days",
	        "592e708a-fcb5-4b91-9ec0-412462ba3e5c"), new BooleanObsColumn("pnc_mother_gt7days",
	        "e137e7eb-571a-438d-bd88-300ce848525c"), new BooleanObsColumn("pnc_newborn_day1",
	        "80755c22-b9cb-4413-b383-9bc0c29459e0"), new BooleanObsColumn("pnc_newborn_2to3days",
	        "606ca3c7-3e90-44b7-bfc8-0acefdf9c282"), new BooleanObsColumn("pnc_newborn_4to7days",
	        "f11c02c2-de02-47cc-b566-b01a7e39eee9"), new BooleanObsColumn("pnc_newborn_gt7days",
	        "71130877-e7fd-4e21-957a-d5b912c92017"));
	
	public SqlDataSetDefinition build() {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("NHMIS - Postnatal Care");
		dsd.setDescription("Form row 47: postnatal clinic visits for mothers and newborns, by day band");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.setSqlQuery(buildSql());
		return dsd;
	}
	
	private String buildSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) AS pnc_visits_total");
		BooleanObsIndicatorFactory.appendSelectColumns(sql, FLAG_COLUMNS);
		sql.append("\nFROM (\n");
		sql.append("  SELECT e.encounter_id\n");
		sql.append("  FROM encounter e\n");
		sql.append("  JOIN encounter_type et ON et.encounter_type_id = e.encounter_type\n");
		sql.append("  WHERE e.voided = 0 AND et.name = 'Postnatal Care Visit'\n");
		sql.append("    AND e.encounter_datetime BETWEEN :startDate AND :endDate\n");
		sql.append(") ev");
		BooleanObsIndicatorFactory.appendJoins(sql, "ev", FLAG_COLUMNS);
		return sql.toString();
	}
}
