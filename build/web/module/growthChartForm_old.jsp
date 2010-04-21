<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ include file="/WEB-INF/template/headerMinimal.jsp"%>
<html>
<head>
<style>
.th {
	text-align:left;
}
</style>

<script type="text/javascript" src="${pageContext.request.contextPath}/moduleResources/growthchart/scripts/jquery/jquery.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/moduleResources/growthchart/scripts/jquery/flot/jquery.flot.js"></script>

<script type="text/javascript">

var graphType = "weightForAge";
var cutOffAge = 36;

var weightSeries = [];
var heightSeries = [];

var $j = jQuery.noConflict();

<c:if test="${encounterComplete}">

	var ageHeader = '';
	
	if(${patient.age} <= cutOffAge/12) {
		ageHeader = '<spring:message code="growthchart.table.header.age.months"/>';
	} else {
		ageHeader = '<spring:message code="growthchart.table.header.age.years"/>';
	}

	var weightHTML = "<table id=\"growthChartTable\" class=\"box\"  cellspacing=\"0\" cellpadding=\"2\" class=\"patientEncounters\"><tr><th colspan=\"5\" class=\"tableTitle\"><spring:message code="growthchart.table.header.title.weight"/></th></tr><th class=\"encounterEdit\" align=\"center\"><spring:message code="general.edit"/></th><th class=\"encounterView\" align=\"center\"><spring:message code="general.view"/></th><th class=\"encounterDatetimeHeader\"><spring:message code="Encounter.datetime"/></th><th>"+ageHeader+"</th><th><spring:message code="growthchart.table.header.weight"/></th>";
	var heightHTML = "<table id=\"growthChartTable\" class=\"box\"  cellspacing=\"0\" cellpadding=\"2\" class=\"patientEncounters\"><th class=\"encounterDatetimeHeader\"><spring:message code="Encounter.datetime"/></th><th>"+ageHeader+"</th><th><spring:message code="growthchart.table.header.height"/></th><th class=\"encounterEdit\" align=\"center\"><spring:message code="general.edit"/></th>";
	
	<c:choose>
	<c:when test="${not empty weightForAgeSeries}">
	
			// Pull Graph Data
			<c:forEach var="weightSeries" items="${weightForAgeSeries}">
				var data = [];
				<c:forEach var="point" items="${weightSeries.data}">
					data.push([${point.x}, ${point.y}]);
				</c:forEach>
				weightSeries.push( { points: { show: ${weightSeries.showPoints} }, label: "${weightSeries.name}", color: "${weightSeries.color}", data: data } );
			</c:forEach>
			
			// Pull Table Data
			<c:forEach items="${weightTable}" varStatus="status" var="tableRow">
				
				// Display as Years or Months??
				var age = ${tableRow.months};
				
				if(${patient.age} > cutOffAge/12) { // too old for months to be meaningful
					age = age/12;
				}
	
				age = roundNumber(age, 1);
			
				var date = "<openmrs:formatDate date="${tableRow.obs.obsDatetime}" />";

				var oddEven = '<c:choose><c:when test="${status.count % 2 == 0}">oddRow</c:when><c:otherwise>evenRow</c:otherwise></c:choose>';
				
				weightHTML += "<tr class=\""+oddEven+"\"><td class=\"encounterView\" align=\"center\"><a href=\"growthChart.form?obsId=${tableRow.obs.obsId}&patientId=${patient.patientId}\"><img src=\"${pageContext.request.contextPath}/images/file.gif\" title=\"<spring:message code="general.view"/>\" border=\"0\" align=\"top\" /></a></td><td class=\"encounterEdit\" align=\"center\"><a href=\"growthChart.form?obsId=${tableRow.obs.obsId}&patientId=${patient.patientId}\"><img src=\"${pageContext.request.contextPath}/images/edit.gif\" title=\"<spring:message code="general.edit"/>\" border=\"0\" align=\"top\" /></a></td><td>"+date+"</td><td>"+age+"</td><td>${tableRow.obs.valueNumeric}</td></tr>";
			</c:forEach>
	</c:when>
	<c:otherwise>
		weightHTML += "<tr><td colspan=\"3\">No Weight Observations</td></tr>";
	</c:otherwise>
	</c:choose>
	
	<c:choose>
	<c:when test="${not empty heightForAgeSeries}">
			<c:forEach var="heightSeries" items="${heightForAgeSeries}">
				var data = [];
				<c:forEach var="point" items="${heightSeries.data}">
					data.push([${point.x}, ${point.y}]);
				</c:forEach>
				heightSeries.push( { points: { show: ${heightSeries.showPoints} }, label: "${heightSeries.name}", color: "${heightSeries.color}", data: data } );
			</c:forEach>
			// Pull Table Data
			<c:forEach items="${heightTable}" varStatus="status" var="tableRow">

				// Display as Years or Months??
				var age = ${tableRow.months};
				
				if(${patient.age} > cutOffAge/12) { // too old for months to be meaningful
					age = age/12;
				}
	
				age = roundNumber(age, 1);
			
				var date = "<openmrs:formatDate date="${tableRow.obs.obsDatetime}" />";
			
				heightHTML += "<tr><td>"+date+"</td><td>"+age+"</td><td>${tableRow.obs.valueNumeric}</td><td><a href=\"growthChart.form?obsId=${tableRow.obs.obsId}&patientId=${patient.patientId}\"><img src=\"${pageContext.request.contextPath}/images/edit.gif\" title=\"<spring:message code="general.edit"/>\" border=\"0\" align=\"top\" /></a></td></tr>";
			</c:forEach>
	</c:when>
	<c:otherwise>
		heightHTML += "<tr><td colspan=\"3\">No Height Observations</td></tr>";
	</c:otherwise>
	</c:choose>
	
	weightHTML +="</table>";
	heightHTML +="</table>";

</c:if>

	function roundNumber(rnum, rlength) { // Arguments: number to round, number of decimal places
	  	return Math.round(rnum*Math.pow(10,rlength))/Math.pow(10,rlength);
	}

	function drawGraph(graphType) {
		//alert("drawGraph("+graphType+")");

		$j("#growthChartTableHeader").hide();
		$j("#growthChartTable").hide();

		var series;
		if(graphType == 'weightForAge') {
			series = weightSeries;
		} else if(graphType == 'heightForAge') {
			series = heightSeries;
		}

		var tickmin = 0;
		var tickmax = 0;
		//alert("series.length="+series.length);
		for(i=0; i<series.length; i++) {
			var seriesRow = series[i];
			//alert("seriesRow="+seriesRow);
			var seriesData = seriesRow.data;
			//alert("seriesData="+seriesData);
			var seriesLength = seriesData.length-1;
			//alert("seriesLength="+seriesLength);
			if(seriesLength > tickmax) {
				tickmax = seriesLength;
			}
		}
		
		//tickmax = ${fn:length(weightForAgeSeries)};
		
		//alert("tickmax="+tickmax);
		
		if(series != '') {
			//alert("inside series!!! of drawGraph()");
			
			var graphLegend = document.getElementById('graphLegend');
			var xaxis = {"min": tickmin, "max": tickmax};

			xaxis["ticks"] = xTickGenerator(xaxis);
			//alert("xaxis="+xaxis);
			
			var options = {
				legend: { show: true, container: graphLegend }, 
				lines: { show: true },
				//xaxis: { min: 0, ticks: [[0, ""], [1, ""], [2, ""], [3, "3 mo"], [4, ""], [5, ""], [6, "6 mo"], [7, ""], [8, ""], [9, "9 mo"], [10, ""], [11, ""], [12, "1 year"], [13, ""], [14, ""], [15, "15 mo"], [16, ""], [17, ""], [18, "18 mo"], [19, ""], [20, ""], [21, "21 mo"], [22, ""], [23, ""], [24, "2 years"], [25, ""], [26, ""], [27, "27 mo"], [28, ""], [29, ""], [30, "30 mo"], [31, ""], [32, ""], [33, "33 mo"], [34, ""], [35, ""], [36, "3 years"]], max: 37 }
				yaxis: { tickFormatter: yTickGenerator }
			};

			options['xaxis'] = xaxis;
			
			//alert("options="+options);
			
			//alert("inside graphType="+graphType);
			
			if(graphType == "heightForAge") {
				$j(".growthChartHeader").html("Height For Age");
			} else {
				$j(".growthChartHeader").html("Weight For Age");
			}
			
			$j("#growthChartGraphHeader").show();
			$j("#graphLegend").show();
			$j("#growthChartGraph").show();
			var myPlot =  $j.plot($j("#growthChartGraph"), series, options);

			//alert("myPlot="+myPlot);
			//var yaxis = myPlot.getAxes(1);
			//alert("yaxis="+yaxis);
			//var yticks = getAxes().yaxis.ticks;
			//alert("yticks="+yticks);
		}
	}

	function xTickGenerator(xaxis) {
	    var res = [], tick = xaxis.min;
	    //alert("xaxis.min="+xaxis.min+"  xaxis.max="+xaxis.max);
	    while (tick < xaxis.max) {
		    if(tick == 0) {
		    	res.push([tick, ""]);
		    } else if (tick == 12) {
		    	res.push([tick, "1 yr"]);
		    } else if (tick % 12 == 0) {
	    		res.push([tick, (tick/12) + " yrs"]);
		    } else if (tick % 3 == 0 && xaxis.max < 40) {
		    	res.push([tick, tick + " mo"]); 
		    } else {
		    	res.push([tick, ""]);
		    }
	    	tick++;
	    }
	    //alert("res="+res);
	    return res;
	  }

	function yTickGenerator(val) { 
		if(graphType == 'weightForAge') {
			return val + " kg";
		} else {
			return val + " cm";
		}
	} 
		

	function drawTable(graphType) {
		//alert("drawTable("+graphType+")");

		$j("#growthChartGraphHeader").hide();
		$j("#graphLegend").hide();
		$j("#growthChartGraph").hide();

		var series;
		var html;
		if(graphType == 'weightForAge') {
			series = weightSeries;
			html = weightHTML;
		} else if(graphType == 'heightForAge') {
			series = heightSeries;
			html = heightHTML;
		}

		if(graphType == "heightForAge") {
			$j(".growthChartHeader").html("Height For Age");
		} else {
			$j(".growthChartHeader").html("Weight For Age");
		}
		
		if(series != '') {
			//alert("inside series!!! of drawTable()");
			$j("#growthChartTable").html(html);
		}

		$j("#growthChartTableHeader").show();
		$j("#growthChartTable").show();
	}

	function load(graphType) {
		//alert("load("+graphType+")");
		this.graphType = graphType;
		
		drawTable(graphType);
	}

	$j(document).ready(function(){
		
		if(${encounterComplete}) {
			load(graphType);
		}
	});

	function validObs(valueNumeric) {
		//alert("validateObs("+valueNumeric+")");
		
		// is the number a double??
		var pattern = /^\d+.?\d*$/;

		if ( valueNumeric.match(pattern)==null ) {
			return false;
		} else {
			return true;
		}
	}

	function submitForm() {

		// Form Validation of Obs (other fields validated in Spring Validator)
		var valid = true;
		var focus = "";
		
		//alert("submitForm()");
		var obsCount = ${fn:length(growthEncounter.obs)};

		var encounterLocation = document.getElementById('location');
		var encounterDatetime = document.getElementById('encounterDatetime');

		if($j("#weightValue").val() == null || $j("#weightValue").val() == "") {
			//alert("weight null or empty");
			valid = false;
			var message = '<spring:message code="growthchart.weight.value.empty" />';
			$j("#weightValueError").addClass("error");
			$j("#weightValueError").html(message);
		} else if(!validObs($j("#weightValue").val())) {
			valid = false;
			var message = '<spring:message code="growthchart.weight.value.error" />';
			$j("#weightValueError").addClass("error");
			$j("#weightValueError").html(message);
		}

		if($j("#heightValue").val() == null || $j("#heightValue").val() == "") {
			//alert("height null or empty");
			valid = false;
			var message = '<spring:message code="growthchart.height.value.empty" />';
			$j("#heightValueError").addClass("error");
			$j("#heightValueError").html(message);
		} else if(!validObs($j("#heightValue").val())) {
			valid = false;
			var message = '<spring:message code="growthchart.height.value.error" />';
			$j("#heightValueError").addClass("error");
			$j("#heightValueError").html(message);
		}
		
		//alert("encounterLocation="+encounterLocation.value+"   encounterDatetime="+encounterDatetime.value);

		// Set Obs Location and Obs DateTime the same as the Encounter Location and DateTime
		/*
		for(i=0; i<obsCount; i++) {
			var obsLocationName = "obs["+i+"].location";
			var obsDatetimeName = "obs["+i+"].obsDatetime";
			//alert("BEFORE obsLocationName="+obsLocationName+"   obsDatetimeName="+obsDatetimeName);
			var obsLocation = document.getElementById(obsLocationName);
			var obsDatetime = document.getElementById(obsDatetimeName);

			obsLocation.value = encounterLocation.value;
			obsDatetime.value = encounterDatetime.value;
			//alert("AFTER obsLocation="+obsLocation.value+"   obsDatetime="+obsDatetime.value);
		}
		*/

		//alert("valid="+valid);
		return valid;
	}
//alert("location=${growthEncounter.location}");
</script>
</head>
<body>
<openmrs:globalProperty key="growthChartGraph.maximumAge" var="maxAgeForGraph"/>
<div id="growthChart" style="float:left; width:100%; height:100%">
	<h4 style="float:left;"><spring:message code="growthchart.growthChartGraph.label"/></h4>
	<c:choose>
	<c:when test="${ patient.age <= maxAgeForGraph && !encounterComplete }">
		<div style="clear:both;"></div>
		<b class="boxHeader"><spring:message code="Encounter.summary"/></b>
		<div id="growthChartForm" class="box">
			<form:form method="post" commandName="growthEncounter" onsubmit="return submitForm();">
        <table cellpadding="3" cellspacing="0">
		<tr>
			<th><spring:message code="Encounter.provider"/></th>
			<td><spring:bind path="provider">
				<openmrs_tag:userField formFieldName="${status.expression}"
					initialValue="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.location"/></th>
			<td><spring:bind path="growthEncounter.location">
				<openmrs_tag:locationField formFieldName="${status.expression}"
					initialValue="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Encounter.datetime"/></th>
			<td>
				<spring:bind path="encounterDatetime">
					<input type="text" id="${status.expression}" name="${status.expression}" size="10" 
						   value="${status.value}" onClick="showCalendar(this)" />
				   (<spring:message code="general.format"/>: <openmrs:datePattern />)
				   <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
			<td></td>
		</tr>
		<!-- 
        <c:forEach items="${growthEncounter.obs}" varStatus="status" var="obs">
        
        <tr>
        	<td colspan="2">${obs.concept.name}</td>
        </tr>
        <tr>
			<td>Value Numeric:</td>
			<td><form:input path="obs[${status.index}].valueNumeric" /></td>
			<td><form:errors path="obs[${status.index}].valueNumeric" cssClass="error" /></td>
		</tr>
        <tr>
        	<td colspan="2"><spring:bind path="obs[${status.index}].location">
        		<input type="hidden" id="${status.expression}" name="${status.expression}" value="${status.value}" />
        	</spring:bind></td>
        </tr>
        <tr>
        	<td colspan="2"><spring:bind path="obs[${status.index}].obsDatetime">
        		<input type="hidden" id="${status.expression}" name="${status.expression}" value="${status.value}" />
        	</spring:bind></td>
        </tr>
        <script>
        	if(${obs.valueNumeric} == 0) {
        		//clearInput('obs${status.index}.valueNumeric');
        	}
        </script>
		</c:forEach> 
		 -->
		 <tr>
		 	<th><spring:message code="growthchart.height.label"/></th>
		 	<td><input type="text" id="heightValue" name="heightValue" value="${heightValue}">
		 		<span id="heightValueError"></span>
		 	</td>
		 	<td id="heightValueError"></td>
        </tr>
		 <tr>
		 	<th><spring:message code="growthchart.weight.label"/></th>
		 	<td><input type="text" id="weightValue" name="weightValue" value="${weightValue}">
		 		<span id="weightValueError"></span>
		 	</td>
        </tr>
        </table>
        <br>
	    <input type="submit" align="center" value="<spring:message code="Encounter.save"/>">
	    &nbsp;
		<input type="button" value='<spring:message code="general.cancel"/>' onclick="history.go(-1); return;">
</form:form>
		</div>
	</c:when>
	<c:otherwise>
		<div">
			<div id="growthChartNav" style="float:right;">
				<span><a class="nav" onclick="load('weightForAge');"><spring:message code="growthchart.nav.weight.link"/></a>
					  <a class="nav" onclick="load('heightForAge');"><spring:message code="growthchart.nav.height.link"/></a>
				</span>
			</div>
			<br/>
			
			<div style="clear:both;"></div>
			<b id="growthChartTableHeader" class="boxHeader"><span class="growthChartHeader"></span><span style="float:right;color:white;"><a onclick="drawGraph(graphType);"><spring:message code="growthchart.nav.graph.link"/></a></span></b>
			<div id="growthChartTable" class="box" style="display:none;">
				<table id="growthChartTable" border="1">
				</table>
			</div>
			<div style="clear:both;"></div>
			<b id="growthChartGraphHeader" class="boxHeader"><span class="growthChartHeader"></span><span style="float:right;color:white;"><a onclick="drawTable(graphType);"><spring:message code="growthchart.nav.table.link"/></a></span></b>
			<div style="clear:both;"></div>
			<div id="graphLegend" style="display:none;float:right;"></div>
			<div style="clear:both;"></div>
			<div id="growthChartGraph" style="display:none; width:<openmrs:globalProperty key="growthChartGraph.width"/>px; height:<openmrs:globalProperty key="growthChartGraph.height"/>px">
				&nbsp;
			</div>
		</div>
	</div>
	</c:otherwise>
	</c:choose>
</div>
<div>&nbsp;</div>
</body>
</html>