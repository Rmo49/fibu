package com.rmo.fibu.view;

import javax.swing.event.EventListenerList;

import com.rmo.fibu.util.KontoEvent;
import com.rmo.fibu.util.KontoListener;

/**
 * @author Ruedi
 * hält die Liste der KontoListener
 */
public class KontoListenerList {

	private EventListenerList	mListenerList = new EventListenerList();

	/** Konstruktor	 */
	public KontoListenerList() {
	}


	 public void addKontoListener(KontoListener l) {
		 mListenerList.add(KontoListener.class, l);
	 }

	 public void removeKontoListener(KontoListener l) {
		 mListenerList.remove(KontoListener.class, l);
	 }


	 /** Notify all listeners that have registered interest for
	  * notification on this event type.  The event instance
	  * is lazily created using the parameters passed into
	  * the fire method.
	  */
	 public void fireKontoShow(KontoEvent event) {
		 // Guaranteed to return a non-null array
		 Object[] listeners = mListenerList.getListenerList();
		 // Process the listeners last to first, notifying
		 // those that are interested in this event
		 for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==KontoListener.class) {
			 // Lazily create the event:
				//if (mKontoEvent == null) mKontoEvent = new KontoEvent(this);
				 ((KontoListener)listeners[i+1]).showKontoList();
			 }
		 }
	 }

	public void fireKontoHide(KontoEvent event) {
		// Guaranteed to return a non-null array
		Object[] listeners = mListenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
		   if (listeners[i]==KontoListener.class) {
			// Lazily create the event:
			   //if (mKontoEvent == null) mKontoEvent = new KontoEvent(this);
				((KontoListener)listeners[i+1]).hideKontoList();
			}
		}
	}

}
