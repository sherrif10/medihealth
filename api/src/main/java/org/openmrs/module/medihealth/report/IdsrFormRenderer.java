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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openmrs.Location;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.renderer.RenderingException;
import org.openmrs.module.reporting.report.renderer.ReportTemplateRenderer;

/**
 * Fills the official IDSR workbook's IDSR 002 or 003 sheet (health facility or LGA level, see
 * {@link IdsrForm}) with the counts, and the facility, period and facility totals in the header.
 * The download contains only that sheet.
 */
@Handler
@Localized("reporting.ExcelTemplateRenderer")
public class IdsrFormRenderer extends ReportTemplateRenderer {
	
	/** Design properties set by {@link IdsrReportManager}. */
	public static final String FORM_PROPERTY = "idsrForm";
	
	public static final String PER_FACILITY_PROPERTY = "idsrPerFacility";
	
	/** Global properties for the header fields the EMR does not record. */
	public static final String GP_LGA = "medihealth.idsr.lga";
	
	public static final String GP_STATE = "medihealth.idsr.state";
	
	// 0-based column of the first age band of each measure block, and of its total
	private static final int COL_C = 2, COL_J = 9, COL_K = 10, COL_R = 17, COL_S = 18, COL_T = 19, COL_Z = 25, COL_AA = 26;
	
	@Override
	public void render(ReportData results, String argument, OutputStream out) throws IOException, RenderingException {
		ReportDesign design = getDesign(argument);
		IdsrForm form = IdsrForm.valueOf(design.getPropertyValue(FORM_PROPERTY, IdsrForm.WEEKLY_002.name()));
		boolean perFacility = Boolean.parseBoolean(design.getPropertyValue(PER_FACILITY_PROPERTY, "true"));
		XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(getTemplate(design).getContents()));
		Sheet sheet = wb.getSheet(form.getSheet(perFacility));
		if (sheet == null) {
			throw new RenderingException("The IDSR template has no sheet " + form.getSheet(perFacility));
		}
		fillCounts(sheet, form, counts(results.getDataSets().get("cells")));
		fillHeader(sheet, form, perFacility, results);
		
		// the download is this one form: drop the workbook's other sheets (POI also drops the
		// calculation chain, so Excel does not offer to repair the file)
		for (int i = wb.getNumberOfSheets() - 1; i >= 0; i--) {
			if (wb.getSheetAt(i) != sheet) {
				removeSheet(wb, i);
			}
		}
		removeNavigation((XSSFSheet) sheet);
		// print the whole form (to the signature line) on one landscape page
		sheet.setFitToPage(true);
		sheet.getPrintSetup().setLandscape(true);
		sheet.getPrintSetup().setFitWidth((short) 1);
		sheet.getPrintSetup().setFitHeight((short) 1);
		wb.setPrintArea(0, 0, form == IdsrForm.MONTHLY_003 ? 26 : 25, 0, 29);
		sheet.setSelected(true);
		wb.setActiveSheet(0);
		wb.setFirstVisibleTab(0);
		wb.setForceFormulaRecalculation(true);
		wb.write(out);
	}
	
	/**
	 * Clears the template's Home/Prev/Next/End/Exit buttons (row 4, B-G): they link to sheets of
	 * the source workbook ("LGA1", "Control Sheet") that are not in the download.
	 */
	private static void removeNavigation(XSSFSheet sheet) {
		Row row = sheet.getRow(3);
		if (row == null) {
			return;
		}
		for (int c = 1; c <= 6; c++) {
			sheet.removeHyperlink(3, c);
			Cell cell = row.getCell(c);
			if (cell != null) {
				row.removeCell(cell);
			}
		}
		// POI 3.12 only rewrites the hyperlinks element when some remain, so clear it ourselves
		if (sheet.getNumHyperlinks() == 0 && sheet.getCTWorksheet().isSetHyperlinks()) {
			sheet.getCTWorksheet().unsetHyperlinks();
		}
	}
	
	/**
	 * Removes a sheet and the parts only it uses (its Excel table, printer settings): POI removes
	 * the sheet itself but would leave those parts in the file.
	 */
	private static void removeSheet(XSSFWorkbook wb, int index) throws IOException {
		PackagePart sheetPart = wb.getSheetAt(index).getPackagePart();
		List<PackagePartName> parts = new ArrayList<PackagePartName>();
		try {
			for (PackageRelationship rel : sheetPart.getRelationships()) {
				if (rel.getTargetMode() == TargetMode.INTERNAL) {
					parts.add(PackagingURIHelper.createPartName(PackagingURIHelper.resolvePartUri(sheetPart.getPartName()
					        .getURI(), rel.getTargetURI())));
				}
			}
		}
		catch (InvalidFormatException e) {
			throw new IOException("Unable to read the parts of sheet " + index, e);
		}
		wb.removeSheetAt(index);
		OPCPackage pkg = wb.getPackage();
		for (PackagePartName part : parts) {
			if (pkg.containPart(part)) {
				pkg.removePart(part);
			}
		}
	}
	
	/** "rowKey|measure|ageBand" mapped to the number of patients. */
	private static Map<String, Integer> counts(DataSet cells) {
		Map<String, Integer> counts = new HashMap<String, Integer>();
		if (cells != null) {
			for (DataSetRow row : cells) {
				Object n = row.getColumnValue("patients");
				counts.put(
				    row.getColumnValue("row_key") + "|" + row.getColumnValue("measure") + "|"
				            + row.getColumnValue("age_band"), n == null ? 0 : ((Number) n).intValue());
			}
		}
		return counts;
	}
	
	private static void fillCounts(Sheet sheet, IdsrForm form, Map<String, Integer> counts) {
		for (Map.Entry<String, Integer> entry : form.getRows().entrySet()) {
			String key = entry.getKey();
			int r = entry.getValue() - 1;
			if (form == IdsrForm.WEEKLY_002) {
				block(sheet, r, COL_C, COL_J, key, IdsrForm.SUSPECTED, counts);
				block(sheet, r, COL_K, COL_R, key, IdsrForm.CONFIRMED, counts);
				block(sheet, r, COL_S, COL_Z, key, IdsrForm.DEATHS, counts);
			} else {
				int outpatients = block(sheet, r, COL_C, COL_J, key, IdsrForm.OUTPATIENT, counts);
				int inpatients = block(sheet, r, COL_K, COL_R, key, IdsrForm.INPATIENT, counts);
				set(sheet, r, COL_S, outpatients + inpatients);
				block(sheet, r, COL_T, COL_AA, key, IdsrForm.DEATHS, counts);
			}
		}
	}
	
	/** Writes the seven age bands of one measure starting at {@code firstCol}, and their total. */
	private static int block(Sheet sheet, int r, int firstCol, int totalCol, String key, String measure,
	        Map<String, Integer> counts) {
		int total = 0;
		for (int i = 0; i < IdsrForm.AGE_BANDS.size(); i++) {
			Integer n = counts.get(key + "|" + measure + "|" + IdsrForm.AGE_BANDS.get(i));
			int value = n == null ? 0 : n;
			total += value;
			set(sheet, r, firstCol + i, value);
		}
		set(sheet, r, totalCol, total);
		return total;
	}
	
	private void fillHeader(Sheet sheet, IdsrForm form, boolean perFacility, ReportData results) {
		Map<String, Object> params = results.getContext().getParameterValues();
		Date start = (Date) params.get("startDate");
		Date end = (Date) params.get("endDate");
		Location facility = params.get("location") instanceof Location ? (Location) params.get("location") : null;
		String lga = firstNonBlank(facility == null ? null : facility.getCountyDistrict(), globalProperty(GP_LGA, ""));
		String state = firstNonBlank(facility == null ? null : facility.getStateProvince(), globalProperty(GP_STATE, "Oyo"));
		String period = form == IdsrForm.WEEKLY_002 ? weekLabel(start, end) : monthLabel(start, end);
		String year = start == null ? "" : new SimpleDateFormat("yyyy").format(start);
		
		set(sheet, 5, 2, perFacility ? (facility == null ? "" : facility.getName()) : lga);
		set(sheet, 7, 2, period);
		if (perFacility) {
			// health facility sheets: LGA and State after their labels; Year after its label
			if (form == IdsrForm.WEEKLY_002) {
				set(sheet, 5, 6, lga); // G6
				set(sheet, 5, 10, state); // K6
				set(sheet, 7, 6, year); // G8
			} else {
				set(sheet, 5, 7, lga); // H6
				set(sheet, 5, 11, state); // L6
				set(sheet, 7, 7, year); // H8
			}
			return;
		}
		// LGA sheets: State, total health facilities, and those with any activity in the period
		set(sheet, 5, 6, state); // G6
		set(sheet, 7, 6, year); // G8
		DataSet header = results.getDataSets().get("header");
		if (header != null && header.iterator().hasNext()) {
			DataSetRow row = header.iterator().next();
			int total = ((Number) row.getColumnValue("total_hfs")).intValue();
			int reporting = ((Number) row.getColumnValue("reporting_hfs")).intValue();
			set(sheet, 5, 13, total); // N6
			set(sheet, 7, 13, reporting); // N8: health facilities with data in the period
			set(sheet, 7, 18, 0); // S8: late reports are not tracked in the EMR
		}
	}
	
	/** e.g. "Week 45: 02/11/2026 - 08/11/2026" (ISO week of the start date). */
	static String weekLabel(Date start, Date end) {
		if (start == null) {
			return "";
		}
		Calendar c = Calendar.getInstance();
		c.setFirstDayOfWeek(Calendar.MONDAY);
		c.setMinimalDaysInFirstWeek(4);
		c.setTime(start);
		SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
		return "Week " + c.get(Calendar.WEEK_OF_YEAR) + ": " + f.format(start) + (end == null ? "" : " - " + f.format(end));
	}
	
	/** e.g. "November", or "October - November" when the period spans months. */
	static String monthLabel(Date start, Date end) {
		if (start == null) {
			return "";
		}
		SimpleDateFormat f = new SimpleDateFormat("MMMM");
		String first = f.format(start);
		String last = end == null ? first : f.format(end);
		return first.equals(last) ? first : first + " - " + last;
	}
	
	private static String globalProperty(String name, String defaultValue) {
		return Context.getAdministrationService().getGlobalProperty(name, defaultValue);
	}
	
	private static String firstNonBlank(String a, String b) {
		return StringUtils.isNotBlank(a) ? a : b;
	}
	
	private static Cell cell(Sheet sheet, int r, int c) {
		Row row = sheet.getRow(r);
		if (row == null) {
			row = sheet.createRow(r);
		}
		Cell cell = row.getCell(c);
		return cell == null ? row.createCell(c) : cell;
	}
	
	private static void set(Sheet sheet, int r, int c, int value) {
		Cell cell = cell(sheet, r, c);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC); // replaces the template's SUM formulas with the value
		cell.setCellValue(value);
	}
	
	private static void set(Sheet sheet, int r, int c, String value) {
		Cell cell = cell(sheet, r, c);
		cell.setCellType(Cell.CELL_TYPE_STRING);
		cell.setCellValue(value);
	}
}
