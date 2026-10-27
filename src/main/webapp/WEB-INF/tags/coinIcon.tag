<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="ticker" required="true" %>
<%@ attribute name="iconPath" required="false" %>

<c:choose>
    <c:when test="${not empty iconPath}">
        <img class="coin-icon" src="${pageContext.request.contextPath}/files/${iconPath}" alt="">
    </c:when>
    <c:otherwise>
        <span class="coin-icon" data-ticker="${ticker}" aria-hidden="true">${fn:substring(ticker, 0, 1)}</span>
    </c:otherwise>
</c:choose>
