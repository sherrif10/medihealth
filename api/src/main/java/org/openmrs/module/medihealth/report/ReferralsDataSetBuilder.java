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

import static org.openmrs.module.medihealth.report.NhmisConcepts.REFERRAL_REASON;
import static org.openmrs.module.medihealth.report.NhmisSql.encounters;
import static org.openmrs.module.medihealth.report.NhmisSql.hasAnswer;
import static org.openmrs.module.medihealth.report.NhmisSql.obsTrue;
import static org.openmrs.module.medihealth.report.NhmisSql.union;

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.springframework.stereotype.Component;

/**
 * Builds the "Referrals" section of the NHMIS Monthly Summary Form (form rows 126-130). Row 126
 * (all out-going referrals) counts each patient once per day across the "Referral Out" form and the
 * "referred" flags of the TB screening, GBV care and ANC forms, so a referral recorded on both is
 * not counted twice. Rows 127-130 come from the reason on the "Referral Out" form.
 */
@Component("medihealth.ReferralsDataSetBuilder")
public class ReferralsDataSetBuilder {
	
	public SqlDataSetDefinition build() {
		NhmisBlock all = new NhmisBlock(union(encounters(NhmisConcepts.REFERRAL_OUT), obsTrue(NhmisConcepts.TB_REFERRED),
		    obsTrue(NhmisConcepts.GBV_REFERRED), obsTrue(NhmisConcepts.ANC_HEPB_REFERRED),
		    obsTrue(NhmisConcepts.ANC_HEPC_REFERRED))).patientDays("referrals_out_total", "1 = 1");
		NhmisBlock reasons = new NhmisBlock(encounters(NhmisConcepts.REFERRAL_OUT))
		        .with("malaria_treatment",
		            hasAnswer("ev.event_id", REFERRAL_REASON, NhmisConcepts.REFERRAL_MALARIA_TREATMENT))
		        .with("malaria_adr", hasAnswer("ev.event_id", REFERRAL_REASON, NhmisConcepts.REFERRAL_MALARIA_ADR))
		        .with("pregnancy", hasAnswer("ev.event_id", REFERRAL_REASON, NhmisConcepts.REFERRAL_PREGNANCY_COMPLICATION))
		        .with("fistula", hasAnswer("ev.event_id", REFERRAL_REASON, NhmisConcepts.REFERRAL_FISTULA))
		        .patients("malaria_referred_further_treatment", "x.malaria_treatment = 1")
		        .patients("malaria_referred_adverse_drug_reaction", "x.malaria_adr = 1")
		        .patients("women_referred_pregnancy_complications", "x.pregnancy = 1 AND x.sex = 'F'")
		        .patients("women_referred_obstetric_fistula", "x.fistula = 1 AND x.sex = 'F'");
		return NhmisSql.dataSet("NHMIS - Referrals",
		    "Form rows 126-130: out-going referrals, malaria referrals, pregnancy complication and fistula referrals", all,
		    reasons);
	}
}
