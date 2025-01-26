package com.rmo.fibu.util;

import java.awt.print.PrinterException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.swing.table.TableModel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Exportieren einer Tabelle nach Excel
 *
 * @author Ruedi
 *
 */
public class ExcelExportKonto {
	// Das Model der Tabelle, immer über getTabelModel lesen
	private TableModel mTableModel;
	// Excel workbook
	private Workbook mWorkbook;
	// for creating formats
	private CreationHelper mCreateHelper;
	// sheet
	private Sheet mSheet1;
	// das verwendete Datumsformat
	private CellStyle mDateFormat;
	// data format wird für number verwendet
	private short mNumberFormat;
	// das verwendete Datumsformat
	private CellStyle mNumberStyle;

	// fett druck
	private Font mFontBold;

	/** Konstruktor mit TableModel für die Datenabfrage */
	public ExcelExportKonto(TableModel theModel) {
		mTableModel = theModel;
	}

	/**
	 * Export eines Kontos starten.
	 *
	 * @throws PrinterException
	 */
	public void doExport(int kontoNr) throws Exception {
		createWorkbook(kontoNr);
		if (mTableModel.getRowCount() <= 0) {
			throw new PrinterException("Keine Konto selektiert");
		}
		addHeader();
		Row lRow = null;
		for (int rowNr = 0; rowNr < mTableModel.getRowCount(); rowNr++) {
			lRow = addRow(rowNr);
		}
		// format summen in der letzten Zeile
		if (lRow.getCell(2).getStringCellValue().startsWith(Config.sSummen)) {
			CellStyle lCellStyle = mWorkbook.createCellStyle();
			lCellStyle.setBorderTop(BorderStyle.THIN);
			lCellStyle.setFont(mFontBold);
			lCellStyle.setDataFormat(mNumberFormat);
			for (int i = 4; i <= 6; i++) {
				if (lRow.getCell(i) != null) {
					lRow.getCell(i).setCellStyle(lCellStyle);
				}
			}
		}
		writeWorkbook(kontoNr);
	}

	/**
	 * Workbook erstellen mit sheet und datumsformat.
	 */
	private void createWorkbook(int kontoNr) {
		mWorkbook = new HSSFWorkbook();
		mSheet1 = mWorkbook.createSheet("Konto" + kontoNr);
		mCreateHelper = mWorkbook.getCreationHelper();
		// init some formats
		mDateFormat = mWorkbook.createCellStyle();
		mDateFormat.setDataFormat(mCreateHelper.createDataFormat().getFormat(Config.sDatumFormat1));
		mNumberStyle = mWorkbook.createCellStyle();
		DataFormat lFormat = mWorkbook.createDataFormat();
		mNumberFormat = lFormat.getFormat("#,##0.00");
		mNumberStyle.setDataFormat(mNumberFormat);
		mFontBold = mWorkbook.createFont();
		mFontBold.setBold(true);
	}

	/**
	 * fügt die überschrift ein.
	 */
	private void addHeader() {
		// format header row
		CellStyle lCellStyleLeft = mWorkbook.createCellStyle();
		lCellStyleLeft.setBorderBottom(BorderStyle.THIN);
		lCellStyleLeft.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		lCellStyleLeft.setFont(mFontBold);
		lCellStyleLeft.setAlignment(HorizontalAlignment.LEFT);
		CellStyle lCellStyleRight = mWorkbook.createCellStyle();
		lCellStyleRight.setBorderBottom(BorderStyle.THIN);
		lCellStyleRight.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		lCellStyleRight.setFont(mFontBold);
		lCellStyleRight.setAlignment(HorizontalAlignment.RIGHT);

		// fill lineValues
		Row row = mSheet1.createRow(0);
		for (int colNr = 0; colNr < mTableModel.getColumnCount(); colNr++) {
			// Create a cell and put a value in it.
			Cell lCell = row.createCell(colNr);
			lCell.setCellValue(mTableModel.getColumnName(colNr));
			// rechts ajustiert
			if (colNr < 3) {
				lCell.setCellStyle(lCellStyleLeft);
			} else {
				lCell.setCellStyle(lCellStyleRight);
			}
		}
	}

	/**
	 * fügt eine neue Zeilen in das Excel sheet ein.
	 *
	 * @param rowNr
	 */
	private Row addRow(int rowNr) {
		// Create a row, read row 0, write row 1 (row 0 is the header)
		Row row = mSheet1.createRow(rowNr + 1);
		// set date and format date
		Cell lCell = row.createCell(0);
		Object value = mTableModel.getValueAt(rowNr, 0);
		if (value != null) {
			lCell.setCellValue((Date) value);
			lCell.setCellStyle(mDateFormat);
		}
		row.createCell(1).setCellValue((String) mTableModel.getValueAt(rowNr, 1));
		row.createCell(2).setCellValue((String) mTableModel.getValueAt(rowNr, 2));
		row.createCell(3).setCellValue((Integer) mTableModel.getValueAt(rowNr, 3));
		createBetrag(row, rowNr, 4);
		createBetrag(row, rowNr, 5);
		createBetrag(row, rowNr, 6);
		return row;
	}

	/**
	 * Setzt das Feld nur, wenn Betrag >= 0
	 *
	 * @param row
	 *            die Zeile
	 * @param rowNr
	 *            die Zeilennummer für den Wert
	 * @param colNr
	 *            die Spalten nrToPrint
	 */
	private void createBetrag(Row row, int rowNr, int colNr) {
		Double lBetrag = (Double) mTableModel.getValueAt(rowNr, colNr);
		// wenn leer wir -1 zurückgegeben
		if ((colNr < 6) && (lBetrag < 0)) {
			return;
		} else {
			Cell lCell = row.createCell(colNr);
			lCell.setCellValue(lBetrag);
			lCell.setCellStyle(mNumberStyle);
		}
	}

	/**
	 * Speichern in Datei.
	 *
	 * @param kontoNr
	 * @throws PrinterException
	 */
	private void writeWorkbook(int kontoNr) throws PrinterException {
		String fileName = "/Konto" + kontoNr + ".xls";
		try {
			String lFileName = Config.sDefaultDir + fileName;
			FileOutputStream fileOut = new FileOutputStream(lFileName);
			mWorkbook.write(fileOut);
			fileOut.close();
		} catch (FileNotFoundException ex) {
			throw new PrinterException(ex.getMessage());
		} catch (IOException ex) {
			throw new PrinterException(ex.getMessage());
		}
	}

	/**
	 * Workbook erstellen mit sheet und datumsformat.
	 */
//	private void createWorkbook(String name) {
//		mWorkbook = new HSSFWorkbook();
//		mSheet1 = mWorkbook.createSheet(name);
//		mCreateHelper = mWorkbook.getCreationHelper();
//		// init some formats
//		mDateFormat = mWorkbook.createCellStyle();
//		mDateFormat.setDataFormat(mCreateHelper.createDataFormat().getFormat(Config.sdatumFormat1));
//		mNumberStyle = mWorkbook.createCellStyle();
//		DataFormat lFormat = mWorkbook.createDataFormat();
//		mNumberFormat = lFormat.getFormat("#,##0.00");
//		mNumberStyle.setDataFormat(mNumberFormat);
//		mFontBold = mWorkbook.createFont();
//		mFontBold.setBoldweight(Font.BOLDWEIGHT_BOLD);
//	}

}
