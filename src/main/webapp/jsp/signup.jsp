<%--
  Created by IntelliJ IDEA.
  User: peace
  Date: 23.10.2026
  Time: 13:18
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>


<ui:layout title="Регистрация">
    <div class="auth">
        <span class="auth-step">Регистрация · шаг 1 из 2</span>
        <h1>Создать аккаунт</h1>
        <p>После регистрации на счёт придёт 10 000 USDT для торговли.</p>

        <form method="post" action="${pageContext.request.contextPath}/signup">

            <label for="username">Логин</label>
            <input type="text" id="username" name="username" value="${form.username}" autocomplete="username">
            <ui:fieldError field="username"/>

            <label for="email">Email</label>
            <input type="email" id="email" name="email" value="${form.email}" autocomplete="email">
            <ui:fieldError field="email"/>

            <label for="password">Пароль</label>
            <input type="password" id="password" name="password" autocomplete="new-password">
            <ui:fieldError field="password"/>

            <label for="passwordRepeat">Повторите пароль</label>
            <input type="password" id="passwordRepeat" name="passwordRepeat" autocomplete="new-password">
            <ui:fieldError field="passwordRepeat"/>

            <button type="submit">Зарегистрироваться</button>
        </form>

        <p class="auth-footer">
            Уже есть аккаунт?
            <a href="${pageContext.request.contextPath}/login">Войти</a>
        </p>
    </div>
</ui:layout>
