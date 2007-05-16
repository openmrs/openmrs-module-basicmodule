package org.openmrs.module.printing.impl;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.printing.PrintingService;
import org.openmrs.module.printing.db.PrintingDAO;

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
	 * @see org.openmrs.module.printing.PrintingService#addPrintJob(java.io.InputStream)
	 */
	public void addPrintJob(InputStream inStream) {
		
		PrintService printService = getDefaultPrinter();
		
		DocFlavor inFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
		Doc myDoc = new SimpleDoc(inStream, inFormat, null);  
		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
		aset.add(new Copies(1));
		
		if (printService.isDocFlavorSupported(inFormat) == false) {
			throw new APIException("Unable to print to " + printService.getName() + " with the current document flavor: " + inFormat);
		}
		
		DocPrintJob job = printService.createPrintJob();
		try {
			job.print(myDoc, aset);
		} 
		catch (PrintException pe) {
			log.error("Error occurred while trying to print", pe);
		}
	}

	/**
	 * @see org.openmrs.module.printing.PrintingService#getPrintJobs()
	 */
	public List<?> getPrintJobs() {
		// TODO use a listener on the print jobs to know what jobs are outstanding
		
		log.warn("unimplemented");
		return null;
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
			
			if (defaultPrinter == null)
				defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
			
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
