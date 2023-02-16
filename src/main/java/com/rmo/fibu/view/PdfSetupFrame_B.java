package com.rmo.fibu.view;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import com.rmo.fibu.util.Config;

public class PdfSetupFrame_B extends JFrame {

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
					PdfSetupFrame_B frame = new PdfSetupFrame_B();
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
	public PdfSetupFrame_B() {
		setTitle("CsvSetup");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 507);
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

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(39, 200, 494, 87);
		contentPane.add(scrollPane);

		table_1 = new JTable();
		table_1.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null, null, null, null},
				{null, null, null, null, null},
			},
			new String[] {
				"1", "2", "3", "4", "5"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, String.class, String.class, String.class, String.class
			};
			@Override
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, false, false, false, false
			};
			@Override
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		scrollPane.setRowHeaderView(table_1);

		JLabel lblNewLabel_1 = new JLabel("Einstellungen für");
		lblNewLabel_1.setVerticalAlignment(SwingConstants.TOP);
		lblNewLabel_1.setBounds(22, 11, 87, 14);
		contentPane.add(lblNewLabel_1);

		JLabel lblCompany = new JLabel("Name");
		lblCompany.setBounds(136, 11, 48, 14);
		contentPane.add(lblCompany);
	}

// ----- Model der ersten Zeile -------------------------------------------

	/** Schnittstelle zum Daten-Objekt KontoData */
	private class CsvSetupModel extends AbstractTableModel {

		private static final long serialVersionUID = -6657696461382710139L;

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return 2;
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return 4;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
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
			// TODO Auto-generated method stub
			super.setValueAt(aValue, rowIndex, columnIndex);
		}


	}
}
