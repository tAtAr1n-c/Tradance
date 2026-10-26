<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<nav>
    <a href="${pageContext.request.contextPath}/market">Рынок</a>

    <c:if test="${empty sessionScope.user}">
        <a href="${pageContext.request.contextPath}/login">Вход</a>
        <a href="${pageContext.request.contextPath}/signup">Регистрация</a>
    </c:if>
    
    <c:if test="${not empty sessionScope.user}">
        <a href="${pageContext.request.contextPath}/wallets">Кошельки</a>
        <a href="${pageContext.request.contextPath}/orders">Мои заявки</a>
        <a href="${pageContext.request.contextPath}/trades">Мои сделки</a>
        <a href="${pageContext.request.contextPath}/logout">Выход</a>
    </c:if>

    <c:if test="${sessionScope.user.role == 'ADMIN'}">
        <a href="${pageContext.request.contextPath}/admin/currencies">Админка</a>
    </c:if>
</nav>
