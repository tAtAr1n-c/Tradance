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

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<ui:layout title="Мои заявки">

    <div class="page-head"><h1>Мои заявки</h1></div>
    <c:if test="${empty orders}">
        <p class="empty">Заявок пока нет</p>
    </c:if>

    <c:if test="${not empty orders}">
        <table class="data-table">
            <tr>
                <th>Время</th>
                <th>Пара</th>
                <th>Сторона</th>
                <th class="num">Цена</th>
                <th class="num">Количество</th>
                <th class="num">Исполнено</th>
                <th>Статус</th>
                <th>Действие</th>
            </tr>
            <c:forEach items="${orders}" var="o">
                <tr>
                    <td>${o.createdAt.dayOfMonth < 10 ? '0' : ''}${o.createdAt.dayOfMonth}.${o.createdAt.monthValue < 10 ? '0' : ''}${o.createdAt.monthValue} ${o.createdAt.hour}:${o.createdAt.minute < 10 ? '0' : ''}${o.createdAt.minute}</td>
                    <td>${tickers[o.currencyId]}/USDT</td>
                    <td class="${o.side == 'BUY' ? 'buy' : 'sell'}">
                            ${o.side == 'BUY' ? 'Покупка' : 'Продажа'}
                    </td>
                    <td class="num"><ui:money value="${o.price}"/></td>
                    <td class="num"><ui:money value="${o.amount}"/></td>
                    <td class="num"><ui:money value="${o.filled}"/></td>
                    <td><ui:statusBadge status="${o.status}"></ui:statusBadge></td>
                    <td>
                        <c:if test="${o.status == 'OPEN' or o.status == 'PARTIAL'}">
                            <form class="inline-form toggle-form" method="post" action="${ctx}/orders/cancel">
                                <input type="hidden" name="id" value="${o.id}">
                                <button type="submit">Отменить</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </table>

    </c:if>

</ui:layout>