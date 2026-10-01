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

import static org.openmrs.module.medihealth.report.NhmisSql.POSITIVE_RESULTS;
import static org.openmrs.module.medihealth.report.NhmisSql.drugOrders;
import static org.openmrs.module.medihealth.report.NhmisSql.everDiagnosed;
import static org.openmrs.module.medihealth.report.NhmisSql.everResult;
import static org.openmrs.module.medihealth.report.NhmisSql.obsResults;
import static org.openmrs.module.medihealth.report.NhmisSql.obsTrue;
import static org.openmrs.module.medihealth.report.NhmisSql.union;

import java.util.Collections;
import java.util.List;

import org.openmrs.module.medihealth.report.NhmisSql.DrugMatch;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.springframework.stereotype.Component;

/**
 * Builds the "Hepatitis B" and "Hepatitis C" screening and treatment sections of the NHMIS Monthly
 * Summary Form (form rows 158-165), by sex and age 10-19 / 20 and over.
 * <ul>
 * <li>Tested / positive: lab results of the hepatitis tests, plus the ANC form's test flags.</li>
 * <li>Treated: a hepatitis medicine ordered in the month for a patient with a positive test or a
 * hepatitis diagnosis on record (so tenofovir for HIV alone is not counted).</li>
 * <li>Referred: "Referral Out" for hepatitis treatment, plus the ANC form's referral flags.</li>
 * </ul>
 */
@Component("medihealth.HepatitisDataSetBuilder")
public class HepatitisDataSetBuilder {
	
	public SqlDataSetDefinition build() {
		NhmisBlock[] b = section("hepb", NhmisConcepts.HEPATITIS_B_TESTS, "71628a1e-447e-4897-aaa2-10fc57c96fe2",
		    "0639c9d7-6a66-4aa0-a42e-7193761e7e22", NhmisConcepts.HEPATITIS_B, NhmisConcepts.HEPATITIS_B_TREATMENT,
		    NhmisConcepts.REFERRAL_HEPATITIS_B, NhmisConcepts.ANC_HEPB_REFERRED);
		NhmisBlock[] c = section("hepc", NhmisConcepts.HEPATITIS_C_TESTS, "0988bebc-0245-4fc5-84c1-2c7a922e698d",
		    "bde5a07e-2085-48fb-bbd9-fb41046b662f", NhmisConcepts.HEPATITIS_C, NhmisConcepts.HEPATITIS_C_TREATMENT,
		    NhmisConcepts.REFERRAL_HEPATITIS_C, NhmisConcepts.ANC_HEPC_REFERRED);
		return NhmisSql.dataSet("NHMIS - Hepatitis B and C",
		    "Form rows 158-165: persons tested, positive, treated and referred for hepatitis B and C", b[0], b[1], b[2],
		    b[3], c[0], c[1], c[2], c[3]);
	}
	
	private NhmisBlock[] section(String prefix, List<String> tests, String ancTestDone, String ancPositive,
	        List<String> diagnosis, DrugMatch treatment, String referralReason, String ancReferred) {
		NhmisBlock tested = new NhmisBlock(union(obsResults(tests, null), obsTrue(ancTestDone)));
		NhmisBlock positive = new NhmisBlock(union(obsResults(tests, POSITIVE_RESULTS), obsTrue(ancPositive)));
		NhmisBlock treated = new NhmisBlock(drugOrders(treatment)).with("infected",
		    everResult("ev.patient_id", tests, POSITIVE_RESULTS) + " OR " + everDiagnosed("ev.patient_id", diagnosis));
		NhmisBlock referred = new NhmisBlock(union(
		    obsResults(Collections.singletonList(NhmisConcepts.REFERRAL_REASON), Collections.singletonList(referralReason)),
		    obsTrue(ancReferred)));
		byBand(tested, prefix + "_tested", "1 = 1");
		byBand(positive, prefix + "_positive", "1 = 1");
		byBand(treated, prefix + "_treated", "x.infected = 1");
		byBand(referred, prefix + "_referred", "1 = 1");
		return new NhmisBlock[] { tested, positive, treated, referred };
	}
	
	private static void byBand(NhmisBlock block, String prefix, String condition) {
		for (String[] s : NhmisBlock.SEXES) {
			String sex = condition + " AND x.sex = '" + s[0] + "'";
			block.patients(prefix + "_" + s[1] + "_10_19", sex + " AND x.age_y BETWEEN 10 AND 19");
			block.patients(prefix + "_" + s[1] + "_gte20", sex + " AND x.age_y >= 20");
		}
		block.patients(prefix + "_total", condition + " AND x.age_y >= 10");
	}
}
