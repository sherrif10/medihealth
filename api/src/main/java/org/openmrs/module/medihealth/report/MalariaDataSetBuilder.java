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

import static org.openmrs.module.medihealth.report.NhmisConcepts.MALARIA_MICROSCOPY;
import static org.openmrs.module.medihealth.report.NhmisConcepts.MALARIA_MICROSCOPY_POSITIVE;
import static org.openmrs.module.medihealth.report.NhmisConcepts.MALARIA_RDT;
import static org.openmrs.module.medihealth.report.NhmisConcepts.MALARIA_RDT_POSITIVE;
import static org.openmrs.module.medihealth.report.NhmisSql.diagnoses;
import static org.openmrs.module.medihealth.report.NhmisSql.hadAnswer;
import static org.openmrs.module.medihealth.report.NhmisSql.hadDiagnosis;
import static org.openmrs.module.medihealth.report.NhmisSql.hadDrug;
import static org.openmrs.module.medihealth.report.NhmisSql.hadResult;
import static org.openmrs.module.medihealth.report.NhmisSql.obsAnswers;
import static org.openmrs.module.medihealth.report.NhmisSql.obsAtLeast;
import static org.openmrs.module.medihealth.report.NhmisSql.obsResults;
import static org.openmrs.module.medihealth.report.NhmisSql.obsTrue;
import static org.openmrs.module.medihealth.report.NhmisSql.pregnant;
import static org.openmrs.module.medihealth.report.NhmisSql.union;

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.springframework.stereotype.Component;

/**
 * Builds the "Malaria Services" section of the NHMIS Monthly Summary Form (form rows 140-154).
 * <p>
 * Persons are split into the form's three groups: under 5, 5 and over (not pregnant), and pregnant
 * women (a woman with an Antenatal Care Visit in the 280 days up to the event).
 * <ul>
 * <li>Fever (141-144): temperature of 37.5C or more, or a fever diagnosis/complaint.</li>
 * <li>Tests (142-145): malaria RDT and smear/MP results from the lab.</li>
 * <li>Cases (146-148): malaria diagnoses. A case is confirmed if the patient had a positive RDT or
 * smear in the month (or was diagnosed "Malaria, confirmed"), otherwise clinical; it is severe if
 * any severe malaria diagnosis was made in the month.</li>
 * <li>Treatment (149-154): drug orders for the patient in the month, matched by drug name.
 * Pre-referral treatment is rectal artesunate, or a "Referral Out" for further malaria treatment.</li>
 * </ul>
 */
@Component("medihealth.MalariaDataSetBuilder")
public class MalariaDataSetBuilder {
	
	private static final String LLIN_GIVEN = "4edbfe3c-396a-5bf0-b459-b994b51629f6";
	
	/** Column suffix, condition: the form's three groups and the total. */
	private static final String[][] GROUPS = { { "lt5", "x.age_y < 5" }, { "gte5", "x.age_y >= 5 AND x.pw = 0" },
	        { "pw", "x.pw = 1" }, { "total", "1 = 1" } };
	
	public SqlDataSetDefinition build() {
		NhmisBlock llin = new NhmisBlock(obsTrue(LLIN_GIVEN)).patients("children_lt5_received_llin", "x.age_m < 60");
		
		NhmisBlock fever = withPregnancy(
		    new NhmisBlock(union(obsAtLeast(NhmisConcepts.TEMPERATURE, "37.5"), diagnoses(NhmisConcepts.FEVER),
		        obsAnswers(NhmisConcepts.FEVER)))).with("rdt", hadResult("ev.patient_id", MALARIA_RDT, null)).with(
		    "microscopy", hadResult("ev.patient_id", MALARIA_MICROSCOPY, null));
		byGroup(fever, "fever", "1 = 1");
		byGroup(fever, "fever_tested_rdt", "x.rdt = 1");
		byGroup(fever, "fever_tested_microscopy", "x.microscopy = 1");
		
		NhmisBlock rdtPositive = withPregnancy(new NhmisBlock(obsResults(MALARIA_RDT, MALARIA_RDT_POSITIVE)));
		byGroup(rdtPositive, "rdt_positive", "1 = 1");
		NhmisBlock microscopyPositive = withPregnancy(new NhmisBlock(obsResults(MALARIA_MICROSCOPY,
		    MALARIA_MICROSCOPY_POSITIVE)));
		byGroup(microscopyPositive, "microscopy_positive", "1 = 1");
		
		NhmisBlock cases = withPregnancy(new NhmisBlock(diagnoses(NhmisConcepts.MALARIA)))
		        .with("severe", hadDiagnosis("ev.patient_id", NhmisConcepts.SEVERE_MALARIA))
		        .with(
		            "confirmed",
		            hadResult("ev.patient_id", MALARIA_RDT, MALARIA_RDT_POSITIVE) + " OR "
		                    + hadResult("ev.patient_id", MALARIA_MICROSCOPY, MALARIA_MICROSCOPY_POSITIVE) + " OR "
		                    + hadDiagnosis("ev.patient_id", NhmisConcepts.CONFIRMED_MALARIA))
		        .with("act", hadDrug("ev.patient_id", NhmisConcepts.ACT))
		        .with("other_oral", hadDrug("ev.patient_id", NhmisConcepts.OTHER_ORAL_ANTIMALARIAL))
		        .with(
		            "prereferral",
		            hadDrug("ev.patient_id", NhmisConcepts.RECTAL_ARTESUNATE)
		                    + " OR "
		                    + hadAnswer("ev.patient_id", NhmisConcepts.REFERRAL_REASON,
		                        NhmisConcepts.REFERRAL_MALARIA_TREATMENT))
		        .with("artesunate_inj", hadDrug("ev.patient_id", NhmisConcepts.ARTESUNATE_INJECTION))
		        .with("other_inj", hadDrug("ev.patient_id", NhmisConcepts.OTHER_INJECTABLE_ANTIMALARIAL));
		String clinical = "x.severe = 0 AND x.confirmed = 0";
		String confirmed = "x.severe = 0 AND x.confirmed = 1";
		byGroup(cases, "malaria_clinically_diagnosed", clinical);
		byGroup(cases, "malaria_confirmed_uncomplicated", confirmed);
		byGroup(cases, "malaria_severe", "x.severe = 1");
		byGroup(cases, "confirmed_uncomplicated_treated_act", confirmed + " AND x.act = 1");
		byGroup(cases, "clinical_treated_act", clinical + " AND x.act = 1");
		byGroup(cases, "confirmed_uncomplicated_other_antimalarial", confirmed + " AND x.other_oral = 1");
		byGroup(cases, "severe_prereferral_treatment", "x.severe = 1 AND x.prereferral = 1");
		byGroup(cases, "severe_artesunate_injection", "x.severe = 1 AND x.artesunate_inj = 1");
		byGroup(cases, "severe_other_injectable_antimalarial", "x.severe = 1 AND x.other_inj = 1");
		
		return NhmisSql.dataSet("NHMIS - Malaria Services",
		    "Form rows 140-154: LLIN for under 5s, fever and malaria testing, malaria cases and treatment", llin, fever,
		    rdtPositive, microscopyPositive, cases);
	}
	
	private static NhmisBlock withPregnancy(NhmisBlock block) {
		return block.with("pw", "p.gender = 'F' AND " + pregnant("ev.patient_id", "ev.event_date"));
	}
	
	private static void byGroup(NhmisBlock block, String prefix, String condition) {
		for (String[] g : GROUPS) {
			block.patients(prefix + "_" + g[0], condition + " AND " + g[1]);
		}
	}
}
