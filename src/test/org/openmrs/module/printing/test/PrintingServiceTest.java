package test.org.openmrs.module.printing.test;

import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseTest;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.printing.PrintingService;

public class PrintingServiceTest extends BaseTest {
	
	private Log log = LogFactory.getLog(getClass());
	
	/**
	 * Prints a string 
	 * 
	 * @throws Exception
	 */
	public void testStringPrinting() throws Exception {
		
		startup();
		authenticate();
		
		PrintingService printingService = null;
		try {
			printingService = (PrintingService)Context.getService(PrintingService.class);
		}
		catch (APIException e) {
			// service wasn't found?? Be sure we have loaded the module
			log.error("Error while getting printingService", e);
			log.error("Modules: " + ModuleFactory.getLoadedModules());
			log.error("Module loading was attempted from " + ModuleUtil.getModuleRepository().getAbsolutePath());
			fail("Error while getting printingService.  See error log for details");
		}
		
		String testString = "This string should print on the default printer";
		InputStream inStream = new StringBufferInputStream(testString);
		
		printingService.addPrintJob(inStream);
		
		shutdown();
	}
	
}

