package org.openmrs.module.printing;

import java.net.URL;
import java.util.List;

import javax.print.PrintService;

import org.openmrs.module.printing.db.PrintingDAO;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

/**
 * Defines methods for printing in openmrs.  Methods include getting available printers, 
 * setting default printer, and adding/removing print jobs to the print queue
 *
 */
@Transactional
public interface PrintingService {

	public void setPrintingDAO(PrintingDAO dao);
	
	public static final String PRINT_SERVICE_GP = "printing.defaultPrinterService";
	
	/**
	 * Add print job as doc
	 * @param doc
	 */
	public void addPrintJob(Document doc);
	
	/**
	 * Add print job as byte array
	 * @param byteArray
	 */
	public void addPrintJob(Byte[] byteArray);
	
	/**
	 * Add print job as url
	 * @param url
	 */
	public void addPrintJob(URL url);
	
	/**
	 * 
	 * @param printJobId
	 * @return true/false whether the print job was deleted
	 */
	public Boolean deletePrintJob(Integer printJobId);
	
	/**
	 * 
	 * @param includePrinted whether or not to include the already printed jobs
	 */
	@Transactional(readOnly=true)
	public void getPrintJobs(Boolean includePrinted);
	
	/**
	 * 
	 * @param printServiceName name of the PrintService to set as the default
	 */
	public void setDefaultPrinter(String printServiceName);
	
	/**
	 * Return the printer selected as the default one to use for openmrs
	 */
	@Transactional(readOnly=true)
	public PrintService getDefaultPrinter();
	
	/**
	 * @return a list of PrintService objects
	 */
	@Transactional(readOnly=true)
	public List<PrintService> getAvailablePrinters();
}