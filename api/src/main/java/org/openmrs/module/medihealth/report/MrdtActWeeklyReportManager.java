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

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.BaseReportManager;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;
import org.springframework.stereotype.Component;

/**
 * Weekly MRDT and ACT report in the Reports app: malaria rapid tests (done, positive, negative) and
 * ACT prescriptions by age category (ACT 1-4), for one health facility or all PHCs. The period is
 * any start and end date, usually one reporting week. See {@link MrdtActWeekly} for the counting
 * rules.
 */
public abstract class MrdtActWeeklyReportManager extends BaseReportManager {
	
	private final boolean perFacility;
	
	private final String uuid;
	
	private final String csvDesignUuid;
	
	private final String excelDesignUuid;
	
	protected MrdtActWeeklyReportManager(boolean perFacility, String uuid, String csvDesignUuid, String excelDesignUuid) {
		this.perFacility = perFacility;
		this.uuid = uuid;
		this.csvDesignUuid = csvDesignUuid;
		this.excelDesignUuid = excelDesignUuid;
	}
	
	@Override
	public String getUuid() {
		return uuid;
	}
	
	@Override
	public String getName() {
		return "MRDT and ACT Weekly" + (perFacility ? " (Health facility)" : " (All PHCs)");
	}
	
	@Override
	public String getDescription() {
		return "Malaria rapid tests done (positive, negative) and ACT prescriptions by age: ACT 1 under 5 years, "
		        + "ACT 2 5-9, ACT 3 10-14, ACT 4 15 and above, "
		        + (perFacility ? "for one health facility" : "for all PHCs together")
		        + ". Each patient is counted once: per day for MRDT, per period for ACT";
	}
	
	/**
	 * Bump when the definition changes: setupReport skips the rebuild while the stored version
	 * matches.
	 */
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
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("MRDT and ACT");
		dsd.setParameters(getParameters());
		dsd.setSqlQuery(MrdtActWeekly.sql(perFacility));
		String mappings = "startDate=${startDate},endDate=${endDate}" + (perFacility ? ",location=${location}" : "");
		rd.addDataSetDefinition("mrdtAct",
		    new Mapped<SqlDataSetDefinition>(dsd, ParameterizableUtil.createParameterMappings(mappings)));
		return rd;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		List<ReportDesign> designs = new ArrayList<ReportDesign>();
		designs.add(design(reportDefinition, excelDesignUuid, getName() + " Excel", XlsReportRenderer.class));
		designs.add(design(reportDefinition, csvDesignUuid, getName() + " CSV", CsvReportRenderer.class));
		return designs;
	}
	
	private static ReportDesign design(ReportDefinition rd, String uuid, String name,
	        Class<? extends org.openmrs.module.reporting.report.renderer.ReportRenderer> renderer) {
		ReportDesign design = new ReportDesign();
		design.setUuid(uuid);
		design.setName(name);
		design.setReportDefinition(rd);
		design.setRendererType(renderer);
		return design;
	}
	
	@Component("medihealth.MrdtActWeeklyFacilityReportManager")
	public static class Facility extends MrdtActWeeklyReportManager {
		
		public Facility() {
			super(true, "6e2a4c81-3f5d-4b7a-9c1e-2d8f0a6b4c01", "6e2a4c81-3f5d-4b7a-9c1e-2d8f0a6b4c11",
			        "6e2a4c81-3f5d-4b7a-9c1e-2d8f0a6b4c21");
		}
	}
	
	@Component("medihealth.MrdtActWeeklyAllPhcsReportManager")
	public static class AllPhcs extends MrdtActWeeklyReportManager {
		
		public AllPhcs() {
			super(false, "6e2a4c81-3f5d-4b7a-9c1e-2d8f0a6b4c02", "6e2a4c81-3f5d-4b7a-9c1e-2d8f0a6b4c12",
			        "6e2a4c81-3f5d-4b7a-9c1e-2d8f0a6b4c22");
		}
	}
}
