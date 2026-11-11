/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.medihealth.order;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Visit;

public class RetrospectiveDrugOrderSaveHandlerTest {
	
	private final RetrospectiveDrugOrderSaveHandler handler = new RetrospectiveDrugOrderSaveHandler();
	
	private DrugOrder orderIn(Date encounterDate, Date visitStop) {
		Visit visit = new Visit();
		visit.setStartDatetime(encounterDate);
		visit.setStopDatetime(visitStop);
		Encounter encounter = new Encounter();
		encounter.setEncounterDatetime(encounterDate);
		encounter.setVisit(visit);
		DrugOrder order = new DrugOrder();
		order.setEncounter(encounter);
		return order;
	}
	
	@Test
	public void handle_shouldStartOrderOnEncounterDateForAVisitDaysAgo() {
		Date fourDaysAgo = DateUtils.addDays(new Date(), -4);
		DrugOrder order = orderIn(fourDaysAgo, fourDaysAgo);
		handler.handle(order, null, null, null);
		assertEquals(fourDaysAgo, order.getDateActivated());
	}
	
	@Test
	public void handle_shouldStartOrderOnEncounterDateForAnEndedVisitEarlierToday() {
		Date twoHoursAgo = DateUtils.addHours(new Date(), -2);
		DrugOrder order = orderIn(twoHoursAgo, DateUtils.addMinutes(twoHoursAgo, 30));
		handler.handle(order, null, null, null);
		assertEquals(twoHoursAgo, order.getDateActivated());
	}
	
	@Test
	public void handle_shouldLeaveOrderInAnOngoingVisitForCoreToStartNow() {
		DrugOrder order = orderIn(DateUtils.addHours(new Date(), -1), null);
		handler.handle(order, null, null, null);
		assertNull(order.getDateActivated());
	}
	
	@Test
	public void handle_shouldKeepAnExplicitStartDate() {
		Date chosen = DateUtils.addDays(new Date(), -1);
		DrugOrder order = orderIn(DateUtils.addDays(new Date(), -4), DateUtils.addDays(new Date(), -4));
		order.setDateActivated(chosen);
		handler.handle(order, null, null, null);
		assertEquals(chosen, order.getDateActivated());
	}
	
	@Test
	public void handle_shouldIgnoreOrdersWithoutAnEncounter() {
		DrugOrder order = new DrugOrder();
		handler.handle(order, null, null, null);
		assertNull(order.getDateActivated());
	}
}
