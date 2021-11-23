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

    <script src="${checkoutSDKjs}"></script>
    <div class="row">
        <div class="col-sm-6">
            <div class="checkout-headline">
                <span class="glyphicon glyphicon-lock"></span>
                <spring:theme code="checkout.multi.secure.checkout"/>
            </div>
            <multi-checkout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
                <jsp:body>
                    <ycommerce:testId code="checkoutStepFour">
                        <div class="checkout-paymentmethod">
                            <div class="checkout-indent">

                                <div class="headline"><spring:theme code="checkout.multi.paymentMethod.addPaymentDetails.header"/></div>

                            </div>

                            <div class="container_for_checkout"
                                 data-checkoutrequestdata='${checkoutSDKRequestData}'>
                            </div>

                            <form:form action="submit-payment-data" method="post" id="checkoutSDKResponseForm">
                                <input type="hidden" id="checkoutSDKResponse" name="checkoutSDKResponse"/>
                            </form:form>
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
