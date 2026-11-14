<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<ui:layout title="Мои сделки P2P">

    <div class="page-head">
        <h1>Мои сделки P2P</h1>
        <a class="p2p-deals-link" href="${ctx}/p2p">← К объявлениям</a>
    </div>

    <c:if test="${empty deals}">
        <p class="empty">Сделок P2P пока нет</p>
    </c:if>

    <c:if test="${not empty deals}">
        <table class="data-table">
            <tr>
                <th>№</th>
                <th>Время</th>
                <th>Монета</th>
                <th>Роль</th>
                <th class="num">Количество</th>
                <th class="num">Сумма</th>
                <th>Контрагент</th>
                <th>Статус</th>
                <th><span class="visually-hidden">Открыть</span></th>
            </tr>
            <c:forEach items="${deals}" var="d">
                <c:set var="mine" value="${d.buyerId == sessionScope.user.id}"/>
                <tr>
                    <td class="muted">${d.id}</td>
                    <td>${d.createdAt.dayOfMonth < 10 ? '0' : ''}${d.createdAt.dayOfMonth}.${d.createdAt.monthValue < 10 ? '0' : ''}${d.createdAt.monthValue} ${d.createdAt.hour < 10 ? '0' : ''}${d.createdAt.hour}:${d.createdAt.minute < 10 ? '0' : ''}${d.createdAt.minute}</td>
                    <td class="strong">${currencies[d.currencyId]}</td>
                    <td class="${mine ? 'buy' : 'sell'}">${mine ? 'Покупка' : 'Продажа'}</td>
                    <td class="num"><ui:money value="${d.amount}"/></td>
                    <td class="num"><ui:money value="${d.price * d.amount}" digits="2"/> ₽</td>
                    <td>${usersMap[mine ? d.sellerId : d.buyerId]}</td>
                    <td>
                        <c:choose>
                            <c:when test="${d.status == 'CREATED'}"><span class="badge badge-open">Ждёт оплаты</span></c:when>
                            <c:when test="${d.status == 'PAID'}"><span class="badge badge-partial">Оплачена</span></c:when>
                            <c:when test="${d.status == 'COMPLETED'}"><span class="badge badge-filled">Завершена</span></c:when>
                            <c:otherwise><span class="badge badge-cancelled">Отменена</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td><a href="${ctx}/p2p/deal?id=${d.id}">Открыть</a></td>
                </tr>
            </c:forEach>
        </table>
    </c:if>

</ui:layout>
