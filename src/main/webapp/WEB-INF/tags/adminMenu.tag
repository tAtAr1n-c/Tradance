<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="active" required="false" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<nav class="admin-menu">
    <a class="admin-menu-item ${active == 'currencies' ? 'active' : ''}" href="${ctx}/admin/currencies">Монеты</a>
    <a class="admin-menu-item ${active == 'users' ? 'active' : ''}" href="${ctx}/admin/users">Пользователи</a>
    <a class="admin-menu-item ${active == 'orders' ? 'active' : ''}" href="${ctx}/admin/orders">Заявки</a>
    <a class="admin-menu-item ${active == 'trades' ? 'active' : ''}" href="${ctx}/admin/trades">Сделки</a>
    <a class="admin-menu-item ${active == 'fees' ? 'active' : ''}" href="${ctx}/admin/fees">Доход биржи</a>
</nav>
