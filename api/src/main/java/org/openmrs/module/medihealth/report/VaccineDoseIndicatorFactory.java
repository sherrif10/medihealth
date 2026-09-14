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

import java.util.List;

/**
 * Every EPI/antigen row of the NHMIS Monthly Summary Form (rows 65-87) is "count doses of vaccine X
 * given in the period, split by recipient age (&lt;1yr / &gt;=1yr) and delivery location (Fixed /
 * Outreach)". This factory builds the repeated 4-column-per-row SQL for that shape, from a source
 * derived table exposing {@code vaccine_uuid}, {@code age_band}, {@code location_band} columns -
 * one row per dose actually given (not per encounter, since one visit can include several doses).
 */
public class VaccineDoseIndicatorFactory {
	
	public static final String UNDER_1YR = "<1yr";
	
	public static final String OVER_1YR = ">=1yr";
	
	public static final String FIXED = "Fixed";
	
	public static final String OUTREACH = "Outreach";
	
	public static void appendSelectColumns(StringBuilder sql, List<VaccineDoseColumn> columns) {
		for (VaccineDoseColumn c : columns) {
			appendColumn(sql, c, UNDER_1YR, FIXED);
			appendColumn(sql, c, UNDER_1YR, OUTREACH);
			appendColumn(sql, c, OVER_1YR, FIXED);
			appendColumn(sql, c, OVER_1YR, OUTREACH);
		}
	}
	
	private static void appendColumn(StringBuilder sql, VaccineDoseColumn c, String ageBand, String locationBand) {
		sql.append(",\n  SUM(CASE WHEN vaccine_uuid = '").append(c.vaccineConceptUuid).append("' AND age_band = '")
		        .append(ageBand).append("' AND location_band = '").append(locationBand).append("' THEN 1 ELSE 0 END) AS ")
		        .append(c.rowPrefix).append('_').append(ageBand.equals(UNDER_1YR) ? "lt1yr" : "gte1yr").append('_')
		        .append(locationBand.toLowerCase());
	}
	
	private VaccineDoseIndicatorFactory() {
	}
}
