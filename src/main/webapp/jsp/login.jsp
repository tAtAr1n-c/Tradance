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
    <div class="auth">
        <span class="auth-step">Вход · шаг 1 из 2</span>
        <h1>Войти в аккаунт</h1>
        <form method="post" action="${pageContext.request.contextPath}/login">
            <label for="username">Логин</label>
            <input type="text" id="username" name="username" value="${form.username}" autocomplete="username">

            <label for="password">Пароль</label>
            <input type="password" id="password" name="password" autocomplete="current-password">

            <button type="submit">Войти в аккаунт</button>

        </form>
        <c:if test="${not empty error}">
            <div class="form-error">${error}</div>
        </c:if>



        <p class="auth-footer">
            Нет аккаунта?
            <a href="${pageContext.request.contextPath}/signup">Зарегистрироваться</a>
        </p>
    </div>

</ui:layout>
