package org.openmrs.module.printing.impl;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
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
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.DocumentName;
import javax.print.attribute.standard.MediaName;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;

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
	 * @throws PrinterException 
	 * @see org.openmrs.module.printing.PrintingService#addPrintJob(java.io.InputStream)
	 */
	public boolean addPrintJob(InputStream inStream) throws PrinterException {
		DocFlavor inFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
		
		return addPrintJob(inStream, inFormat);
	}
	
	/**
	 * @throws PrinterException 
	 * @see org.openmrs.module.printing.PrintingService#addPrintJob(byte[])
	 */
	public boolean addPrintJob(byte[] byteArray) throws PrinterException {
		DocFlavor byteFormat = DocFlavor.BYTE_ARRAY.AUTOSENSE;
		
		return addPrintJob(byteArray, byteFormat);
	}
	
	/**
	 * Does the actual printing
	 * 
	 * @param o object to print (byteArray, input stream, etc)
	 * @param flavor the document flavor/type
	 * @return true/false whether it printed successfully
	 * @throws PrinterException
	 */
	private boolean addPrintJob(Object o, DocFlavor flavor) throws PrinterException {
		DocAttributeSet docAttributes = new HashDocAttributeSet();
		docAttributes.add(MediaName.NA_LETTER_WHITE);
		docAttributes.add(new DocumentName(Context.getAuthenticatedUser().getUsername() + "- print job", Context.getLocale()));
		Doc myDoc = new SimpleDoc(o, flavor, docAttributes);
		
		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
		aset.add(MediaTray.MAIN);
		aset.add(MediaSizeName.NA_LETTER);
		aset.add(new Copies(1));
		
		PrinterJob jobForAttrs = PrinterJob.getPrinterJob();
		jobForAttrs.setPrintService(getDefaultPrinter());

		if (jobForAttrs.printDialog(aset) == false)
			return false;
		
		PrintService printService = jobForAttrs.getPrintService();
		DocPrintJob job = printService.createPrintJob();
		try {
			if (printService.isDocFlavorSupported(flavor) == false)
				throw new APIException("Unable to print to " + printService.getName() + " with the current document flavor: " + flavor);
			
			job.print(myDoc, aset);
		} 
		catch (PrintException pe) {
			log.error("Error occurred while trying to print", pe);
			return false;
		}
		
		return true;
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
