<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ attribute name="value" required="true" type="java.math.BigDecimal" %>
<%@ attribute name="digits" required="false" type="java.lang.Integer" %>

<c:if test="${empty digits}">
    <c:set var="digits" value="8"/>
</c:if>

<fmt:setLocale value="en_US"/>

<c:choose>
    <c:when test="${empty value}">—</c:when>
    <c:otherwise>
        <fmt:formatNumber value="${value}"
                          minFractionDigits="0"
                          maxFractionDigits="${digits}"
                          groupingUsed="true"/>
    </c:otherwise>
</c:choose>
