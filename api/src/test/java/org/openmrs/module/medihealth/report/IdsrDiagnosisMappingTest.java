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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * IDSR 003 rows for the STI syndromes, AEFI and sickle cell disorder count every catalog variant of
 * the condition (ticket "Add Missing and Standardized IDSR 002/003 Diagnoses"), so past and new
 * records aggregate the same way.
 */
public class IdsrDiagnosisMappingTest {
	
	private static final String MONTHLY_SQL = IdsrForm.MONTHLY_003.cellsSql(false);
	
	/** The SELECTs that produce events for one row key. */
	private static List<String> eventsFor(String rowKey) {
		List<String> events = new ArrayList<String>();
		Matcher m = Pattern.compile("SELECT '" + rowKey + "'.*?(?=\\n  UNION ALL|\\n\\) x)", Pattern.DOTALL).matcher(
		    MONTHLY_SQL);
		while (m.find()) {
			events.add(m.group());
		}
		return events;
	}
	
	private static boolean countsConcept(String rowKey, String conceptUuid) {
		for (String event : eventsFor(rowKey)) {
			if (event.contains("'" + conceptUuid + "'")) {
				return true;
			}
		}
		return false;
	}
	
	@Test
	public void vaginalDischarge_shouldCountPlainVaginalDischargeAndItsVariants() {
		assertTrue(countsConcept("sti_vaginal_discharge", "123396AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // Vaginal discharge
		assertTrue(countsConcept("sti_vaginal_discharge", "8ae0045e-db54-53b5-83f0-fd9703810d73")); // Excessive
		assertTrue(countsConcept("sti_vaginal_discharge", "165162AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // Foul smelling
	}
	
	@Test
	public void urethralDischarge_shouldCountUrethralDischargeAndUrethritisIncludingRetiredCopies() {
		assertFalse("row was wired to nothing", eventsFor("sti_urethral_discharge").isEmpty());
		assertTrue(countsConcept("sti_urethral_discharge", "123529AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // Urethrorrhoea
		assertTrue(countsConcept("sti_urethral_discharge", "9c510a83-c0bd-4b1d-9d90-08ed059e5e73")); // URETHRITIS
		assertTrue(countsConcept("sti_urethral_discharge", "25cb409e-d100-4d0b-a23f-7dd2fc199b5f")); // retired copy
	}
	
	@Test
	public void genitalUlcer_shouldCountTheNewTermAndChancroidChancreAndLgv() {
		assertFalse("row was wired to nothing", eventsFor("sti_genital_ulcer").isEmpty());
		assertTrue(countsConcept("sti_genital_ulcer", IdsrConcepts.GENITAL_ULCER_CONCEPT));
		assertTrue(countsConcept("sti_genital_ulcer", "145762AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // Chancroid
		assertTrue(countsConcept("sti_genital_ulcer", "113656AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // Chancre
		assertTrue(countsConcept("sti_genital_ulcer", "135462AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // LGV
	}
	
	@Test
	public void aefi_shouldCountTheImmunizationRecordAnswerAndAefiDiagnoses() {
		List<String> events = eventsFor("aefi");
		boolean fromForm = false, fromDiagnosis = false;
		for (String event : events) {
			fromForm |= event.contains("'" + IdsrConcepts.AEFI_QUESTION + "'");
			fromDiagnosis |= event.contains("encounter_diagnosis") && event.contains("'" + IdsrConcepts.AEFI_CONCEPT + "'");
		}
		assertTrue("Immunization Record AEFI answer", fromForm);
		assertTrue("AEFI diagnosis", fromDiagnosis);
		assertTrue(countsConcept("aefi", "27ee895a-8283-403d-8ae4-4462bd1212ce")); // minor ESAVI
		assertTrue(countsConcept("aefi", "32256e9f-cbdd-45c7-bbba-a748cb7db943")); // major ESAVI
	}
	
	@Test
	public void sickleCell_shouldCountHbssAndHbscButNotTheTrait() {
		assertTrue(countsConcept("sickle_cell_new", "117703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // sickle cell disease
		assertTrue(countsConcept("sickle_cell_new", "a8924189-57c1-47df-860a-3cf7bc42e91b")); // HbSC
		assertFalse(countsConcept("sickle_cell_new", "126513AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // trait
	}
	
	@Test
	public void eachChildCountsOncePerRowMeasureAndAgeBand() {
		assertTrue(MONTHLY_SQL.contains("COUNT(DISTINCT x.patient_id) AS patients"));
		assertTrue(MONTHLY_SQL.contains("GROUP BY x.row_key, x.measure, age_band"));
	}
	
	@Test
	public void newConceptUuidsShouldBeDistinctAndWellFormed() {
		String uuid = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
		assertTrue(IdsrConcepts.GENITAL_ULCER_CONCEPT.matches(uuid));
		assertTrue(IdsrConcepts.AEFI_CONCEPT.matches(uuid));
		assertFalse(IdsrConcepts.GENITAL_ULCER_CONCEPT.equals(IdsrConcepts.AEFI_CONCEPT));
		for (String u : IdsrConcepts.URETHRAL_DISCHARGE) {
			assertEquals(36, u.length());
		}
	}
}
