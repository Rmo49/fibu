package com.rmo.fibu.view;

import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.ParseException;
import java.util.Iterator;

import javax.swing.JInternalFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.rmo.fibu.exception.BuchungValueException;
import com.rmo.fibu.model.Buchung;
import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.util.Trace;
import com.rmo.fibu.view.util.ColumnRenderer;
import com.rmo.fibu.view.util.DoubleRenderer;


/** Die Anzeige der Buchungen. Bestent aus einer Tabelle in einem Scrollpane.
 * Aufbau:
 * @author Ruedi
 *
 */
public class BuchungenFrame extends BuchungenBaseFrame
	implements MouseListener, ComponentListener {

	private static final long serialVersionUID = 3288908916928770193L;
	/** Die Verbindung zur Buchung-View */
	private BuchungView		mBuchungView;
	/** Tabelle für die Anzeige der Buchungen, enthält alle Buchungen */
	private JScrollPane     mBuchungScroller;
	private JTable          mBuchungTable;

	/** Das Model zu dieser View, Verbindung zur DB */
	private BuchungData     mBuchungData = null;
	/** Das Model zur Tabelle */
	private BuchungModel    mBuchungModel;

	/**
	 *
	 */
	public BuchungenFrame(BuchungView parent) {
		// title resizable closable maximizable iconifiable
		super("Buchung Liste", true, false, true, false);
		mBuchungView = parent;
		init();
	}

	//	----- Initialisierung ------------------------------------------------
	 /**
	  * Start der Initialisierung, muss von jedem Konstruktor aufgerufen werden.
	  */
	 private void init() {
		 Trace.println(1,"BuchungenFrame.init()");
		 mBuchungData = (BuchungData) DataBeanContext.getContext().getDataBean(BuchungData.class);
		 initView();
	 }

	/** Initialisierung der View:
	 */
	private void initView() {
		Trace.println(2,"BuchungenFrame.initView()");
		getContentPane().add(initTable());
		setTitle("Buchungen");
		this.setSize(500, 400);
		mBuchungTable.addMouseListener(this);
		this.addComponentListener(this);
	}

	/** Initialisierung der Tabelle mit dem Model
	 */
	private Container initTable() {
		Trace.println(3,"BuchungView.initTable()");
		// ----- die Tabelle mit dem Model
		mBuchungModel = new BuchungModel(mBuchungData);
		mBuchungTable = new JTable(mBuchungModel);
		// der font wird in den Cell Renderer gesetzt
		mBuchungTable.setRowHeight(Config.windowTextSize + 4);
		mBuchungTable.setFont(Config.fontText);
		mBuchungTable.getTableHeader().setFont(Config.fontText);
		mBuchungModel.addTableModelListener(mBuchungTable);
		// die Breite der Cols
		TableColumn column = null;
		for (int i = 0; i < mBuchungModel.getColumnCount(); i++) {
			column = mBuchungTable.getColumnModel().getColumn(i);
			switch (i) {
				case 0:
				case 5: column.setPreferredWidth(80); break;
				case 2: column.setPreferredWidth(300); break;
				default: column.setPreferredWidth(40);
			}
		}
		//SetUp cell-Editor (noch) nicht verwendet
		//setUpDoubleEditor(tableBilanz);
		// Den Column-Renderer für alle Spalten setzen (setzt die Farben)
		// ausser für den Betrag
		TableColumnModel columModel = mBuchungTable.getColumnModel();
		ColumnRenderer colRender = new ColumnRenderer(mBuchungModel);
		colRender.setFont(Config.fontText);
		for (int i = 0; i < columModel.getColumnCount()-1; i++) {
			columModel.getColumn(i).setCellRenderer(colRender);
		}
		// für den Betrag den Double Renderer
		mBuchungTable.setDefaultRenderer(Double.class, new DoubleRenderer());
		// Die ganze Tabelle in einem ScrollPane
		mBuchungScroller = new JScrollPane(mBuchungTable);
		return mBuchungScroller;
	}


	//--- Listeners ------------------------------
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO zuerst testen, ob was selektiert, wenn nicht Fehlermeldung
		mBuchungView.showPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mBuchungView.showPopup(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}


	@Override
	public void componentResized(ComponentEvent e) {
		// do nothing
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// do nothing
	}

	@Override
	public void componentShown(ComponentEvent e) {
		scrollToLastEntry();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// do nothing

	}

	//--- Steuerung der Anzeige ----------------------
	public void setRowSelectionInterval(int von, int bis) {
		mBuchungTable.setRowSelectionInterval(von, bis);
	}

	/** An das Ende der Liste scrollen */
	public void scrollToLastEntry() {
		validate();
		JScrollBar bar = mBuchungScroller.getVerticalScrollBar();
		bar.setValue( bar.getMaximum() );
	}

	/** Zeigt die Zeile mit der rowNr an */
	public void showRow(int rowNr) {
		JScrollBar bar = mBuchungScroller.getVerticalScrollBar();
		float faktor = bar.getMaximum() / mBuchungModel.getRowCount();
		bar.setValue( (int) faktor * rowNr );
	}

	/** Meldung, dass Zeilen eingefügt wurden */
	public void fireRowsInserted(int firstRow, int lastRow) {
		mBuchungModel.fireTableRowsInserted(firstRow, lastRow);
	}

	/** Meldung, dass Zeilen geändert wurden */
	public void fireTableDataChanged() {
		mBuchungModel.fireTableDataChanged();
	}


	//--- Zugriff auf die Daten	-------------------------

	/** Gibt den Inhalt der Letzten Buchung zurück */
	public Buchung getLastBuchung() {
		Trace.println(3,"BuchungenFrame.getLastBuchung()");
		Buchung lBuchung = new Buchung();
		try {
		lBuchung.setDatum(mBuchungTable.getValueAt(mBuchungTable.getRowCount()-1,0).toString());
		lBuchung.setBuchungText(mBuchungTable.getValueAt(mBuchungTable.getRowCount()-1,2).toString());
		lBuchung.setSoll(mBuchungTable.getValueAt(mBuchungTable.getRowCount()-1,3).toString());
		lBuchung.setHaben(mBuchungTable.getValueAt(mBuchungTable.getRowCount()-1,4).toString());
		lBuchung.setBetrag(mBuchungTable.getValueAt(mBuchungTable.getRowCount()-1,5).toString());
		}
		catch (ParseException e) {}
		catch (BuchungValueException e) {} // nix machen

		//--- letzt Beleg-Nr berechnen
		Iterator<Buchung> buchungIter = mBuchungData.getIterator();
		String lBeleg = new String("");
		while (buchungIter.hasNext()) {
			Buchung tmpBuchung = buchungIter.next();
			if (tmpBuchung.getBeleg().length()!= lBeleg.length()) {
				if (tmpBuchung.getBeleg().length() > lBeleg.length()) {
					lBeleg = "0" + lBeleg;
				}
				else {
					tmpBuchung.setBeleg("0" + tmpBuchung.getBeleg());
				}
			 }
			 if (lBeleg.compareTo(tmpBuchung.getBeleg()) < 0) {
				// austauschen
				lBeleg = tmpBuchung.getBeleg();
			}
		}
		lBuchung.setBeleg(lBeleg);
		return lBuchung;
	}

	public int[] getSelectedRows() {
		return mBuchungTable.getSelectedRows();
	}

	/** Gibt den Wert einer Buchung an der Koordinate row / col zurück.
	 * @param row die Zeile im Model
	 * @param col die Spalte
	 * 0 = Datum (Datum)
	 * 1 = Beleg (String)
	 * 2 = Text (String)
	 * 3 = Soll (Integer)
	 * 4 = Haben (Integer)
	 * 5 = Betrag (Double)
	 * 6 = ID (Long)
	 * @return Den Wert, oder LeerString wenn nichts gefunden
	 */
	public Object getValueAt(int row, int col) {
		return mBuchungModel.getValueAt(row, col);
	}

	/**
	 * Anzahl Buchungen
	 * @return
	 */
	public int getRowCount() {
		return mBuchungTable.getRowCount();
	}

	public JScrollPane getScroller() {
		return mBuchungScroller;
	}

	@Override
	public void repaintBuchungen() {
		mBuchungTable.repaint();
		
	}
	
	/**
	 * Meldung, dass Zeilen eingefüft wurden
	 */
	public void rowsInserted(int firstRow, int lastRow) {
		this.fireRowsInserted(firstRow, lastRow);
	}
	
}

