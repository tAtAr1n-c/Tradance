<%--
  Created by IntelliJ IDEA.
  User: peace
  Date: 23.10.2026
  Time: 13:18
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<ui:layout title="Вход">
    <span class="auth-step">Вход · шаг 2 из 2</span>
    <h1>Проверочные слова</h1>
    <c:if test="${not empty error}">
        <div class="form-error">${error}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/login/verify">
        <c:forEach items="${positions}" var="pos">
            <label>Слово №${pos}</label>
            <input name="words" autocomplete="off">
        </c:forEach>
        <button type="submit">Войти</button>
    </form>

</ui:layout>
