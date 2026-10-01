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
import static org.openmrs.module.medihealth.report.NhmisSql.encounters;
import static org.openmrs.module.medihealth.report.NhmisSql.obsResults;
import static org.openmrs.module.medihealth.report.NhmisSql.obsTrue;
import static org.openmrs.module.medihealth.report.NhmisSql.union;

import java.util.Collections;

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.springframework.stereotype.Component;

/**
 * Builds the "Gender Based Violence Care Services" section of the NHMIS Monthly Summary Form (form
 * rows 166-168), by sex and age under 20 / 20 and over. Cases seen are "Gender Based Violence Care"
 * encounters or a GBV/sexual assault/abuse diagnosis; care received and referrals come from that
 * form, and referrals also from a "Referral Out" for GBV care.
 */
@Component("medihealth.GbvDataSetBuilder")
public class GbvDataSetBuilder {
	
	private static final String CARE_RECEIVED = "887751e5-8c90-55f7-bc3c-deb81ba97e0f";
	
	public SqlDataSetDefinition build() {
		NhmisBlock seen = new NhmisBlock(union(encounters(NhmisConcepts.GBV_CARE), diagnoses(NhmisConcepts.GBV)));
		NhmisBlock care = new NhmisBlock(obsTrue(CARE_RECEIVED));
		NhmisBlock referred = new NhmisBlock(union(
		    obsTrue(NhmisConcepts.GBV_REFERRED),
		    obsResults(Collections.singletonList(NhmisConcepts.REFERRAL_REASON),
		        Collections.singletonList(NhmisConcepts.REFERRAL_GBV))));
		byBand(seen, "gbv_cases_seen");
		byBand(care, "gbv_post_care_received");
		byBand(referred, "gbv_referred");
		return NhmisSql.dataSet("NHMIS - Gender Based Violence",
		    "Form rows 166-168: GBV cases seen, receiving post-GBV care, and referred for further treatment", seen, care,
		    referred);
	}
	
	private static void byBand(NhmisBlock block, String prefix) {
		for (String[] s : NhmisBlock.SEXES) {
			block.patients(prefix + "_" + s[1] + "_lt20", "x.sex = '" + s[0] + "' AND x.age_y < 20");
			block.patients(prefix + "_" + s[1] + "_gte20", "x.sex = '" + s[0] + "' AND x.age_y >= 20");
		}
		block.patients(prefix + "_total", "1 = 1");
	}
}
