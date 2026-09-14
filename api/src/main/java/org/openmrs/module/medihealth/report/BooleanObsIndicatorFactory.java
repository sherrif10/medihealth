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
 * Most rows in the ANC, Labour &amp; Delivery, Postnatal Care and Newborn Health sections of the
 * NHMIS Monthly Summary Form reduce to the same shape: "count encounters of type X, within the
 * reporting period, where boolean concept Y was recorded true". This factory builds the repeated
 * "one LEFT JOIN to obs, one SUM(...) column" part of that shape, so each section's dataset builder
 * only has to list its (row, concept) pairs and compose them with whatever else that section's
 * query also needs (age bands, other obs values, etc).
 */
public class BooleanObsIndicatorFactory {
	
	/**
	 * OpenMRS has no {@code obs.value_boolean} column - a Boolean-datatype obs stores its answer as
	 * {@code value_coded} pointing at whichever concept the {@code concept.true} global property
	 * names (id 1/2 are common but not guaranteed, so this is resolved per-instance rather than
	 * hardcoded).
	 */
	public static final String TRUE_CONCEPT_ID_SQL = "(SELECT CAST(property_value AS UNSIGNED) FROM global_property "
	        + "WHERE property = 'concept.true')";
	
	/**
	 * Appends one {@code SUM(CASE WHEN ... value_coded = <true concept> ...) AS <alias>} column per
	 * entry.
	 */
	public static void appendSelectColumns(StringBuilder sql, List<BooleanObsColumn> columns) {
		for (BooleanObsColumn c : columns) {
			sql.append(",\n  SUM(CASE WHEN ").append(joinAlias(c)).append(".value_coded = ").append(TRUE_CONCEPT_ID_SQL)
			        .append(" THEN 1 ELSE 0 END) AS ").append(c.columnAlias);
		}
	}
	
	/**
	 * Appends one {@code LEFT JOIN obs ... ON ...encounter_id = <baseAlias>.encounter_id AND
	 * ...concept_id = (looked up by uuid)} per entry.
	 * 
	 * @param baseAlias alias of the already-joined derived table/subquery that exposes
	 *            {@code encounter_id}
	 */
	public static void appendJoins(StringBuilder sql, String baseAlias, List<BooleanObsColumn> columns) {
		for (BooleanObsColumn c : columns) {
			String j = joinAlias(c);
			sql.append("\nLEFT JOIN obs ").append(j).append(" ON ").append(j).append(".encounter_id = ").append(baseAlias)
			        .append(".encounter_id").append(" AND ").append(j).append(".voided = 0").append(" AND ").append(j)
			        .append(".concept_id = (SELECT concept_id FROM concept WHERE uuid = '").append(c.conceptUuid)
			        .append("')");
		}
	}
	
	private static String joinAlias(BooleanObsColumn c) {
		return "o_" + c.columnAlias;
	}
	
	private BooleanObsIndicatorFactory() {
	}
}
