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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.BaseReportManager;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Registers the "Pharmacy Sales Ledger" report with the Reporting Module, so it shows up in the O3
 * Reports app with CSV and Excel download already wired up - requested by the pharmacist for daily
 * auditing of how much was sold and how many units of each medication were dispensed. Excel uses
 * {@link XlsReportRenderer}, which writes a plain workbook straight from the dataset with no
 * template file required - {@code ExcelTemplateRenderer} was the other option available in this
 * reporting module version, but it needs a template resource that doesn't exist for this report and
 * isn't worth building for a straightforward tabular export like this one.
 */
@Component("medihealth.PharmacySalesLedgerReportManager")
public class PharmacySalesLedgerReportManager extends BaseReportManager {
	
	/** Fixed so re-running setup on every module startup updates the same report, not a new one. */
	public static final String UUID = "7c4e1f8a-3b6d-4e9a-8f2c-1a5d9e6b3c70";
	
	@Autowired
	private PharmacySalesDailyTotalsDataSetBuilder dailyTotalsDataSetBuilder;
	
	@Autowired
	private PharmacySalesByMedicationDataSetBuilder byMedicationDataSetBuilder;
	
	@Override
	public String getUuid() {
		return UUID;
	}
	
	@Override
	public String getName() {
		return "Pharmacy Sales Ledger";
	}
	
	@Override
	public String getDescription() {
		return "Daily pharmacy sales: revenue and quantity dispensed per medication, for auditing";
	}
	
	/**
	 * Bump this every time constructReportDefinition()/constructReportDesigns() actually changes -
	 * ReportManagerUtil.setupReport() reads the last-registered version from a global property
	 * (reporting.reportManager.{uuid}.version) and skips rebuilding the report entirely if it
	 * already matches this value, even across a full module reload. The column-name fix that
	 * shipped in this same branch's next commit was correct but silently never took effect on QA
	 * for exactly this reason - the version was still "1.0" from the first deploy, so every restart
	 * short-circuited before ever calling constructReportDefinition() again. Confirmed from
	 * ReportManagerUtil's decompiled bytecode, not assumed.
	 */
	@Override
	public String getVersion() {
		return "1.1";
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		addPeriodDataSet(rd, "dailyTotals", dailyTotalsDataSetBuilder.build());
		addPeriodDataSet(rd, "byMedication", byMedicationDataSetBuilder.build());
		return rd;
	}
	
	private void addPeriodDataSet(ReportDefinition rd, String key,
	        org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition dsd) {
		rd.addDataSetDefinition(key,
		    new Mapped<>(dsd, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		List<ReportDesign> designs = new ArrayList<>();
		ReportDesign csvDesign = new ReportDesign();
		csvDesign.setUuid("9d5f2a8b-4c7e-4f1a-9b3d-2e6c8a4f7d81");
		csvDesign.setName(getName() + " CSV");
		csvDesign.setReportDefinition(reportDefinition);
		csvDesign.setRendererType(CsvReportRenderer.class);
		designs.add(csvDesign);

		ReportDesign xlsDesign = new ReportDesign();
		xlsDesign.setUuid("b1e6f3a9-7c2d-4e8b-9a5f-3d6c1e8b4f92");
		xlsDesign.setName(getName() + " Excel");
		xlsDesign.setReportDefinition(reportDefinition);
		xlsDesign.setRendererType(XlsReportRenderer.class);
		designs.add(xlsDesign);
		return designs;
	}
}
