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

import java.util.regex.Pattern;

import org.junit.Test;
import org.openmrs.module.medihealth.report.MrdtActWeekly.ActCategory;

public class MrdtActWeeklyTest {
	
	/**
	 * The ACT pattern as MySQL applies it: to the lower-cased drug name, concept names and dosage
	 * form.
	 */
	private static boolean isAct(String drugText) {
		return Pattern.compile(NhmisConcepts.ACT_NAMES).matcher(drugText.toLowerCase()).find();
	}
	
	@Test
	public void actNames_shouldMatchTheActDrugsOnTheMediHealthFormulary() {
		assertTrue(isAct("ACT 20/120 (X6)"));
		assertTrue(isAct("ACT 20/120 (X12)"));
		assertTrue(isAct("ACT 20/120 (X18)"));
		assertTrue(isAct("ACT TAB 80/480"));
		assertTrue(isAct("ACT FREE"));
		assertTrue(isAct("ACT"));
	}
	
	@Test
	public void actNames_shouldStillMatchActsByIngredientOrBrand() {
		assertTrue(isAct("Artemether / Lumefantrine 20/120mg tablet"));
		assertTrue(isAct("Coartem dispersible"));
		assertTrue(isAct("Dihydroartemisinin-piperaquine"));
		assertTrue(isAct("Artesunate + Amodiaquine"));
	}
	
	@Test
	public void actNames_shouldNotMatchDrugsThatOnlyContainTheLettersAct() {
		assertFalse(isAct("ACTIVATED CHARCOAL TAB X 10"));
		assertFalse(isAct("Actrapid insulin"));
		assertFalse(isAct("Lactulose syrup"));
		assertFalse(isAct("Artesunate injection 60mg"));
		assertFalse(isAct("Paracetamol 500mg tablet"));
	}
	
	@Test
	public void actCategory_shouldPlaceAgesInTheFourReportingBands() {
		assertEquals(ActCategory.ACT1, ActCategory.of(0));
		assertEquals(ActCategory.ACT1, ActCategory.of(55));
		assertEquals(ActCategory.ACT1, ActCategory.of(59));
		assertEquals(ActCategory.ACT2, ActCategory.of(60));
		assertEquals(ActCategory.ACT2, ActCategory.of(119));
		assertEquals(ActCategory.ACT3, ActCategory.of(120));
		assertEquals(ActCategory.ACT3, ActCategory.of(179));
		assertEquals(ActCategory.ACT4, ActCategory.of(180));
		assertEquals(ActCategory.ACT4, ActCategory.of(900));
		assertEquals(ActCategory.AGE_UNKNOWN, ActCategory.of(null));
	}
	
	@Test
	public void actCategory_sqlConditionsShouldUseTheSameBoundaries() {
		assertEquals("a.age_m < 60", ActCategory.ACT1.condition);
		assertEquals("a.age_m >= 60 AND a.age_y < 10", ActCategory.ACT2.condition);
		assertEquals("a.age_y >= 10 AND a.age_y < 15", ActCategory.ACT3.condition);
		assertEquals("a.age_y >= 15", ActCategory.ACT4.condition);
	}
	
	@Test
	public void sql_shouldListTheReportRowsInOrder() {
		String sql = MrdtActWeekly.sql(true);
		String[] rows = { MrdtActWeekly.MRDT_TESTED, MrdtActWeekly.MRDT_POSITIVE, MrdtActWeekly.MRDT_NEGATIVE,
		        ActCategory.ACT1.label, ActCategory.ACT2.label, ActCategory.ACT3.label, ActCategory.ACT4.label,
		        ActCategory.AGE_UNKNOWN.label, MrdtActWeekly.ACT_TOTAL };
		int last = -1;
		for (String row : rows) {
			int at = sql.indexOf("'" + row + "'");
			assertTrue(row + " missing", at >= 0);
			assertTrue(row + " out of order", at > last);
			last = at;
		}
		assertTrue(sql.endsWith("ORDER BY sort_order"));
	}
	
	@Test
	public void sql_shouldCountMrdtOncePerPatientPerDayAndSkipNotPerformed() {
		String mrdt = MrdtActWeekly.mrdtPatientDays(true);
		assertTrue(mrdt.contains("GROUP BY o.person_id, DATE(o.obs_datetime)"));
		assertTrue(mrdt.contains("MAX(o.value_coded IN"));
		assertTrue(mrdt.contains("NOT IN"));
		assertTrue(mrdt.contains("o.voided = 0"));
	}
	
	@Test
	public void sql_shouldCountActOncePerPatientAtTheAgeOfTheirFirstActInThePeriod() {
		String act = MrdtActWeekly.actPatients(true);
		assertTrue(act.contains("MIN(o.date_activated) AS first_act"));
		assertTrue(act.contains("GROUP BY o.patient_id"));
		assertTrue(act.contains("TIMESTAMPDIFF(MONTH, p.birthdate, f.first_act)"));
		assertTrue(act.contains("o.order_action <> 'DISCONTINUE'"));
		assertTrue(act.contains("o.voided = 0"));
	}
	
	@Test
	public void sql_shouldFilterByTheChosenFacilityOrByEveryPhc() {
		assertTrue(MrdtActWeekly.sql(true).contains(":location"));
		String allPhcs = MrdtActWeekly.sql(false);
		assertFalse(allPhcs.contains(":location"));
		assertTrue(allPhcs.contains("LIKE '%PHC%'"));
		assertTrue(allPhcs.contains(":startDate"));
		assertTrue(allPhcs.contains(":endDate"));
	}
}
