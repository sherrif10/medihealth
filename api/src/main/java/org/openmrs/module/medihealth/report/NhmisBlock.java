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
import java.util.List;

/**
 * One aggregate query of an NHMIS section: a set of output columns counted over one event source
 * (see {@link NhmisSql}). The events are joined to the person, and each row exposes, as {@code x.*}
 * : {@code patient_id}, {@code event_id}, {@code event_date}, {@code sex} ('M'/'F'), {@code age_m}
 * and {@code age_y} (age in completed months/years at the event), plus any per-event flags added
 * with {@link #with}.
 */
public class NhmisBlock {
	
	public static final String[][] SEXES = { { "M", "male" }, { "F", "female" } };
	
	private final String events;
	
	private final List<String> derived = new ArrayList<String>();
	
	private final List<String> columns = new ArrayList<String>();
	
	public NhmisBlock(String events) {
		this.events = events;
	}
	
	/**
	 * Adds a per-event column {@code x.<alias>}. The expression sees the event as {@code ev} and
	 * its person as {@code p}.
	 */
	public NhmisBlock with(String alias, String expression) {
		derived.add("(" + expression + ") AS " + alias);
		return this;
	}
	
	/** Number of different patients with an event matching {@code condition}. */
	public NhmisBlock patients(String alias, String condition) {
		columns.add("COUNT(DISTINCT CASE WHEN " + condition + " THEN x.patient_id END) AS " + alias);
		return this;
	}
	
	/** {@link #patients} for male, female and total: {@code <prefix>_male/_female/_total}. */
	public NhmisBlock patientsBySex(String prefix, String condition) {
		for (String[] s : SEXES) {
			patients(prefix + "_" + s[1], condition + " AND x.sex = '" + s[0] + "'");
		}
		return patients(prefix + "_total", condition);
	}
	
	/** Number of events (encounters, orders, results) matching {@code condition}. */
	public NhmisBlock events(String alias, String condition) {
		columns.add("COUNT(DISTINCT CASE WHEN " + condition + " THEN x.event_id END) AS " + alias);
		return this;
	}
	
	/** Number of different (patient, day) pairs matching {@code condition}. */
	public NhmisBlock patientDays(String alias, String condition) {
		columns.add("COUNT(DISTINCT CASE WHEN " + condition + " THEN CONCAT(x.patient_id, '/', DATE(x.event_date)) END) AS "
		        + alias);
		return this;
	}
	
	/** Sum of {@code value} over events matching {@code condition} (0 if none). */
	public NhmisBlock sum(String alias, String value, String condition) {
		columns.add("COALESCE(SUM(CASE WHEN " + condition + " THEN " + value + " END), 0) AS " + alias);
		return this;
	}
	
	String toSql() {
		StringBuilder sql = new StringBuilder("(SELECT\n  ");
		sql.append(String.join(",\n  ", columns));
		sql.append("\nFROM (SELECT ev.patient_id, ev.event_id, ev.event_date, p.gender AS sex, "
		        + "TIMESTAMPDIFF(MONTH, p.birthdate, ev.event_date) AS age_m, "
		        + "TIMESTAMPDIFF(YEAR, p.birthdate, ev.event_date) AS age_y");
		for (String d : derived) {
			sql.append(",\n    ").append(d);
		}
		sql.append("\n  FROM (").append(events)
		        .append(") ev\n  JOIN person p ON p.person_id = ev.patient_id AND p.voided = 0) x)");
		return sql.toString();
	}
}
