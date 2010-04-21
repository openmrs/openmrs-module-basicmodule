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

	var $j = jQuery.noConflict();

	$j(document).ready(function(){
		
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

		var valid = true;
		var focus = "";
		
		//alert("submitForm()");
		var obsCount = ${fn:length(growthEncounter.obs)};

		var encounterLocation = document.getElementById('location');
		var encounterDatetime = document.getElementById('encounterDatetime');

		// Form Validation of Obs (other fields validated in Spring Validator)
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
		return valid;
	}
</script>
</head>
<body>
<openmrs:hasPrivilege privilege="Edit Encounters">
<div id="growthChart" style="float:left; width:100%; height:100%">
	<h4 style="float:left;"><spring:message code="growthchart.growthChartGraph.label"/></h4>
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
</div>
<div>&nbsp;</div>

</openmrs:hasPrivilege>
</body>
</html>