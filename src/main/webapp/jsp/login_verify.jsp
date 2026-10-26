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
        <span class="auth-step">Вход · шаг 2 из 2</span>
        <h1>Секретная фраза</h1>
        <p>Введите слова под этими номерами из фразы, которую вы записали при регистрации.</p>

        <c:if test="${not empty error}">
            <div class="form-error">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/login/verify">
            <c:forEach items="${positions}" var="pos">
                <label for="word-${pos}">Слово <span class="word-num">№${pos}</span></label>
                <input id="word-${pos}" name="words" autocomplete="off" spellcheck="false" autocapitalize="off">
            </c:forEach>
            <button type="submit">Войти</button>
        </form>

        <p class="auth-footer">
            <a href="${pageContext.request.contextPath}/login">Другой аккаунт</a>
        </p>
    </div>
</ui:layout>
