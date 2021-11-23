<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="nuvei-address" tagdir="/WEB-INF/tags/addons/nuveiaddon/responsive/address" %>

<form:form modelAttribute="billingAddressForm">
	<nuvei-address:billingAddressFormSelector regions="${regions}" country="${country}" tabindex="12"/>
</form:form>
