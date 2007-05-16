<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Printers" otherwise="/login.htm" redirect="/module/printing/summary.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="printing.printerSetup" /></h2>

<div id="printers">
	<b class="boxHeader">
		<spring:message code="general.properties" />
	</b>
	<div class="box">
		<form action="" method="post">
			<b><spring:message code="printing.defaultPrintService"/></b><br/>
			<c:forEach var="service" items="${services}">
				<input type="radio" value="${service.name}" id="${service.name}" name="printService" <c:if test="${defaultPrintService.name == service.name}">checked</c:if> >
				<label for="${service.name}">${service.name}</label> <br/>
				
				<i style="color: gray; display: none;">Supported DocFlavors: <br><c:forEach var="flavor" items="${service.supportedDocFlavors}"><br/>${flavor}</c:forEach><br/></i>
			</c:forEach>
			
			<br/>
			
			<input type="submit" value='<spring:message code="general.save"/>'/>
		</form>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
