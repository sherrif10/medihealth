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

/**
 * Shared SQL fragments for bucketing patients into the age/sex bands used throughout the NHMIS
 * Monthly Summary Form. Ages are computed as of the event date (encounter date, or death date for
 * mortality rows) rather than "today", since the form reports age at time of the event.
 * <p>
 * Callers must alias the person/patient row as {@code p} and the event date column as
 * {@code event_date} in the FROM clause that these fragments are used in.
 */
public class AgeSexBands {
	
	public static final String SEX_CASE_SQL = "CASE p.gender WHEN 'M' THEN 'Male' WHEN 'F' THEN 'Female' ELSE 'Unknown' END";
	
	/**
	 * Age bands used by the General/Out-patient Attendance rows (form rows 1-2): 0-28 days,
	 * 29d-11mths, 12-59mths, 5-9yrs, 10-19yrs, >=20yrs.
	 */
	public static final String ATTENDANCE_AGE_BAND_SQL = ageBandCaseSql("10-19yrs");
	
	/**
	 * Age bands used by the Admissions/Discharges/Mortality rows (form rows 3-9).
	 * <p>
	 * NOTE: the official NHMIS MSF v2019 PDF prints "11-19yrs" for the Male admissions column but
	 * "10-19yrs" for the Female admissions column and every other section of the form (ANC,
	 * mortality, etc). This is almost certainly a typo in the source form - a facility could not
	 * plausibly be expected to report 10-year-old male inpatients under a different rule than every
	 * other category - so we use 10-19yrs uniformly here. Verify with the state M&E/HMIS officer
	 * before relying on this for compliance submissions.
	 */
	public static final String INPATIENT_AGE_BAND_SQL = ageBandCaseSql("10-19yrs");
	
	private static String ageBandCaseSql(String teenBandLabel) {
		return "CASE " + "WHEN DATEDIFF(event_date, p.birthdate) <= 28 THEN '0-28 days' "
		        + "WHEN TIMESTAMPDIFF(MONTH, p.birthdate, event_date) < 12 THEN '29d-11mths' "
		        + "WHEN TIMESTAMPDIFF(MONTH, p.birthdate, event_date) < 60 THEN '12-59mths' "
		        + "WHEN TIMESTAMPDIFF(YEAR, p.birthdate, event_date) < 10 THEN '5-9yrs' "
		        + "WHEN TIMESTAMPDIFF(YEAR, p.birthdate, event_date) < 20 THEN '" + teenBandLabel + "' "
		        + "ELSE '>=20yrs' END";
	}
	
	private AgeSexBands() {
	}
}
