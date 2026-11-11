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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.BaseReportManager;
import org.springframework.stereotype.Component;

/**
 * The IDSR notification forms in the Reports app: IDSR 002 (weekly) and IDSR 003 (monthly), each
 * for one health facility or for all PHCs together (the LGA form). Each download is the official
 * IDSR workbook with the matching sheet filled in by {@link IdsrFormRenderer}.
 */
public abstract class IdsrReportManager extends BaseReportManager {
	
	static final String TEMPLATE = "org/openmrs/module/medihealth/report/idsr-template.xlsx";
	
	private final IdsrForm form;
	
	private final boolean perFacility;
	
	private final String uuid;
	
	private final String designUuid;
	
	protected IdsrReportManager(IdsrForm form, boolean perFacility, String uuid, String designUuid) {
		this.form = form;
		this.perFacility = perFacility;
		this.uuid = uuid;
		this.designUuid = designUuid;
	}
	
	@Override
	public String getUuid() {
		return uuid;
	}
	
	@Override
	public String getName() {
		return (form == IdsrForm.WEEKLY_002 ? "IDSR 002 Weekly Notification" : "IDSR 003 Monthly Notification")
		        + (perFacility ? " (Health facility)" : " (All PHCs - LGA)");
	}
	
	@Override
	public String getDescription() {
		String what = form == IdsrForm.WEEKLY_002 ? "suspected, lab-confirmed cases and deaths of the weekly IDSR diseases"
		        : "out-patient, in-patient cases and deaths of the monthly IDSR conditions";
		return "IDSR " + (form == IdsrForm.WEEKLY_002 ? "002" : "003") + ": " + what + ", by age band, "
		        + (perFacility ? "for one health facility" : "for all PHCs together") + ", on the official IDSR workbook";
	}
	
	@Override
	public String getVersion() {
		return "1.0";
	}
	
	@Override
	public List<Parameter> getParameters() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("startDate", "Start Date", Date.class));
		parameters.add(new Parameter("endDate", "End Date", Date.class));
		if (perFacility) {
			parameters.add(NhmisSql.facilityParameter());
		}
		return parameters;
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());
		String mappings = "startDate=${startDate},endDate=${endDate}" + (perFacility ? ",location=${location}" : "");
		rd.addDataSetDefinition("cells", new Mapped<SqlDataSetDefinition>(dataSet("IDSR cells", form.cellsSql(perFacility)),
		        ParameterizableUtil.createParameterMappings(mappings)));
		rd.addDataSetDefinition(
		    "header",
		    new Mapped<SqlDataSetDefinition>(dataSet("IDSR header", form.headerSql(perFacility)), ParameterizableUtil
		            .createParameterMappings(mappings)));
		return rd;
	}
	
	private SqlDataSetDefinition dataSet(String name, String sql) {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName(name);
		dsd.setParameters(getParameters());
		dsd.setSqlQuery(sql);
		return dsd;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		ReportDesign design = new ReportDesign();
		design.setUuid(designUuid);
		design.setName(getName() + " (official form, Excel)");
		design.setReportDefinition(reportDefinition);
		design.setRendererType(IdsrFormRenderer.class);
		design.addPropertyValue(IdsrFormRenderer.FORM_PROPERTY, form.name());
		design.addPropertyValue(IdsrFormRenderer.PER_FACILITY_PROPERTY, Boolean.toString(perFacility));
		ReportDesignResource template = new ReportDesignResource();
		template.setName("template");
		template.setExtension("xlsx");
		template.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		template.setContents(readTemplate());
		template.setReportDesign(design);
		design.addResource(template);
		List<ReportDesign> designs = new ArrayList<ReportDesign>();
		designs.add(design);
		return designs;
	}
	
	private static byte[] readTemplate() {
		InputStream in = IdsrReportManager.class.getClassLoader().getResourceAsStream(TEMPLATE);
		if (in == null) {
			throw new IllegalStateException("Missing " + TEMPLATE);
		}
		try {
			return IOUtils.toByteArray(in);
		}
		catch (IOException e) {
			throw new IllegalStateException("Unable to read " + TEMPLATE, e);
		}
		finally {
			IOUtils.closeQuietly(in);
		}
	}
	
	@Component("medihealth.IdsrWeeklyFacilityReportManager")
	public static class WeeklyFacility extends IdsrReportManager {
		
		public WeeklyFacility() {
			super(IdsrForm.WEEKLY_002, true, "5b1e7c2a-0d3f-4a61-9c8e-1f2a3b4c5d01", "5b1e7c2a-0d3f-4a61-9c8e-1f2a3b4c5d11");
		}
	}
	
	@Component("medihealth.IdsrWeeklyLgaReportManager")
	public static class WeeklyLga extends IdsrReportManager {
		
		public WeeklyLga() {
			super(IdsrForm.WEEKLY_002, false, "5b1e7c2a-0d3f-4a61-9c8e-1f2a3b4c5d02", "5b1e7c2a-0d3f-4a61-9c8e-1f2a3b4c5d12");
		}
	}
	
	@Component("medihealth.IdsrMonthlyFacilityReportManager")
	public static class MonthlyFacility extends IdsrReportManager {
		
		public MonthlyFacility() {
			super(IdsrForm.MONTHLY_003, true, "5b1e7c2a-0d3f-4a61-9c8e-1f2a3b4c5d03", "5b1e7c2a-0d3f-4a61-9c8e-1f2a3b4c5d13");
		}
	}
	
	@Component("medihealth.IdsrMonthlyLgaReportManager")
	public static class MonthlyLga extends IdsrReportManager {
		
		public MonthlyLga() {
			super(IdsrForm.MONTHLY_003, false, "5b1e7c2a-0d3f-4a61-9c8e-1f2a3b4c5d04",
			        "5b1e7c2a-0d3f-4a61-9c8e-1f2a3b4c5d14");
		}
	}
}
