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

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.springframework.aop.AfterReturningAdvice;

/**
 * After an "Immunization Record" encounter is saved, voided or unvoided, updates its copy in the
 * immunization history ({@link ImmunizationHistory}). A failure is logged and never undoes the form
 * save. Registered on EncounterService in config.xml.
 */
public class ImmunizationHistoryAdvice implements AfterReturningAdvice {
	
	private static final Log log = LogFactory.getLog(ImmunizationHistoryAdvice.class);
	
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
		String name = method.getName();
		if (!"saveEncounter".equals(name) && !"voidEncounter".equals(name) && !"unvoidEncounter".equals(name)) {
			return;
		}
		Encounter encounter = returnValue instanceof Encounter ? (Encounter) returnValue : null;
		if (!ImmunizationHistory.isImmunizationRecord(encounter)) {
			return;
		}
		try {
			ImmunizationHistory.sync(encounter);
		}
		catch (RuntimeException e) {
			log.error("Could not update the immunization history for Immunization Record " + encounter.getUuid(), e);
		}
	}
}
