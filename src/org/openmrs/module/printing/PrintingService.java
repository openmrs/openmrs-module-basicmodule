package org.openmrs.module.printing;

import java.io.InputStream;
import java.util.List;

import javax.print.PrintService;

import org.openmrs.module.printing.db.PrintingDAO;
import org.springframework.transaction.annotation.Transactional;

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
	 * Add print job as inputStream
	 * 
	 * The stream will be closed in this method
	 * 
	 * @param inputStream
	 */
	public void addPrintJob(InputStream inputStream);
	
	/**
	 * 
	 * @param includePrinted whether or not to include the already printed jobs
	 */
	@Transactional(readOnly=true)
	public List<?> getPrintJobs();
	
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