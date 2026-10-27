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


<ui:layout title="Кошельки">

    <div class="page-head">
        <h1>Кошельки</h1>

        <c:if test="${not empty usdt}">
            <div class="stat">
                <span class="stat-label">Баланс USDT</span>
                <span class="stat-value"><ui:money value="${usdt.balance}" digits="2"/></span>
            </div>
            <div class="stat">
                <span class="stat-label">В заявках</span>
                <span class="stat-value accent"><ui:money value="${usdt.locked}" digits="2"/></span>
            </div>
            <div class="stat">
                <span class="stat-label">Доступно</span>
                <span class="stat-value"><ui:money value="${usdt.available}" digits="2"/></span>
            </div>
        </c:if>
    </div>

    <table class="wallets">
        <tr>
            <th>Валюта</th>
            <th>Всего</th>
            <th>В заявках</th>
            <th>Доступно</th>
            <th></th>
        </tr>

        <c:forEach items="${wallets}" var="w">
            <tr>
                <td>
                    <ui:coinIcon ticker="${w.ticker}" iconPath="${w.iconPath}"/>
                    <b>${w.ticker}</b><br>
                    <span class="muted">${w.name}</span>
                </td>
                <td><ui:money value="${w.balance}"/></td>

                <td class="${w.locked > 0 ? 'accent' : 'muted'}"><ui:money value="${w.locked}"/></td>
                <td><ui:money value="${w.available}"/></td>
                <td>
                    <c:choose>
                        <c:when test="${w.ticker == 'USDT'}">
                            <a href="${pageContext.request.contextPath}/market">Купить монеты</a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/trade?currency=${w.ticker}">Торговать</a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
    </table>

</ui:layout>
