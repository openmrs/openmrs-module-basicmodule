package org.openmrs.module.printing.impl;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.printing.PrintingService;
import org.openmrs.module.printing.db.PrintingDAO;
import org.w3c.dom.Document;

/**
 * Printing-related services
 */
public class PrintingServiceImpl implements PrintingService {

	private Log log = LogFactory.getLog(this.getClass());
	
	private PrintingDAO dao;
	private PrintService defaultPrinter = null;
	
	/**
	 * Default constructor
	 */
	public PrintingServiceImpl() { }
	
	/**
	 * @see org.openmrs.module.printing.PrintingService#setPrintingDAO(org.openmrs.module.printing.db.PrintingDAO)
	 */
	public void setPrintingDAO(PrintingDAO dao) {
		this.dao = dao;
	}

	/**
	 * @see org.openmrs.module.printing.PrintingService#addPrintJob(java.lang.Byte[])
	 */
	public void addPrintJob(Byte[] byteArray) {
		log.debug("Adding byte array print job");
		
		dao.addPrintJob(byteArray);
	}

	/**
	 * @see org.openmrs.module.printing.PrintingService#addPrintJob(org.w3c.dom.Document)
	 */
	public void addPrintJob(Document doc) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.openmrs.module.printing.PrintingService#addPrintJob(java.net.URL)
	 */
	public void addPrintJob(URL url) {
		// TODO Auto-generated method stub
	}

	/**
	 * @see org.openmrs.module.printing.PrintingService#deletePrintJob(java.lang.Integer)
	 */
	public Boolean deletePrintJob(Integer printJobId) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.openmrs.module.printing.PrintingService#getPrintJobs(java.lang.Boolean)
	 */
	public void getPrintJobs(Boolean includePrinted) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @see org.openmrs.module.printing.PrintingService#getAvailablePrinters()
	 */
	public List<PrintService> getAvailablePrinters() {
		PrintService[] services =
			PrintServiceLookup.lookupPrintServices(null, null);
		
		// list to return
		List<PrintService> printServices = new Vector<PrintService>();
		
		// copy array onto list
		if (services != null)
			Collections.addAll(printServices, services);
		
		return printServices;
	}
	
	/**
	 * @see org.openmrs.module.printing.PrintingService#getDefaultPrinter()
	 */
	public PrintService getDefaultPrinter() {
		if (defaultPrinter == null) {
			
			// get default printer name stored in the global properties
			String printer = Context.getAdministrationService().getGlobalProperty(PRINT_SERVICE_GP);
			
			for (PrintService service : getAvailablePrinters()) {
				if (service.getName().equals(printer)) {
					defaultPrinter = service;
					break;
				}
			}
		}
	
		return defaultPrinter;
		
	}

	/**
	 * @see org.openmrs.module.printing.PrintingService#setDefaultPrinter(java.lang.String)
	 */
	public void setDefaultPrinter(String printServiceName) {
    	Context.getAdministrationService().setGlobalProperty(PRINT_SERVICE_GP, printServiceName);
    	
    	// reset the printer object so that the new printer is used on next call to getDefaultPrinter()
    	defaultPrinter = null;
	}
	
}
