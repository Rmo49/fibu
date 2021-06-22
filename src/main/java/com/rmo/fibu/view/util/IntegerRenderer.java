package com.rmo.fibu.view.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.rmo.fibu.util.Config;

/** Renderer für alle Integer in einer Tabelle.
 *  für die Formatierung wird DecimalFormat verwendet
 */
public class IntegerRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -8275818907591137813L;

	/** Konstruktor */
	public IntegerRenderer() {
		super();
		setHorizontalAlignment(SwingConstants.RIGHT);
		setFont(Config.fontText);
		setForeground(Color.black);
	}

	/** Die implementierte Methode von TableCellRenderer.
	 *  Wird aufgerufen, wenn eine Zelle mit Integer-Werten dargestellt werden
	 *  soll.
	 *  Wenn der Wert 0 ist, wird nichts ausgegeben, sonst der formatierte Wert.
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
			setBackground(table.getBackground());
			setForeground(table.getForeground());
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
		if (value instanceof String) {
			setText((String) value);
		}
		return this;
	}
}
