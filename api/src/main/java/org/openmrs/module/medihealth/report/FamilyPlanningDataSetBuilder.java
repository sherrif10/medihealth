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
import static org.openmrs.module.medihealth.report.NhmisSql.hasAnswer;
import static org.openmrs.module.medihealth.report.NhmisSql.hasObs;
import static org.openmrs.module.medihealth.report.NhmisSql.hasTrue;
import static org.openmrs.module.medihealth.report.NhmisSql.numericSum;

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.springframework.stereotype.Component;

/**
 * Builds the "Family Planning" section of the NHMIS Monthly Summary Form (form rows 108-125) from
 * the "Family Planning" form. "Clients"/"women" rows count each person once; "given", "inserted"
 * and "dispensed" rows count visits (or the quantities recorded), since a client can be given a
 * method more than once in a month.
 */
@Component("medihealth.FamilyPlanningDataSetBuilder")
public class FamilyPlanningDataSetBuilder {
	
	private static final String METHOD = "6a08c06b-17f0-5018-881b-b57e1da2c0ca";
	
	private static final String REFERRED_FROM = "7835a79b-06b3-5328-9701-27734c39042e";
	
	/** Column flag, answer of "FP method provided". */
	private static final String[][] METHODS = { { "ocp", "e3a1fdf1-cd2a-5ad1-8fec-fc4898ee8aff" },
	        { "ecp", "5de74c1a-0cdf-5790-aff2-a50118c6c2fc" }, { "noristerat", "d4be1420-c26d-5720-8730-94b078ccdb20" },
	        { "dmpa_im", "b47e5227-4391-520a-83aa-539dcea6b14a" }, { "dmpa_sc", "dde264b7-d223-5204-a453-cf6137126448" },
	        { "iud_cut", "7868257d-d87a-5b09-8339-f218d994a646" }, { "lng_ius", "d5d35d93-72c8-55b6-b637-60e56fe52811" },
	        { "implanon", "46823fbf-d7c9-52b3-811f-25dfa04bb03e" }, { "jadelle", "203e7c26-5d89-5fa4-9b3f-1d47932cee98" },
	        { "sterilization", "716ce138-48bb-5747-8966-a3a23b33bc02" },
	        { "male_condoms", "6a573db5-88da-5709-9068-7f27095f31a4" },
	        { "female_condoms", "ed69d074-40a4-5758-aca6-7684cbddb809" } };
	
	private static final String[][] REFERRAL_SOURCES = { { "pmtct", "850663d3-801b-5736-85ef-5878982d2e30" },
	        { "hct", "e9464e73-08ba-5daa-8926-ccb3ee9e9a40" }, { "pac", "2a1f4cfd-e43c-5d2a-a3a6-a126174e075e" },
	        { "immunization", "fa1e61e0-1d60-53de-bfc5-85c930e35046" },
	        { "labour_delivery", "3c701e17-6032-51dd-a4c9-0be95633c290" } };
	
	/** Row 110 age bands, in years. */
	private static final Object[][] AGE_BANDS = { { "10_14", 10, 14 }, { "15_19", 15, 19 }, { "20_24", 20, 24 },
	        { "25_49", 25, 49 }, { "gte50", 50, 200 } };
	
	public SqlDataSetDefinition build() {
		NhmisBlock fp = new NhmisBlock(encounters(NhmisConcepts.FAMILY_PLANNING))
		        .with("counselled", hasTrue("ev.event_id", "49ee38aa-51e5-5dda-85a9-6d706d975023"))
		        .with("new_acceptor", hasTrue("ev.event_id", "8e501627-5fc2-5a44-976d-a99f1a8c9382"))
		        .with("postpartum", hasTrue("ev.event_id", "84631de9-61ea-5112-8e9d-5e5b41e09107"))
		        .with("any_method", hasObs("ev.event_id", METHOD)).with("referred_in", hasObs("ev.event_id", REFERRED_FROM))
		        .with("pill_cycles", numericSum("ev.event_id", "b8dbbdc8-713b-5994-950c-3083986df9cc"))
		        .with("male_condom_qty", numericSum("ev.event_id", "91b55617-f89c-5fb2-932a-b859e9a51539"))
		        .with("female_condom_qty", numericSum("ev.event_id", "921110b1-2a8e-5b0c-a9b4-40b3737c4952"));
		for (String[] m : METHODS) {
			fp.with(m[0], hasAnswer("ev.event_id", METHOD, m[1]));
		}
		for (String[] r : REFERRAL_SOURCES) {
			fp.with("from_" + r[0], hasAnswer("ev.event_id", REFERRED_FROM, r[1]));
		}
		
		fp.patientsBySex("fp_clients_counselled", "x.counselled = 1");
		fp.patientsBySex("fp_new_acceptors", "x.new_acceptor = 1");
		for (Object[] band : AGE_BANDS) {
			fp.patients("females_modern_contraception_" + band[0], "x.any_method = 1 AND x.sex = 'F' AND x.age_y BETWEEN "
			        + band[1] + " AND " + band[2]);
		}
		fp.patients("females_modern_contraception_total", "x.any_method = 1 AND x.sex = 'F'");
		fp.patients("fp_clients_given_oral_pills", "x.ocp = 1");
		fp.sum("fp_oral_pill_cycles_dispensed", "x.pill_cycles", "1 = 1");
		fp.events("fp_emergency_pills_dispensed", "x.ecp = 1");
		fp.events("fp_injectable_noristerat", "x.noristerat = 1");
		fp.events("fp_injectable_dmpa_im", "x.dmpa_im = 1");
		fp.patients("fp_women_self_inject_dmpa_sc", "x.dmpa_sc = 1");
		fp.events("fp_iud_inserted_cut380a", "x.iud_cut = 1");
		fp.events("fp_iud_inserted_lng_ius", "x.lng_ius = 1");
		fp.events("fp_implant_inserted_implanon_nxt", "x.implanon = 1");
		fp.events("fp_implant_inserted_jadelle", "x.jadelle = 1");
		fp.patients("fp_voluntary_sterilization_male", "x.sterilization = 1 AND x.sex = 'M'");
		fp.patients("fp_voluntary_sterilization_female", "x.sterilization = 1 AND x.sex = 'F'");
		fp.patients("fp_clients_received_condoms", "(x.male_condoms = 1 OR x.female_condoms = 1)");
		fp.sum("fp_male_condoms_distributed", "x.male_condom_qty", "1 = 1");
		fp.sum("fp_female_condoms_distributed", "x.female_condom_qty", "1 = 1");
		fp.patients("fp_referred_in_total", "x.referred_in = 1");
		for (String[] r : REFERRAL_SOURCES) {
			fp.patients("fp_referred_in_from_" + r[0], "x.from_" + r[0] + " = 1");
		}
		fp.patients("ppfp_women_counselled", "x.postpartum = 1 AND x.counselled = 1 AND x.sex = 'F'");
		fp.events("ppfp_implanon_nxt_inserted", "x.postpartum = 1 AND x.implanon = 1");
		fp.events("ppfp_jadelle_inserted", "x.postpartum = 1 AND x.jadelle = 1");
		fp.events("ppfp_iud_inserted", "x.postpartum = 1 AND (x.iud_cut = 1 OR x.lng_ius = 1)");
		
		return NhmisSql.dataSet("NHMIS - Family Planning",
		    "Form rows 108-125: FP counselling, new acceptors, modern contraception users, methods and "
		            + "commodities given, referrals in for FP and postpartum FP", fp);
	}
}
