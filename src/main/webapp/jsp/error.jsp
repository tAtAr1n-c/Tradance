<%--
  Created by IntelliJ IDEA.
  User: peace
  Date: 23.10.2026
  Time: 13:19
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<ui:layout title="404">
    <div class="error-page">
        <span class="error-code">Ошибка 404</span>
        <h1>Такой страницы нет</h1>
        <p>
            <c:choose>
                <c:when test="${not empty errorMessage}">${errorMessage}</c:when>
                <c:otherwise>Возможно, ссылка устарела или в адресе опечатка.</c:otherwise>
            </c:choose>
        </p>
        <div class="error-links">
            <a href="${ctx}/market">Перейти на рынок</a>
            <c:if test="${not empty sessionScope.user}">
                <a class="error-link-muted" href="${ctx}/wallets">Мои кошельки</a>
            </c:if>
        </div>
    </div>
</ui:layout>
