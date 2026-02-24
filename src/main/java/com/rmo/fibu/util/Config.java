package com.rmo.fibu.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.DefaultListModel;

import com.rmo.fibu.exception.FibuException;

/**
 * Configuration der Fibu. Kann seine Daten vom Config-File einlesen, und
 * schreiben. Ein Teil davon wird in Config-File gespeichert, der ander in der
 * DB.
 */
public class Config {
	/** für Singelton */
//	private static Config sConfig = null;
	/** Config-Filename und File */
	private static final String sConfigFileName = "FibuConfig.txt";
	public static final String sJsonExtension = ".json";
	private static File sConfigFile;
	/** Die Properties, gespeichert in der Config-Datei */
	private static PropertiesFibu mProperties;
	private static Vector<Object> mPropertyList;

	private static final String sDefaultDirToken = "fibu.default.dir";
	public static String sDefaultDir = "form: F:/Doku/Ruedi/Java/Fibu";
	private static final String sCsvFileNameToken = "fibu.csv.filename";
	public static String sCsvFileName = "form: F:/doc/Postfinance151001.pdf";

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
	public static double printerPageWidth =  (8.27 * 72); //(1 Zoll = 72 Punkte) 210 mm;
	private static final String printerPageHeightToken = "printer.page.height";
	public static double printerPageHeight = 11.69 * 72; // 297 mm;
	private static final String printerColAbstandToken = "printer.abstand.col";
	public static float printerColAbstand = 1F; // Abstand zwischen Spalten
	private static final String printerRowAbstandToken = "printer.abstand.row";
	public static float printerRowAbstand = 2F; // Abstand nach Row
	private static final String printerHeaderAbstandToken = "printer.abstand.header";
	public static float printerHeaderAbstand = 5F; // Abstand nach Kopfzeile
	private static final String printerSummeAbstandToken = "printer.abstand.summe";
	public static float printerSummeAbstand = 3F; // Abstand zu Summe
	// KontoListe
	private static final String printerKtoCol1Token = "printer.kto.col1";
	public static int printerKtoCol1 = 16; // Breite der Spalte
	private static final String printerKtoCol2Token = "printer.kto.col2";
	public static int printerKtoCol2 = 10; // Breite der Spalte
	private static final String printerKtoCol3Token = "printer.kto.col3";
	public static int printerKtoCol3 = 50; // Breite der Spalte
	private static final String printerKtoCol4Token = "printer.kto.col4";
	public static int printerKtoCol4 = 10; // Breite der Spalte
	private static final String printerKtoCol5Token = "printer.kto.col5";
	public static int printerKtoCol5 = 20; // Breite der Spalte
	private static final String printerKtoCol6Token = "printer.kto.col6";
	public static int printerKtoCol6 = 20; // Breite der Spalte
	private static final String printerKtoCol7Token = "printer.kto.col7";
	public static int printerKtoCol7 = 20; // Breite der Spalte
	// bilanz
	private static final String printerBilanzCol1Token = "printer.bilanz.col1";
	public static int printerBilanzCol1 = 7; // Breite der Spalte
	private static final String printerBilanzCol2Token = "printer.bilanz.col2";
	public static int printerBilanzCol2 = 50; // Breite der Spalte
	private static final String printerBilanzCol3Token = "printer.bilanz.col3";
	public static int printerBilanzCol3 = 12; // Breite der Spalte
	private static final String printerBilanzCol4Token = "printer.bilanz.col4";
	public static int printerBilanzCol4 = 12; // Breite der Spalte
	// Journal
	private static final String printerJournalCol1Token = "printer.journal.col1";
	public static int printerJournalCol1 = 10; // Breite der Spalte
	private static final String printerJournalCol2Token = "printer.journal.col2";
	public static int printerJournalCol2 = 8; // Breite der Spalte
	private static final String printerJournalCol3Token = "printer.journal.col3";
	public static int printerJournalCol3 = 50; // Breite der Spalte
	private static final String printerJournalCol4Token = "printer.journal.col4";
	public static int printerJournalCol4 = 10; // Breite der Spalte
	private static final String printerJournalCol5Token = "printer.journal.col5";
	public static int printerJournalCol5 = 10; // Breite der Spalte
	private static final String printerJournalCol6Token = "printer.journal.col6";
	public static int printerJournalCol6 = 20; // Breite der Spalte

	// --- java.awt.Font
	public static java.awt.Font printerTitelFont = new java.awt.Font("Arial", 0, 14);
	public static java.awt.Font printerNormalFont = new java.awt.Font("Arial", 0, 10);
	public static java.awt.Font printerFontBold = new java.awt.Font("Arial", Font.BOLD, 10);
	public static java.awt.Font fontText;
	public static java.awt.Font fontTextBold;
	public static final double printerPaperSizeWidth = 595.3;
	public static final double printerPaperSizeHeigth = 841.9;

	// ----- Variable pro Buchhaltung ------------------------------------------
	// Der DB-Name der Fibu
	public static String sFibuDbName;
	// Name der Buchhaltung die gerade geöffnet ist
	public static String sFibuTitel;
	// die Grenze der geöffneten Buchhaltung
	public static Datum sDatumVon = new Datum();
	public static Datum sDatumBis = new Datum();
	public static String sDatumFormat1 = "dd.MM.yyyy";
	/** Format für Beträge */
	public static final DecimalFormat sDecimalFormat = new DecimalFormat("###,###,##0.00");


	// ----- View --------------------------------------------------------------
	// ---- Windows Grösse und Positon
	private static final String sWinBuchung = "window.buchung";
	public static Point winBuchungLoc;
	public static Dimension winBuchungDim;

	private static final String sWinKontoblatt = "window.kontoblatt";
	public static Point winKontoblattLoc;
	public static Dimension winKontoblattDim;

	private static final String sWinKontoBuchung = "window.kontoBuchung";
	public static Point winKontoBuchungLoc;
	public static Dimension winKontoBuchungDim;

	private static final String sWinKontoplan = "window.kontoplan";
	public static Point winKontoplanLoc;
	public static Dimension winKontoplanDim;

	private static final String sWinBilanzen = "window.bilanzen";
	public static Point winBilanzenLoc;
	public static Dimension winBilanzenDim;

	private static final String sWinCsvSetup = "window.CsvSetup";
	public static Point winCsvSetupLoc;
	public static Dimension winCsvSetupDim;

	private static final String sWinPdfSetup = "window.PdfSetup";
	public static Point winPdfSetupLoc;
	public static Dimension winPdfSetupDim;

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
	public static int windowTextSize = 12;
	public static double windowTextMultiplikator;

	// --- language settings
	private static final String languageToken = "language.language";
	public static String  languageLanguage = "de";
	private static final String countryToken = "language.country";
	public static String  languageCountry = "CH";

	// --- database
	private static String mDBname;
	public static final String userNameToken = "db.username";
	public static String userName = "root";
	public static final String passwordToken = "db.password";
	public static String password = "RudMar49";
	public static final String dbUrlToken = "db.url";
	public static String dbUrl = "jdbc:mysql://localhost:3306/";

	// ----- Steuerung für Kontorahmen, nicht in Config-file
	public static int sBilanzStart = 1000;
	public static int sBilanzEnd = 2999;
	public static int sERStart = 3000;
	public static int sEREnd = 6999;
	public static String sSummen = "Total";

	/**
	 * Config constructor comment.
	 */
	private Config() {
		mDBname = "FibuLeer";
	}


	/** Alle Properties einlesen */
	public static void readPropertyFile() throws FibuException {
		Trace.println(1, "Config.readProperties()");
		if (sConfigFile == null) {
			checkConfigFile(sConfigFileName);
		}
		try {
			mProperties = new PropertiesFibu();
			FileInputStream inputStream = new FileInputStream(sConfigFile);
			mProperties.load(inputStream);
			inputStream.close();
		} catch (IOException ex) {
			throw new FibuException(ex.getMessage());
		}
	}

	public static void setAllProperties() throws FibuException {
		Trace.println(2, "Config.setAllProperties()");
		// --- alle Property werte setzen
		traceLevel = readNumber(traceLevelToken, traceLevel);

		traceTimestamp = readBoolean(traceTimestampToken, traceTimestamp);
		sDefaultDir = mProperties.getProperty(sDefaultDirToken, sDefaultDir);
		sCsvFileName = mProperties.getProperty(sCsvFileNameToken, sCsvFileName);
		sCsvTextLen = readNumber(sCsvTextLenToken, sCsvTextLen);

		sFibuNames = readList(mProperties.getProperty(sFibuNamesToken), ",");
		if (sFibuNames == null) {
			sFibuNames = new DefaultListModel<>();
			sFibuNames.addElement("FibuLeer");
			mProperties.setProperty(sFibuNamesToken, "FibuLeer,");
		}

		printerRandLinks = readNumber(printerRandLinksToken, printerRandLinks);
		printerRandOben = readNumber(printerRandObenToken, printerRandOben);
		printerPageWidth= readNumber(printerPageWidthToken, printerPageWidth);
		printerPageHeight = readNumber(printerPageHeightToken, printerPageHeight);
		printerColAbstand= readNumber(printerColAbstandToken, printerColAbstand);
		printerRowAbstand = readNumber(printerRowAbstandToken, printerRowAbstand);
		printerHeaderAbstand = readNumber(printerHeaderAbstandToken, printerHeaderAbstand);
		printerSummeAbstand = readNumber(printerSummeAbstandToken, printerSummeAbstand);
		// KontoListe ausgeben
		printerKtoCol1 = readNumber(printerKtoCol1Token, printerKtoCol1);
		printerKtoCol2 = readNumber(printerKtoCol2Token, printerKtoCol2);
		printerKtoCol3 = readNumber(printerKtoCol3Token, printerKtoCol3);
		printerKtoCol4 = readNumber(printerKtoCol4Token, printerKtoCol4);
		printerKtoCol5 = readNumber(printerKtoCol5Token, printerKtoCol5);
		printerKtoCol6 = readNumber(printerKtoCol6Token, printerKtoCol6);
		printerKtoCol7 = readNumber(printerKtoCol7Token, printerKtoCol7);
		// Bilanz
		printerBilanzCol1 = readNumber(printerBilanzCol1Token, printerBilanzCol1);
		printerBilanzCol2 = readNumber(printerBilanzCol2Token, printerBilanzCol2);
		printerBilanzCol3 = readNumber(printerBilanzCol3Token, printerBilanzCol3);
		printerBilanzCol4 = readNumber(printerBilanzCol4Token, printerBilanzCol4);
		// journal
		printerJournalCol1 = readNumber(printerJournalCol1Token, printerJournalCol1);
		printerJournalCol2 = readNumber(printerJournalCol2Token, printerJournalCol2);
		printerJournalCol3 = readNumber(printerJournalCol3Token, printerJournalCol3);
		printerJournalCol4 = readNumber(printerJournalCol4Token, printerJournalCol4);
		printerJournalCol5 = readNumber(printerJournalCol5Token, printerJournalCol5);
		printerJournalCol6 = readNumber(printerJournalCol6Token, printerJournalCol6);

		// --- Fenster einlesen
		readWindowBuchung();
		readWindowKontoblatt();
		readWindowKontoBuchung();
		readWindowKontoplan();
		readWindowBilanzen();

		readWindowCsvSetup();
		readWindowPdfSetup();
		readWindowCsvReaderKeyword();
		readWindowCsvReaderBuchung();

		// --- Size von Text, Menu, Buttons
		windowTextSize = readNumber(WindowTextSizeToken, windowTextSize);
		windowTextMultiplikator = (double) windowTextSize / (double) 12;

		// --- language settings
		languageLanguage = mProperties.getProperty(languageToken, languageLanguage);
		languageCountry = mProperties.getProperty(countryToken, languageCountry);

		// --- db-connetion
		userName = mProperties.getProperty(userNameToken, userName);
		password = mProperties.getProperty(passwordToken, password);
		dbUrl = mProperties.getProperty(dbUrlToken, dbUrl);
	}

	/** java.awt.Fonts initialisieren */
	public static void initFont() {
		fontText = new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.PLAIN, Config.windowTextSize);
		fontTextBold = new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.BOLD, Config.windowTextSize);
	}

	/** Alle Properites in das File schreiben */
	public static void saveProperties() throws FibuException {
		Trace.println(0, "Config.writeProperties()");
		// alles in
		writeWindowBuchung();
		writeWindowKontoblatt();
		writeWindowKontoBuchung();
		writeWindowKontoplan();
		writeWindowBilanzen();
		writeWindowCsvSetup();
		writeWindowPdfSetup();
		writeWindowCsvReaderKeyword();
		writeWindowCsvReaderBuchung();
		writeList(sFibuNames, ",", sFibuNamesToken);
		mProperties.setProperty(sCsvFileNameToken, sCsvFileName);
		// sortieren
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
				String test = mProperties.getProperty(key);
				outputStream.println(test);
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
		mPropertyList = new Vector<>();
		while (properityKeys.hasMoreElements()) {
			mPropertyList.add(properityKeys.nextElement());
		}

		Collections.sort(mPropertyList, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

	}

	/**
	 * Alle einträge der Fibu-Liste lesen, diese in das Model der JList kopieren.
	 */
	private static DefaultListModel<String> readList(String property, String separtor) throws FibuException {
		Trace.println(3, "Config.readList() of Fibus");
		if (property == null) {
			return null;
		}
		// zuerst Model anlengen
		DefaultListModel<String> stringList = new DefaultListModel<>();
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
		if (stringList == null || stringList.isEmpty()) {
			mProperties.setProperty(propertyName, "empty");
			return;
		}
		StringBuffer bufferList = new StringBuffer(80);
		for (Enumeration<?> e = stringList.elements(); e.hasMoreElements();) {
			String next = (String) e.nextElement();
			bufferList.append(next);
			bufferList.append(separtor);
		}
		mProperties.setProperty(propertyName, bufferList.toString());
	}

	/**
	 * Position und Grösse des Windows Buchung
	 *
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
	 * Position und Grösse des Windows
	 */
	private static void readWindowKontoBuchung() throws FibuException {
		winKontoBuchungDim = readWindowDimension(sWinKontoBuchung);
		winKontoBuchungLoc = readWindowPoint(sWinKontoBuchung);
		writeWindowKontoBuchung();
	}

	/**
	 * Position und Grösse des Windows in das Property schreiben
	 */
	private static void writeWindowKontoBuchung() throws FibuException {
		writeWindowConfig(sWinKontoBuchung, winKontoBuchungLoc, winKontoBuchungDim);
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
		winCsvSetupLoc = readWindowPoint(sWinCsvSetup);
		writeWindowCsvSetup();
	}

	/**
	 * Position und Grösse des Windows in das Property schreiben
	 */
	private static void writeWindowCsvSetup() throws FibuException {
		writeWindowConfig(sWinCsvSetup, winCsvSetupLoc, winCsvSetupDim);
	}

	/**
	 * Position und Grösse des Windows
	 */
	private static void readWindowPdfSetup() throws FibuException {
		winPdfSetupDim = readWindowDimension(sWinPdfSetup);
		winPdfSetupLoc = readWindowPoint(sWinPdfSetup);
		writeWindowPdfSetup();
	}

	/**
	 * Position und Grösse des Windows in das Property schreiben
	 */
	private static void writeWindowPdfSetup() throws FibuException {
		writeWindowConfig(sWinPdfSetup, winPdfSetupLoc, winPdfSetupDim);
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
	 *
	 * @param winName: die Bezeichnung des Windows
	 */
	private static Dimension readWindowDimension(String winName) throws FibuException {
		int width = readNumber(winName + ".width", 600);
		int height = readNumber(winName + ".height", 400);
		if (width < 0) {
			width = 600;
			height = 400;
		}
		return new Dimension(width, height);
	}

	/**
	 * Die Config für ein Window lesen
	 *
	 * @param winName: die Bezeichnung des Windows
	 */
	private static Point readWindowPoint(String winName) throws FibuException {
		int x = readNumber(winName + ".x", 10);
		int y = readNumber(winName + ".y", 10);
		if (x < 0) {
			x = 10;
			y = 10;
		}
		return new Point(x, y);
	}

	/**
	 * Die Config für ein Window speichern
	 *
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
	private static boolean readBoolean(String property, boolean defaultValue) throws FibuException {
		String defValue = Boolean.toString(defaultValue);
		String value = mProperties.getProperty(property, defValue);
		if (value == null) {
			mProperties.setProperty(property, "FALSE");
			return false;
		}
		if (value.equalsIgnoreCase("true")) {
			return true;
		}
		if (value.equalsIgnoreCase("false")) {
			return false;
		}
		throw new FibuException("Property: '" + property + "' true oder false erwartet");
	}

	/** Einen int-Werte von den Properties lesen */
	private static int readNumber(String property, int defaultValue) throws FibuException {
		try {
			String defValue = Integer.toString(defaultValue);
			String value = mProperties.getProperty(property, defValue);
			if (value == null) {
				return -1;
			}
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			throw new FibuException("Property: '" + property + "' falsch \n" + "Fehler: " + ex.getMessage());
		}
	}

	/** Einen float-Werte von den Properties lesen */
	private static float readNumber(String property, float defaultValue) throws FibuException {
		try {
			String defValue = Float.toString(defaultValue);
			String value = mProperties.getProperty(property, defValue);
			if (value == null) {
				return -1F;
			}
			return Float.parseFloat(value);
		} catch (NumberFormatException ex) {
			throw new FibuException("Property: '" + property + "' falsches Format \n" + "Fehler: " + ex.getMessage());
		}
	}

	/** Einen double-Werte von den Properties lesen */
	private static double readNumber(String property, double defaultValue) throws FibuException {
		try {
			String defValue = Double.toString(defaultValue);
			String value = mProperties.getProperty(property, defValue);
			if (value == null) {
				return -1.0;
			}
			return Double.parseDouble(value);
		} catch (NumberFormatException ex) {
			throw new FibuException("Property: '" + property + "' falsches Format \n" + "Fehler: " + ex.getMessage());
		}
	}

	/** Einen int-Werte in ein Property schreiben */
	private static void writeInt(String property, int value) {
		String strValue = Integer.toString(value);
		mProperties.setProperty(property, strValue);
	}

	/**
	 * Die Parameter der Applikation überprüfen. Wenn der FileName der Config-Datei
	 * vorhanden ist diesen setzen, sonst im aktuellen dir nachsehen. Die Properties
	 * einlesen
	 */
	public static void checkArgs(String[] args) throws FibuException {
		// ConfigFile öffnen
		String configFilePath;
		if (args.length > 0) {
			configFilePath = args[0];
		} else {
			configFilePath = sConfigFileName;
		}
		checkConfigFile(configFilePath);
	}

	/** prüft, ob das File vorhanden ist */
	private static void checkConfigFile(String configFilePath) throws FibuException {
		Trace.println(0, "Config-Path = '" + sConfigFileName + "'");
		sConfigFile = new File(configFilePath);
		if (!sConfigFile.exists()) {
			Trace.println(0, "ConfigFile nicht gefunden, Path: " + sConfigFile.getAbsolutePath());
			new FibuException("ConfigFile nicht gefunden, Path: " + sConfigFile.getAbsolutePath());
		}
	}

	// ------- getter und setter -----------------------
	public static String getDbName() {
		return mDBname;
	}

	public static void setDbName(String dbName) {
		mDBname = dbName;
	}

	public static DefaultListModel<String> getFibuList() {
		return sFibuNames;
	}

	public static void addFibuToList(String fibuName) {
		sFibuNames.addElement(fibuName);
	}

	/**
	 * Delete element from the list of Fibus
	 *
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

	/**
	 * Adds 1 to a String ending with 0..9. Or a letter when endig a..z / A..Z (not
	 * implemented)
	 */
	public static String addOne(String pBelegNr) {
		if (pBelegNr == null || pBelegNr.length() <= 0) {
			return null;
		}
		try {
			int i = Integer.parseInt(pBelegNr);
			return Integer.toString(i + 1);
		} catch (NumberFormatException e) {
			// @todo charakter dazuzählen
			return pBelegNr;
		}
	}

}
