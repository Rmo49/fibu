package com.rmo.fibu.util;

//import java.util.Iterator;
//import com.rmo.fibu.model.Konto;

/** Das Interface zum TablePrinter.
 *  Klassen, die den TablePrinter verwenden, müssen dieses Interface
 * implementieren.
 */
public interface KontoListPrinterInterface {

	/** Die Anzahl Kopfzeilen */
	public int getHeaderCount();

	/** Die Anzahl Spalten
	*/
	public int getColCount();

	/** Die überschrift einer Spalte der Liste.
	* @param columnIndex Nummer der Spalte */
	public String getColName(int columnIndex);

	/** Die relative Spaltenbreiten, diese werden der Seitenbreite angepasst. */
	public int getColSize(int columnIndex);

	/** Die Spalten-Nummern, die eine Summen enthalten sollen.
	 * @param columnIndex Nummer der Spalte die Summe hat */
	public boolean isColToAdd(int columnIndex);

	/** Die Spalten, die rechtsbündig gedruckt werden.
	 *  Zahlen werden automatisch rechtsbündig gedruckt,
	 *  hier angeben, wenn Ueberschrift auch rechtsböndig sein soll
	 * @param columnIndex Nummer der Spalte die rechtsbündig hat */
	public boolean getColRight(int columnIndex);

	/** Der Iterator über alle Konti */
//	public Iterator<Konto> getIterator();

	/** Die Kopfzeile, wir linksbündig angezeigt.
	 * @param kontoNr die gewählte Kontonummer
	 *  @param number die Zeilennummer
	 * */
	public String getKontoName(int kontoNr, int number);

	/** Die Anzahl Zeilen
	 * @param kontoNr die gewählte Kontonummer
	*/
	public int getRowCount(int kontoNr);

	/** Der Wert einer Zelle.
	 * @param row Zeile
	 * @param col Spalte
	 * @return Wert der Zelle */
	public Object getValueAt(int kontoNr, int row, int col);
	
}
