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


<ui:layout title="Мои сделки">
    <div class="page-head"><h1>Мои сделки</h1></div>
    <c:if test="${empty trades}">
        <p class="empty">Сделок пока нет</p>
    </c:if>

    <c:if test="${not empty trades}">

        <table class="data-table">
            <tr>
                <th>Время</th>
                <th>Пара</th>
                <th>Сторона</th>
                <th class="num">Цена</th>
                <th class="num">Количество</th>
                <th class="num">Сумма</th>
                <th class="num">Комиссия, USDT</th>
            </tr>

            <c:forEach items="${trades}" var="i">
                <tr>
                    <td>${i.createdAt.dayOfMonth < 10 ? '0' : ''}${i.createdAt.dayOfMonth}.${i.createdAt.monthValue < 10 ? '0' : ''}${i.createdAt.monthValue} ${i.createdAt.hour}:${i.createdAt.minute < 10 ? '0' : ''}${i.createdAt.minute}</td>
                    <td>${tickers[i.currencyId]}/USDT</td>
                    <td class="${i.buyerId == sessionScope.user.id ? 'buy' : 'sell'}">
                            ${i.buyerId == sessionScope.user.id ? 'Покупка' : 'Продажа'}
                    </td>
                    <td class="num"><ui:money value="${i.price}"/></td>
                    <td class="num"><ui:money value="${i.amount}"/></td>
                    <td class="num"><ui:money value="${i.price * i.amount}" digits="2"/></td>

                    <td class="num">
                        <c:choose>
                            <c:when test="${i.sellerId == sessionScope.user.id}">
                                <ui:money value="${i.price * i.amount * feeRate}" digits="2"/>
                            </c:when>
                            <c:otherwise>
                                <span class="muted">—</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
        </table>

    </c:if>


</ui:layout>
