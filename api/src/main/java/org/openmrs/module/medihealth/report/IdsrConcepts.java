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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Diagnosis groups for the IDSR 002 and 003 rows that the NHMIS groups ({@link NhmisConcepts}) do
 * not already cover. CIEL concepts as found in the MediHealth dictionary; "history of", screening
 * and carrier concepts are left out on purpose.
 */
public final class IdsrConcepts {
	
	private IdsrConcepts() {
	}
	
	/**
	 * Acute hepatitis and acute/unspecified viral hepatitis A, B, C, E (not chronic, alcoholic or
	 * toxic).
	 */
	public static final List<String> ACUTE_VIRAL_HEPATITIS = Arrays.asList("149745AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "149743AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "149484AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "121812AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "152665AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "111481AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "111479AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "28AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/** "Diarrhea with dehydration" itself; diarrhoea plus {@link #DEHYDRATION} also counts. */
	public static final List<String> DIARRHOEA_WITH_DEHYDRATION = Arrays.asList("4f11bafc-7bb5-5b63-a91b-ac2039facdb6");
	
	/** Mild and moderate dehydration (not neonatal dehydration). */
	public static final List<String> DEHYDRATION = Arrays.asList("154017AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "154016AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/** Dysentery, bacillary dysentery, shigellosis. */
	public static final List<String> DIARRHOEA_WITH_BLOOD = Arrays.asList("152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "148036AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "126558AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "137327AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> AFRICAN_TRYPANOSOMIASIS = Arrays.asList("149241AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/** Tetanus, not tetanus neonatorum. */
	public static final List<String> NON_NEONATAL_TETANUS = Arrays.asList("124957AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/** HIV disease and HIV-positive, and the HIV-related conditions recorded as diagnoses. */
	public static final List<String> HIV = Arrays.asList("138405AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "138571AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "823AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "160442AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "153701AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "163712AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "160160AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "156671AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "156673AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "5034AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "5350AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "5041AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "5043AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "5354AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "5035AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> HIV_DEATH_CAUSES = Arrays.asList("db0ee483-04cc-4e21-83cf-3c8b56fdff92");
	
	public static final List<String> SCHISTOSOMIASIS = Arrays.asList("127133AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/** Helminthiasis, ascariasis, hookworm. */
	public static final List<String> SOIL_TRANSMITTED_HELMINTHS = Arrays.asList("199AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "148353AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "137504AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "119356AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/** Typhoid and paratyphoid fever. */
	public static final List<String> TYPHOID = Arrays.asList("141AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "130811AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/** Vaginal discharge, and its excessive and foul-smelling variants. */
	public static final List<String> VAGINAL_DISCHARGE = Arrays.asList("123396AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "8ae0045e-db54-53b5-83f0-fd9703810d73", "165162AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "2a18f197-07aa-5227-bc2b-444e8286043d", "b2fa35be-e413-4e4c-8a51-c1a7cb2393b2");
	
	/** "Genital ulcer" (MediHealth concept, see forms/idsr-diagnoses in openmrs-custom-medihealth). */
	public static final String GENITAL_ULCER_CONCEPT = "7b1d3e5f-2a4c-4e6b-8d0f-1a3c5e7b9d01";
	
	/**
	 * Genital ulcer syndrome: genital ulcer, chancroid, chancre, LGV, and the older "Genital sores"
	 * finding.
	 */
	public static final List<String> GENITAL_ULCER = Arrays.asList(GENITAL_ULCER_CONCEPT,
	    "145762AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "113656AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "135462AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "ac633ae8-9960-47fe-b40b-98c1f64a3a34");
	
	/**
	 * Urethral discharge syndrome: urethral discharge (urethrorrhoea), urethritis (including the
	 * retired copies, so past cases still count), and the "Urethral discharge" finding.
	 */
	public static final List<String> URETHRAL_DISCHARGE = Arrays.asList("123529AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "9c510a83-c0bd-4b1d-9d90-08ed059e5e73", "25cb409e-d100-4d0b-a23f-7dd2fc199b5f",
	    "111639AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "ddf70c67-28fa-47df-8d32-cb80a4bdcd4a");
	
	/** Gonorrhoea, primary genital syphilis, sexually transmitted disease (also in pregnancy). */
	public static final List<String> OTHER_STIS = Arrays.asList("117767AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "129119AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "112992AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "163771AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/**
	 * Adverse event following immunization, recorded on the Immunization Record form (as in NHMIS
	 * row 88).
	 */
	public static final String AEFI_QUESTION = "d506ce85-f1c4-4f2d-af8a-9ef7a15ea5b4";
	
	/**
	 * "Adverse event following immunization (AEFI)" (MediHealth concept, see forms/idsr-diagnoses).
	 */
	public static final String AEFI_CONCEPT = "7b1d3e5f-2a4c-4e6b-8d0f-1a3c5e7b9d02";
	
	/**
	 * AEFI diagnosed in a consultation: AEFI, minor and major ESAVI (non-severe / severe adverse
	 * event from vaccination), and "ADR following immunization". Counted together with the
	 * Immunization Record answer; a child counts once.
	 */
	public static final List<String> AEFI_DIAGNOSES = Arrays.asList(AEFI_CONCEPT, "27ee895a-8283-403d-8ae4-4462bd1212ce",
	    "32256e9f-cbdd-45c7-bbba-a748cb7db943", "409ad0d8-6ad7-5f7b-a13e-ca2972b1ecbd");
	
	/**
	 * Sickle cell disorder: sickle cell disease (HbSS, also named "Sickle cell disorder") and HbSC.
	 * Not the trait.
	 */
	public static final List<String> SICKLE_CELL_DISORDER = Arrays.asList("117703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "a8924189-57c1-47df-860a-3cf7bc42e91b");
	
	public static final List<String> ROAD_TRAFFIC_INJURY = Arrays.asList("86AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/** Acute, protein-calorie and graded malnutrition, and kwashiorkor. */
	public static final List<String> MALNUTRITION = Arrays.asList("163303AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "163302AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "162330AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "162331AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "119614AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "126598AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "134722AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "134723AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "116474AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> MALNUTRITION_DEATH_CAUSES = Arrays.asList("165faa42-83b4-4da2-8276-ad91d850c42b");
	
	/**
	 * Mental, neurological and substance-use disorders: epilepsy, schizophrenia, psychoses,
	 * depression.
	 */
	public static final List<String> MNS_DISORDERS;
	
	static {
		List<String> mns = new ArrayList<String>(Arrays.asList("118557AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
		    "114320AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "125943AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
		    "113146AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "131585AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
		    "146915AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "166212AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
		    "166213AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		mns.addAll(NhmisConcepts.DEPRESSION);
		MNS_DISORDERS = Collections.unmodifiableList(mns);
	}
}
