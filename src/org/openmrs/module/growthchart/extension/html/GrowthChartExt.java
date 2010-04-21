package org.openmrs.module.growthchart.extension.html;

import org.openmrs.module.web.extension.LinkExt;

public class GrowthChartExt extends LinkExt {

	@Override
	public String getLabel() {
		return "Get Growth Chart";
	}

	@Override
	public String getRequiredPrivilege() {
		//TODO check privileges
		return null;
	}

	@Override
	public String getUrl() {
		return "module/growthchart/growthChart.htm";
	}

}
