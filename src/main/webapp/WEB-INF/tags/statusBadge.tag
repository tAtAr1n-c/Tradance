<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="status" required="true" %>

<c:choose>
    <c:when test="${status == 'OPEN'}">
        <span class="badge badge-open">Открыта</span>
    </c:when>
    <c:when test="${status == 'PARTIAL'}">
        <span class="badge badge-partial">Частично</span>
    </c:when>
    <c:when test="${status == 'FILLED'}">
        <span class="badge badge-filled">Исполнена</span>
    </c:when>
    <c:when test="${status == 'CANCELLED'}">
        <span class="badge badge-cancelled">Отменена</span>
    </c:when>
</c:choose>
