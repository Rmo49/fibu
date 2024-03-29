package com.rmo.fibu.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.rmo.fibu.model.CsvBank;
import com.rmo.fibu.model.CsvBankData;
import com.rmo.fibu.model.DataBeanContext;
import com.rmo.fibu.util.Config;

/** Die Menu-Steuerung der Buchung-View.
 * @author Ruedii
  */
public class BuchungMenu {

	/** Die Verbindung zur Buchung-View */
	private BuchungView		mBuchungView;

	// ---- Das Menu (inkl. Popup)
	private JMenu			mnuFile;
	private JMenuItem		mnuClose;

	private JMenu			mnuEdit;
	private JMenuItem		mnuSearch;
	private JMenuItem		mnuCopy;
	private JMenuItem		mnuDelete;
	private JMenuItem		mnuSelect;

	private JMenu			mnuCsv;

	private JMenuItem		mnuCsvSetup;

	private JPopupMenu		popMenu;
	private JMenuItem		popCopy;
	private JMenuItem		popDelete;
	private JMenuItem		popSort;


	/** Konstruktor mit Initialisierung	 */
	public BuchungMenu(BuchungView parent) {
		super();
		mBuchungView = parent;
		initMenu();
		initMenuActions();
	}

	/** Initialisierung der Menus
	 */
	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		mnuFile = new JMenu("Datei");
		mnuFile.setFont(Config.fontTextBold);
		mnuClose = new JMenuItem("Schliessen");
		mnuClose.setFont(Config.fontTextBold);

		menuBar.add(mnuFile);
		mnuFile.add(mnuClose);

		mnuEdit = new JMenu("Bearbeiten");
		mnuEdit.setFont(Config.fontTextBold);
		mnuSearch = new JMenuItem("suchen");
		mnuSearch.setFont(Config.fontTextBold);
		mnuCopy = new JMenuItem("Buchung bearbeiten");
		mnuCopy.setFont(Config.fontTextBold);
		mnuDelete = new JMenuItem("Buchungen löschen");
		mnuDelete.setFont(Config.fontTextBold);
		mnuSelect = new JMenuItem("selektieren");
		mnuSelect.setFont(Config.fontTextBold);

		menuBar.add(mnuEdit);
		mnuEdit.add(mnuSearch);
		mnuEdit.addSeparator();
		mnuEdit.add(mnuCopy);
		mnuEdit.add(mnuDelete);
		mnuEdit.addSeparator();
		mnuEdit.add(mnuSelect);

		mnuCsv = new JMenu("Einlesen");
		mnuCsv.setFont(Config.fontTextBold);

		menuBar.add(mnuCsv);
		insertCsvMenu(mnuCsv);

		mnuCsvSetup = new JMenuItem("Setup");
		mnuCsvSetup.setFont(Config.fontTextBold);
		menuBar.add(mnuCsvSetup);

		mBuchungView.setJMenuBar(menuBar);



		popMenu = new JPopupMenu();
		popCopy = new JMenuItem("Buchung bearbeiten");
		popCopy.setFont(Config.fontTextBold);
		popDelete = new JMenuItem("Buchungen löschen");
		popDelete.setFont(Config.fontTextBold);
		popSort = new JMenuItem("sortieren");
		popSort.setFont(Config.fontTextBold);
		popMenu.add(popCopy);
		popMenu.add(popDelete);
		popMenu.addSeparator();
		popMenu.add(popSort);
	}

	/**
	 * Das CSV Menu setzen, gemäss Einträge in der DB (pdfcompany)
	 * @param mnuCsv
	 */
	private void insertCsvMenu(JMenu mnuCsv) {
		CsvBankData 	mBankData = null;
		mBankData = (CsvBankData) DataBeanContext.getContext().getDataBean(CsvBankData.class);
		Iterator<CsvBank> iter = mBankData.getIterator();
		JMenuItem mnuItem;
		while (iter.hasNext()) {
			CsvBank bank = iter.next();
			mnuItem = new JMenuItem(bank.getBankName());
			mnuItem.setFont(Config.fontTextBold);

			mnuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mBuchungView.csvAction(bank);
				}
			});
			mnuCsv.add(mnuItem);
		}
	}


	/** Zu allen Menus die entspenden Actions initialisieren */
	private void initMenuActions() {
		mnuClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//mBuchungView.hide();
				mBuchungView.setVisible(false);
			}
		});
		mnuSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BuchungSuchDialog lSuchen = new BuchungSuchDialog(mBuchungView, false);
				lSuchen.setVisible(true);
			}
		});

		mnuCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mBuchungView.copyActionPerformed();
			}
		});
		mnuDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mBuchungView.deleteActionPerformed();
			}
		});
		mnuSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mBuchungView.sortActionPerformed();
			}
		});
		mnuCsvSetup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mBuchungView.csvSetup();
			}
		});

		popCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mBuchungView.copyActionPerformed();
			}
		});
		popDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mBuchungView.deleteActionPerformed();
			}
		});
		popSort.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mBuchungView.sortActionPerformed();
			}
		});
	}

	public JPopupMenu getPopMenu() {
		return popMenu;
	}

	/** SortMenu und SortPopup setzen */
	public void setEnableSort(boolean active) {
		mnuSelect.setEnabled(active);
		popSort.setEnabled(active);
	}

	/** copyMenu und copyPopup setzen */
	public void setEnableCopy(boolean active) {
		mnuCopy.setEnabled(active);
		popCopy.setEnabled(active);
	}

	/** DelesteMenu und deletePopup setzen */
	public void setEnableDelete(boolean active) {
		mnuDelete.setEnabled(active);
		popDelete.setEnabled(active);
	}
}
