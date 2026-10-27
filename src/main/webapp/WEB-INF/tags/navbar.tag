<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<nav>
    <a class="brand" href="${ctx}/market">
        <svg width="26" height="26" viewBox="0 0 26 26" aria-hidden="true">
            <rect x="3" y="9" width="5" height="12" rx="1" fill="#FF8A1E"/>
            <rect x="5" y="5" width="1" height="18" fill="#FF8A1E"/>
            <rect x="12" y="4" width="5" height="10" rx="1" fill="#EAECEF"/>
            <rect x="14" y="2" width="1" height="15" fill="#EAECEF"/>
            <rect x="20" y="11" width="4" height="8" rx="1" fill="#FF8A1E"/>
        </svg>
        Tradance
    </a>

    <a href="${ctx}/market">Рынок</a>

    <c:if test="${not empty sessionScope.user}">
        <a href="${ctx}/wallets">Кошельки</a>
        <a href="${ctx}/orders">Мои заявки</a>
        <a href="${ctx}/trades">Мои сделки</a>
    </c:if>

    <c:if test="${sessionScope.user.role == 'ADMIN'}">
        <a href="${ctx}/admin/currencies">Админка</a>
    </c:if>

    <div class="nav-right">
        <c:if test="${empty sessionScope.user}">
            <a href="${ctx}/login">Вход</a>
            <a class="nav-cta" href="${ctx}/signup">Регистрация</a>
        </c:if>

        <c:if test="${not empty sessionScope.user}">
            <a class="nav-user" href="${ctx}/profile">
                <span class="avatar">${fn:toUpperCase(fn:substring(sessionScope.user.username, 0, 1))}</span>
                ${sessionScope.user.username}
            </a>
            <a href="${ctx}/logout">Выход</a>
        </c:if>
    </div>
</nav>
