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
 * Counterpart to {@link BooleanObsIndicatorFactory} for rows that read a specific answer off a
 * Coded-datatype obs (e.g. AEFI severity: Non-Serious/Serious) rather than a true/false flag.
 * Several columns can share the same question concept but count different answers (one join per
 * distinct question, reused across its columns).
 */
public class CodedObsIndicatorFactory {
	
	public static void appendSelectColumns(StringBuilder sql, String baseAlias, List<CodedObsColumn> columns) {
		for (CodedObsColumn c : columns) {
			sql.append(",\n  SUM(CASE WHEN ").append(joinAlias(c))
			        .append(".value_coded = (SELECT concept_id FROM concept " + "WHERE uuid = '")
			        .append(c.answerConceptUuid).append("') THEN 1 ELSE 0 END) AS ").append(c.columnAlias);
		}
	}
	
	/**
	 * One LEFT JOIN per distinct question concept among the columns (not one per column, to avoid
	 * duplicate joins).
	 */
	public static void appendJoins(StringBuilder sql, String baseAlias, List<CodedObsColumn> columns) {
		java.util.LinkedHashMap<String, String> seen = new java.util.LinkedHashMap<>();
		for (CodedObsColumn c : columns) {
			seen.putIfAbsent(c.questionConceptUuid, joinAlias(c));
		}
		for (java.util.Map.Entry<String, String> e : seen.entrySet()) {
			String alias = e.getValue();
			sql.append("\nLEFT JOIN obs ").append(alias).append(" ON ").append(alias).append(".encounter_id = ")
			        .append(baseAlias).append(".encounter_id AND ").append(alias).append(".voided = 0 AND ").append(alias)
			        .append(".concept_id = (SELECT concept_id FROM concept WHERE uuid = '").append(e.getKey())
			        .append("')");
		}
	}
	
	private static String joinAlias(CodedObsColumn c) {
		return "oc_" + questionAliasPart(c.questionConceptUuid);
	}
	
	private static String questionAliasPart(String questionUuid) {
		return questionUuid.substring(0, 8);
	}
	
	private CodedObsIndicatorFactory() {
	}
}
