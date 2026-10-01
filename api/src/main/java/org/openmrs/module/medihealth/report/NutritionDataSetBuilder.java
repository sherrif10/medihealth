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

import static org.openmrs.module.medihealth.report.NhmisConcepts.GROWTH_MONITORING;
import static org.openmrs.module.medihealth.report.NhmisConcepts.SAM_TREATMENT;
import static org.openmrs.module.medihealth.report.NhmisSql.drugOrders;
import static org.openmrs.module.medihealth.report.NhmisSql.encounters;
import static org.openmrs.module.medihealth.report.NhmisSql.hasAnswer;
import static org.openmrs.module.medihealth.report.NhmisSql.hasTrue;
import static org.openmrs.module.medihealth.report.NhmisSql.obsAnswers;
import static org.openmrs.module.medihealth.report.NhmisSql.obsTrue;
import static org.openmrs.module.medihealth.report.NhmisSql.union;

import java.util.Collections;

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.springframework.stereotype.Component;

/**
 * Builds the "Nutrition" and "Severe Acute Malnutrition" sections of the NHMIS Monthly Summary Form
 * (form rows 94-102) from the "Child Growth Monitoring" and "Severe Acute Malnutrition Treatment"
 * forms. Vitamin A, MNP and deworming (rows 98-100) also count drug orders for those medicines, and
 * Vitamin A also counts doses recorded on the Immunization Record form, so a child is counted
 * whichever way the dose was recorded (once per child).
 */
@Component("medihealth.NutritionDataSetBuilder")
public class NutritionDataSetBuilder {
	
	private static final String VISIT_TYPE = "ae15aaf6-0860-5230-9b70-75d152dbc406";
	
	private static final String NEW_CLIENT = "0f8d1a7f-3dac-5498-a7b3-baf05fac2fba";
	
	private static final String REVISIT = "0bc5a143-bbb2-5e23-a9ba-d4b03cb699ec";
	
	private static final String GROWING_WELL = "3fd1564c-5f11-5a0d-a469-4a6b2bff60b0";
	
	private static final String EXCLUSIVELY_BREASTFED = "6654c15e-e610-587d-b08e-699ddd95e245";
	
	private static final String IYCN_COUNSELLED = "b4fa96d2-9c25-55c3-8395-52fcc6d2f55e";
	
	private static final String VITAMIN_A_GIVEN = "2750afaa-9067-5fbe-bbde-2df3bc95d74f";
	
	private static final String MNP_GIVEN = "5cc4d110-1421-5dd5-8a21-edfaf5c727a7";
	
	private static final String DEWORMING_GIVEN = "6860763e-9587-5676-88e6-a141884e1b07";
	
	private static final String SAM_ADMISSION = "2d293cce-9061-5792-9503-9cac29eb6fdf";
	
	private static final String SAM_OUTCOME = "5cf73400-e7c3-5bf1-a5cb-eba75df85f29";
	
	/** Row 94 age bands, in months. */
	private static final Object[][] GMP_BANDS = { { "0_5m", 0, 5 }, { "6_23m", 6, 23 }, { "24_59m", 24, 59 } };
	
	public SqlDataSetDefinition build() {
		NhmisBlock gmp = new NhmisBlock(encounters(GROWTH_MONITORING))
		        .with("is_new", hasAnswer("ev.event_id", VISIT_TYPE, NEW_CLIENT))
		        .with("is_revisit", hasAnswer("ev.event_id", VISIT_TYPE, REVISIT))
		        .with("growing_well", hasTrue("ev.event_id", GROWING_WELL))
		        .with("ebf", hasTrue("ev.event_id", EXCLUSIVELY_BREASTFED))
		        .with("iycn", hasTrue("ev.event_id", IYCN_COUNSELLED));
		for (String visit : new String[] { "new", "revisit" }) {
			String flag = visit.equals("new") ? "x.is_new = 1" : "x.is_revisit = 1";
			for (String[] sex : NhmisBlock.SEXES) {
				for (Object[] band : GMP_BANDS) {
					gmp.patients("gmp_" + visit + "_" + sex[1] + "_" + band[0], flag + " AND x.sex = '" + sex[0]
					        + "' AND x.age_m BETWEEN " + band[1] + " AND " + band[2]);
				}
			}
			gmp.patients("gmp_" + visit + "_total", flag + " AND x.age_m < 60");
		}
		gmp.patientsBySex("children_growing_well", "x.growing_well = 1 AND x.age_m < 60")
		        .patientsBySex("children_0_6m_exclusive_breastfeeding", "x.ebf = 1 AND x.age_m < 6")
		        .patientsBySex("clients_counselled_iycn", "x.iycn = 1");
		
		NhmisBlock vitaminA = new NhmisBlock(union(obsTrue(VITAMIN_A_GIVEN), drugOrders(NhmisConcepts.VITAMIN_A),
		    obsAnswers(Collections.singletonList(NhmisConcepts.VITAMIN_A_IMMUNIZATION)))).patientsBySex("vitamin_a_6_11m",
		    "x.age_m BETWEEN 6 AND 11").patientsBySex("vitamin_a_12_59m", "x.age_m BETWEEN 12 AND 59");
		NhmisBlock mnp = new NhmisBlock(union(obsTrue(MNP_GIVEN), drugOrders(NhmisConcepts.MICRONUTRIENT_POWDER)))
		        .patientsBySex("mnp_6_23m", "x.age_m BETWEEN 6 AND 23");
		NhmisBlock deworming = new NhmisBlock(union(obsTrue(DEWORMING_GIVEN), drugOrders(NhmisConcepts.DEWORMING)))
		        .patientsBySex("deworming_12_59m", "x.age_m BETWEEN 12 AND 59");
		
		NhmisBlock sam = new NhmisBlock(encounters(SAM_TREATMENT));
		String[][] samAnswers = { { "sam_admitted_new", SAM_ADMISSION, "4b9f42a4-dcc0-5fd2-b72a-b34a73f51864" },
		        { "sam_admitted_transferred_in", SAM_ADMISSION, "3afd6dd1-c9ba-5f93-912c-2a46538cc9bb" },
		        { "sam_recovered", SAM_OUTCOME, "e0ff33c7-0e40-521f-bca3-bac4cf0ebdf4" },
		        { "sam_defaulted", SAM_OUTCOME, "5e892dcb-f58a-5044-9f25-38e6cb75fe62" },
		        { "sam_dead", SAM_OUTCOME, "87a5762e-60a7-5953-93fe-c563e7079c66" },
		        { "sam_transferred_out", SAM_OUTCOME, "99834f80-dcef-53ef-bf3c-1099ef79d350" } };
		for (String[] a : samAnswers) {
			sam.with(a[0], hasAnswer("ev.event_id", a[1], a[2]));
		}
		for (String[] a : samAnswers) {
			sam.patientsBySex(a[0], "x." + a[0] + " = 1 AND x.age_m < 60");
		}
		
		return NhmisSql.dataSet("NHMIS - Nutrition and SAM",
		    "Form rows 94-102: growth monitoring and promotion, exclusive breastfeeding, IYCN counselling, "
		            + "Vitamin A, MNP, deworming, and severe acute malnutrition admissions and outcomes", gmp, vitaminA,
		    mnp, deworming, sam);
	}
}
