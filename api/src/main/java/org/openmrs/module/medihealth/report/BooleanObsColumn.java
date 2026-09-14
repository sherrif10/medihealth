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
 * One output column of a {@link BooleanObsIndicatorFactory} query: a form row's machine name and
 * the boolean-valued concept it reads from.
 */
public class BooleanObsColumn {
	
	public final String columnAlias;
	
	public final String conceptUuid;
	
	public BooleanObsColumn(String columnAlias, String conceptUuid) {
		this.columnAlias = columnAlias;
		this.conceptUuid = conceptUuid;
	}
}
