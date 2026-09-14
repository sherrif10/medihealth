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

/**
 * UUIDs of the cause-of-death concepts added for the NHMIS Monthly Summary Form (see
 * {@code configuration/concepts/medihealth/cause_of_death_concepts-medihealth.csv}).
 * <p>
 * TODO these concepts are defined as standalone concepts, not yet wired up as answers on whatever
 * concept is bound to the {@code concept.causeOfDeath} global property in this instance (needed so
 * they show up as options when a clinician records a patient's cause of death). Someone with admin
 * access needs to look up that global property's concept and add these as answers via Manage
 * Concepts, or we add an Initializer "Answers" mapping once confirmed - see conversation notes.
 */
public class CauseOfDeathConcepts {
	
	public static final String SEPSIS = "cb29c976-f787-4aeb-ae37-3addca61b651";
	
	public static final String OBSTRUCTED_LABOUR = "1f8896fa-7515-4cc5-b80c-cca982f6c875";
	
	public static final String ABORTION = "a1b259c0-02c1-4657-a782-6a5b505f2f8f";
	
	public static final String POSTPARTUM_HAEMORRHAGE = "2cf675b2-13b0-4448-90b0-56701a1bcffd";
	
	public static final String ANAEMIA = "5fe02ac3-96aa-460b-9de1-a3e00b3fca53";
	
	public static final String HIV = "db0ee483-04cc-4e21-83cf-3c8b56fdff92";
	
	public static final String MALARIA = "2e1c604d-fbcc-4835-b5b4-8fcab3f23af6";
	
	public static final String OTHER_MATERNAL = "6eaa2801-faf0-4d32-b346-357a4cd2fa4c";
	
	public static final String PREMATURITY = "16307088-6255-4d8e-a763-75ce74d7762a";
	
	public static final String NEONATAL_TETANUS = "b28b6829-e00f-42a5-ad00-2116f7f6f68e";
	
	public static final String CONGENITAL_MALFORMATION = "fb720f1e-380d-44e8-b28e-625df1105d30";
	
	public static final String OTHER_NEONATAL = "3467e9d9-f5df-43d0-b245-0d0dcef95b6a";
	
	public static final String PNEUMONIA = "5ae13819-72fb-4e60-ac19-c199154fd257";
	
	public static final String MALNUTRITION = "165faa42-83b4-4da2-8276-ad91d850c42b";
	
	public static final String OTHER_UNDER5 = "b0ea7fa2-b0e7-425c-84fa-ab1adce0c9ab";
	
	/**
	 * Row 7 answer set: causes of confirmed maternal death. Also drives row 6 (maternal mortality).
	 */
	public static final String[] MATERNAL_CAUSES = { POSTPARTUM_HAEMORRHAGE, SEPSIS, OBSTRUCTED_LABOUR, ABORTION, MALARIA,
	        ANAEMIA, HIV, OTHER_MATERNAL };
	
	/** Row 8 answer set: causes of confirmed neonatal death. */
	public static final String[] NEONATAL_CAUSES = { PREMATURITY, NEONATAL_TETANUS, CONGENITAL_MALFORMATION, OTHER_NEONATAL };
	
	/** Row 9 answer set: causes of confirmed under-5 death. */
	public static final String[] UNDER5_CAUSES = { MALARIA, PNEUMONIA, MALNUTRITION, OTHER_UNDER5 };
	
	public static String quotedInClause(String[] uuids) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < uuids.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append("'").append(uuids[i]).append("'");
		}
		return sb.toString();
	}
	
	private CauseOfDeathConcepts() {
	}
}
