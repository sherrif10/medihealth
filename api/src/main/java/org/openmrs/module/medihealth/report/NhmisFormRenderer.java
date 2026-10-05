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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.renderer.RenderingException;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;

/**
 * Fills the official NHMIS form template (see {@link XlsReportRenderer}) and then removes the
 * merged-cell areas the reporting module's template renderer adds a second time (it re-adds most of
 * the form's 976 merged areas), which Microsoft Excel reports as damaged content on opening.
 */
@Handler
@Localized("reporting.XlsReportRenderer")
public class NhmisFormRenderer extends XlsReportRenderer {
	
	@Override
	public void render(ReportData results, String argument, OutputStream out) throws IOException, RenderingException {
		ByteArrayOutputStream filled = new ByteArrayOutputStream();
		super.render(results, argument, filled);
		Workbook wb;
		try {
			wb = WorkbookFactory.create(new ByteArrayInputStream(filled.toByteArray()));
		}
		catch (Exception e) {
			throw new RenderingException("Unable to read the filled NHMIS form", e);
		}
		for (int s = 0; s < wb.getNumberOfSheets(); s++) {
			Sheet sheet = wb.getSheetAt(s);
			Set<String> seen = new HashSet<String>();
			for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--) {
				if (!seen.add(sheet.getMergedRegion(i).formatAsString())) {
					sheet.removeMergedRegion(i);
				}
			}
		}
		wb.setForceFormulaRecalculation(true);
		wb.write(out);
	}
}
