package org.openmrs.module.basicModule.extension.html;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.Extension;

public class AdminList extends Extension {

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	public String getTitle() {
		return "basicModule.title";
	}
	
	public Map<String, String> getLinks() {
		
		Map<String, String> map = new HashMap<String, String>();
		
		//map.put("module/helloWorld/addResponse.form", "helloWorld.addResponse");
		
		return map;
	}
	
}
