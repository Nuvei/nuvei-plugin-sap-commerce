<%@ attribute name="supportedCountries" required="false" type="java.util.List"%>
<%@ attribute name="regions" required="false" type="java.util.List"%>
<%@ attribute name="country" required="false" type="java.lang.String"%>
<%@ attribute name="tabindex" required="false" type="java.lang.String"%>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement"%>
<%@ taglib prefix="nuvei-address" tagdir="/WEB-INF/tags/addons/nuveiaddon/responsive/address" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="nuveiBillingAddressForm" class="billingAddressForm">
	<c:if test="${not empty country}">
		<nuvei-address:billingAddressFormElements regions="${regions}" country="${country}" tabindex="${tabindex + 1}"/>
	</c:if>
</div>

