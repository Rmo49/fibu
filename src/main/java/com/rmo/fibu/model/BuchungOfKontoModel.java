package com.rmo.fibu.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.rmo.fibu.exception.BuchungNotFoundException;
import com.rmo.fibu.exception.FibuException;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;

/**
 * Die Buchungen eines Kontos. Klasse hält einen Vektor mit allen Buchungen, die
 * zu einem Konto gehören.
 */
public abstract class BuchungOfKontoModel extends AbstractTableModel {

	private static final long serialVersionUID = -141586862566627246L;
	/** Die Models zu dieser View */
	protected KontoData mKontoData = null;
	/** das Model für alle Buchungen */
	protected BuchungData mBuchungData = null;
	/** der StartSaldo */
	protected double mStartSaldo = 0;
	/** der Saldo, wird berechnet auch für Buchungen die nicht angezeigt werden */
	protected double mSaldo = 0;
	/** die Summe der Soll- und HabenSpalte */
	protected double mSummeSoll;
	protected double mSummeHaben;
	/** Der StartSaldo muss noch angezeigt werden */
	protected boolean mShowStartLine;

	/** Die angezeigten Zeilen */
	protected class BuchungRow {
		public long ID = -1;
		public Date datum;
		public String beleg;
		public String text;
		public int gegenKonto;
		public double sollBetrag = -1; // wenn -1 wird nie angezeigt (leerer Text)
		public double habenBetrag = -1;
		public double saldo = -1;

	}

	public BuchungOfKontoModel() {
		mBuchungData = (BuchungData) DataBeanContext.getContext().getDataBean(BuchungData.class);
		mKontoData = (KontoData) DataBeanContext.getContext().getDataBean(KontoData.class);
	}

	/**
	 * Setup the new display (Vector)
	 *
	 * @param int  konto die kontonummer, die angezeigt werden soll
	 * @param Date from ab diesem Datum Buchungen anzeigen
	 */
	abstract public void setup(int kontoNr, Date from);

	/**
	 * Die Liste aller Buchungen des Kontos
	 *
	 * @return
	 */
	abstract protected List<BuchungRow> getBuchungen();

	/** 7 Cols anzeigen */
	@Override
	public int getColumnCount() {
		return 7;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case 0:
			return "Datum";
		case 1:
			return "Beleg";
		case 2:
			return "Text";
		case 3:
			return "GegenKto";
		case 4:
			return "Soll";
		case 5:
			return "Haben";
		case 6:
			return "Saldo";
		}
		return "";
	}

	/** Steuert das aussehen einer Spalte */
	@Override
	public Class<?> getColumnClass(int col) {
		return getValueAt(0, col).getClass();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Momentan noch nicht implementiert, siehe setValueAt()
		return false;
	}

	/**
	 * Den StartSaldo dazufuegen.
	 */
	protected BuchungRow rowStartSaldo(Date from) {
		BuchungRow lRow = new BuchungRow();
		lRow.datum = from;
		lRow.beleg = "";
		lRow.text = "Saldo";
		lRow.saldo = mSaldo;
		return lRow;
	}

	/**
	 * Eine Zeile der Liste dazufügen.
	 */
	protected BuchungRow rowValue(int kontoNr, Buchung pBuchung) {
		BuchungRow lRow = new BuchungRow();
		lRow.ID = pBuchung.getID();
		lRow.datum = pBuchung.getDatum();
		lRow.beleg = pBuchung.getBeleg();
		lRow.text = pBuchung.getBuchungText();
		if (pBuchung.getSoll() == kontoNr) {
			lRow.gegenKonto = pBuchung.getHaben();
			lRow.sollBetrag = pBuchung.getBetrag();
		} else {
			lRow.gegenKonto = pBuchung.getSoll();
			lRow.habenBetrag = pBuchung.getBetrag();
		}
		lRow.saldo = mSaldo;
		return lRow;
	}

	/**
	 * Die Summen und den Saldo berechnen
	 */
	protected void addSaldo(int kontoNr, boolean isSoll, Buchung pBuchung) {
		if (pBuchung.getSoll() == kontoNr)
			mSummeSoll += pBuchung.getBetrag();
		else
			mSummeHaben += pBuchung.getBetrag();
		if (isSoll)
			mSaldo = mSummeSoll - mSummeHaben;
		else
			mSaldo = mSummeHaben - mSummeSoll;
	}

	/**
	 * Die Summen dazufuegen.
	 */
	protected BuchungRow rowSummen() {
		BuchungRow lRow = new BuchungRow();
		lRow.datum = Config.sDatumBis;
		lRow.beleg = "";
		lRow.text = Config.sSummen;
		lRow.sollBetrag = mSummeSoll;
		lRow.habenBetrag = mSummeHaben;
		lRow.saldo = mSaldo;
		return lRow;
	}

	/**
	 * Die Buchung auf der Zeile zurückgeben
	 * @param row die Zeilennummer
	 * @return gefundene Buchung
	 */
	public Buchung getBuchungAt(int row) {
		BuchungRow lRow = null;
		lRow = getBuchungen().get(row);
		Buchung lBuchung = null;
		try {
			lBuchung  = mBuchungData.read(lRow.ID);
		}
		catch (FibuException ex) {
			
		}
		return lBuchung;
	}
	
	
	/**
	 * Gibt den Wert an der Koordinate row / col zurück.
	 */
	@Override
	public Object getValueAt(int row, int col) {
		Trace.println(7, "KontoView.BuchungModel.getValueAt(" + row + ',' + col + ')');
		BuchungRow lRow = null;
		lRow = getBuchungen().get(row);
		switch (col) {
		case 0:
			return lRow.datum;
		case 1:
			return lRow.beleg;
		case 2:
			return lRow.text;
		case 3:
			return Integer.valueOf(lRow.gegenKonto);
		case 4:
			return Double.valueOf(lRow.sollBetrag);
		case 5:
			return Double.valueOf(lRow.habenBetrag);
		case 6:
			return Double.valueOf(lRow.saldo);
		}
		return "";
	}

	/**
	 * Den Wert in die DB schreiben,
	 */
	@Override
	public void setValueAt(Object aValue, int row, int col) {
		// TODO Probleme mit Datum, vielleicht nicht ändern, wenn geändert muss gespeichert werden
		// TODO dazu einen neuen Button, und es muss gemerkt werden, welche Buchungen geändert wurden.
		Trace.println(7, "KontoView.BuchungModel.setValueAt(" + row + ',' + col + ')');
		BuchungRow lRow = getBuchungen().get(row);
		switch (col) {
		case 0:
			try {
				Date datum = new SimpleDateFormat("dd.MM.yy").parse(aValue.toString());
				lRow.datum = datum;
			}
			catch (ParseException ex) {
				// do nothing
			}
			break;
		case 1:
			lRow.beleg = aValue.toString();
			break;
		case 2:
			lRow.text = aValue.toString();
			break;
		}
	}


}