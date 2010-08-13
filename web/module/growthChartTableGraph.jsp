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
.headerLink {
	color: #FFFFFF; 
}
#graphLegend {
	margin: 15 auto 0 auto;
	width:175px;
	border:1px solid black;
}
</style>

<script type="text/javascript" src="${pageContext.request.contextPath}/moduleResources/growthchart/scripts/jquery/jquery.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/moduleResources/growthchart/scripts/jquery/flot/jquery.flot.js"></script>

<script type="text/javascript">

var graphType = "weightForAge";

var weightSeries = [];
var heightSeries = [];

var $j = jQuery.noConflict();

	var ageHeader = '';
	
	if(${patient.age} <= 3) {
		ageHeader = '<spring:message code="growthchart.table.header.age.months"/>';
	} else {
		ageHeader = '<spring:message code="growthchart.table.header.age.years"/>';
	}

	var weightHTML = "<table id=\"growthChartTable\" class=\"box\"  cellspacing=\"0\" cellpadding=\"2\" class=\"patientEncounters\"><tr><th colspan=\"4\" class=\"tableTitle\"><spring:message code="growthchart.table.header.title.weight"/></th></tr><openmrs:hasPrivilege privilege="Edit Encounters"><th class=\"encounterEdit\" align=\"center\"><spring:message code="general.edit"/></th></openmrs:hasPrivilege><th class=\"encounterDatetimeHeader\"><spring:message code="Encounter.datetime"/></th><th>"+ageHeader+"</th><th><spring:message code="growthchart.table.header.weight"/></th>";
	var heightHTML = "<table id=\"growthChartTable\" class=\"box\"  cellspacing=\"0\" cellpadding=\"2\" class=\"patientEncounters\"><tr><th colspan=\"4\" class=\"tableTitle\"><spring:message code="growthchart.table.header.title.height"/></th></tr><openmrs:hasPrivilege privilege="Edit Encounters"><th class=\"encounterEdit\" align=\"center\"><spring:message code="general.edit"/></th></openmrs:hasPrivilege><th class=\"encounterDatetimeHeader\"><spring:message code="Encounter.datetime"/></th><th>"+ageHeader+"</th><th><spring:message code="growthchart.table.header.height"/></th>";

//alert("weightForAgeSeries=${weightForAgeSeries}");
	
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
				
				if(${patient.age} > 3) { // too old for months to be meaningful
					age = age/12;
				}
	
				age = roundNumber(age, 1);
			
				var date = "<openmrs:formatDate date="${tableRow.obs.obsDatetime}" />";

				var oddEven = '<c:choose><c:when test="${status.count % 2 == 0}">oddRow</c:when><c:otherwise>evenRow</c:otherwise></c:choose>';
				
				weightHTML += "<tr class=\""+oddEven+"\"><openmrs:hasPrivilege privilege="Edit Encounters"><td class=\"encounterEdit\" align=\"center\"><a href=\"growthChart.form?obsId=${tableRow.obs.obsId}&patientId=${patient.patientId}\"><img src=\"${pageContext.request.contextPath}/images/edit.gif\" title=\"<spring:message code="general.edit"/>\" border=\"0\" align=\"top\" /></a></td></openmrs:hasPrivilege><td>"+date+"</td><td>"+age+"</td><td>${tableRow.obs.valueNumeric}</td></tr>";
			</c:forEach>
	</c:when>
	<c:otherwise>
		weightHTML += "<tr><td colspan=\"3\">No Weight Observations</td></tr>";
	</c:otherwise>
	</c:choose>
	
	<c:choose>
	<c:when test="${not empty heightForAgeSeries}">

			// Pull Graph Data
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
			
			if(${patient.age} > 3) { // too old for months to be meaningful
				age = age/12;
			}

			age = roundNumber(age, 1);
		
			var date = "<openmrs:formatDate date="${tableRow.obs.obsDatetime}" />";

			var oddEven = '<c:choose><c:when test="${status.count % 2 == 0}">oddRow</c:when><c:otherwise>evenRow</c:otherwise></c:choose>';
			
			heightHTML += "<tr class=\""+oddEven+"\"><td class=\"encounterEdit\" align=\"center\"><a href=\"growthChart.form?obsId=${tableRow.obs.obsId}&patientId=${patient.patientId}\"><img src=\"${pageContext.request.contextPath}/images/edit.gif\" title=\"<spring:message code="general.edit"/>\" border=\"0\" align=\"top\" /></a></td><td>"+date+"</td><td>"+age+"</td><td>${tableRow.obs.valueNumeric}</td></tr>";

			</c:forEach>
	</c:when>
	<c:otherwise>
		heightHTML += "<tr><td colspan=\"3\">No Height Observations</td></tr>";
	</c:otherwise>
	</c:choose>
	
	weightHTML +="</table>";
	heightHTML +="</table>";

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
		
		load(graphType);
	});
</script>
</head>
<body>
<openmrs:globalProperty key="growthchart.growthChartGraph.maximumAge" var="maxAgeForGraph"/>
<div id="growthChart" style="float:left; width:100%; height:100%">
	<openmrs:hasPrivilege privilege="View Encounters">
	<h4 style="float:left;"><spring:message code="growthchart.growthChartGraph.label"/></h4>
	<div>
		<div id="growthChartNav" style="float:right;">
			<span><a class="nav" onclick="load('weightForAge');"><spring:message code="growthchart.nav.weight.link"/></a>
				  <a class="nav" onclick="load('heightForAge');"><spring:message code="growthchart.nav.height.link"/></a>
			</span>
		</div>
		<br/>
		
		<div style="clear:both;"></div>
		<b id="growthChartTableHeader" class="boxHeader"><span class="growthChartHeader"></span><span style="float:right;"><a class="headerLink" style="color:white;" onclick="drawGraph(graphType);"><spring:message code="growthchart.nav.graph.link"/></a></span></b>
		<div id="growthChartTable" class="box" style="display:none;">
			<table id="growthChartTable" border="1">
			</table>
		</div>
		<div style="clear:both;"></div>
		<b id="growthChartGraphHeader" class="boxHeader"><span class="growthChartHeader"></span><span style="float:right;"><a class="headerLink" style="color:white;" onclick="drawTable(graphType);"><spring:message code="growthchart.nav.table.link"/></a></span></b>
		<div style="clear:both;"></div>
		<div id="growthChartGraph" style="display:none; width:<openmrs:globalProperty key="growthchart.growthChartGraph.width"/>px; height:<openmrs:globalProperty key="growthchart.growthChartGraph.height"/>px">
			&nbsp;
		</div>
		<div style="clear:both;"></div>
		<div id="graphLegend" style="display:none;"></div>
	</div>
	</openmrs:hasPrivilege>
</div>
<div>&nbsp;</div>
</body>
</html>