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

import static org.openmrs.module.medihealth.report.NhmisSql.diagnoses;
import static org.openmrs.module.medihealth.report.NhmisSql.drugOrders;
import static org.openmrs.module.medihealth.report.NhmisSql.encounters;
import static org.openmrs.module.medihealth.report.NhmisSql.hasAnswer;
import static org.openmrs.module.medihealth.report.NhmisSql.union;

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.springframework.stereotype.Component;

/**
 * Builds the last two sections of the NHMIS Monthly Summary Form: "Neglected Tropical Diseases"
 * (rows 176-178: snake bite, elephantiasis and yaws new cases, from diagnoses, with an antivenom
 * order also counting as a snake bite) and "Pharmacovigilance" (rows 179-180: adverse drug
 * reactions reported on the "Adverse Drug Reaction Report" form after immunization or
 * antimalarials).
 */
@Component("medihealth.NtdPharmacovigilanceDataSetBuilder")
public class NtdPharmacovigilanceDataSetBuilder {
	
	private static final String ADR_CATEGORY = "188e8c59-92b7-531a-ae8c-04551f7649c6";
	
	public SqlDataSetDefinition build() {
		NhmisBlock snakeBite = new NhmisBlock(
		        union(diagnoses(NhmisConcepts.SNAKE_BITE), drugOrders(NhmisConcepts.ANTIVENOM))).patientsBySex(
		    "snake_bite_new_cases", "1 = 1");
		NhmisBlock elephantiasis = new NhmisBlock(diagnoses(NhmisConcepts.ELEPHANTIASIS)).patientsBySex(
		    "elephantiasis_new_cases", "1 = 1");
		NhmisBlock yaws = new NhmisBlock(diagnoses(NhmisConcepts.YAWS)).patientsBySex("yaws_new_cases", "1 = 1");
		NhmisBlock adr = new NhmisBlock(encounters(NhmisConcepts.ADR_REPORT))
		        .with("immunization", hasAnswer("ev.event_id", ADR_CATEGORY, "409ad0d8-6ad7-5f7b-a13e-ca2972b1ecbd"))
		        .with("antimalarial", hasAnswer("ev.event_id", ADR_CATEGORY, "08f9b987-cb43-5361-9a0f-5e173c100215"))
		        .events("adr_following_immunization", "x.immunization = 1")
		        .events("adr_following_antimalarials", "x.antimalarial = 1");
		return NhmisSql.dataSet("NHMIS - NTDs and Pharmacovigilance",
		    "Form rows 176-180: snake bite, elephantiasis and yaws new cases; ADRs reported after immunization "
		            + "and antimalarials", snakeBite, elephantiasis, yaws, adr);
	}
}
