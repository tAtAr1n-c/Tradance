<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="field" required="true" %>

<c:if test="${not empty errors[field]}">
    <div class="field-error">${errors[field]}</div>
</c:if>
