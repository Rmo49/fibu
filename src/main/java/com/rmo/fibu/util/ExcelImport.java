package com.rmo.fibu.util;

import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.table.TableModel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.rmo.fibu.exception.FibuException;


/**
 * Exportieren einer Tabelle nach Excel
 * 
 * @author Ruedi
 * 
 */
public class ExcelImport {
	// file von dem gelesen wird
	FileInputStream mFileIn = null;
	// Das Model der Tabelle, immer über getTabelModel lesen
	protected TableModel mTableModel;
	// Excel workbook
	protected Workbook mWorkbook;
	// for creating formats
	protected CreationHelper mCreateHelper;
	// sheet
	protected Sheet mSheet1;
	// das verwendete Datumsformat
	protected Row mRow = null;
	

	/** Konstruktor mit TableModel für die Datenabfrage */
	public ExcelImport() {
	}

	/**
	 * Export eines Kontos starten.
	 * 
	 * @throws PrinterException
	 */
	public void doOpen(File file) throws FibuException {
		String fileName = file.getPath();
		openWorkbook(fileName);
	}

	/**
	 * Export eines Kontos starten.
	 * 
	 * @throws PrinterException
	 */
	public void doOpen(String name) throws Exception {
		String fileName = Config.sDefaultDir + "/" + name + ".xls";
		openWorkbook(fileName);
	}

	/**
	 * Export eines Kontos starten.
	 * 
	 * @throws PrinterException
	 */
	public void doClose() throws Exception {
		if (mFileIn != null) {
			mFileIn.close();
		}
	}

	/**
	 * Speichern in Datei.
	 * 
	 * @param kontoNr
	 * @throws PrinterException
	 */
	protected void openWorkbook(String lFileName) throws FibuException {
		try {
			mFileIn = new FileInputStream(lFileName);
			mWorkbook = new HSSFWorkbook(mFileIn);
			mSheet1 = mWorkbook.getSheetAt(0);
		} catch (FileNotFoundException ex) {
			throw new FibuException(ex.getMessage());
		} catch (IOException ex) {
			throw new FibuException(ex.getMessage());
		}
	}
	
	public int getMaxRow() {
		return mSheet1.getLastRowNum();
	}
	
	public String getStringAt(int row, int col) {
		mRow = mSheet1.getRow(row);
		return mRow.getCell(col).getStringCellValue();
	}

	public double getDoubleAt(int row, int col) {
		double value = 0;
		mRow = mSheet1.getRow(row);
		try {
			value = mRow.getCell(col).getNumericCellValue();
		} catch (Exception ex) {
			// problem with negative numbers do nothing
		}
		return value;
	}

}
