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
        <c:if test="${param.logout == '1'}">
            <div class="flash">Вы вышли из аккаунта</div>
        </c:if>

        <span class="auth-step">Вход · шаг 1 из 2</span>
        <h1>Войти в аккаунт</h1>
        <p>Введите логин и пароль. На следующем шаге спросим три слова из секретной фразы.</p>

        <c:if test="${not empty error}">
            <div class="form-error">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/login">
            <label for="username">Логин</label>
            <input type="text" id="username" name="username" value="${form.username}" autocomplete="username">

            <label for="password">Пароль</label>
            <input type="password" id="password" name="password" autocomplete="current-password">

            <button type="submit">Продолжить</button>
        </form>

        <p class="auth-footer">
            Нет аккаунта?
            <a href="${pageContext.request.contextPath}/signup">Зарегистрироваться</a>
        </p>
    </div>
</ui:layout>
