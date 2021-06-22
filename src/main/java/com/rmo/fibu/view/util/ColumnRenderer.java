package com.rmo.fibu.view.util;

import java.awt.Color;
import java.awt.Component;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.view.BuchungModel;


/** Renderer für die Buchungs-Tabelle, setzt die Font-Farbe auf blau
 * falls die Buchung noch nicht gesichert wurde.
 * Enthält die Behandlung aller datentypen.
 */
public class ColumnRenderer extends DefaultTableCellRenderer  {

	private static final long serialVersionUID = -3131238251555122763L;
	private BuchungModel	mBuchungModel = null;
	/** Das Model zu der Tabelle */
	private BuchungData     mBuchungData = null;
	private DecimalFormat 	mFormat = new DecimalFormat("###,###,##0.00");

	/** Konstruktor, mit Font */
	public ColumnRenderer(BuchungModel tableModel) {
		super();
		mBuchungModel = tableModel;
		init();
	}

	/** Initialisiert den Renderer */
	private void init(){
		if (mBuchungModel != null) {
			mBuchungData = (BuchungData) DataBeanContext.getContext().getDataBean(BuchungData.class);		
		}
	}

	/** Die implementierte Methode von TableCellRenderer.
	 *  Wird aufgerufen, wenn eine Zelle dargestellt werden soll.
	 *  @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column)
		{
		/** Hintergrund-Farbe setzen */
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		}
		else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}
		if ((mBuchungModel != null) &&
			// andere Farbe, wenn noch nicht gesichert
			((Long)mBuchungModel.getValueAt(row,6)).intValue() > mBuchungData.getIdSaved()){
			super.setForeground(Color.blue);
		}
		else {
//			super.setForeground(table.getForeground());
		}
		if (value instanceof Date) {
			setText( DateFormat.getDateInstance().format(value) );
		}
		if (value instanceof String) {
			setText((String) value);
		}
		if (value instanceof Integer) {
			int wert = ((Integer)value).intValue();
			if (wert == 0) {
				setText("");
			}
			else {
				setText( ((Integer)value).toString() );
			}
		}
		// siehe auch DoubleRenderer
		if (value instanceof Double) {
			double wert = ((Double)value).doubleValue();
			if (wert == -1) {
				setText("");    // nichts anzeigen, wenn 0
			}
			else {
				setText( mFormat.format(wert) );
				this.setHorizontalTextPosition(SwingConstants.RIGHT);
			}
		}
		return this;					   	
	}

}
