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
			<b><spring:message code="printing.defaultPrintService"/></b><br/>
			<c:forEach var="service" items="${services}">
				<input type="radio" value="${service.name}" name="printService" <c:if test="${defaultPrintService.name == service.name}">checked</c:if> >
				${service.name} 
				<i style="color: gray">(<c:forEach var="flavor" items="${service.supportedDocFlavors}"><br/>${flavor}</c:forEach>)</i><br/><br/>
			</c:forEach>
			
			<br/><br/>
			
			<input type="submit" value='<spring:message code="general.save"/>'/>
		</form>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
