package com.rmo.fibu.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Date;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.BuchungOfKontoModel;
import com.rmo.fibu.model.BuchungOfKontoModelNormal;
import com.rmo.fibu.model.BuchungOfKontoModelSorted;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;
import com.rmo.fibu.view.util.BetragRenderer;
import com.rmo.fibu.view.util.IntegerRenderer;

/**
 * Die Anzeige aller Buchungen eines Kontos.
 * Wird in der KontoView verwendet
 */
public class BuchungenKontoFrame extends BuchungenBaseFrame
implements TableModelListener, ComponentListener {

	private static final long serialVersionUID = -7132755797618179989L;

	/** Die Verbindung zur Buchung-View */
	private KontoView		mKontoView;

	/** die Kontonummer deren Buchungen angezeigt werden sollen */
	private int 			mKontoNr = -1;
	/** ab diesem Datum */
	private Date 			mDatum;
	/** Tabelle für die Anzeige der Buchungen eines Konto */
	private JTable 			mBuchungTable;
	private ListSelectionModel mBuchungCellSelection;
	private boolean 		mBuchungSelected = false;
	
	/** Das Model zu allen Buchungen eines Kontos */
	private BuchungOfKontoModel mBuchungModel = null;
	/** der Container aller Buchungen */
	private JScrollPane 	mScrollPaneBuchung = null;

	// Für die Änderung einer Buchung
	private BuchungEingabeFrame mBuchungEingabeFrame = null;


	/**
	 * Der Konstruktor
	 * @param parent
	 */
	public BuchungenKontoFrame(KontoView parent) {
		// title resizable closable maximizable iconifiable
		super("Buchung Liste", true, false, true, false);
		mKontoView = parent;
	}

	/**
	 * Initialisierung der Tabelle für Buchungen mit dem Model
	 */
	public Component initTableBuchung() {
		Trace.println(3, "KontoView.initTableBuchung()");
		// ----- die Tabelle mit dem Model
		mBuchungTable = new JTable();
		mBuchungTable.setFont(Config.fontText);
		mBuchungTable.getTableHeader().setFont(Config.fontText);
		mBuchungTable.setRowHeight(Config.windowTextSize + 4);
		// Den default-Renderer für Spalten mit Double-Werten
		mBuchungTable.setDefaultRenderer(Double.class, new BetragRenderer());
		mBuchungTable.setDefaultRenderer(Integer.class, new IntegerRenderer());
		mScrollPaneBuchung = new JScrollPane(mBuchungTable);
		mScrollPaneBuchung.setPreferredSize(new Dimension(50 * Config.windowTextSize, 100));
		mScrollPaneBuchung.setMinimumSize(new Dimension(30 * Config.windowTextSize, 100));
		mBuchungTable.getModel().addTableModelListener(this);
		
		mBuchungCellSelection =	mBuchungTable.getSelectionModel();
		mBuchungCellSelection.addListSelectionListener(new ListSelectionListener() {
		      public void valueChanged(ListSelectionEvent e) {
		    	  int[] selectedRow = mBuchungTable.getSelectedRows();
		    	  if (selectedRow.length > 0) {
		    		  mBuchungSelected = true;
		    	  }
		    	  else {
		    		  mBuchungSelected = false;
		    	  }
		    	  mKontoView.enableButtons();
		      }	    	  
		});		
		return mScrollPaneBuchung;
	}


	/**
	 * Die Breite der Cols setzen
	 */
	private void setColWidth() {
		TableColumn column = null;
		for (int i = 0; i < mBuchungTable.getColumnCount(); i++) {
			column = mBuchungTable.getColumnModel().getColumn(i);
			switch (i) {
			case 0:
				column.setPreferredWidth(100);
				break;
			case 1:
				column.setPreferredWidth(60);
				break;
			case 2:
				column.setPreferredWidth(300);
				break;
			case 3:
				column.setPreferredWidth(50);
				break;
			default:
				column.setPreferredWidth(90);
			}
		}
	}


	@Override
	public void componentResized(ComponentEvent e) {
		// nix machen
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// nix machen

	}

	@Override
	public void componentShown(ComponentEvent e) {
		scrollToLastEntry();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// nix machen

	}

	/** An das Ende der Liste scrollen */
	@Override
	public void scrollToLastEntry() {
		validate();
		JScrollBar bar = mScrollPaneBuchung.getVerticalScrollBar();
		bar.setValue( bar.getMaximum() );
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		scrollToLastEntry();
	}

	
	/**
	 * Show-Button wurde gedrückt. Ab-Datum einlesen, diese dem Model
	 * bekanntgeben, Anzeige starten
	 */
	public void actionShowBuchnungen(int kontoNr, Date datum) {
		Trace.println(3, "ShowButton->actionPerformed()");
		mKontoNr = kontoNr;
		mDatum = datum;
		// die Kontonummer bestimmen
		if (datum != null) {
			// --- die Daten setzen
			if (mBuchungModel != null) {
				mBuchungModel = null;
			}
			mBuchungModel = new BuchungOfKontoModelNormal();
			loadData();
			setColWidth();
		}
		mBuchungModel.fireTableDataChanged();
		scrollToLastEntry();
	}

	/**
	 * Sort-Button wurde gedrückt. Soriteren der Arrays im Model.
	 */
	public void actionBuchungenSortieren(int kontoNr, Date datum) {
		Trace.println(3, "SortButton->actionPerformed()");
		mKontoNr = kontoNr;
		mDatum = datum;
		// die Kontonummer bestimmen
		if (datum != null) {
			// --- die Daten neu setzen
			if (mBuchungModel != null) {
				mBuchungModel = null;
			}
			mBuchungModel = new BuchungOfKontoModelSorted();
			loadData();
			setColWidth();
		}
		// testen, ob bereits Model vorhanden
		//mBuchungModel.sortValues();
		mBuchungModel.fireTableDataChanged();
	}

	/**
	 * Eine Buchunge ändern
	 */
	public void actionBuchungAendern() {
		Trace.println(3, "BuchungAendern->action");
		if (mBuchungEingabeFrame == null) {
			mBuchungEingabeFrame = new BuchungEingabeFrame(mKontoView);
		}

		int lRow = mBuchungTable.getSelectedRow();
		if (lRow >= 0) {
			Buchung lBuchung = mBuchungModel.getBuchungAt(lRow);
			mBuchungEingabeFrame.init(lBuchung.getID());
		}
		else {
			// nichts selektiert
			mBuchungEingabeFrame.init(-1);
		}
		mBuchungEingabeFrame.setVisible(true);
//		mBuchungModel.fireTableDataChanged();
	}

	/**
	 * Die Tabelle der Buchungen für den Printer
	 * @return
	 */
	public JTable getBuchungTable() {
		return mBuchungTable;
	}
	
	/**
	 * Gibt an, ob eine Buchung selektiert wurde
	 * @return true wenn selektiert
	 */
	public boolean isBuchungSelected() {
		return mBuchungSelected;
	}
	
	/**
	 * Die Daten von der DB lesen und in die Tabelle kopieren
	 */
	private void  loadData() {
		mBuchungModel.setup(mKontoNr, mDatum);
		mBuchungTable.setModel(mBuchungModel);
		mBuchungModel.addTableModelListener(mBuchungTable);
	}

	@Override
	public void repaintBuchungen() {
		loadData();
		mBuchungTable.repaint();
//		this.repaint();
//		mBuchungEingabeFrame.setVisible(false);
	}


	@Override
	public void rowsInserted(int firstRow, int lastRow) {
		// nichts machen, da hier nicht erlaubt
	}



}
