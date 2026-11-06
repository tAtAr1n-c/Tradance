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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<ui:adminLayout title="Заявки" active="orders">

    <div class="page-head">
        <h1>Заявки</h1>
        <span class="admin-count">${fn:length(orders)} всего</span>
    </div>
    <c:if test="${empty orders}">
        <p class="empty">Заявок нет</p>
    </c:if>
    <c:if test="${not empty orders}">
        <table class="admin-table">
            <tr>
                <th>№</th>
                <th>Время</th>
                <th>Пользователь</th>
                <th>Пара</th>
                <th>Сторона</th>
                <th class="num">Цена</th>
                <th class="num">Количество</th>
                <th class="num">Исполнено</th>
                <th>Статус</th>
            </tr>

            <c:forEach items="${orders}" var="o">
                <tr>
                    <td>#${o.id}</td>
                    <td>${o.createdAt.dayOfMonth < 10 ? '0' : ''}${o.createdAt.dayOfMonth}.${o.createdAt.monthValue < 10 ? '0' : ''}${o.createdAt.monthValue} ${o.createdAt.hour}:${o.createdAt.minute < 10 ? '0' : ''}${o.createdAt.minute}</td>
                    <td>${usersMap[o.userId]}</td>
                    <td>${currencyMap[o.currencyId]}/USDT</td>
                    <td class="${o.side=='BUY' ? 'buy': 'sell'}">
                        ${o.side == 'BUY' ? 'Покупка' : 'Продажа'}
                    </td>
                    <td class="num"><ui:money value="${o.price}"/></td>
                    <td class="num"><ui:money value="${o.amount}"/></td>
                    <td class="num"><ui:money value="${o.filled}"/></td>
                    <td><ui:statusBadge status="${o.status}"/></td>
                </tr>
            </c:forEach>
        </table>
    </c:if>
</ui:adminLayout>
