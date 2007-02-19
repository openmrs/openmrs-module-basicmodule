package org.openmrs.module.basicmodule.extension.html;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.Extension;

public class AdminList extends Extension {

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	public String getTitle() {
		return "basicmodule.title";
	}
	
	public Map<String, String> getLinks() {
		
		Map<String, String> map = new HashMap<String, String>();
		
		return map;
	}
	
}
