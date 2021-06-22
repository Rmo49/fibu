package com.rmo.fibu.util;

import java.awt.print.PrinterException;

import javax.swing.table.TableModel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;


/**
 * Exportieren einer Tabelle nach Excel
 * 
 * @author Ruedi
 * 
 */
public class ExcelExportKontoPlan extends ExcelExport {

	/** Konstruktor mit TableModel für die Datenabfrage */
	public ExcelExportKontoPlan(TableModel theModel) {
		super (theModel);
	}

	/**
	 * Export des Kontoplan starten (entry point)
	 * 
	 * @throws PrinterException
	 */
	public void doExport(String name) throws Exception {
		createWorkbook(name);
		if (mTableModel.getRowCount() <= 0) {
			throw new PrinterException("Keine Konto selektiert");
		}
		addHeader();
		// Row lRow = null;
		for (int rowNr = 0; rowNr < mTableModel.getRowCount(); rowNr++) {
			//lRow = addRow(rowNr);
			addRow(rowNr);
		}
		writeWorkbook(name);
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
		row.createCell(0).setCellValue((Integer) mTableModel.getValueAt(rowNr, 0));
		row.createCell(1).setCellValue((String) mTableModel.getValueAt(rowNr, 1));
		row.createCell(2).setCellValue((String) mTableModel.getValueAt(rowNr, 2));
		createBetrag(row, rowNr, 3);
		createBetrag(row, rowNr, 4);
		return row;
	}


}
