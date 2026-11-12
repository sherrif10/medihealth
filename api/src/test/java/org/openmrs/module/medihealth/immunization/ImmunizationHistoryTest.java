/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.medihealth.immunization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.module.medihealth.immunization.ImmunizationHistory.HistoryDose;

public class ImmunizationHistoryTest {
	
	private static final String PENTA_2 = "50e945fd-f828-43c4-bf13-e4b555c5b852";
	
	private static final String YELLOW_FEVER = "400ad32a-1d5d-46cf-ae0d-fb683c4f1f6a";
	
	@Test
	public void historyDose_shouldPutASeriesDoseUnderTheCielVaccineWithItsNumber() {
		HistoryDose dose = ImmunizationHistory.historyDose(PENTA_2, null);
		assertEquals("162339AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", dose.vaccineUuid);
		assertEquals(Integer.valueOf(2), dose.doseNumber);
	}
	
	@Test
	public void historyDose_shouldCopyAVaccineWithoutACielMatchAsItself() {
		HistoryDose dose = ImmunizationHistory.historyDose(YELLOW_FEVER, null);
		assertEquals(YELLOW_FEVER, dose.vaccineUuid);
		assertNull(dose.doseNumber);
	}
	
	@Test
	public void historyDose_shouldPutTdUnderTetanusToxoidWithTheTdDoseNumber() {
		HistoryDose dose = ImmunizationHistory.historyDose(ImmunizationHistory.TD_PREGNANT, 3.0);
		assertEquals("84879AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", dose.vaccineUuid);
		assertEquals(Integer.valueOf(3), dose.doseNumber);
		assertNull(ImmunizationHistory.historyDose(ImmunizationHistory.TD_NON_PREGNANT, 9.0).doseNumber);
	}
	
	@Test
	public void historyDose_shouldNotCopyFullyImmunizedStatus() {
		assertNull(ImmunizationHistory.historyDose(ImmunizationHistory.FULLY_IMMUNIZED_UNDER1, null));
		assertNull(ImmunizationHistory.historyDose(null, null));
	}
	
	@Test
	public void mapping_shouldCoverEveryNumberedSeriesDose() {
		// OPV 1-3, Penta 1-3, Rota 1-3, PCV 1-3, Measles 1-2, Hep B birth dose, BCG, IPV, Vitamin A
		assertEquals(18, ImmunizationHistory.mapping().size());
	}
	
	@Test
	public void doses_shouldReadEachVaccineGroupOnceAndSkipOtherObs() {
		Encounter encounter = new Encounter();
		encounter.addObs(vaccineGroup(PENTA_2));
		encounter.addObs(vaccineGroup(PENTA_2));
		encounter.addObs(vaccineGroup(YELLOW_FEVER));
		encounter.addObs(vaccineGroup(ImmunizationHistory.FULLY_IMMUNIZED_UNDER1));
		Obs venue = new Obs();
		venue.setConcept(concept("7a3c1e90-5b2d-4f6e-8a1c-3d4e5f6a7b01"));
		encounter.addObs(venue);
		List<HistoryDose> doses = ImmunizationHistory.doses(encounter);
		// an encounter's obs are a set: the order of the copies does not matter
		Set<String> vaccines = new HashSet<String>();
		for (HistoryDose dose : doses) {
			vaccines.add(dose.vaccineUuid);
		}
		assertEquals(2, doses.size());
		assertEquals(new HashSet<String>(Arrays.asList("162339AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", YELLOW_FEVER)), vaccines);
	}
	
	private static Obs vaccineGroup(String answerUuid) {
		Obs group = new Obs();
		group.setConcept(concept(ImmunizationHistory.VACCINE_ADMINISTERED));
		Obs vaccine = new Obs();
		vaccine.setConcept(concept(ImmunizationHistory.VACCINE_ADMINISTERED));
		vaccine.setValueCoded(concept(answerUuid));
		group.addGroupMember(vaccine);
		return group;
	}
	
	private static Concept concept(String uuid) {
		Concept c = new Concept();
		c.setUuid(uuid);
		return c;
	}
}
