<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${not empty flash}">
    <div class="flash">${flash}</div>
    <c:remove var="flash" scope="session"/>
</c:if>
