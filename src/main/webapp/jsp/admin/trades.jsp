<%--
  Created by IntelliJ IDEA.
  User: peace
  Date: 06.11.2026
  Time: 19:18
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<ui:adminLayout title="Сделки" active="trades">
    <div class="page-head">
        <h1>Сделки</h1>
        <span class="admin-count">${fn:length(trades)} всего</span>

    </div>

    <c:if test="${empty trades}">
        <p class="empty">Сделок нет</p>
    </c:if>

    <c:if test="${not empty trades}">
        <table class="admin-table">
            <tr>
                <th>№</th>
                <th>Время</th>
                <th>Пара</th>
                <th>Покупатель</th>
                <th>Продавец</th>
                <th class="num">Цена</th>
                <th class="num">Количество</th>
                <th class="num">Сумма</th>
            </tr>

            <c:forEach items="${trades}" var="t">
                <tr>
                    <td>#${t.id}</td>
                    <td>${t.createdAt.dayOfMonth < 10 ? '0' : ''}${t.createdAt.dayOfMonth}.${t.createdAt.monthValue < 10 ? '0' : ''}${t.createdAt.monthValue} ${t.createdAt.hour}:${t.createdAt.minute < 10 ? '0' : ''}${t.createdAt.minute}</td>
                    <td>${currencyMap[t.currencyId]}/USDT</td>
                    <td>${usersMap[t.buyerId]}</td>
                    <td>${usersMap[t.sellerId]}</td>
                    <td class="num"><ui:money value="${t.price}"/></td>
                    <td class="num"><ui:money value="${t.amount}"/></td>
                    <td class="num"><ui:money value="${t.price * t.amount}" digits="2"/></td>
                </tr>
            </c:forEach>
        </table>
    </c:if>
</ui:adminLayout>