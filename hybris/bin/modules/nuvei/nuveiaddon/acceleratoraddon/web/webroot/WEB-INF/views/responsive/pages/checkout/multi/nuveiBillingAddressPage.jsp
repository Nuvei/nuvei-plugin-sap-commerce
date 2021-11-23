<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="multi-checkout" tagdir="/WEB-INF/tags/responsive/checkout/multi" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="nuvei-address" tagdir="/WEB-INF/tags/addons/nuveiaddon/responsive/address" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">

    <div class="row">
        <div class="col-sm-6">
            <div class="checkout-headline">
                <span class="glyphicon glyphicon-lock"></span>
                <spring:theme code="checkout.multi.secure.checkout"/>
            </div>
            <multi-checkout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
                <jsp:body>
                    <ycommerce:testId code="checkoutStepThree">
                        <div class="checkout-paymentmethod">
                            <div class="checkout-indent">

                                <div class="headline"><spring:theme code="checkout.multi.paymentMethod.addPaymentDetails.billingAddress"/></div>

                                <form:form modelAttribute="billingAddressForm" action="add-billing-address" method="POST"
                                           id="billingAddressPostForm">
                                    <c:if test="${cartData.deliveryItemsQuantity > 0}">
                                        <div id="useDeliveryAddressData"
                                             data-title="${fn:escapeXml(deliveryAddress.title)}"
                                             data-firstname="${fn:escapeXml(deliveryAddress.firstName)}"
                                             data-lastname="${fn:escapeXml(deliveryAddress.lastName)}"
                                             data-line1="${fn:escapeXml(deliveryAddress.line1)}"
                                             data-line2="${fn:escapeXml(deliveryAddress.line2)}"
                                             data-town="${fn:escapeXml(deliveryAddress.town)}"
                                             data-postalcode="${fn:escapeXml(deliveryAddress.postalCode)}"
                                             data-countryisocode="${fn:escapeXml(deliveryAddress.country.isocode)}"
                                             data-regionisocode="${fn:escapeXml(deliveryAddress.region.isocodeShort)}"
                                             data-address-id="${fn:escapeXml(deliveryAddress.id)}">
                                        </div>
                                        <formElement:formCheckbox
                                                path="useDeliveryAddress"
                                                idKey="nuveiUseDeliveryAddress"
                                                labelKey="checkout.multi.sop.useMyDeliveryAddress"
                                                tabindex="11"/>
                                    </c:if>
                                    <div id="billingCountrySelector" data-address-code="${fn:escapeXml(cartData.deliveryAddress.id)}" data-country-iso-code="${fn:escapeXml(cartData.deliveryAddress.country.isocode)}" data-display-title="false" class="clearfix">
                                        <formElement:formSelectBox idKey="address.country"
                                                                   labelKey="address.country" path="countryIso" mandatory="true" skipBlank="false"
                                                                   skipBlankMessageKey="address.selectCountry" items="${supportedCountries}" itemValue="isocode"
                                                                   tabindex="${tabindex}" selectCSSClass="form-control" />
                                    </div>

                                    <nuvei-address:billingAddressFormSelector supportedCountries="${countries}"
                                                                          regions="${regions}" tabindex="12"/>
                                </form:form>
                            </div>
                            <button type="button" id="nextToPaymentMethod" class="btn btn-primary btn-block checkout-next">
                                <spring:theme code="checkout.multi.paymentMethod.continue"/>
                            </button>
                        </div>

                    </ycommerce:testId>
                </jsp:body>
            </multi-checkout:checkoutSteps>
        </div>

        <div class="col-sm-6 hidden-xs">
            <multi-checkout:checkoutOrderDetails cartData="${cartData}" showDeliveryAddress="false"
                                                 showPaymentInfo="false" showTaxEstimate="false" showTax="true"/>
        </div>

        <div class="col-sm-12 col-lg-12">
            <cms:pageSlot position="SideContent" var="feature" element="div" class="checkout-help">
                <cms:component component="${feature}"/>
            </cms:pageSlot>
        </div>
    </div>

</template:page>
