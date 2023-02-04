package com.rmo.fibu.util;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.DefaultListModel;

import com.rmo.fibu.exception.FibuException;

/**
 * Configuration der Fibu. Kann seine Daten vom Config-File einlesen, und
 * schreiben. Ein Teil davon wird in Config-File gespeichert, der ander in
 * der DB.
 */
public class Config {
	/** für Singelton */
//	private static Config sConfig = null;
	/** Config-Filename und File */
	private static final String sConfigFileName = "FibuConfig.txt";
	public static final String sJsonExtension = ".json";
	private static File sConfigFile;
	/** Die Properties, gespeichert in der Config-Datei */
	private static Properties mProperties;
	private static Vector<Object> mPropertyList;

	private static final String sDefaultDirToken = "fibu.default.dir";
	public static String sDefaultDir;
	private static final String sCsvFileNameToken = "fibu.csv.filename";
	public static String sCsvFileName;
	
	// Alle Namen der bisher geöffneten Fibus, wird von Config eingelesen
	private static final String sFibuNamesToken = "fibu.list";
	private static DefaultListModel<String> sFibuNames;
	
	public static boolean sConfigError = false;

	public static int traceLevel = 3;
	private static final String traceLevelToken = "fibu.trace.level";
	public static boolean traceTimestamp;
	private static final String traceTimestampToken = "fibu.trace.timestamp";

	// ----- Printer ----------------------------------------------------------
	private static final String printerRandObenToken = "printer.rand.oben";
	public static float printerRandOben = 42.55F; // Rand Oben in Pixels
	private static final String printerRandLinksToken = "printer.rand.links";
	public static float printerRandLinks = 56.7F;
	private static final String printerPageWidthToken = "printer.page.width";
	public static float printerPageWidth = 496.0F;
	private static final String printerPageHeightToken = "printer.page.height";
	public static float printerPageHeight = 742.64F;
	private static final String printerColAbstandToken = "printer.abstand.col";
	public static float printerColAbstand = 1F; // Abstand zwischen Spalten
	private static final String printerRowAbstandToken = "printer.abstand.row";
	public static float printerRowAbstand = 2F; // Abstand nach Row
	private static final String printerHeaderAbstandToken = "printer.abstand.header";
	public static float printerHeaderAbstand = 5F; // Abstand nach Kopfzeile
	private static final String printerSummeAbstandToken = "printer.abstand.summe";
	public static float printerSummeAbstand = 3F; // Abstand zu Summe

	// --- java.awt.Font
	public static java.awt.Font printerTitelFont = new java.awt.Font("Arial", 0, 14);
	public static java.awt.Font printerNormalFont = new java.awt.Font("Arial", 0, 10);
	public static java.awt.Font fontText;
	public static java.awt.Font fontTextBold;
	

	// ----- Variable pro Buchhaltung ------------------------------------------
	// Der DB-Name der Fibu
	public static String sFibuDbName;
	// Name der Buchhaltung
	public static String sFibuTitel;
	// die Grenze der geöffneten Buchhaltung
	public static Datum sDatumVon = new Datum();
	public static Datum sDatumBis = new Datum();
	public static String sDatumFormat1 = "dd.MM.yyyy";

	// ----- View --------------------------------------------------------------
	// ---- Windows Grösse und Positon
	private static final String sWinBuchung = "window.buchung";	
	public static Point winBuchungLoc;
	public static Dimension winBuchungDim;

	private static final String sWinKontoblatt = "window.kontoblatt";	
	public static Point winKontoblattLoc;
	public static Dimension winKontoblattDim;

	private static final String sWinKontoplan = "window.kontoplan";	
	public static Point winKontoplanLoc;
	public static Dimension winKontoplanDim;

	private static final String sWinBilanzen = "window.bilanzen";	
	public static Point winBilanzenLoc;
	public static Dimension winBilanzenDim;

	private static final String sWinCsvSetup = "window.CsvSetup";	
	public static Point winCsvSetupLoc;
	public static Dimension winCsvSetupDim;

	private static final String sWinCsvReaderKeyword = "window.CsvReaderKeyword";	
	public static Point winCsvReaderKeywordLoc;
	public static Dimension winCsvReaderKeywordDim;

	private static final String sWinCsvReaderBuchung = "window.CsvReaderBuchung";	
	public static Point winCsvReaderBuchungLoc;
	public static Dimension winCsvReaderBuchungDim;
	public static final String sCsvTextLenToken = "fibu.csv.buchungText.length";
	public static int sCsvTextLen = 30;
	

	// --- die Grösse des Textes
	public static final String WindowTextSizeToken = "window.size.text";
	public static int 		windowTextSize;
	public static double 	windowTextMultiplikator;

	// --- language settings
	private static final String languageToken = "language.language";
	public static String languageLanguage;
	private static final String countryToken = "language.country";
	public static String languageCountry;

	// --- database
	private static String mDBname;
	public static final String userNameToken = "db.username";
	public static String userName;
	public static final String passwordToken = "db.password";
	public static String password;
	public static final String dbUrlToken = "db.url";
	public static String dbUrl;

	//----- Steuerung für Kontorahmen, nicht in Config-file
	public static int sBilanzStart = 1000;
	public static int sBilanzEnd = 2999;
	public static int sERStart = 3000;
	public static int sEREnd = 6999;
	public static String sSummen = "Summen";

	/**
	 * Config constructor comment.
	 */
	private Config() {
		mDBname = "FibuLeer";
	}

	/**
	 * Singleton
	 */
//	private static Config getConfig() {
//		if (sConfig == null) {
//			sConfig = new Config();
//		}
//		return sConfig;
//	}

	/** Alle Properties einlesen */
	public static void readProperties() throws FibuException {
		Trace.println(0, "Config.readProperties()");
		if (sConfigFile == null) {
			checkConfigFile(sConfigFileName);
		}
		try {
			FileInputStream inputStream = new FileInputStream(sConfigFile);
			mProperties = new Properties();
			mProperties.load(inputStream);
			inputStream.close();
		} catch (IOException ex) {
			throw new FibuException(ex.getMessage());
		}
		// --- alle Properites einlesen
		int temp = traceLevel;
		temp = readInt(traceLevelToken);
		if (temp > 0) {
			traceLevel = temp;
		}
		mProperties.setProperty(traceLevelToken, Integer.toString(traceLevel));

		traceTimestamp = readBoolean(traceTimestampToken);
		
		sDefaultDir = mProperties.getProperty(sDefaultDirToken);
		if (sDefaultDir == null) {
			sDefaultDir="form: F:/Doku/Ruedi/Java/Fibu";
			mProperties.setProperty(sDefaultDirToken, "form: F:/Doku/Ruedi/Java/Fibu");
		}
		sCsvFileName = mProperties.getProperty(sCsvFileNameToken);
		if (sCsvFileName == null) {
			sCsvFileName="form: f:/doc/Postfinance151001.pdf";
			mProperties.setProperty(sCsvFileNameToken, "form: f:/doc/Postfinance151001.pdf");
		}
		temp = sCsvTextLen;
		temp = readInt(sCsvTextLenToken);
		if (temp > 0) {
			sCsvTextLen = temp;
		}
		mProperties.setProperty(sCsvTextLenToken, Integer.toString(sCsvTextLen));
		
		sFibuNames = readList(mProperties.getProperty(sFibuNamesToken), ",");
		if (sFibuNames == null) {
			sFibuNames = new DefaultListModel<String>();
			sFibuNames.addElement("FibuLeer");
			mProperties.setProperty(sFibuNamesToken, "FibuLeer,");
		}
		
		float tempF = 0;
		tempF = readFloat(printerRandLinksToken);
		if (tempF >= 0F) {
			printerRandLinks = tempF;
		}
		else {
			mProperties.setProperty(printerRandLinksToken, Float.toString(printerRandLinks));
		}
		tempF = readFloat(printerRandObenToken);
		if (tempF >= 0F) {
			printerRandOben = tempF;
		}
		else {
			mProperties.setProperty(printerRandObenToken, Float.toString(printerRandOben));
		}
		tempF = readFloat(printerPageWidthToken);
		if (tempF >= 0F) {
			printerPageWidth = tempF;
		}
		else {
			mProperties.setProperty(printerPageWidthToken, Float.toString(printerPageWidth));
		}
		tempF = readFloat(printerPageHeightToken);
		if (tempF >= 0F) {
			printerPageHeight = tempF;
		}
		else {
			mProperties.setProperty(printerPageHeightToken, Float.toString(printerPageHeight));
		}
		tempF = readFloat(printerColAbstandToken);
		if (tempF >= 0F) {
			printerColAbstand = tempF;
		}
		else {
			mProperties.setProperty(printerColAbstandToken, Float.toString(printerColAbstand));
			
		}
		tempF = readFloat(printerRowAbstandToken);
		if (tempF >= 0F) {
			printerRowAbstand = tempF;
		}
		else {
			mProperties.setProperty(printerRowAbstandToken, Float.toString(printerRowAbstand));
		}		
		tempF = readFloat(printerHeaderAbstandToken);
		if (tempF >= 0F) {
			printerHeaderAbstand = tempF;
		}
		else {
			mProperties.setProperty(printerHeaderAbstandToken, Float.toString(printerHeaderAbstand));
		}		
		tempF = readFloat(printerSummeAbstandToken);
		if (tempF >= 0F) {
			printerSummeAbstand = tempF;
		}
		else {
			mProperties.setProperty(printerSummeAbstandToken, Float.toString(printerSummeAbstand));
		}
		
		// --- Fenster einlesen
		readWindowBuchung();
		readWindowKontoblatt();
		readWindowKontoplan();
		readWindowBilanzen();
		
		readWindowCsvSetup();
		readWindowCsvReaderKeyword();
		readWindowCsvReaderBuchung();
		
		// --- Size von Text, Menu, Buttons
		windowTextSize = readInt(WindowTextSizeToken);
		if (windowTextSize < 1) {
			windowTextSize = 12;
			mProperties.setProperty(WindowTextSizeToken, "12");
		}
		windowTextMultiplikator = (double) windowTextSize / (double) 12;

		// --- language settings
		languageLanguage = mProperties.getProperty(languageToken);
		if (languageLanguage == null) {
			languageLanguage="de";
			mProperties.setProperty(languageToken, "de");
		}
		languageCountry = mProperties.getProperty(countryToken);
		if (languageCountry == null) {
			languageCountry="CH";
			mProperties.setProperty(countryToken, "CH");
		}
		// --- db-connetion
		userName = mProperties.getProperty(userNameToken);
		if (userName == null) {
			userName="root";
			mProperties.setProperty(userNameToken, "root");
		}
		password = mProperties.getProperty(passwordToken);
		if (password == null) {
			password="laura99";
			mProperties.setProperty(passwordToken, "laura99");
		}
		dbUrl = mProperties.getProperty(dbUrlToken);
		if (dbUrl == null) {
			dbUrl="jdbc:mysql://localhost:3307/";
			mProperties.setProperty(dbUrlToken, "jdbc:mysql://localhost:3307/");
		}		
	}

	/** java.awt.Fonts initialisieren */
	public static void initFont() {
		fontText = new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.PLAIN, Config.windowTextSize);
		fontTextBold = new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.BOLD, Config.windowTextSize);
	}
	
	/** Alle Properites in das File schreiben */
	public static void writeProperties() throws FibuException {
		Trace.println(0, "Config.writeProperties()");
		writeWindowBuchung();
		writeWindowKontoblatt();
		writeWindowKontoplan();
		writeWindowBilanzen();
		writeWindowCsvSetup();
		writeWindowCsvReaderKeyword();
		writeWindowCsvReaderBuchung();
		writeList(sFibuNames, ",", sFibuNamesToken);
		mProperties.setProperty(sCsvFileNameToken, sCsvFileName);
		sortPorperties();
		try {
			PrintWriter outputStream = new PrintWriter(sConfigFile);
			outputStream.println("# Fibu Config-Einstellungen");
			outputStream.println("# " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()));
			Iterator<Object> iterProp = mPropertyList.iterator();
			String key = new String();
			while (iterProp.hasNext()) {
				key = (String) iterProp.next();
				outputStream.print(key);
				outputStream.print("=");
				outputStream.println(mProperties.getProperty(key));
			}
			outputStream.flush();
			outputStream.close();
		} catch (IOException ex) {
			throw new FibuException(ex.getMessage());
		}
	}

	/**
	 * Alle keys der Properites sortieren
	 */
	private static void sortPorperties() {
		Enumeration<Object> properityKeys = mProperties.keys();
		mPropertyList = new Vector<Object>();
		while (properityKeys.hasMoreElements()) {
			mPropertyList.add(properityKeys.nextElement());
		}
		
		Collections.sort(mPropertyList, new Comparator <Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		
	}
	

	/**
	 * Alle einträge der Fibu-Liste lesen, diese in das Model der JList
	 * kopieren.
	 */
	private static DefaultListModel<String> readList(String property, String separtor) throws FibuException {
		Trace.println(3, "Config.readList()");
		if (property == null) {
			return null;
		}
		// zuerst Model anlengen
		DefaultListModel<String> stringList = new DefaultListModel<String>();
		StringTokenizer lToken = new StringTokenizer(property, separtor);
		while (lToken.hasMoreTokens()) {
			String zeile = lToken.nextToken().trim();
			if (zeile.length() > 0) {
				stringList.addElement(zeile.trim());
			}
		}
		return stringList;
	}
	
	
	/**
	 * FibuListe in das Property scheiben
	 */
	private static void writeList(DefaultListModel<String> stringList, String separtor, String propertyName) 
			throws FibuException {
		Trace.println(3, "Config.writeList()");
		if (stringList== null || stringList.isEmpty()) {
			mProperties.setProperty(propertyName, "empty");
			return;
		}
		StringBuffer bufferList = new StringBuffer(80);
		for (Enumeration<?> e = stringList.elements(); e.hasMoreElements() ;) {
			String next = (String) e.nextElement();
			bufferList.append(next);
			bufferList.append(separtor);
	     }
		mProperties.setProperty(propertyName, bufferList.toString());
	}


	/**
	 * Position und Grösse des Windows Buchung
	 * @todo generischer lösen
	 */
	private static void readWindowBuchung() throws FibuException {
		winBuchungDim = readWindowDimension(sWinBuchung);
		winBuchungLoc = readWindowPoint(sWinBuchung);
		writeWindowBuchung();
	}

	/**
	 * Position und Grösse des Windows Buchung in das Property scheiben
	 */
	private static void writeWindowBuchung() throws FibuException {
		writeWindowConfig(sWinBuchung, winBuchungLoc, winBuchungDim);
	}

	/**
	 * Position und Grösse des Windows Kotoblatt
	 */
	private static void readWindowKontoblatt() throws FibuException {
		winKontoblattDim = readWindowDimension(sWinKontoblatt);
		winKontoblattLoc = readWindowPoint(sWinKontoblatt);
		writeWindowKontoblatt();
	}

	/**
	 * Position und Grösse des Windows Kontoblatt in das Property schreiben
	 */
	private static void writeWindowKontoblatt() throws FibuException {
		writeWindowConfig(sWinKontoblatt, winKontoblattLoc, winKontoblattDim);
	}

	/**
	 * Position und Grösse des Windows Kotoplan
	 */
	private static void readWindowKontoplan() throws FibuException {
		winKontoplanDim = readWindowDimension(sWinKontoplan);
		winKontoplanLoc = readWindowPoint(sWinKontoplan);
		writeWindowKontoplan();
	}

	/**
	 * Position und Grösse des Windows Kontoplan in das Property schreiben
	 */
	private static void writeWindowKontoplan() throws FibuException {
		writeWindowConfig(sWinKontoplan, winKontoplanLoc, winKontoplanDim);
	}


	/**
	 * Position und Grösse des Windows Kotoplan
	 */
	private static void readWindowBilanzen() throws FibuException {
		winBilanzenDim = readWindowDimension(sWinBilanzen);
		winBilanzenLoc = readWindowPoint(sWinBilanzen);
		writeWindowBilanzen();
	}

	/**
	 * Position und Grösse des Windows Bilanzen in das Property schreiben
	 */
	private static void writeWindowBilanzen() throws FibuException {
		writeWindowConfig(sWinBilanzen, winBilanzenLoc, winBilanzenDim);
	}

	/**
	 * Position und Grösse des Windows 
	 */
	private static void readWindowCsvSetup() throws FibuException {
		winCsvSetupDim = readWindowDimension(sWinCsvSetup);
		winCsvSetupLoc= readWindowPoint(sWinCsvSetup);
		writeWindowCsvSetup();
	}


	/**
	 * Position und Grösse des Windows 
	 */
	private static void readWindowCsvReaderKeyword() throws FibuException {
		winCsvReaderKeywordDim = readWindowDimension(sWinCsvReaderKeyword);
		winCsvReaderKeywordLoc = readWindowPoint(sWinCsvReaderKeyword);
		writeWindowCsvReaderKeyword();
	}

	/**
	 * Position und Grösse des Windows in das Property schreiben
	 */
	private static void writeWindowCsvSetup() throws FibuException {
		writeWindowConfig(sWinCsvSetup, winCsvSetupLoc, winCsvSetupDim);
	}

	/**
	 * Position und Grösse des Windows in das Property schreiben
	 */
	private static void writeWindowCsvReaderKeyword() throws FibuException {
		writeWindowConfig(sWinCsvReaderKeyword, winCsvReaderKeywordLoc, winCsvReaderKeywordDim);
	}

	/**
	 * Position und Grösse des Windows
	 */
	private static void readWindowCsvReaderBuchung() throws FibuException {
		winCsvReaderBuchungDim = readWindowDimension(sWinCsvReaderBuchung);
		winCsvReaderBuchungLoc = readWindowPoint(sWinCsvReaderBuchung);
		writeWindowCsvReaderBuchung();
	}

	/**
	 * Position und Grösse des Windows in das Property schreiben
	 */
	private static void writeWindowCsvReaderBuchung() throws FibuException {
		writeWindowConfig(sWinCsvReaderBuchung, winCsvReaderBuchungLoc, winCsvReaderBuchungDim);
	}


	/**
	 * Die Config für ein Window lesen
	 * @param winName: die Bezeichnung des Windows
	 */
	private static Dimension readWindowDimension(String winName) throws FibuException {
		int width = readInt(winName + ".width");
		int height = readInt(winName + ".height");
		if (width < 0) {
			width = 400;
			height = 600;
		}
		return new Dimension(width, height);
	}

	/**
	 * Die Config für ein Window lesen
	 * @param winName: die Bezeichnung des Windows
	 */
	private static Point readWindowPoint(String winName) throws FibuException {
		int x = readInt(winName + ".x");
		int y = readInt(winName + ".y");
		if (x < 0) {
			x = 10;
			y = 10;
		}
		return new Point(x, y);
	}


	/**
	 * Die Config für ein Window speichern
	 * @param winName: die Bezeichnung des Windwos
	 * @param dim
	 * @param point
	 */
	private static void writeWindowConfig(String winName, Point point, Dimension dim) {
		writeInt(winName + ".x", (int) point.getX());
		writeInt(winName + ".y", (int) point.getY());
		writeInt(winName + ".width", (int) dim.getWidth());
		writeInt(winName + ".height", (int) dim.getHeight());
	}
	

	/** Einen boolean-Werte von den Properties lesen */
	private static boolean readBoolean(String property) throws FibuException {
		String value = mProperties.getProperty(property);
		if (value == null) {
			mProperties.setProperty(property, "FALSE");
			return false;
		}
		if (value.equalsIgnoreCase("TRUE"))
			return true;
		if (value.equalsIgnoreCase("FALSE"))
			return false;
		throw new FibuException("Property: '" + property
				+ "' true oder false erwartet");
	}

	/** Einen int-Werte von den Properties lesen */
	private static int readInt(String property) throws FibuException {
		try {
			String value = mProperties.getProperty(property);
			if (value == null)
				return -1;
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			throw new FibuException("Property: '" + property + "' falsch \n"
					+ "Fehler: " + ex.getMessage());
		}
	}

	/** Einen float-Werte von den Properties lesen */
	private static float readFloat(String property) throws FibuException {
		try {
			String value = mProperties.getProperty(property);
			if (value == null)
				return -1F;
			return Float.parseFloat(value);
		} catch (NumberFormatException ex) {
			throw new FibuException("Property: '" + property
					+ "' falsches Format \n" + "Fehler: " + ex.getMessage());
		}
	}

	/** Einen float-Werte von den Properties lesen */
	// private double readDouble(String property) throws FibuException {
	// try {
	// String value = mProperties.getProperty(property);
	// if (value == null) return 0.0;
	// return Double.parseDouble(value);
	// }
	// catch (NumberFormatException ex) {
	// throw new FibuException ("Property: '" +property +"' falsches Format \n"
	// + "Fehler: " +ex.getMessage());
	// }
	// }

	/** Einen double-Werte in ein Property schreiben */
	// private void writeDouble(String property, double value) throws
	// FibuException {
	// String strValue = Double.toString(value);
	// mProperties.setProperty(property, strValue);
	// }

	/** Einen int-Werte in ein Property schreiben */
	private static void writeInt(String property, int value) {
		String strValue = Integer.toString(value);
		mProperties.setProperty(property, strValue);
	}

	/**
	 * Die Parameter der Applikation überprüfen. Wenn der FileName der
	 * Config-Datei vorhanden ist diesen setzen, sonst im aktuellen dir
	 * nachsehen. Die Properties einlesen
	 */
	public static void checkArgs(String[] args) throws FibuException {
		// ConfigFile öffnen
		String configFilePath;
		if (args.length > 0)
			configFilePath = args[0];
		else
			configFilePath = sConfigFileName;
		checkConfigFile(configFilePath);
	}

	/** prüft, ob das File vorhanden ist */
	private static void checkConfigFile(String configFilePath)
			throws FibuException {
		sConfigFile = new File(configFilePath);
		if (!sConfigFile.exists()) {
			throw new FibuException("ConfigFile nicht gefunden, Path: "
					+ sConfigFile.getAbsolutePath());
		}
	}

	// ------- getter und setter -----------------------
	public static String getDbName() {
		return mDBname;
	}

	public static void setDbName(String dbName) {
		mDBname = dbName;
	}
	
	public static DefaultListModel<String> getFibuList(){
		return sFibuNames;
	}
	
	public static void addFibuToList(String fibuName) {
		sFibuNames.addElement(fibuName);
	}

	/**
	 * Delete element from the list of Fibus
	 * @param fibuName
	 */
	public static void deleteFibuFromList(String fibuName) {
		String element = null;
		for (int index = 0; index < sFibuNames.getSize(); index++) {
			element = sFibuNames.getElementAt(index);
			if (element != null && element.compareTo(fibuName) == 0) {
				sFibuNames.removeElementAt(index);
			}
	     }
	}
	
	/** Adds 1 to a String ending with 0..9.
	 *  Or a letter when endig a..z / A..Z (not implemented) */
	public static String addOne(String pBelegNr) {
		if (pBelegNr == null || pBelegNr.length() <= 0) return null;
		try {
			int i = Integer.parseInt(pBelegNr);
			return Integer.toString(i+1);
		}
		catch (NumberFormatException e) {
			//@todo charakter dazuzählen
			return pBelegNr;
		}
	}


}
