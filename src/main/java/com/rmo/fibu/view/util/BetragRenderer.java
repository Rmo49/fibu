package com.rmo.fibu.view.util;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.rmo.fibu.model.BuchungData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.util.Config;
import com.rmo.fibu.view.BuchungModel;

/** Renderer für alle double-werte (Beträge) in einer Tabelle.
 *  für die Formatierung wird DecimalFormat verwendet
 */
public class BetragRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -8134423132902425546L;
	private DecimalFormat 	mFormat = new DecimalFormat("###,###,##0.00");
	private BuchungModel	mBuchungModel = null;
	/** Das Model zu der Tabelle */
	private BuchungData     mBuchungData = null;

	/** Konstruktor */
	public BetragRenderer() {
		super();
		setFont(Config.fontText);
		init();
	}

	/** Konstruktor, mit Model */
	public BetragRenderer(BuchungModel tableModel) {
		super();
		setFont(Config.fontText);
		mBuchungModel = tableModel;
		init();
	}

	private void init(){
		setHorizontalAlignment(SwingConstants.RIGHT);
//		super.setForeground(Color.yellow);
//		super.setBackground(Color.yellow);
		if (mBuchungModel != null) {
			mBuchungData = (BuchungData) DataBeanContext.getContext().getDataBean(BuchungData.class);
		}
	}

	/** Die implementierte Methode von TableCellRenderer.
	 *  Wird aufgerufen, wenn eine Zelle mit Double-Werten dargestellt werden soll.
	 *  Wenn der Wert 0 ist, wird nichts ausgegeben, sonst der formatierte Wert.
	 *  Wenn ein String in der Zelle enthalten ist, wird dieser ausgegeben.
	 */
	@Override
	public Component getTableCellRendererComponent(
		JTable table, Object value,
		boolean isSelected, boolean hasFocus,
		int row, int column)
	{
		/** Hintergrund-Farbe setzen */
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		}
		else {
			super.setBackground(table.getBackground());
			super.setForeground(table.getForeground());
		}
		if ((mBuchungModel != null) &&
			// andere Farbe, wenn noch nicht gesichert
			((Long)mBuchungModel.getValueAt(row,6)).intValue() > mBuchungData.getIdSaved()){
			super.setForeground(Color.blue);
		}
		else {
//			super.setForeground(table.getForeground());
		}
		if (value instanceof Double) {
			double wert = ((Double)value).doubleValue();
			if (wert == -1) {
				setText("");    // nichts anzeigen, wenn 0
			}
			else {
				setText( mFormat.format(wert) );
			}
		}
		if (value instanceof String) {
			setText((String) value);
		}
		return this;
	}
}
