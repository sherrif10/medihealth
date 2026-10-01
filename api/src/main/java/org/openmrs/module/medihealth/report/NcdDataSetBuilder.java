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

import static org.openmrs.module.medihealth.report.NhmisSql.firstDiagnoses;

import java.util.List;

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.springframework.stereotype.Component;

/**
 * Builds the "Non-Communicable Diseases" section of the NHMIS Monthly Summary Form (form rows
 * 131-139). A "new case" is a patient whose first ever diagnosis (visit note or condition list) of
 * that disease is in the month, so a known hypertensive seen every month is counted only once.
 */
@Component("medihealth.NcdDataSetBuilder")
public class NcdDataSetBuilder {
	
	public SqlDataSetDefinition build() {
		return NhmisSql.dataSet("NHMIS - Non-Communicable Diseases",
		    "Form rows 131-139: suspected new cases of diabetes, gestational diabetes, hypertension, arthritis, "
		            + "sickle cell disease, asthma, depression, breast and cervical cancer",
		    newCases("diabetes_new_cases", NhmisConcepts.DIABETES), new NhmisBlock(
		            firstDiagnoses(NhmisConcepts.GESTATIONAL_DIABETES))
		            .patients("gestational_diabetes_women", "x.sex = 'F'"),
		    newCases("hypertension_new_cases", NhmisConcepts.HYPERTENSION),
		    newCases("arthritis_new_cases", NhmisConcepts.ARTHRITIS),
		    newCases("sickle_cell_new_cases", NhmisConcepts.SICKLE_CELL),
		    newCases("asthma_new_cases", NhmisConcepts.ASTHMA), newCases("depression_new_cases", NhmisConcepts.DEPRESSION),
		    newCases("breast_cancer_new_cases", NhmisConcepts.BREAST_CANCER),
		    newCases("cervical_cancer_new_cases", NhmisConcepts.CERVICAL_CANCER));
	}
	
	private NhmisBlock newCases(String prefix, List<String> diagnosis) {
		return new NhmisBlock(firstDiagnoses(diagnosis)).patientsBySex(prefix, "1 = 1");
	}
}
