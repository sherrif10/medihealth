/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.medihealth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.reporting.report.manager.ReportManager;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class MedihealthActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see #started()
	 */
	public void started() {
		log.info("Started Medihealth");
		setupReports();
	}
	
	/**
	 * Registers/updates every {@link ReportManager} bean (e.g. the NHMIS Monthly Summary Form) with
	 * the Reporting Module so they show up in the Reports app. Wrapped defensively since a problem
	 * building one report definition should not prevent the rest of the module from starting.
	 */
	private void setupReports() {
		for (ReportManager reportManager : Context.getRegisteredComponents(ReportManager.class)) {
			try {
				ReportManagerUtil.setupReport(reportManager);
			}
			catch (Exception e) {
				log.error("Failed to set up report: " + reportManager.getName(), e);
			}
		}
	}
	
	/**
	 * @see #shutdown()
	 */
	public void shutdown() {
		log.info("Shutdown Medihealth");
	}
	
}
