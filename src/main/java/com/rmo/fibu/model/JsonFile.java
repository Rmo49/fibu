package com.rmo.fibu.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.cliftonlabs.json_simple.Jsoner;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;

/**
 * Verwaltet das JsonFile, kennt Name und Location wo gespeichert wird.
 * @author ruedi
 *
 */
public class JsonFile {
	
	/**
	 * Der FileName des Json-Files
	 */
	private static String getJsonFileName() {
		String lFileName = Config.sDefaultDir + "/" + Config.sFibuDbName + Config.sJsonExtension;
		return lFileName;
	}
	
	/**
	 * Check, ob das File exisitert
	 * @return
	 */
	public static boolean exist() {
		File jsonFile = new File(getJsonFileName());
		return jsonFile.exists();
	}
	
	/**
	 * Das File löschen
	 */
	public static void delete() {
		File jsonFile = new File(getJsonFileName());
		jsonFile.delete();
	}

	
	
	/**
	 * Einlesen alle Daten vom Json file, Zeile um Zeile.
	 */
	public static List<BuchungCsv> readFromFile() {
		Trace.println(5,"JsonFile.readFromFile()");

		FileReader fileIn = null;
		try {
			fileIn = new FileReader(getJsonFileName());
		} catch (FileNotFoundException ex) {
			Trace.println(4, ex.getMessage());
		}
		
		List<BuchungCsv> buchungList = new ArrayList<BuchungCsv>();
		// hier werden die Daten eingelesen, Zeile um Zeile
		BufferedReader br = new BufferedReader(fileIn);
		try {
			String companyName = br.readLine();
			// alle Buchungen in File schreiben
			String json = br.readLine();
			while (json != null) {
				BuchungCsv buchungCsv = new BuchungCsv(json);
				buchungCsv.setCompanyName(companyName);
				buchungList.add(buchungCsv);
				json = br.readLine();
			}
			fileIn.close();
		} catch (IOException ex) {
			Trace.println(4, ex.getMessage());
		}
		Trace.println(5,"JsonFile.readFromFile() => end");		
		return buchungList;
	}
	
	
	/** Die Buchungen in einer Json-Datei speichern
	 */
	public static void saveInFile(String companyName, List<BuchungCsv> buchungList) {
		// file öffnen
		FileWriter fileOut = null;
		try {
			fileOut = new FileWriter(getJsonFileName());
		} catch (FileNotFoundException ex) {
			Trace.println(4, ex.getMessage());
		} catch (IOException ex) {
			Trace.println(4, ex.getMessage());
		}
		
		BufferedWriter bw = new BufferedWriter(fileOut);
		try {
			bw.append(companyName);
			bw.newLine();
			// alle Buchungen in File schreiben
			BuchungCsv buchungCsv = null;
			Iterator<BuchungCsv> iter = buchungList.iterator();
			while (iter.hasNext()) {
				buchungCsv = iter.next();
				String json = Jsoner.serialize(buchungCsv);
				bw.append(json);
				bw.newLine();
			}
			bw.flush();
			fileOut.close();
		} catch (IOException ex) {
			Trace.println(4, ex.getMessage());
		}
	}

}
