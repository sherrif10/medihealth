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

import static org.openmrs.module.medihealth.report.NhmisSql.diagnoses;
import static org.openmrs.module.medihealth.report.NhmisSql.hadDrug;

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.springframework.stereotype.Component;

/**
 * Builds the "Child Health and IMCI" section of the NHMIS Monthly Summary Form (form rows 103-107)
 * from visit-note diagnoses/conditions and drug orders: children under 5 diagnosed with diarrhoea,
 * pneumonia or measles in the month, and whether they were ordered ORS and zinc (diarrhoea) or
 * amoxicillin (pneumonia) in the same month. Each child is counted once per row.
 */
@Component("medihealth.ChildHealthDataSetBuilder")
public class ChildHealthDataSetBuilder {
	
	private static final String UNDER_5 = "x.age_m < 60";
	
	public SqlDataSetDefinition build() {
		NhmisBlock diarrhoea = new NhmisBlock(diagnoses(NhmisConcepts.DIARRHOEA))
		        .with("ors_zinc",
		            hadDrug("ev.patient_id", NhmisConcepts.ORS) + " AND " + hadDrug("ev.patient_id", NhmisConcepts.ZINC))
		        .patientsBySex("diarrhoea_new_cases_lt5", UNDER_5)
		        .patientsBySex("diarrhoea_lt5_given_ors_zinc", UNDER_5 + " AND x.ors_zinc = 1");
		NhmisBlock pneumonia = new NhmisBlock(diagnoses(NhmisConcepts.PNEUMONIA))
		        .with("amoxicillin", hadDrug("ev.patient_id", NhmisConcepts.AMOXICILLIN))
		        .patientsBySex("pneumonia_new_cases_lt5", UNDER_5)
		        .patientsBySex("pneumonia_lt5_given_antibiotics", UNDER_5 + " AND x.amoxicillin = 1");
		NhmisBlock measles = new NhmisBlock(diagnoses(NhmisConcepts.MEASLES))
		        .patientsBySex("measles_new_cases_lt5", UNDER_5);
		return NhmisSql.dataSet("NHMIS - Child Health and IMCI",
		    "Form rows 103-107: diarrhoea (and ORS + zinc), pneumonia (and amoxicillin) and measles new cases under 5",
		    diarrhoea, pneumonia, measles);
	}
}
