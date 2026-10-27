<%--
  Created by IntelliJ IDEA.
  User: peace
  Date: 23.10.2026
  Time: 13:17
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<ui:layout title="Рынок">

    <div class="page-head">
        <h1>Рынок</h1>
    </div>

    <c:if test="${empty market}">
        <p class="empty">Монет Пока нет</p>
    </c:if>
    <c:if test="${not empty market}">
        <table class="markets">
            <tr>
                <th></th>
                <th>Монета</th>
                <th>Последняя цена, USDT</th>
                <th>К прошлой сделке</th>
                <th></th>
            </tr>



            <c:forEach items="${market}" var="m">
                <tr>
                    <td>
                        <c:choose>
                            <c:when test="${m.favorite}">
                                <span class="star star-on">&#9733;</span>
                            </c:when>
                            <c:otherwise>
                                <span class="star">&#9734;</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <ui:coinIcon ticker="${m.ticker}" iconPath="${m.iconPath}"/>
                        <b>${m.ticker}</b><br>
                        <span class="muted">${m.name}</span>
                    </td>
                    <td><ui:money value="${m.lastPrice}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${m.trend == 'UP'}">
                                <span class="buy">▲ рост</span>
                            </c:when>
                            <c:when test="${m.trend == 'DOWN'}">
                                <span class="sell">▼ падение</span>
                            </c:when>

                            <c:when test="${m.trend == 'NONE'}">
                                <span class="muted">сделок ещё не было</span>
                            </c:when>
                            <c:otherwise>
                                <span class="muted">без изменений</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <a href="${pageContext.request.contextPath}/trade?currency=${m.ticker}">Торговать</a>

                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:if>



</ui:layout>
