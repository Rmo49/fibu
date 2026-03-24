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
	private static String sConfigFileName = "FibuConfig.txt";
	public static final String sJsonExtension = ".json";
	public static File sConfigFile;
	/** Die Properties, gespeichert in der Config-Datei */
	private static PropertiesFibu mProperties;
	private static Vector<Object> mPropertyList;

	private static final String sDefaultDirKey = "fibu.default.dir";
	public static String sDefaultDir = "D:/Daten/Fibu";
	private static final String sParserFileNameKey = "fibu.parser.filename";
	public static String sParserFileName = "F:/doc/Postfinance151001.pdf";

	// Alle Namen der bisher geöffneten Fibus, wird von Config eingelesen
	private static final String sFibuNamesKey = "fibu.list";
	private static DefaultListModel<String> sFibuNames;

	public static boolean sConfigError = false;

	public static int traceLevel = 3;
	private static final String traceLevelKey = "fibu.trace.level";
	public static boolean traceTimestamp;
	private static final String traceTimestampKey = "fibu.trace.timestamp";

	// ----- Printer ----------------------------------------------------------
	private static final String printerRandObenKey = "printer.rand.oben";
	public static float printerRandOben = 42.55F; // Rand Oben in Pixels
	private static final String printerRandLinksKey = "printer.rand.links";
	public static float printerRandLinks = 56.7F;
	private static final String printerPageWidthKey = "printer.page.width";
	public static double printerPageWidth =  (8.27 * 72); //(1 Zoll = 72 Punkte) 210 mm;
	private static final String printerPageHeightKey = "printer.page.height";
	public static double printerPageHeight = 11.69 * 72; // 297 mm;
	private static final String printerColAbstandKey = "printer.abstand.col";
	public static float printerColAbstand = 1F; // Abstand zwischen Spalten
	private static final String printerRowAbstandKey = "printer.abstand.row";
	public static float printerRowAbstand = 2F; // Abstand nach Row
	private static final String printerHeaderAbstandKey = "printer.abstand.header";
	public static float printerHeaderAbstand = 5F; // Abstand nach Kopfzeile
	private static final String printerSummeAbstandKey = "printer.abstand.summe";
	public static float printerSummeAbstand = 3F; // Abstand zu Summe
	// KontoListe
	private static final String printerKtoCol1Key = "printer.kto.col1";
	public static int printerKtoCol1 = 16; // Breite der Spalte
	private static final String printerKtoCol2Key = "printer.kto.col2";
	public static int printerKtoCol2 = 10; // Breite der Spalte
	private static final String printerKtoCol3Key = "printer.kto.col3";
	public static int printerKtoCol3 = 50; // Breite der Spalte
	private static final String printerKtoCol4Key = "printer.kto.col4";
	public static int printerKtoCol4 = 10; // Breite der Spalte
	private static final String printerKtoCol5Key = "printer.kto.col5";
	public static int printerKtoCol5 = 20; // Breite der Spalte
	private static final String printerKtoCol6Key = "printer.kto.col6";
	public static int printerKtoCol6 = 20; // Breite der Spalte
	private static final String printerKtoCol7Key = "printer.kto.col7";
	public static int printerKtoCol7 = 20; // Breite der Spalte
	// bilanz
	private static final String printerBilanzCol1Key = "printer.bilanz.col1";
	public static int printerBilanzCol1 = 7; // Breite der Spalte
	private static final String printerBilanzCol2Key = "printer.bilanz.col2";
	public static int printerBilanzCol2 = 50; // Breite der Spalte
	private static final String printerBilanzCol3Key = "printer.bilanz.col3";
	public static int printerBilanzCol3 = 12; // Breite der Spalte
	private static final String printerBilanzCol4Key = "printer.bilanz.col4";
	public static int printerBilanzCol4 = 12; // Breite der Spalte
	// Journal
	private static final String printerJournalCol1Key = "printer.journal.col1";
	public static int printerJournalCol1 = 10; // Breite der Spalte
	private static final String printerJournalCol2Key = "printer.journal.col2";
	public static int printerJournalCol2 = 8; // Breite der Spalte
	private static final String printerJournalCol3Key = "printer.journal.col3";
	public static int printerJournalCol3 = 50; // Breite der Spalte
	private static final String printerJournalCol4Key = "printer.journal.col4";
	public static int printerJournalCol4 = 10; // Breite der Spalte
	private static final String printerJournalCol5Key = "printer.journal.col5";
	public static int printerJournalCol5 = 10; // Breite der Spalte
	private static final String printerJournalCol6Key = "printer.journal.col6";
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


	private static final String sWinPdfSetup = "window.PdfSetup";
	public static Point winPdfSetupLoc;
	public static Dimension winPdfSetupDim;

	private static final String sWinParserBank = "window.ParserBank";
	public static Point winParserBankLoc;
	public static Dimension winParserBankDim;

	private static final String sWinParserSelectFile = "window.ParserSelectFile";
	public static Point winParserSelectFileLoc;
	public static Dimension winParserSelectFileDim;

	private static final String sWinParserKeyword = "window.ParserKeyword";
	public static Point winParserKeywordLoc;
	public static Dimension winParserKeywordDim;

	private static final String sWinParserBuchung = "window.ParserBuchung";
	public static Point winParserBuchungLoc;
	public static Dimension winParserBuchungDim;
	public static final String sParserTextLenKey = "fibu.parser.buchungText.length";
	public static int sParserTextLen = 30;

	// --- die Grösse des Textes
	public static final String windowTextSizeKey = "window.size.text";
	public static int windowTextSize = 12;
	public static double windowTextMultiplikator;

	// --- language settings
	private static final String languageKey = "language.language";
	public static String  languageLanguage = "de";
	private static final String countryKey = "language.country";
	public static String  languageCountry = "CH";

	// --- database
	private static String mDBname;
	public static final String dbUserNameKey = "db.username";
	public static String dbUserName = "root";
	public static final String dbPasswordKey = "db.password";
	public static String dbPassword = "xxx";
	public static final String dbUrlKey = "db.url";
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

	/**
	 * Den Namen des Configfiles setzen, meist für Testzwecke
	 */
	public static void setConfigFileName(String fileName) {
		sConfigFileName = fileName;
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

	/**
	 * Alle Werte setzen, 
	 * @throws FibuException
	 */
	public static void setAllProperties() throws FibuException {
		Trace.println(2, "Config.setAllProperties()");
		// --- alle Property werte setzen
		traceLevel = readNumber(traceLevelKey, traceLevel);

		traceTimestamp = readBoolean(traceTimestampKey, traceTimestamp);
		sDefaultDir = mProperties.getProperty(sDefaultDirKey, sDefaultDir);
		sParserFileName = mProperties.getProperty(sParserFileNameKey, sParserFileName);
		sParserTextLen = readNumber(sParserTextLenKey, sParserTextLen);

		sFibuNames = readList(mProperties.getProperty(sFibuNamesKey), ",");
		if (sFibuNames == null) {
			sFibuNames = new DefaultListModel<>();
			sFibuNames.addElement("FibuLeer");
			mProperties.setProperty(sFibuNamesKey, "FibuLeer,");
		}

		printerRandLinks = readNumber(printerRandLinksKey, printerRandLinks);
		printerRandOben = readNumber(printerRandObenKey, printerRandOben);
		printerPageWidth= readNumber(printerPageWidthKey, printerPageWidth);
		printerPageHeight = readNumber(printerPageHeightKey, printerPageHeight);
		printerColAbstand= readNumber(printerColAbstandKey, printerColAbstand);
		printerRowAbstand = readNumber(printerRowAbstandKey, printerRowAbstand);
		printerHeaderAbstand = readNumber(printerHeaderAbstandKey, printerHeaderAbstand);
		printerSummeAbstand = readNumber(printerSummeAbstandKey, printerSummeAbstand);
		// KontoListe ausgeben
		printerKtoCol1 = readNumber(printerKtoCol1Key, printerKtoCol1);
		printerKtoCol2 = readNumber(printerKtoCol2Key, printerKtoCol2);
		printerKtoCol3 = readNumber(printerKtoCol3Key, printerKtoCol3);
		printerKtoCol4 = readNumber(printerKtoCol4Key, printerKtoCol4);
		printerKtoCol5 = readNumber(printerKtoCol5Key, printerKtoCol5);
		printerKtoCol6 = readNumber(printerKtoCol6Key, printerKtoCol6);
		printerKtoCol7 = readNumber(printerKtoCol7Key, printerKtoCol7);
		// Bilanz
		printerBilanzCol1 = readNumber(printerBilanzCol1Key, printerBilanzCol1);
		printerBilanzCol2 = readNumber(printerBilanzCol2Key, printerBilanzCol2);
		printerBilanzCol3 = readNumber(printerBilanzCol3Key, printerBilanzCol3);
		printerBilanzCol4 = readNumber(printerBilanzCol4Key, printerBilanzCol4);
		// journal
		printerJournalCol1 = readNumber(printerJournalCol1Key, printerJournalCol1);
		printerJournalCol2 = readNumber(printerJournalCol2Key, printerJournalCol2);
		printerJournalCol3 = readNumber(printerJournalCol3Key, printerJournalCol3);
		printerJournalCol4 = readNumber(printerJournalCol4Key, printerJournalCol4);
		printerJournalCol5 = readNumber(printerJournalCol5Key, printerJournalCol5);
		printerJournalCol6 = readNumber(printerJournalCol6Key, printerJournalCol6);

		// --- Fenster einlesen
		readWindowBuchung();
		readWindowKontoblatt();
		readWindowKontoBuchung();
		readWindowKontoplan();
		readWindowBilanzen();

		readWindowPdfSetup();
		readWindowParserSelectFile();
		readWindowParserKeyword();
		readWindowParserBuchung();
		readWindowParserBank();

		// --- Size von Text, Menu, Buttons
		windowTextSize = readNumber(windowTextSizeKey, windowTextSize);
		windowTextMultiplikator = (double) windowTextSize / (double) 12;

		// --- language settings
		languageLanguage = mProperties.getProperty(languageKey, languageLanguage);
		languageCountry = mProperties.getProperty(countryKey, languageCountry);

		// --- db-connetion
		dbUserName = mProperties.getProperty(dbUserNameKey, dbUserName);
		dbPassword = mProperties.getProperty(dbPasswordKey, dbPassword);
		dbUrl = mProperties.getProperty(dbUrlKey, dbUrl);
	}

	/** java.awt.Fonts initialisieren */
	public static void initFont() {
		fontText = new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.PLAIN, Config.windowTextSize);
		fontTextBold = new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.BOLD, Config.windowTextSize);
	}

	/** 
	 * Alle Properites in das File schreiben
	 */
	public static void saveProperties() throws FibuException {
		Trace.println(0, "Config.writeProperties()");
		// zuerst alles in mProperties schreiben
		mProperties.clear();
		writeFibuValues();
		writeWindowValues();
		writePrinterValues();
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
	 * Alle Werte der Fibu in Properties schreiben.
	 * @throws FibuException
	 */
	private static void writeFibuValues() throws FibuException {
		mProperties.setProperty(sDefaultDirKey, sDefaultDir);
		writeList(sFibuNames, ",", sFibuNamesKey);
		mProperties.setProperty(sParserFileNameKey, sParserFileName);
		writeNumber(sParserTextLenKey, sParserTextLen);
		writeNumber(traceLevelKey,traceLevel);
		writeBoolean(traceTimestampKey, traceTimestamp);
		mProperties.setProperty(languageKey, languageLanguage);
		mProperties.setProperty(countryKey, languageCountry);
		mProperties.setProperty(dbUserNameKey, dbUserName);
		mProperties.setProperty(dbPasswordKey, dbPassword);		
		mProperties.setProperty(dbUrlKey, dbUrl);	
	}
	
	/**
	 * Alle Werte der Fenster in Properties schreiben
	 * @throws FibuException
	 */
	private static void writeWindowValues() throws FibuException {
		writeWindowBuchung();
		writeWindowKontoblatt();
		writeWindowKontoBuchung();
		writeWindowKontoplan();
		writeWindowBilanzen();
		writeWindowPdfSetup();
		writeWindowParserSelectFile();
		writeWindowParserKeyword();
		writeWindowParserBuchung();
		writeWindowParserBank();
	}
	
	/**
	 * Alle Werte der Printer-Steuerung in Properties schreiben
	 * @throws FibuException
	 */
	private static void writePrinterValues() throws FibuException {
		writeNumber(printerRandLinksKey, printerRandLinks);
	
		writeNumber(printerRandObenKey, printerRandOben);
		writeNumber(printerPageWidthKey, printerPageWidth);
		writeNumber(printerPageHeightKey, printerPageHeight);
		writeNumber(printerColAbstandKey, printerColAbstand);
		writeNumber(printerRowAbstandKey, printerRowAbstand);
		writeNumber(printerHeaderAbstandKey, printerHeaderAbstand);
		writeNumber(printerSummeAbstandKey, printerSummeAbstand);
		// KontoListe ausgeben
		writeNumber(printerKtoCol1Key, printerKtoCol1);
		writeNumber(printerKtoCol2Key, printerKtoCol2);
		writeNumber(printerKtoCol3Key, printerKtoCol3);
		writeNumber(printerKtoCol4Key, printerKtoCol4);
		writeNumber(printerKtoCol5Key, printerKtoCol5);
		writeNumber(printerKtoCol6Key, printerKtoCol6);
		writeNumber(printerKtoCol7Key, printerKtoCol7);
		// Bilanz
		writeNumber(printerBilanzCol1Key, printerBilanzCol1);
		writeNumber(printerBilanzCol2Key, printerBilanzCol2);
		writeNumber(printerBilanzCol3Key, printerBilanzCol3);
		writeNumber(printerBilanzCol4Key, printerBilanzCol4);
		// journal
		writeNumber(printerJournalCol1Key, printerJournalCol1);
		writeNumber(printerJournalCol2Key, printerJournalCol2);
		writeNumber(printerJournalCol3Key, printerJournalCol3);
		writeNumber(printerJournalCol4Key, printerJournalCol4);
		writeNumber(printerJournalCol5Key, printerJournalCol5);
		writeNumber(printerJournalCol6Key, printerJournalCol6);
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

		Collections.sort(mPropertyList, new Comparator<>() {
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
	 * Position und Grösse des Windows lesen
	 */
	private static void readWindowParserSelectFile() throws FibuException {
		winParserSelectFileDim = readWindowDimension(sWinParserSelectFile);
		winParserSelectFileLoc = readWindowPoint(sWinParserSelectFile);
		writeWindowParserSelectFile();
	}

	/**
	 * Position und Grösse des Windows in das Property schreiben
	 */
	private static void writeWindowParserSelectFile() throws FibuException {
		writeWindowConfig(sWinParserSelectFile, winParserSelectFileLoc, winParserSelectFileDim);
	}

	/**
	 * Position und Grösse des Windows lesen
	 */
	private static void readWindowParserKeyword() throws FibuException {
		winParserKeywordDim = readWindowDimension(sWinParserKeyword);
		winParserKeywordLoc = readWindowPoint(sWinParserKeyword);
		writeWindowParserKeyword();
	}

	/**
	 * Position und Grösse des Windows in das Property schreiben
	 */
	private static void writeWindowParserKeyword() throws FibuException {
		writeWindowConfig(sWinParserKeyword, winParserKeywordLoc, winParserKeywordDim);
	}

	/**
	 * Position und Grösse des Windows
	 */
	private static void readWindowParserBuchung() throws FibuException {
		winParserBuchungDim = readWindowDimension(sWinParserBuchung);
		winParserBuchungLoc = readWindowPoint(sWinParserBuchung);
		writeWindowParserBuchung();
	}

	/**
	 * Position und Grösse des Windows in das Property schreiben
	 */
	private static void writeWindowParserBuchung() throws FibuException {
		writeWindowConfig(sWinParserBuchung, winParserBuchungLoc, winParserBuchungDim);
	}

	/**
	 * Position und Grösse des Windows
	 */
	private static void readWindowParserBank() throws FibuException {
		winParserBankDim = readWindowDimension(sWinParserBank);
		winParserBankLoc = readWindowPoint(sWinParserBank);
		writeWindowParserBuchung();
	}

	/**
	 * Position und Grösse des Windows in das Property schreiben
	 */
	private static void writeWindowParserBank() throws FibuException {
		writeWindowConfig(sWinParserBank, winParserBankLoc, winParserBankDim);
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
		writeNumber(winName + ".x", (int) point.getX());
		writeNumber(winName + ".y", (int) point.getY());
		writeNumber(winName + ".width", (int) dim.getWidth());
		writeNumber(winName + ".height", (int) dim.getHeight());
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

	/** 
	 * Einen int-Werte in ein Property schreiben
	 */
	private static void writeNumber(String property, int value) {
		String strValue = Integer.toString(value);
		mProperties.setProperty(property, strValue);
	}
	
	/**
	 * Einen Float-Wert in die Propety schreiben
	 * @param property
	 * @param value
	 */
	private static void writeNumber(String property, float value) {
		String strValue = Float.toString(value);
		mProperties.setProperty(property, strValue);
	}
	

	/**
	 * Einen Double-Wert in die Propety schreiben
	 * @param property
	 * @param value
	 */
	private static void writeNumber(String property, double value) {
		String strValue = Double.toString(value);
		mProperties.setProperty(property, strValue);
	}

	/**
	 * Boolean in Property schreiben.
	 * @param key
	 * @param value
	 */
	private static void writeBoolean(String key, boolean value) {
		String strValue = value ? "true" : "false";
		mProperties.setProperty(key, strValue);
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

	/**
	 * Prüft, ob das File vorhanden ist
	 */
	static void checkConfigFile(String configFilePath) throws FibuException {
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
		if (sFibuNames == null) {
			sFibuNames = new DefaultListModel<>();
		}
		sFibuNames.addElement(fibuName);
	}

	/**
	 * Delete element from the list of Fibus
	 *
	 * @param fibuName
	 */
	public static void deleteFibuFromList(String fibuName) {
		String element = null;
		if (sFibuNames == null) {
			return;	// wenn keine Liste, dann auch nichts löschen
		}
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
			//TODO charakter dazuzählen
			return pBelegNr;
		}
	}

}
