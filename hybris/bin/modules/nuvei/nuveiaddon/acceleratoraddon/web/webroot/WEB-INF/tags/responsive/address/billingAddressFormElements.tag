<%@ attribute name="regions" required="true" type="java.util.List"%>
<%@ attribute name="country" required="false" type="java.lang.String"%>
<%@ attribute name="tabindex" required="false" type="java.lang.Integer"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>

<c:choose>
	<c:when test="${country == 'US'}">
		<formElement:formSelectBoxDefaultEnabled idKey="address.title" labelKey="address.title" path="titleCode" mandatory="true" skipBlank="false" skipBlankMessageKey="address.title.none" items="${titles}" selectedValue="${titleCode}" tabindex="${tabindex + 1}" selectCSSClass="form-control"/>
		<formElement:formInputBox idKey="address.firstName" labelKey="address.firstName" path="firstName" inputCSS="text" mandatory="true" tabindex="${tabindex + 2}"/>
		<formElement:formInputBox idKey="address.surname" labelKey="address.surname" path="lastName" inputCSS="text" mandatory="true" tabindex="${tabindex + 3}"/>
		<formElement:formInputBox idKey="address.line1" labelKey="address.line1" path="line1" inputCSS="text" mandatory="true" tabindex="${tabindex + 4}"/>
		<formElement:formInputBox idKey="address.line2" labelKey="address.line2" path="line2" inputCSS="text" mandatory="false" tabindex="${tabindex + 5}"/>
		<formElement:formInputBox idKey="address.townCity" labelKey="address.townCity" path="townCity" inputCSS="text" mandatory="true" tabindex="${tabindex + 6}"/>
		<formElement:formSelectBox idKey="address.region" labelKey="address.regionIso" path="regionIso" mandatory="true" skipBlank="false" skipBlankMessageKey="address.selectregionIso" items="${regions}" itemValue="isocodeShort" tabindex="${tabindex + 7}" selectCSSClass="form-control"/>
		<formElement:formInputBox idKey="address.postcode" labelKey="address.postcode" path="postcode" inputCSS="text" mandatory="true" tabindex="${tabindex + 8}"/>
		<formElement:formInputBox idKey="address.phone" labelKey="address.phone" path="phone" inputCSS="text" mandatory="false" tabindex="${tabindex + 9}"/>
	</c:when>
	<c:when test="${country == 'CA'}">
		<formElement:formSelectBoxDefaultEnabled idKey="address.title" labelKey="address.title" path="titleCode" mandatory="true" skipBlank="false" skipBlankMessageKey="address.title.none" items="${titles}" selectedValue="${titleCode}" tabindex="${tabindex + 1}" selectCSSClass="form-control"/>
		<formElement:formInputBox idKey="address.firstName" labelKey="address.firstName" path="firstName" inputCSS="text" mandatory="true" tabindex="${tabindex + 2}"/>
		<formElement:formInputBox idKey="address.surname" labelKey="address.surname" path="lastName" inputCSS="text" mandatory="true" tabindex="${tabindex + 3}"/>
		<formElement:formInputBox idKey="address.line1" labelKey="address.line1" path="line1" inputCSS="text" mandatory="true" tabindex="${tabindex + 4}"/>
		<formElement:formInputBox idKey="address.line2" labelKey="address.line2" path="line2" inputCSS="text" mandatory="false" tabindex="${tabindex + 5}"/>
		<formElement:formInputBox idKey="address.townCity" labelKey="address.townCity" path="townCity" inputCSS="text" mandatory="true" tabindex="${tabindex + 6}"/>
		<formElement:formSelectBox idKey="address.region" labelKey="address.province" path="regionIso" mandatory="true" skipBlank="false" skipBlankMessageKey="address.selectProvince" items="${regions}" itemValue="isocodeShort" tabindex="${tabindex + 7}" selectCSSClass="form-control"/>
		<formElement:formInputBox idKey="address.postcode" labelKey="address.postcode" path="postcode" inputCSS="text" mandatory="true" tabindex="${tabindex + 8}"/>
		<formElement:formInputBox idKey="address.phone" labelKey="address.phone" path="phone" inputCSS="text" mandatory="false" tabindex="${tabindex + 9}"/>		
	</c:when>
	<c:when test="${country == 'CN'}">
		<formElement:formInputBox idKey="address.postcode" labelKey="address.postcode" path="postcode" inputCSS="text" mandatory="true" tabindex="${tabindex + 2}"/>
		<formElement:formSelectBox idKey="address.region" labelKey="address.province" path="regionIso" mandatory="true" skipBlank="false" skipBlankMessageKey="address.selectProvince" items="${regions}" itemValue="isocodeShort" tabindex="${tabindex + 3}" selectCSSClass="form-control"/>
		<formElement:formInputBox idKey="address.townCity" labelKey="address.townCity" path="townCity" inputCSS="text" mandatory="true" tabindex="${tabindex + 4}"/>
		<formElement:formInputBox idKey="address.line1" labelKey="address.district_and_street" path="line1" inputCSS="text" mandatory="true" tabindex="${tabindex + 5}"/>
		<formElement:formInputBox idKey="address.line2" labelKey="address.building_and_room" path="line2" inputCSS="text" mandatory="false" tabindex="${tabindex + 6}"/>
		<formElement:formInputBox idKey="address.surname" labelKey="address.surname" path="lastName" inputCSS="text" mandatory="true" tabindex="${tabindex + 7}"/>
		<formElement:formInputBox idKey="address.firstName" labelKey="address.firstName" path="firstName" inputCSS="text" mandatory="true" tabindex="${tabindex + 8}"/>
		<formElement:formSelectBoxDefaultEnabled idKey="address.title" labelKey="address.title" path="titleCode" mandatory="true" skipBlank="false" skipBlankMessageKey="address.title.none" items="${titles}" selectedValue="${titleCode}" tabindex="${tabindex + 1}" selectCSSClass="form-control"/>
		<formElement:formInputBox idKey="address.phone" labelKey="address.phone" path="phone" inputCSS="text" mandatory="false" tabindex="${tabindex + 9}"/>
	</c:when>
	<c:when test="${country == 'JP'}">
		<formElement:formSelectBoxDefaultEnabled idKey="address.title" labelKey="address.title" path="titleCode" mandatory="true" skipBlank="false" skipBlankMessageKey="address.title.none" items="${titles}" selectedValue="${titleCode}" tabindex="${tabindex + 1}" selectCSSClass="form-control"/>
		<formElement:formInputBox idKey="address.surname" labelKey="address.surname" path="lastName" inputCSS="text" mandatory="true" tabindex="${tabindex + 2}"/>
		<formElement:formInputBox idKey="address.firstName" labelKey="address.firstName" path="firstName" inputCSS="text" mandatory="true" tabindex="${tabindex + 3}"/>
		<formElement:formInputBox idKey="address.postcode" labelKey="address.postcodeJP" path="postcode" inputCSS="text" mandatory="true" tabindex="${tabindex + 4}"/>
		<formElement:formSelectBox idKey="address.region" labelKey="address.prefecture" path="regionIso" mandatory="true" skipBlank="false" skipBlankMessageKey="address.selectPrefecture" items="${regions}" itemValue="isocodeShort" tabindex="${tabindex + 5}" selectCSSClass="form-control"/>
		<formElement:formInputBox idKey="address.townCity" labelKey="address.townJP" path="townCity" inputCSS="text" mandatory="true" tabindex="${tabindex + 6}"/>
		<formElement:formInputBox idKey="address.line2" labelKey="address.subarea" path="line2" inputCSS="text" mandatory="true" tabindex="${tabindex + 7}"/>
		<formElement:formInputBox idKey="address.line1" labelKey="address.furtherSubarea" path="line1" inputCSS="text" mandatory="true" tabindex="${tabindex + 8}"/>
		<formElement:formInputBox idKey="address.phone" labelKey="address.phone" path="phone" inputCSS="text" mandatory="false" tabindex="${tabindex + 9}"/>	
	</c:when>
	<c:otherwise>
		<formElement:formSelectBoxDefaultEnabled idKey="address.title" labelKey="address.title" path="titleCode" mandatory="true" skipBlank="false" skipBlankMessageKey="address.title.none" items="${titles}" selectedValue="${titleCode}" tabindex="${tabindex + 1}" selectCSSClass="form-control"/>
		<formElement:formInputBox idKey="address.firstName" labelKey="address.firstName" path="firstName" inputCSS="text" mandatory="true" tabindex="${tabindex + 2}"/>
		<formElement:formInputBox idKey="address.surname" labelKey="address.surname" path="lastName" inputCSS="text" mandatory="true" tabindex="${tabindex + 3}"/>
		<formElement:formInputBox idKey="address.line1" labelKey="address.line1" path="line1" inputCSS="text" mandatory="true" tabindex="${tabindex + 4}"/>
		<formElement:formInputBox idKey="address.line2" labelKey="address.line2" path="line2" inputCSS="text" mandatory="false" tabindex="${tabindex + 5}"/>
		<formElement:formInputBox idKey="address.townCity" labelKey="address.townCity" path="townCity" inputCSS="text" mandatory="true" tabindex="${tabindex + 6}"/>
		<formElement:formInputBox idKey="address.postcode" labelKey="address.postcode" path="postcode" inputCSS="text" mandatory="true" tabindex="${tabindex + 7}"/>
		<formElement:formInputBox idKey="address.phone" labelKey="address.phone" path="phone" inputCSS="text" mandatory="false" tabindex="${tabindex + 9}"/>	
	</c:otherwise>
</c:choose>
