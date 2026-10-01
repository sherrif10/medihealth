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

import static org.openmrs.module.medihealth.report.NhmisSql.encounters;
import static org.openmrs.module.medihealth.report.NhmisSql.hasAnswer;
import static org.openmrs.module.medihealth.report.NhmisSql.hasTrue;

import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.springframework.stereotype.Component;

/**
 * Builds the "Obstetric Fistula Services" section of the NHMIS Monthly Summary Form (form rows
 * 169-175) from the "Obstetric Fistula Care" form, by fistula type (VVF, RVF, both) and age 10-19 /
 * 20 and over. Each woman is counted once per row.
 */
@Component("medihealth.ObstetricFistulaDataSetBuilder")
public class ObstetricFistulaDataSetBuilder {
	
	private static final String TYPE = "d4ae963a-0d81-594c-91fa-6b9fc2e9903e";
	
	private static final String REPAIR_NUMBER = "b45cdb94-a433-554f-b33a-1b732c0710a0";
	
	private static final String[][] TYPES = { { "vvf", "49AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" },
	        { "rvf", "879d71d2-adaa-43e7-8aeb-c64a9e91b064" }, { "vvf_rvf", "f49f652c-40cb-5c1c-bbc8-67d02bee424d" } };
	
	public SqlDataSetDefinition build() {
		NhmisBlock fistula = new NhmisBlock(encounters(NhmisConcepts.FISTULA_CARE))
		        .with("is_new", hasTrue("ev.event_id", "e8074f13-5fd2-5973-b7c6-b5126e5ed17a"))
		        .with("admitted", hasTrue("ev.event_id", "6b006fdb-1e62-52f7-b9f9-48f6cb479cc6"))
		        .with("surgery", hasTrue("ev.event_id", "a2482c8d-93c4-5216-a593-fb46d6cb5be9"))
		        .with("first_repair", hasAnswer("ev.event_id", REPAIR_NUMBER, "5f620c3b-ef5d-547d-b7de-d0813cb83611"))
		        .with("second_repair", hasAnswer("ev.event_id", REPAIR_NUMBER, "54721482-88f3-5d1f-bc67-bf2b9b5622f5"))
		        .with("discharged", hasTrue("ev.event_id", "3d342fc4-518b-59f2-9005-9cd5f743672f"))
		        .with("closed_dry", hasTrue("ev.event_id", "dbc337ea-9456-565c-8f05-6ded93c0c29d"));
		for (String[] t : TYPES) {
			fistula.with(t[0], hasAnswer("ev.event_id", TYPE, t[1]));
		}
		String[][] rows = { { "fistula_new_cases", "x.is_new = 1" }, { "fistula_admitted", "x.admitted = 1" },
		        { "fistula_surgery", "x.surgery = 1" }, { "fistula_first_repair", "x.surgery = 1 AND x.first_repair = 1" },
		        { "fistula_second_repair", "x.surgery = 1 AND x.second_repair = 1" },
		        { "fistula_discharged_after_surgery", "x.discharged = 1" },
		        { "fistula_closed_dry_at_discharge", "x.closed_dry = 1" } };
		for (String[] row : rows) {
			for (String[] t : TYPES) {
				fistula.patients(row[0] + "_" + t[0] + "_10_19", row[1] + " AND x." + t[0]
				        + " = 1 AND x.age_y BETWEEN 10 AND 19");
				fistula.patients(row[0] + "_" + t[0] + "_gte20", row[1] + " AND x." + t[0] + " = 1 AND x.age_y >= 20");
			}
			fistula.patients(row[0] + "_total", row[1]);
		}
		return NhmisSql.dataSet("NHMIS - Obstetric Fistula",
		    "Form rows 169-175: fistula new cases, admissions, repairs, discharges and closed/dry at discharge", fistula);
	}
}
