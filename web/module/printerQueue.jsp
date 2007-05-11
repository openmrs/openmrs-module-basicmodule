<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Printers" otherwise="/login.htm" redirect="/module/printing/summary.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="printing.setup" /></h2>

<div id="printers">
	<b class="boxHeader">
		<spring:message code="printing.choosePrinter" />
	</b>
	<div class="box">
		<form action="" method="post">
			<b><spring:message code="printing.selectedPrintService"/></b><br/>
			<c:forEach var="service" items="${services}">
				<input type="radio" value="${service.name}" name="printService" <c:if test="${selectedPrintService == service.name}">checked</c:if> >
				${service.name} 
				<i>(<c:forEach var="flavor" items="${service.supportedDocFlavors}"><br/>${flavor}</c:forEach>)</i><br/><br/>
			</c:forEach>
			
			<br/><br/>
			
			<input type="submit" value='<spring:message code="general.save"/>'/>
		</form>
		
		<!-- 
		<table width="90%">
			<tr>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.description" /></th>
				<th><spring:message code="general.preferred" /></th>
				<th><spring:message code="general.createdBy" /></th>
				
			</tr>
		<c:forEach var="summary" items="${summaries}" varStatus="varStatus">
			<tr>
				<td><a href="summary.form?clinicalSummaryId=${summary.clinicalSummaryId}">${summary.name}</a></td>
				<td>${summary.description}</td>
				<td><c:if test="${summary.preferred}"><spring:message code="general.yes" /></c:if></td>
				<td>${summary.creator.firstName} ${summary.creator.lastName} - <openmrs:formatDate date="${summary.dateCreated}" type="long" /></td>
			</tr>
		</c:forEach>
		</table>
		-->
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
