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
 * One EPI/antigen row of the NHMIS Monthly Summary Form: a machine name and the vaccine-answer
 * concept it counts.
 */
public class VaccineDoseColumn {
	
	public final String rowPrefix;
	
	public final String vaccineConceptUuid;
	
	public VaccineDoseColumn(String rowPrefix, String vaccineConceptUuid) {
		this.rowPrefix = rowPrefix;
		this.vaccineConceptUuid = vaccineConceptUuid;
	}
}
