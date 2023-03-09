package com.rmo.fibu.view;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;

import com.rmo.fibu.util.Config;
import javax.swing.JCheckBox;

public class PdfSetupFrame_Design extends JFrame {

	private static final long serialVersionUID = -8382082396350983484L;
	
	private JPanel contentPane;
	private JTextField textField;
	/** Die Daten in der DB */
	private CsvSetupModel 	mSetupData = null;
	private JTable table;
	private JTable table_1;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					PdfSetupFrame_Design frame = new PdfSetupFrame_Design();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public PdfSetupFrame_Design() {
		setTitle("CsvSetup");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 388);
		contentPane = new JPanel();
		contentPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblNewLabel = new JLabel("Zeile vor Buchungen");
		lblNewLabel.setBounds(22, 42, 120, 14);
		contentPane.add(lblNewLabel);

		textField = new JTextField();
		textField.setBounds(135, 39, 192, 20);
		contentPane.add(textField);
		textField.setColumns(10);

		JButton btnShowFirstRow = new JButton("Nächste Zeile anzeigen");
		btnShowFirstRow.setBounds(22, 67, 185, 23);
		contentPane.add(btnShowFirstRow);

		mSetupData = new CsvSetupModel();

		table = new JTable(mSetupData);
		table.setForeground(new Color(0, 0, 255));
		table.setShowGrid(true);
		table.setBounds(58, 135, 238, -39);
		table.getTableHeader().setFont(Config.fontText);
		table.setRowHeight(Config.windowTextSize + 4);
		table.setFont(Config.fontText);

		contentPane.add(table);

		JLabel lblNewLabel_1 = new JLabel("Einstellungen für");
		lblNewLabel_1.setVerticalAlignment(SwingConstants.TOP);
		lblNewLabel_1.setBounds(22, 11, 87, 14);
		contentPane.add(lblNewLabel_1);

		JLabel lblCompany = new JLabel("Name");
		lblCompany.setBounds(136, 11, 48, 14);
		contentPane.add(lblCompany);

		table_1 = new JTable();
		table_1.setBounds(107, 170, 1, 1);
		contentPane.add(table_1);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("buchen");
		chckbxNewCheckBox.setBounds(64, 135, 66, 23);
		contentPane.add(chckbxNewCheckBox);
		
		JCheckBox chckbxNewCheckBox_1 = new JCheckBox("gebucht");
		chckbxNewCheckBox_1.setSelected(true);
		chckbxNewCheckBox_1.setBounds(64, 166, 78, 23);
		contentPane.add(chckbxNewCheckBox_1);
	}

// ----- Model der ersten Zeile -------------------------------------------

	/** Schnittstelle zum Daten-Objekt KontoData */
	private class CsvSetupModel extends AbstractTableModel {

		private static final long serialVersionUID = -6657696461382710139L;

		@Override
		public int getRowCount() {
			return 2;
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "1";
			case 1:
				return "2";
			case 2:
				return "3";
			case 3:
				return "4";
			case 4:
				return "5";
			}
			return "";
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			super.setValueAt(aValue, rowIndex, columnIndex);
		}


	}
}
