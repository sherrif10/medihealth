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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Registers the "NHMIS Monthly Summary Form" report definition with the Reporting Module, so it
 * shows up in the O3 Reports app with CSV/Excel download already wired up.
 * <p>
 * Phase 1 covers the sections that need no new clinical forms: Health Facility Attendance,
 * Inpatient Care, and Mortality (form rows 1-9). Each later phase adds one more
 * {@code *DataSetDefinition} to {@link #constructReportDefinition()} as its clinical forms/concepts
 * land - see the build plan for the full section list and ordering.
 */
@Component("medihealth.NhmisMonthlySummaryReportManager")
public class NhmisMonthlySummaryReportManager extends BaseReportManager {
	
	/** Fixed so re-running setup on every module startup updates the same report, not a new one. */
	public static final String UUID = "3d9b6e2a-8f0e-4a3b-9c9e-9b0d5b7b6a01";
	
	@Autowired
	private AttendanceDataSetBuilder attendanceDataSetBuilder;
	
	@Autowired
	private InpatientCareDataSetBuilder inpatientCareDataSetBuilder;
	
	@Autowired
	private MortalityDataSetBuilder mortalityDataSetBuilder;
	
	@Autowired
	private AncDataSetBuilder ancDataSetBuilder;
	
	@Autowired
	private LabourDeliveryDataSetBuilder labourDeliveryDataSetBuilder;
	
	@Autowired
	private PostnatalCareDataSetBuilder postnatalCareDataSetBuilder;
	
	@Autowired
	private NewbornDataSetBuilder newbornDataSetBuilder;
	
	@Override
	public String getUuid() {
		return UUID;
	}
	
	@Override
	public String getName() {
		return "NHMIS Monthly Summary Form";
	}
	
	@Override
	public String getDescription() {
		return "National Health Management Information System (NHMIS) Health Facility Monthly Summary Form, version 2019";
	}
	
	@Override
	public String getVersion() {
		return "1.0";
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		addPeriodDataSet(rd, "attendance", attendanceDataSetBuilder.build());
		addPeriodDataSet(rd, "inpatientCare", inpatientCareDataSetBuilder.build());
		addPeriodDataSet(rd, "mortality", mortalityDataSetBuilder.build());
		addPeriodDataSet(rd, "anc", ancDataSetBuilder.build());
		addPeriodDataSet(rd, "labourDelivery", labourDeliveryDataSetBuilder.build());
		addPeriodDataSet(rd, "postnatalCare", postnatalCareDataSetBuilder.build());
		addPeriodDataSet(rd, "newborn", newbornDataSetBuilder.build());
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
		csvDesign.setUuid("6e1a3b7c-2d4f-4b8a-9a1e-0b6c8d2e7f10");
		csvDesign.setName(getName() + " CSV");
		csvDesign.setReportDefinition(reportDefinition);
		csvDesign.setRendererType(CsvReportRenderer.class);
		designs.add(csvDesign);
		return designs;
	}
}
