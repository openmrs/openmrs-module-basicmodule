package org.openmrs.module.printing.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.printing.PrintingService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller backs and saves the printing module printer settings
 * 
 */
public class PrinterSetupController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    /**
     * Get the printing service from the Context
     * @return PrintingService
     */
    private PrintingService getPrintingService() {
    	return (PrintingService)Context.getService(PrintingService.class);
    }
    	
    @Override
	protected ModelAndView onSubmit(HttpServletRequest req, HttpServletResponse response, Object object, BindException exceptions) throws Exception {
		
    	String selectedPrinter = req.getParameter("printService");
    	
    	getPrintingService().setDefaultPrinter(selectedPrinter);
    	
    	return new ModelAndView(new RedirectView(getSuccessView()));
	}

    @Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return getPrintingService().getAvailablePrinters();
	}

	@Override
	protected Map referenceData(HttpServletRequest arg0) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		
		map.put("defaultPrintService", getPrintingService().getDefaultPrinter());
		
		return map;
	}
	    
}