package com.rmo.fibu.util;

import java.awt.print.PrinterException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.table.TableModel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


/**
 * Exportieren einer Tabelle nach Excel
 * 
 * @author Ruedi
 * 
 */
public class ExcelExport {
	// Das Model der Tabelle, immer über getTabelModel lesen
	protected TableModel mTableModel;
	// Excel workbook
	protected Workbook mWorkbook;
	// for creating formats
	protected CreationHelper mCreateHelper;
	// sheet
	protected Sheet mSheet1;
	// das verwendete Datumsformat
	protected CellStyle mDateFormat;
	// data format wird für number verwendet
	protected short mNumberFormat;
	// das verwendete Datumsformat
	protected CellStyle mNumberStyle;

	// fett druck
	protected Font mFontBold;

	/** Konstruktor mit TableModel für die Datenabfrage */
	protected ExcelExport(TableModel theModel) {
		mTableModel = theModel;
	}


	/**
	 * Setzt das Feld nur, wenn Betrag >= 0
	 * 
	 * @param row
	 *            die Zeile
	 * @param rowNr
	 *            die Zeilennummer für den Wert
	 * @param colNr
	 *            die Spalten nummer
	 */
	protected void createBetrag(Row row, int rowNr, int colNr) {
		Double lBetrag = (Double) mTableModel.getValueAt(rowNr, colNr);
		Cell lCell = row.createCell(colNr);
		lCell.setCellValue(lBetrag);
		lCell.setCellStyle(mNumberStyle);
	}

	/**
	 * Speichern in Datei.
	 * 
	 * @param kontoNr
	 * @throws PrinterException
	 */
	protected void writeWorkbook(int kontoNr) throws PrinterException {
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
	protected void createWorkbook(String name) {
		mWorkbook = new HSSFWorkbook();
		mSheet1 = mWorkbook.createSheet(name);
		mCreateHelper = mWorkbook.getCreationHelper();
		// init some formats
		mDateFormat = mWorkbook.createCellStyle();
		mDateFormat.setDataFormat(mCreateHelper.createDataFormat().getFormat("dd.mm.yyyy"));
		mNumberStyle = mWorkbook.createCellStyle();
		DataFormat lFormat = mWorkbook.createDataFormat();
		mNumberFormat = lFormat.getFormat("#,##0.00");
		mNumberStyle.setDataFormat(mNumberFormat);
		mFontBold = mWorkbook.createFont();
		mFontBold.setBold(true);
	}

	/**
	 * Speichern in Datei.
	 * 
	 * @param kontoNr
	 * @throws PrinterException
	 */
	protected void writeWorkbook(String name) throws PrinterException {
		String fileName = "/" + name + ".xls";
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

}
