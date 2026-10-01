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

import java.util.Arrays;
import java.util.List;

import org.openmrs.module.medihealth.report.NhmisSql.DrugMatch;

/**
 * Metadata and diagnosis groups read by the NHMIS sections for form rows 94-180.
 * <p>
 * Diagnosis groups list every concept on MediHealth a clinician might pick for the condition: the
 * CIEL concept plus the older uppercase MediHealth concepts (e.g. "PNEUMONIA NOS", "DM"), which are
 * both still in use. "History of", "family history of" and screening concepts are left out on
 * purpose.
 */
public final class NhmisConcepts {
	
	private NhmisConcepts() {
	}
	
	// ---- encounter types --------------------------------------------------------------------
	
	public static final String ANC_ENCOUNTER_TYPE = "85564b0e-9ab2-4f27-b4d3-63e227ca6127";
	
	public static final String GROWTH_MONITORING = "82c37475-3540-5a9e-8f2e-2922d3d27a71";
	
	public static final String SAM_TREATMENT = "396ff461-24a6-5cd2-96f7-3b1b0fbe16db";
	
	public static final String FAMILY_PLANNING = "579f1444-0b52-5a40-86de-c38e8df19d32";
	
	public static final String REFERRAL_OUT = "9f18a7ae-eeb4-50fb-b89c-2312ab0e879b";
	
	public static final String TB_SCREENING = "25890217-9834-5601-acf1-1ff9327a8c05";
	
	public static final String GBV_CARE = "9643e102-18a3-5dc9-aa02-f5a3e06bbdaf";
	
	public static final String FISTULA_CARE = "e63641c8-b4c4-5180-9939-e0f1b3e7204b";
	
	public static final String ADR_REPORT = "7096043b-5f75-5b65-8988-2709a0ca32a9";
	
	// ---- referral out reasons -------------------------------------------------------------------
	
	public static final String REFERRAL_REASON = "3fa765b2-86cf-5350-9553-bda5a71c3d41";
	
	public static final String REFERRAL_MALARIA_TREATMENT = "0cdbab33-9af4-52d6-a332-cb17706aa84d";
	
	public static final String REFERRAL_MALARIA_ADR = "363e141f-d3f7-5a06-83f7-3a493fcf9264";
	
	public static final String REFERRAL_PREGNANCY_COMPLICATION = "5dfbe8ee-47bb-5a36-a5ed-56190fb79ac7";
	
	public static final String REFERRAL_FISTULA = "995f1251-9c0b-5237-a805-470a5f200197";
	
	public static final String REFERRAL_HEPATITIS_B = "72745711-aff6-5f8c-8346-a3d287087c39";
	
	public static final String REFERRAL_HEPATITIS_C = "84d13c74-d19d-5189-a163-a71b9c403b69";
	
	public static final String REFERRAL_GBV = "cc2a287b-84ab-5a0f-aa73-a925329031b3";
	
	/** "Referred" flags on the other forms, which are also out-going referrals (row 126). */
	public static final String TB_REFERRED = "13d4c26a-c050-57c3-a560-fb185f11fe08";
	
	public static final String GBV_REFERRED = "6aa1f576-165f-5fb5-819c-ad5b3882e4e4";
	
	public static final String ANC_HEPB_REFERRED = "142518fd-5d32-44bc-b789-fcb430cd0d64";
	
	public static final String ANC_HEPC_REFERRED = "9cd39926-22be-471c-bd39-f3956185fadf";
	
	// ---- diagnosis groups ---------------------------------------------------------------------
	
	public static final List<String> DIARRHOEA = Arrays.asList("149856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "142407AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "163713AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "145443AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "1467AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "163465AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "139753AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "138868AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "5a592dc5-7a76-5b03-834b-58ef8a29e004",
	    "0f56fa0a-8542-5622-bf29-e83e229001d9", "80c61f95-138a-5731-9fe3-ff44de573449",
	    "4337b3a4-3765-5768-84c2-f88f8ec37169", "9fcdb15e-e537-589c-ac77-ad7d94e8bbe4",
	    "a21aaed7-d5b0-5e3d-b024-525d07485117", "77cc5a0e-f976-5f80-a904-567801a5e25d",
	    "60369fe1-2ab3-5aa4-b8e9-053f841df1ef", "1d0636d0-a819-5a8b-94dd-94e63b6a55ea",
	    "73d48aea-7f80-52dd-803c-3c0fe9f831e5", "c36c3c75-9fcc-5a24-83f4-841ac2919584",
	    "31879b25-a2c8-53c4-9a73-8c4c0a90cbf2", "f104411b-31eb-5a5c-9a97-e4c130abbf07",
	    "4f11bafc-7bb5-5b63-a91b-ac2039facdb6", "5bc5977d-939a-5176-a212-866979daa839",
	    "7e2290bb-0050-5b6e-9b7e-4b8aac48ae7a", "142412AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "148023AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "6251a8cf-ffe4-4621-a0b7-68b88237d400",
	    "3047e126-3601-4321-9eb4-e6aba6573200", "4082b431-fdb0-41e7-9ca1-5a65d8579afe",
	    "117889AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "149779AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "123114AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "148036AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> PNEUMONIA = Arrays.asList("114100AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "121252AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "1215AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "123098AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "143772AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "1463AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "833b2fbe-aa56-4bac-81b8-c2f60ada1ba0",
	    "3a8a3f08-3d16-4473-a23a-8c933f06389f", "3472c32d-b416-428b-a39a-160ef42233b9");
	
	public static final List<String> MEASLES = Arrays.asList("134561AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "115886AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "152209AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "aa380edb-14f4-42a8-9026-479c6763b5c0");
	
	public static final List<String> DIABETES = Arrays.asList("119481AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "141730AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "105903f4-7b6d-496a-b613-37ab9d0f5450",
	    "119476AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "119457AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "142474AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "142473AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "137941AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "119441AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "6accecb1-626b-4ffb-8cdd-75f6fa68b3ce");
	
	public static final List<String> GESTATIONAL_DIABETES = Arrays.asList("1449AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/** Pregnancy-induced hypertension and pulmonary/portal hypertension are not counted here. */
	public static final List<String> HYPERTENSION = Arrays.asList("117399AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "140987AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "117401AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "117386AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "113087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "113875AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "138185AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "165587AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "161644AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "4dd3da8e-2b90-4a52-9df5-4dcdaa98161b");
	
	public static final List<String> ARTHRITIS = Arrays.asList("157409AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "158793AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "152339AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "117769AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "127417AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "136343AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "128007AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "131651AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "114698AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "114702AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "9498fc71-7b26-497e-8b4f-ea9c9aa4ed6e",
	    "06c2f16b-6d06-427f-b61d-aef0d00faf93");
	
	/** Sickle cell disease only, not sickle cell trait. */
	public static final List<String> SICKLE_CELL = Arrays.asList("117703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> ASTHMA = Arrays.asList("121375AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "140849AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "0016512d-4388-44f0-a4b6-f6ad9e18fdcd", "121372AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "839a4aa1-abdf-4ef8-bdd0-5427e027d8ed", "2abc496a-172d-4199-b4f4-d3d2a39abd94");
	
	public static final List<String> DEPRESSION = Arrays.asList("119537AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "142563AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "158802AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "158801AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "141732AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "127799AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "127798AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "129325AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "157810AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "157791AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "134147AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "134079AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "134016AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "ed8a8401-0d65-4c90-b104-5314c635459d", "166672AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "126616AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "126597AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "126594AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "126443AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "166215AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "135380AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "4e998362-cac6-44d6-96ed-067194e5d479", "118779AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> BREAST_CANCER = Arrays.asList("113753AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> CERVICAL_CANCER = Arrays.asList("159008AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "116023AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "146299AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> SEVERE_MALARIA = Arrays.asList("160155AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "145851AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "152295AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> CONFIRMED_MALARIA = Arrays.asList("160148AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/** Every malaria diagnosis, severe ones included. */
	public static final List<String> MALARIA = Arrays.asList("116128AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "131096AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "160155AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "160148AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "127971AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "126490AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "143850AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "123008AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "145851AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "134041AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "131377AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "118353AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "152295AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "134594AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "134592AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "135361AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "116125AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "117627AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "135360AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/**
	 * Fever as a diagnosis or an OPD complaint (the complaint list is recorded as Diagnosis
	 * concepts).
	 */
	public static final List<String> FEVER = Arrays.asList("140238AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "a50f95e7-93ce-58d3-8194-93cbd65288fe", "52f91f3c-f9ea-50f1-8d41-a9e8a3f725df",
	    "cd5f7e91-492a-5f59-a6d6-f12d5c01f345", "382bfd06-7cc2-54e2-8b89-7e1431ac2bf0",
	    "a2880581-3ede-5747-aa94-28a6cac045b8", "594ac379-1bea-5ad0-89db-cd5ba8f48edc",
	    "9e0a24bc-50a2-56ea-bbe2-8011e80bc54d", "0c641876-f9bd-51fc-af15-e7e8d25d3fc0",
	    "18a788e5-3ffb-5590-b2bd-2261ec0692e3", "3141a24c-c102-55f0-88d0-2fc7589e6b9d",
	    "f6024c9f-6ec7-56db-ab90-2f40fc39368b", "9f7fdfac-6b3b-5f27-b622-1c630caf12f7",
	    "84021767-9a90-577a-810f-0c5d1b3eaf8e", "174a789b-cb29-5e7b-9acc-9f74b52efa11",
	    "891b00bd-96bc-5333-9d01-e5fe549e1d0b", "5a592dc5-7a76-5b03-834b-58ef8a29e004",
	    "cb7000d6-5d3d-5bdd-ab42-1ab7a00bc5ac", "2e77d926-f9f3-5cd0-8e49-525b7be2c485",
	    "9d1bcdef-a53b-5443-9a04-a79e01100426", "33d94c96-c761-53bb-816b-e39e209a6ec3",
	    "a5776469-b5c0-5f9b-b759-54134bc2ca60", "7aa95d38-3abd-5405-bcb3-1fdc8db99a47",
	    "26e0c28f-25fd-5faf-85e3-df9cd58f0c30", "7c002188-f89c-5bc3-8807-e9bcbc3542a7",
	    "d48bcb07-ae2d-5f5e-9b62-1b63200c6558", "fb2d36cf-40cc-550c-bc9a-0d71eed680e7",
	    "cd8c3d59-fef5-5ae0-98ff-a2181d7fba10", "c36c3c75-9fcc-5a24-83f4-841ac2919584",
	    "d8f84968-a9d9-5744-b7f9-0133ea3b7382", "47659c60-4126-5da2-a7de-023456509f4e",
	    "76b4c130-3f37-5448-9451-ebd6376f247c", "5fbd7001-ec6d-5c05-bd06-bd071a09b205",
	    "60123a0b-3524-5916-8dc3-179c4a07f977", "127990AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "b40ec717-538a-5e9a-a540-950817f72232", "1494AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "1892AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "361bee1d-778c-52f4-92f2-6693e1ae6761",
	    "a7f1ee19-d00e-41df-a736-fd5fbb4b456f", "116125AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> HEPATITIS_B = Arrays.asList("40ceef99-a0a9-4e2a-ba05-a1cbe36565ce",
	    "138821AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "163325AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> HEPATITIS_C = Arrays.asList("7107e329-f5c7-454f-8cf0-6325ae739269",
	    "149743AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "145347AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "138820AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "28AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> GBV = Arrays.asList("c0a2c4b6-51a1-5f79-be12-ca20d2c94149",
	    "123160AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "fdc1bba4-3539-4d64-97af-9bf462c2a752",
	    "152370AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "114155AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "126582AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> SNAKE_BITE = Arrays.asList("62f53e46-27ca-58e7-a238-64874454fe50");
	
	public static final List<String> ELEPHANTIASIS = Arrays.asList("d60ebaf2-ef14-55fd-87df-474348b955e4",
	    "119354AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "161565AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> YAWS = Arrays.asList("02665efc-f7eb-5c62-ba4a-f78bb7e1c9aa");
	
	// ---- lab tests ----------------------------------------------------------------------------
	
	public static final List<String> MALARIA_RDT = Arrays.asList("1643AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	public static final List<String> MALARIA_RDT_POSITIVE = Arrays.asList("703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "161246AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "161247AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "161248AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	
	/** Malarial smear, MP test, and the species/impression results of the smear panel. */
	public static final List<String> MALARIA_MICROSCOPY = Arrays.asList("32AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "b4192622-e5e8-4e8e-a455-97fbe1075064", "037971b5-4272-43f1-8d48-50677d8f00b6",
	    "09b4f7a6-d6b7-4d7e-a09d-47e2be77c150");
	
	/** Positive answers to the smear/MP test, and any species seen on the smear. */
	public static final List<String> MALARIA_MICROSCOPY_POSITIVE = Arrays.asList("703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "9982fc9a-575b-4af7-9407-1c12653f357b", "161246AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "161247AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "2107130a-df39-423d-802e-8f73b3a36d67",
	    "3cc7c256-77e6-4fdb-8975-fc942290882e");
	
	public static final List<String> HEPATITIS_B_TESTS = Arrays.asList("159430AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "165301AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "161472AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "a322ab77-3559-45fd-af9d-b1dbb94039e6");
	
	public static final List<String> HEPATITIS_C_TESTS = Arrays.asList("1325AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "165302AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "161471AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
	    "61c3529a-cfc7-44b9-9dce-d8e2d2292576");
	
	public static final String TEMPERATURE = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	// ---- drugs ----------------------------------------------------------------------------------
	
	public static final String ACT_NAMES = "lumefantrin|coartem|dihydroartemisinin|piperaquin|pyronaridin|lonart|amatem"
	        + "|artesunate.{0,40}(amodiaquin|mefloquin)|(amodiaquin|mefloquin).{0,40}artesunate";
	
	public static final DrugMatch ACT = DrugMatch.named(ACT_NAMES);
	
	public static final DrugMatch ARTESUNATE_INJECTION = DrugMatch.named("artesunate", NhmisSql.INJECTABLE)
	        .except(ACT_NAMES);
	
	public static final DrugMatch RECTAL_ARTESUNATE = DrugMatch.named("artesunate", NhmisSql.RECTAL);
	
	public static final DrugMatch OTHER_INJECTABLE_ANTIMALARIAL = DrugMatch.named("artemether|arteether|quinine",
	    NhmisSql.INJECTABLE).except("lumefantrin|artesunate");
	
	public static final DrugMatch OTHER_ORAL_ANTIMALARIAL = DrugMatch
	        .named(
	            "chloroquin|quinine|sulfadoxin|fansidar|amodiaquin|mefloquin|primaquin|proguanil|halofantrin|artemether|arteether|artesunate")
	        .except(ACT_NAMES + "|" + NhmisSql.INJECTABLE + "|" + NhmisSql.RECTAL);
	
	public static final DrugMatch ORS = DrugMatch.named("(^|[^a-z])ors([^a-z]|$)|rehydrat");
	
	public static final DrugMatch ZINC = DrugMatch.named("zinc").except("oxide|bacitracin|neomycin|nystatin|cream|ointment");
	
	public static final DrugMatch AMOXICILLIN = DrugMatch.named("amox");
	
	public static final DrugMatch VITAMIN_A = DrugMatch.named("vitamin a([^a-z]|$)|retinol");
	
	public static final DrugMatch DEWORMING = DrugMatch.named("albendazol|mebendazol|benaworm|zentel|vermox");
	
	public static final DrugMatch MICRONUTRIENT_POWDER = DrugMatch.named("micronutrient powder|(^|[^a-z])mnp([^a-z]|$)");
	
	public static final DrugMatch HEPATITIS_B_TREATMENT = DrugMatch.named("tenofovir|entecavir");
	
	public static final DrugMatch HEPATITIS_C_TREATMENT = DrugMatch.named("sofosbuvir|daclatasvir|velpatasvir|ledipasvir");
	
	public static final DrugMatch ANTIVENOM = DrugMatch.named("venom|venin");
	
	/** "Vitamin A (immunization schedule)", recorded as a vaccine on the Immunization Record form. */
	public static final String VITAMIN_A_IMMUNIZATION = "37fc0998-dbb6-468b-aab4-fe06973a70be";
}
