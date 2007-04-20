package org.openmrs.module.datadeletion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Activator;

public class DataDeletionActivator implements Activator {

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * @see org.openmrs.module.Activator#startup()
	 */
	public void startup() {
		log.info("Starting Data Deletion Module");
	}
	
	/**
	 *  @see org.openmrs.module.Activator#shutdown()
	 */
	public void shutdown() {
		log.info("Shutting down Data Deletion Module");
	}
	
}
