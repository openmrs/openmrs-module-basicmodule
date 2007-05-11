package org.openmrs.module.printing.db;


/**
 * Data Access Object for Printing Service
 *
 */
public interface PrintingDAO {
	
	/** 
	 * Append the given byte array to the print queue
	 * @param byteArray
	 */
	public void addPrintJob(Byte[] byteArray);
}
