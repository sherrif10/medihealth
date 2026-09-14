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
import java.util.List;

/**
 * Builds the repeated "one column per age band x sex" SQL fragments that most NHMIS Monthly Summary
 * Form rows need, so each {@code *DataSetBuilder} only has to say which row/filter it wants a
 * column for, instead of re-typing the 12-way age/sex cross product every time.
 */
public class AgeSexColumnFactory {
	
	public static final List<String> AGE_BANDS = Arrays.asList("0-28 days", "29d-11mths", "12-59mths", "5-9yrs", "10-19yrs",
	    ">=20yrs");
	
	public static final List<String> SEXES = Arrays.asList("Male", "Female");
	
	/**
	 * Appends one output column per age band x sex to {@code sql}, each counting rows of the
	 * (already sex/age_band-bucketed) source table that match that band/sex and, optionally, an
	 * extra condition (e.g. a visit type or encounter type filter). The column alias is
	 * {@code <rowPrefix>_<sex>_<band>}, matching how each cell will be labelled in the exported
	 * CSV/Excel.
	 * 
	 * @param sql the SQL string being built; columns are appended as ",<expr> AS <alias>"
	 * @param rowPrefix short machine name for the MSF row, e.g. "general_attendance"
	 * @param extraCondition additional SQL boolean condition ANDed into every column's CASE (e.g.
	 *            "visit_type_name = 'OPD Visit'"), or null for none
	 */
	public static void appendColumns(StringBuilder sql, String rowPrefix, String extraCondition) {
		for (String sex : SEXES) {
			for (String band : AGE_BANDS) {
				String condition = "sex = '" + sex + "' AND age_band = '" + band + "'";
				if (extraCondition != null) {
					condition += " AND " + extraCondition;
				}
				sql.append(",\n  SUM(CASE WHEN ").append(condition).append(" THEN 1 ELSE 0 END) AS ")
				        .append(columnAlias(rowPrefix, sex, band));
			}
		}
	}
	
	public static String columnAlias(String rowPrefix, String sex, String band) {
		String bandToken = band.toLowerCase().replace(">=", "gte").replace("-", "_").replace(" ", "");
		return rowPrefix + "_" + sex.toLowerCase() + "_" + bandToken;
	}
	
	private AgeSexColumnFactory() {
	}
}
