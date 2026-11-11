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

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.annotation.Handler;
import org.openmrs.api.handler.SaveHandler;

/**
 * Starts a drug prescribed during retrospective data entry on the day the patient was seen, not the
 * day it was keyed in.
 * <p>
 * The O3 order basket does not send a start date, so core would stamp {@code dateActivated} with
 * "now". When the order's encounter belongs to a visit that has already ended, or is dated on an
 * earlier day, the start date is taken from the encounter instead. Core then treats the order as
 * retrospective and counts the duration and auto-expiry date from it. {@code dateCreated} still
 * records when it was entered.
 */
@Handler(supports = DrugOrder.class)
public class RetrospectiveDrugOrderSaveHandler implements SaveHandler<DrugOrder> {
	
	@Override
	public void handle(DrugOrder order, User creator, Date dateCreated, String other) {
		if (order.getOrderId() != null || order.getDateActivated() != null) {
			return;
		}
		Encounter encounter = order.getEncounter();
		if (encounter == null || encounter.getEncounterDatetime() == null) {
			return;
		}
		if (isRetrospective(encounter, new Date())) {
			order.setDateActivated(encounter.getEncounterDatetime());
		}
	}
	
	static boolean isRetrospective(Encounter encounter, Date now) {
		Date encounterDate = encounter.getEncounterDatetime();
		Visit visit = encounter.getVisit();
		boolean visitEnded = visit != null && visit.getStopDatetime() != null && visit.getStopDatetime().before(now);
		boolean earlierDay = encounterDate.before(now) && !DateUtils.isSameDay(encounterDate, now);
		return visitEnded || earlierDay;
	}
}
