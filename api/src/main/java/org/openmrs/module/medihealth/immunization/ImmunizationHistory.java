/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.medihealth.immunization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;

/**
 * Copies the doses recorded on the "Immunization Record" form into the patient's immunization
 * history, so that they show on the patient chart's Immunizations page next to the doses entered
 * there.
 * <p>
 * The Immunizations page reads FHIR Immunization resources, which the FHIR module builds from
 * "Immunization history" (CIEL 1421) obs groups: the vaccine (CIEL 984), the vaccination date (CIEL
 * 1410) and the dose number (CIEL 1418). FHIR only accepts such a group in an encounter with
 * exactly one provider, in the Clinician role, so the copies go into their own "Immunizations"
 * encounter (the encounter type the Immunizations page itself uses), in the same visit, at the same
 * date and location, by the form's provider.
 * <p>
 * Every copied group is tagged with {@link #NAMESPACE}^{@link #PATH_PREFIX}&lt;form encounter
 * uuid&gt;. When the form is edited the copies are replaced, and when it is voided they are voided.
 * The NHMIS report skips tagged groups, because it already counts the dose from the form.
 */
public final class ImmunizationHistory {
	
	private static final Log log = LogFactory.getLog(ImmunizationHistory.class);
	
	public static final String IMMUNIZATION_RECORD_ENCOUNTER_TYPE = "b0c54d2e-fabd-4e55-a0fe-b96f5d96f07f";
	
	public static final String IMMUNIZATIONS_ENCOUNTER_TYPE = "29c02aff-9a93-46c9-bf6f-48b552fcb1fa";
	
	public static final String CLINICIAN_ENCOUNTER_ROLE = "240b26f9-dd88-4172-823d-4a8bfeb7841f";
	
	/** The form's "Vaccine administered": both the obs group and the vaccine question in it. */
	public static final String VACCINE_ADMINISTERED = "cc2f3a5c-ea29-4709-bf70-7c516dbf3719";
	
	public static final String TD_DOSE_NUMBER = "f05e8ce6-2fef-4be8-b578-8e3726ba1bde";
	
	public static final String IMMUNIZATION_HISTORY = "1421AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String VACCINE = "984AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String VACCINATION_DATE = "1410AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String DOSE_NUMBER = "1418AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String NAMESPACE = "medihealth";
	
	public static final String PATH_PREFIX = "immunization-record/";
	
	/** "Fully immunized child under 1 year" is a status, not a dose: it is not copied. */
	static final String FULLY_IMMUNIZED_UNDER1 = "450c0e78-72fe-4156-82b7-b34177e52e9e";
	
	static final String TD_PREGNANT = "c5f21be9-02e6-4924-9d93-33c0a12ab4bf";
	
	static final String TD_NON_PREGNANT = "f3448db1-1bc9-4622-ba08-e1192e4eaaf9";
	
	private static final String CIEL_OPV = "783AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CIEL_HEPB = "782AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CIEL_PENTAVALENT = "162339AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CIEL_ROTAVIRUS = "83531AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CIEL_MEASLES = "36AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CIEL_PNEUMOCOCCAL = "82215AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CIEL_BCG = "886AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CIEL_IPV = "1422AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CIEL_VITAMIN_A = "86339AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CIEL_TETANUS_TOXOID = "84879AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	/**
	 * Form answer -> the vaccine the Immunizations page lists it under, and the dose number. Doses
	 * of a series go under the one CIEL vaccine, so the page shows them together. Answers that are
	 * not here (birth OPV, yellow fever, meningitis A, HPV, and any added later) are copied as
	 * themselves without a dose number.
	 */
	private static final Map<String, HistoryDose> BY_FORM_ANSWER = new HashMap<String, HistoryDose>();
	
	static {
		series(CIEL_OPV, "c0290e1e-0f66-4e3f-aeb7-552e8cdfd67a", "5c7b4af8-f774-4028-9e92-24e78eee0f72",
		    "078dc96b-01f1-49da-b70a-dd20dcb5ee7f");
		series(CIEL_PENTAVALENT, "610e5a89-02ba-43ab-8814-39eb3257db08", "50e945fd-f828-43c4-bf13-e4b555c5b852",
		    "5738a0e8-3b28-444a-9b2f-9f9e3786ba07");
		series(CIEL_ROTAVIRUS, "080dfc3f-b1d4-4dbe-9233-970a675f618d", "1f40e138-d2b6-4881-bc5e-d45fd5ebe25e",
		    "ff329793-7ded-40ab-a51a-f8477a52d519");
		series(CIEL_MEASLES, "4f4de487-c10a-49e0-b88e-946cdc8b42fb", "4797e3eb-5710-4deb-982e-40b0af717142");
		series(CIEL_PNEUMOCOCCAL, "57845954-da0e-46eb-b713-54b8fc55a853", "287d2d7b-f9c5-4568-9c98-cf182f0d6f55",
		    "1e86622d-fc24-4d72-ad8e-3623b2ee0b7a");
		BY_FORM_ANSWER.put("08ab1510-aee0-4687-9c12-5db506470fa7", new HistoryDose(CIEL_HEPB, 1));
		BY_FORM_ANSWER.put("4f60c141-9051-4f22-a4b1-f286e17681a8", new HistoryDose(CIEL_BCG, null));
		BY_FORM_ANSWER.put("412201a9-af61-4edf-9bf0-19510819e314", new HistoryDose(CIEL_IPV, null));
		BY_FORM_ANSWER.put("37fc0998-dbb6-468b-aab4-fe06973a70be", new HistoryDose(CIEL_VITAMIN_A, null));
	}
	
	private ImmunizationHistory() {
	}
	
	private static void series(String vaccine, String... doseAnswers) {
		for (int i = 0; i < doseAnswers.length; i++) {
			BY_FORM_ANSWER.put(doseAnswers[i], new HistoryDose(vaccine, i + 1));
		}
	}
	
	/** A dose as the Immunizations page records it. */
	static final class HistoryDose {
		
		final String vaccineUuid;
		
		final Integer doseNumber;
		
		HistoryDose(String vaccineUuid, Integer doseNumber) {
			this.vaccineUuid = vaccineUuid;
			this.doseNumber = doseNumber;
		}
	}
	
	/**
	 * The history dose of a form answer, or null when it is not a dose. A Td dose goes under
	 * tetanus toxoid with the Td dose number entered next to it.
	 */
	static HistoryDose historyDose(String formAnswerUuid, Double tdDoseNumber) {
		if (formAnswerUuid == null || FULLY_IMMUNIZED_UNDER1.equals(formAnswerUuid)) {
			return null;
		}
		if (TD_PREGNANT.equals(formAnswerUuid) || TD_NON_PREGNANT.equals(formAnswerUuid)) {
			Integer dose = tdDoseNumber != null && tdDoseNumber >= 1 && tdDoseNumber <= 5 ? tdDoseNumber.intValue() : null;
			return new HistoryDose(CIEL_TETANUS_TOXOID, dose);
		}
		HistoryDose dose = BY_FORM_ANSWER.get(formAnswerUuid);
		return dose != null ? dose : new HistoryDose(formAnswerUuid, null);
	}
	
	static boolean isImmunizationRecord(Encounter encounter) {
		return encounter != null && encounter.getEncounterType() != null
		        && IMMUNIZATION_RECORD_ENCOUNTER_TYPE.equals(encounter.getEncounterType().getUuid());
	}
	
	static String tagPath(Encounter formEncounter) {
		return PATH_PREFIX + formEncounter.getUuid();
	}
	
	/**
	 * Brings the history copies of an Immunization Record encounter in line with it: the existing
	 * copies are voided and, unless the form encounter is voided, its doses are copied again.
	 */
	public static void sync(Encounter formEncounter) {
		if (!isImmunizationRecord(formEncounter) || formEncounter.getUuid() == null) {
			return;
		}
		voidCopies(formEncounter, formEncounter.getVoided() ? "Immunization Record voided" : "Immunization Record updated");
		if (formEncounter.getVoided()) {
			return;
		}
		List<HistoryDose> doses = doses(formEncounter);
		if (doses.isEmpty()) {
			return;
		}
		Provider provider = provider(formEncounter);
		EncounterType type = Context.getEncounterService().getEncounterTypeByUuid(IMMUNIZATIONS_ENCOUNTER_TYPE);
		EncounterRole clinician = Context.getEncounterService().getEncounterRoleByUuid(CLINICIAN_ENCOUNTER_ROLE);
		Concept history = concept(IMMUNIZATION_HISTORY), vaccine = concept(VACCINE), date = concept(VACCINATION_DATE), doseNumber = concept(DOSE_NUMBER);
		if (provider == null || type == null || clinician == null || history == null || vaccine == null || date == null
		        || doseNumber == null) {
			// without one Clinician provider FHIR rejects the patient's whole immunization list
			log.warn("Not copying Immunization Record " + formEncounter.getUuid()
			        + " into the immunization history: no provider or immunization metadata");
			return;
		}
		Encounter copy = new Encounter();
		copy.setEncounterType(type);
		copy.setPatient(formEncounter.getPatient());
		copy.setVisit(formEncounter.getVisit());
		copy.setLocation(formEncounter.getLocation());
		copy.setEncounterDatetime(formEncounter.getEncounterDatetime());
		copy.addProvider(clinician, provider);
		for (HistoryDose dose : doses) {
			Concept vaccineAnswer = concept(dose.vaccineUuid);
			if (vaccineAnswer == null) {
				continue;
			}
			Obs group = obs(formEncounter, history);
			group.setFormField(NAMESPACE, tagPath(formEncounter));
			Obs vaccineObs = obs(formEncounter, vaccine);
			vaccineObs.setValueCoded(vaccineAnswer);
			group.addGroupMember(vaccineObs);
			Obs dateObs = obs(formEncounter, date);
			dateObs.setValueDatetime(formEncounter.getEncounterDatetime());
			group.addGroupMember(dateObs);
			if (dose.doseNumber != null) {
				Obs doseObs = obs(formEncounter, doseNumber);
				doseObs.setValueNumeric(dose.doseNumber.doubleValue());
				group.addGroupMember(doseObs);
			}
			copy.addObs(group);
		}
		if (!copy.getObsAtTopLevel(false).isEmpty()) {
			Context.getEncounterService().saveEncounter(copy);
		}
	}
	
	/** The doses on the form, in form order, one per vaccine. */
	static List<HistoryDose> doses(Encounter formEncounter) {
		List<HistoryDose> doses = new ArrayList<HistoryDose>();
		Set<String> seen = new LinkedHashSet<String>();
		for (Obs group : formEncounter.getObsAtTopLevel(false)) {
			if (!group.isObsGrouping() || !VACCINE_ADMINISTERED.equals(group.getConcept().getUuid())) {
				continue;
			}
			String answer = null;
			Double tdDose = null;
			for (Obs member : group.getGroupMembers(false)) {
				String q = member.getConcept().getUuid();
				if (VACCINE_ADMINISTERED.equals(q) && member.getValueCoded() != null) {
					answer = member.getValueCoded().getUuid();
				} else if (TD_DOSE_NUMBER.equals(q)) {
					tdDose = member.getValueNumeric();
				}
			}
			HistoryDose dose = historyDose(answer, tdDose);
			if (dose != null && seen.add(dose.vaccineUuid + "/" + dose.doseNumber)) {
				doses.add(dose);
			}
		}
		return doses;
	}
	
	private static void voidCopies(Encounter formEncounter, String reason) {
		Concept history = concept(IMMUNIZATION_HISTORY);
		if (history == null || formEncounter.getPatient() == null) {
			return;
		}
		String path = tagPath(formEncounter);
		Set<Encounter> copies = new LinkedHashSet<Encounter>();
		for (Obs group : Context.getObsService().getObservationsByPersonAndConcept(formEncounter.getPatient(), history)) {
			if (NAMESPACE.equals(group.getFormFieldNamespace()) && path.equals(group.getFormFieldPath())
			        && group.getEncounter() != null && !group.getEncounter().getVoided()) {
				copies.add(group.getEncounter());
			}
		}
		for (Encounter copy : copies) {
			Context.getEncounterService().voidEncounter(copy, reason);
		}
	}
	
	/** The form's provider (a Clinician first), else the provider of the user saving it. */
	private static Provider provider(Encounter formEncounter) {
		Provider any = null;
		for (EncounterProvider ep : formEncounter.getActiveEncounterProviders()) {
			if (ep.getProvider() == null || ep.getProvider().getRetired()) {
				continue;
			}
			if (ep.getEncounterRole() != null && CLINICIAN_ENCOUNTER_ROLE.equals(ep.getEncounterRole().getUuid())) {
				return ep.getProvider();
			}
			if (any == null) {
				any = ep.getProvider();
			}
		}
		if (any != null) {
			return any;
		}
		if (Context.getAuthenticatedUser() == null) {
			return null;
		}
		Collection<Provider> mine = Context.getProviderService().getProvidersByPerson(
		    Context.getAuthenticatedUser().getPerson(), false);
		return mine.isEmpty() ? null : mine.iterator().next();
	}
	
	private static Obs obs(Encounter formEncounter, Concept question) {
		Obs obs = new Obs();
		obs.setConcept(question);
		obs.setPerson(formEncounter.getPatient());
		obs.setObsDatetime(formEncounter.getEncounterDatetime());
		obs.setLocation(formEncounter.getLocation());
		return obs;
	}
	
	private static Concept concept(String uuid) {
		return Context.getConceptService().getConceptByUuid(uuid);
	}
	
	/** For tests: the form answers that are copied under a CIEL vaccine. */
	static Map<String, HistoryDose> mapping() {
		return Collections.unmodifiableMap(BY_FORM_ANSWER);
	}
}
