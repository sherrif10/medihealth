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
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
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
	
	@Autowired
	private ImmunizationDataSetBuilder immunizationDataSetBuilder;
	
	@Autowired
	private BirthRegistrationDataSetBuilder birthRegistrationDataSetBuilder;
	
	@Autowired
	private NutritionDataSetBuilder nutritionDataSetBuilder;
	
	@Autowired
	private ChildHealthDataSetBuilder childHealthDataSetBuilder;
	
	@Autowired
	private FamilyPlanningDataSetBuilder familyPlanningDataSetBuilder;
	
	@Autowired
	private ReferralsDataSetBuilder referralsDataSetBuilder;
	
	@Autowired
	private NcdDataSetBuilder ncdDataSetBuilder;
	
	@Autowired
	private MalariaDataSetBuilder malariaDataSetBuilder;
	
	@Autowired
	private TbScreeningDataSetBuilder tbScreeningDataSetBuilder;
	
	@Autowired
	private HepatitisDataSetBuilder hepatitisDataSetBuilder;
	
	@Autowired
	private GbvDataSetBuilder gbvDataSetBuilder;
	
	@Autowired
	private ObstetricFistulaDataSetBuilder obstetricFistulaDataSetBuilder;
	
	@Autowired
	private NtdPharmacovigilanceDataSetBuilder ntdPharmacovigilanceDataSetBuilder;
	
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
		return "1.5";
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
		addPeriodDataSet(rd, "immunization", immunizationDataSetBuilder.build());
		addPeriodDataSet(rd, "birthRegistration", birthRegistrationDataSetBuilder.build());
		addPeriodDataSet(rd, "nutrition", nutritionDataSetBuilder.build());
		addPeriodDataSet(rd, "childHealth", childHealthDataSetBuilder.build());
		addPeriodDataSet(rd, "familyPlanning", familyPlanningDataSetBuilder.build());
		addPeriodDataSet(rd, "referrals", referralsDataSetBuilder.build());
		addPeriodDataSet(rd, "ncd", ncdDataSetBuilder.build());
		addPeriodDataSet(rd, "malaria", malariaDataSetBuilder.build());
		addPeriodDataSet(rd, "tbScreening", tbScreeningDataSetBuilder.build());
		addPeriodDataSet(rd, "hepatitis", hepatitisDataSetBuilder.build());
		addPeriodDataSet(rd, "gbv", gbvDataSetBuilder.build());
		addPeriodDataSet(rd, "obstetricFistula", obstetricFistulaDataSetBuilder.build());
		addPeriodDataSet(rd, "ntdPharmacovigilance", ntdPharmacovigilanceDataSetBuilder.build());
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
		// The only download: the official NHMIS MSF v2019 sheet with each box filled in, ready to print and sign.
		// (A CSV design used to sit next to it; it produced one file per section, 20 in all, and confused users.)
		ReportDesign formDesign = ReportManagerUtil.createExcelTemplateDesign("0c5d7e2a-9b41-4f6e-8a3d-2e7b1c9f4a58",
		    reportDefinition, "org/openmrs/module/medihealth/report/nhmis-msf-template.xls");
		formDesign.setName(getName() + " (official form, Excel)");
		formDesign.setRendererType(NhmisFormRenderer.class);
		designs.add(formDesign);
		return designs;
	}
}
