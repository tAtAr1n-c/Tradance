<%--
  Created by IntelliJ IDEA.
  User: peace
  Date: 23.10.2026
  Time: 13:18
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<ui:layout title="Секретная фраза">
    <div class="auth auth-wide">
        <span class="auth-step">Регистрация · шаг 2 из 2</span>
        <h1>Ваша секретная фраза</h1>

        <p>
            Запишите эти 10 слов по порядку и храните в надёжном месте.
            При каждом входе мы спросим три из них.
        </p>

        <div class="notice">Фраза показывается только один раз. Без неё не получится войти и восстановить пароль.</div>

        <ol class="phrase-grid">
            <c:forEach items="${words}" var="word" varStatus="st">
                <li class="phrase-word">
                    <span class="phrase-num">${st.count}</span>${word}
                </li>
            </c:forEach>
        </ol>

        <a class="btn" href="${pageContext.request.contextPath}/login">Я записал(а) слова, войти</a>
    </div>
</ui:layout>
