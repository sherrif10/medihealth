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
	
	public static final List<String> VAGINAL_DISCHARGE = Arrays.asList("8ae0045e-db54-53b5-83f0-fd9703810d73",
	    "165162AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "2a18f197-07aa-5227-bc2b-444e8286043d");
	
	/**
	 * No genital ulcer concept in the MediHealth dictionary yet: the row stays at 0 until one is
	 * added.
	 */
	public static final List<String> GENITAL_ULCER = Collections.emptyList();
	
	/**
	 * No urethral discharge concept in the MediHealth dictionary yet: the row stays at 0 until one
	 * is added.
	 */
	public static final List<String> URETHRAL_DISCHARGE = Collections.emptyList();
	
	/** Gonorrhoea, primary genital syphilis, sexually transmitted disease (also in pregnancy). */
	public static final List<String> OTHER_STIS = Arrays.asList("117767AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "129119AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "112992AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "163771AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/**
	 * Adverse event following immunization, recorded on the Immunization Record form (as in NHMIS
	 * row 88).
	 */
	public static final String AEFI_QUESTION = "d506ce85-f1c4-4f2d-af8a-9ef7a15ea5b4";
	
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
