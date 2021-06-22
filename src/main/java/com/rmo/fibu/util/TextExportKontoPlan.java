package com.rmo.fibu.util;

import java.awt.print.PrinterException;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.table.TableModel;



/**
 * Exportieren einer Tabelle in ein Text file.
 * 
 * @author Ruedi
 * 
 */
public class TextExportKontoPlan extends ExcelExport {

	private FileWriter mFileWriter;
	private String mTrennZeichen = new String(",");
	private String mNewLine = new String("\n");

	/** Konstruktor mit TableModel für die Datenabfrage */
	public TextExportKontoPlan(TableModel theModel) {
		super (theModel);
	}

	/**
	 * Export des Kontoplan starten (entry point)
	 * 
	 * @throws PrinterException
	 */
	public void doExport(String name) throws Exception {
		createFile(name);
		if (mTableModel.getRowCount() <= 0) {
			throw new PrinterException("Keine Konto selektiert");
		}
		addHeader();
		// Row lRow = null;
		for (int rowNr = 0; rowNr < mTableModel.getRowCount(); rowNr++) {
			//lRow = addRow(rowNr);
			addRow(rowNr);
		}
		mFileWriter.close();
	}

	/**
	 * Textfile erstellen erstellen mit sheet und datumsformat.
	 */
	private void createFile(String name) throws PrinterException {		
		String fileName = "/" + name + ".txt";
		try {
			String lFileName = Config.sDefaultDir + fileName;
			mFileWriter = new FileWriter(lFileName);
		} catch (IOException ex) {
			throw new PrinterException(ex.getMessage());
		} 
	}

	/**
	 * fügt die überschrift ein.
	 */
	private void addHeader() throws IOException {
		// fill lineValues
		StringBuffer zeile = new StringBuffer(128);
		for (int colNr = 0; colNr < mTableModel.getColumnCount(); colNr++) {
			// schreibe werte
			zeile.append(mTableModel.getColumnName(colNr));
			zeile.append(mTrennZeichen);
		}
		zeile.append(mNewLine);
		mFileWriter.write(zeile.toString());
	}

	/**
	 * Schreibt eine neue Zeile in das File
	 * 
	 * @param rowNr
	 */
	private void addRow(int rowNr) throws IOException {
		StringBuffer zeile = new StringBuffer(128);
		// set date and format date
		zeile.append(mTableModel.getValueAt(rowNr, 0));
		zeile.append(mTrennZeichen);
		zeile.append((String) mTableModel.getValueAt(rowNr, 1));
		zeile.append(mTrennZeichen);
		zeile.append((String) mTableModel.getValueAt(rowNr, 2));
		zeile.append(mTrennZeichen);
		Double lBetrag = (Double) mTableModel.getValueAt(rowNr, 3);
		zeile.append(lBetrag);
		zeile.append(mTrennZeichen);
		lBetrag = (Double) mTableModel.getValueAt(rowNr, 4);
		zeile.append(lBetrag);
		zeile.append(mNewLine);
		mFileWriter.write(zeile.toString());
	}


}
