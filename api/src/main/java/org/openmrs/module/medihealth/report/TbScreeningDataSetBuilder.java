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

import static org.openmrs.module.medihealth.report.NhmisSql.encounters;
import static org.openmrs.module.medihealth.report.NhmisSql.hasTrue;

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.springframework.stereotype.Component;

/**
 * Builds the "Tuberculosis Screening" section of the NHMIS Monthly Summary Form (form rows 155-157)
 * from the "TB Screening" form. The score is one point per symptom (cough for 2 weeks or more,
 * fever, weight loss, night sweats); "persons with 1 score" are those scoring at least 1.
 */
@Component("medihealth.TbScreeningDataSetBuilder")
public class TbScreeningDataSetBuilder {
	
	private static final String[] SYMPTOMS = { "e9479489-b33f-59c3-b463-6a418d3fd7a1",
	        "a0ec884f-66f9-5363-bcca-80917e9bbe8c", "9d62eb77-3da4-578d-b112-629e82e15f4d",
	        "9862292f-1d21-5f35-b074-3dd60cdb8db8" };
	
	public SqlDataSetDefinition build() {
		StringBuilder score = new StringBuilder();
		for (String symptom : SYMPTOMS) {
			score.append(score.length() == 0 ? "" : " + ").append('(').append(hasTrue("ev.event_id", symptom)).append(')');
		}
		NhmisBlock tb = new NhmisBlock(encounters(NhmisConcepts.TB_SCREENING)).with("score", score.toString())
		        .with("referred", hasTrue("ev.event_id", NhmisConcepts.TB_REFERRED)).patientsBySex("tb_screened", "1 = 1")
		        .patientsBySex("tb_score_1", "x.score >= 1")
		        .patientsBySex("tb_score_1_referred", "x.score >= 1 AND x.referred = 1");
		return NhmisSql.dataSet("NHMIS - TB Screening",
		    "Form rows 155-157: persons screened for TB, scoring 1 or more, and referred to TB services", tb);
	}
}
