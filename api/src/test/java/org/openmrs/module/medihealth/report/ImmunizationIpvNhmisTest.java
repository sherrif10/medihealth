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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openmrs.module.medihealth.immunization.ImmunizationHistory;

/** NHMIS has one IPV row: IPV 1, IPV 2 (form v4) and the older plain IPV answer all count there. */
public class ImmunizationIpvNhmisTest {
	
	private static final String OLD_IPV = "412201a9-af61-4edf-9bf0-19510819e314";
	
	private final String sql = new ImmunizationDataSetBuilder().build().getSqlQuery();
	
	@Test
	public void formDoses_shouldCountIpv1AndIpv2AsTheIpvRow() {
		assertTrue(sql.contains("CASE WHEN vc.uuid IN ('" + ImmunizationHistory.IPV_1 + "', '" + ImmunizationHistory.IPV_2
		        + "') THEN '" + OLD_IPV + "' ELSE vc.uuid END AS vaccine_uuid"));
	}
	
	@Test
	public void ipvRow_shouldStillCountThePlainIpvAnswer() {
		assertTrue(sql.contains("vaccine_uuid = '" + OLD_IPV + "'"));
	}
	
	@Test
	public void aefi_shouldBeCountedPerEncounterWhetherOrNotAVaccineWasGiven() {
		// AEFI is aggregated over Immunization Record encounters, not over vaccine doses
		assertTrue(sql.contains("COUNT(*) AS aefi_encounters_total"));
		assertTrue(sql.contains("et.name = 'Immunization Record'"));
	}
}
