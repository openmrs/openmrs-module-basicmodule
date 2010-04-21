<%@ include file="/WEB-INF/template/include.jsp" %>

<style>
#growthChart {
	width:500px;
	height:700px;
}
#growthChartGraph {
	width:400px;
	height:700px;
}
</style>

<script type="text/javascript" src="${pageContext.request.contextPath}/moduleResources/growthchart/scripts/jquery/jquery.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/moduleResources/growthchart/scripts/jquery/flot/jquery.flot.js"></script>

<script type="text/javascript">

	function drawWeightGraph() {
		alert("modelweightforage=${model.weightForAgeSeries}");
		<c:if test="${not empty model.weightForAgeSeries}">
				var $j = jQuery.noConflict();
				var series = [];
				<c:forEach var="series" items="${model.weightForAgeSeries}">
					var data = [];
					alert("series=${series.data}");
					<c:forEach var="point" items="${series.data}">
						data.push([${point.x}, ${point.y}]);
					</c:forEach>
					series.push( { points: { show: ${series.showPoints} }, label: "${series.name}", color: "${series.color}", data: data } );
				</c:forEach>
				var weightForAgeLegend = $j("#weightForAgeLegend");	
				var options = {
					//legend: { container: weightForAgeLegend },
					xaxis: { min: 0 },
					yaxis: { min: 0 },
					lines: { show: true }
				};
				/*
				$j("#weightForAgeGraph").width(700);
				$j("#weightForAgeGraph").height(500);
				alert("width="+$j("#weightForAgeGraph").width());
				alert("height="+$j("#weightForAgeGraph").height());
				//$j("#weightForAgeGraph").css("background-color", "blue");
				
				var weightForAgeDiv = document.getElementById("weightForAgeDiv");
				alert("weightForAgeDiv="+weightForAgeDiv);
				//weightForAgeDiv.style.width = "600px";
				//weightForAgeDiv.style.height = "400px";
				alert("weightForAgeDiv.style.width="+weightForAgeDiv.style.width);
				alert("weightForAgeDiv.style.height="+weightForAgeDiv.style.height);
				
				alert("$j('#weightForAgeDiv')="+$j("#weightForAgeDiv"));
				//$j("#weightForAgeDiv").width(600);
				//$j("#weightForAgeDiv").height(400);
				alert("width="+$j("#weightForAgeDiv").width());
				alert("height="+$j("#weightForAgeDiv").height());
				//$j("#weightForAgeDiv").css("background-color", "red");
				*/
				alert("before plot");
				$j.plot($j("#growthChartGraph"), series, options);
		</c:if>
	}

	function drawHeightGraph() {
		<c:if test="${not empty model.heightForAgeSeries}">
			var $j = jQuery.noConflict();
				var series = [];
				<c:forEach var="series" items="${model.heightForAgeSeries}">
					var data = [];
					<c:forEach var="point" items="${series.data}">
						data.push([${point.x}, ${point.y}]);
					</c:forEach>
					series.push( { points: { show: ${series.showPoints} }, label: "${series.name}", color: "${series.color}", data: data } );
				</c:forEach>
				var heightForAgeLegend = $j("#heightForAgeLegend");	
				var options = {
					//legend: { container: weightForAgeLegend },
					xaxis: { min: 0 },
					yaxis: { min: 0 },
					lines: { show: true }
				};
				
				$j.plot($j("#growthChartGraph"), series, options);
		</c:if>
	}

	$j(document).ready(function() {
		// start first graph
	});
</script>

<openmrs:globalProperty key="growthChartGraph.maximumAge" var="maxAgeForGraph"/>
<div id="growthChart">
	<h4><u><spring:message code="growthchart.growthChartGraph.label"/></u></h4>
	<span><a onclick="drawWeightGraph();">Weight Chart</a>
		  <a onclick="drawHeightGraph();">Height Chart</a>
	</span>
	<c:if test="${ model.patient.age <= maxAgeForGraph }">
		<div id="growthChartGraph" style="width:<openmrs:globalProperty key="growthChartGraph.width"/>px; height:<openmrs:globalProperty key="growthChartGraph.height"/>px"></div>
	</c:if>
</div>