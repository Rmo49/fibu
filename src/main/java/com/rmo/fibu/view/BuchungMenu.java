package com.rmo.fibu.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.rmo.fibu.model.CsvParserBase;
import com.rmo.fibu.util.Config;

/** Die Menu-Steuerung der Buchung-View.
 * @author Ruedi
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

	private JMenu			mnuPdf;
	private JMenuItem		mnuCsvPost;
	private JMenuItem		mnuCsvCs;

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

		mnuPdf = new JMenu("Einlesen");
		mnuPdf.setFont(Config.fontTextBold);
		mnuCsvPost = new JMenuItem("Post Finance CSV");
		mnuCsvPost.setFont(Config.fontTextBold);
		mnuCsvCs = new JMenuItem("Credit Suisse CSV");
		mnuCsvCs.setFont(Config.fontTextBold);
		
		menuBar.add(mnuFile);
		menuBar.add(mnuEdit);
		mnuFile.add(mnuClose);
		mnuEdit.add(mnuSearch);
		mnuEdit.addSeparator();
		mnuEdit.add(mnuCopy);
		mnuEdit.add(mnuDelete);
		mnuEdit.addSeparator();
		mnuEdit.add(mnuSelect);

		menuBar.add(mnuPdf);
		mnuPdf.add(mnuCsvPost);
		mnuPdf.add(mnuCsvCs);
		
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

	/** Zu allen Menus die entspenden Actions initialisieren */
	private void initMenuActions() {
		mnuClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//mBuchungView.hide();
				mBuchungView.setVisible(false);
			}
		});
		mnuSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BuchungSuchDialog lSuchen = new BuchungSuchDialog(mBuchungView, false);
				lSuchen.setVisible(true);
			}
		});
		
		mnuCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mBuchungView.copyActionPerformed();
			}
		});
		mnuDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mBuchungView.deleteActionPerformed();
			}
		});
		mnuSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mBuchungView.sortActionPerformed();
			}
		});	

		mnuCsvPost.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mBuchungView.csvAction(CsvParserBase.companyNamePost);
			}
		});	
		
		mnuCsvCs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mBuchungView.csvAction(CsvParserBase.companyNameCS);
			}
		});	
		
		popCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mBuchungView.copyActionPerformed();
			}
		});
		popDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mBuchungView.deleteActionPerformed();
			}
		});
		popSort.addActionListener(new ActionListener() {
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
