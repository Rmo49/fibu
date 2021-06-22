package com.rmo.fibu.util;

import java.util.EventListener;

/**
 * @author Ruedi
 * 
 */
public interface KontoListener extends EventListener {
	
	/** Wenn die Liste angezeigt werden soll */
	public void showKontoList();
	
	public void hideKontoList();
}
