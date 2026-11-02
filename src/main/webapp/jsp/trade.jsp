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
<c:set var="ticker" value="${currency.ticker}"/>

<ui:layout title="${ticker}/USDT">

    <section class="pair-bar">
        <div class="pair-name">
            <ui:coinIcon ticker="${ticker}" iconPath="${currency.iconPath}"/>
            <div>
                <b>${ticker}/USDT</b>
                <span class="muted">${currency.name}</span>
            </div>
        </div>

        <div class="stat">
            <span class="stat-value"><ui:money value="${book.lastPrice}"/></span>
            <span class="stat-label">последняя сделка</span>
        </div>

        <c:if test="${not empty sessionScope.user}">
            <div class="stat">
                <span class="stat-label">Доступно USDT</span>
                <span class="stat-value"><ui:money value="${usdtAvailable}" digits="2"/></span>
            </div>
            <div class="stat">
                <span class="stat-label">Доступно ${ticker}</span>
                <span class="stat-value"><ui:money value="${coinAvailable}"/></span>
            </div>
        </c:if>
    </section>

    <div class="trade-cols">

        <section class="book">
            <div class="block-title">Стакан</div>

            <table class="book-table">
                <tr>
                    <th>Цена (USDT)</th>
                    <th class="num">Кол-во (${ticker})</th>
                    <th class="num">Сумма</th>
                </tr>

                <c:forEach items="${book.asks}" var="lvl">
                    <tr class="book-row" data-price="${lvl.price}" data-side="SELL">
                        <td class="sell"><ui:money value="${lvl.price}"/></td>
                        <td class="num"><ui:money value="${lvl.amount}"/></td>
                        <td class="num muted"><ui:money value="${lvl.price * lvl.amount}" digits="2"/></td>
                    </tr>
                </c:forEach>

                <tr class="book-mid">
                    <td colspan="3"><ui:money value="${book.lastPrice}"/></td>
                </tr>

                <c:forEach items="${book.bids}" var="lvl">
                    <tr class="book-row" data-price="${lvl.price}" data-side="BUY">
                        <td class="buy"><ui:money value="${lvl.price}"/></td>
                        <td class="num"><ui:money value="${lvl.amount}"/></td>
                        <td class="num muted"><ui:money value="${lvl.price * lvl.amount}" digits="2"/></td>
                    </tr>
                </c:forEach>
            </table>

            <c:if test="${empty book.asks and empty book.bids}">
                <p class="empty">Заявок пока нет</p>
            </c:if>
        </section>

        <section class="trade-forms">
            <c:choose>
                <c:when test="${empty sessionScope.user}">
                    <p class="hint">
                        <a href="${ctx}/login">Войдите</a>, чтобы торговать.
                    </p>
                </c:when>

                <c:otherwise>
                    <form class="order-form" method="post" action="${ctx}/trade">
                        <input type="hidden" name="currency" value="${ticker}">
                        <input type="hidden" name="side" value="BUY">

                        <div class="order-form-head">
                            <b>Купить ${ticker}</b>
                            <span class="muted">Доступно <ui:money value="${usdtAvailable}" digits="2"/> USDT</span>
                        </div>

                        <label for="buy-price">Цена, USDT</label>
                        <input id="buy-price" name="price" value="${form.side == 'BUY' ? form.price : ''}">
                        <c:if test="${form.side == 'BUY'}"><ui:fieldError field="price"/></c:if>

                        <label for="buy-amount">Количество, ${ticker}</label>
                        <input id="buy-amount" name="amount" value="${form.side == 'BUY' ? form.amount : ''}">
                        <c:if test="${form.side == 'BUY'}"><ui:fieldError field="amount"/></c:if>

                        <div class="order-total">Сумма <span id="buy-total">0</span> USDT</div>
                        <button type="submit" class="btn-buy">Купить ${ticker}</button>
                    </form>

                    <form class="order-form" method="post" action="${ctx}/trade">
                        <input type="hidden" name="currency" value="${ticker}">
                        <input type="hidden" name="side" value="SELL">

                        <div class="order-form-head">
                            <b>Продать ${ticker}</b>
                            <span class="muted">Доступно <ui:money value="${coinAvailable}"/> ${ticker}</span>
                        </div>

                        <label for="sell-price">Цена, USDT</label>
                        <input id="sell-price" name="price" value="${form.side == 'SELL' ? form.price : ''}">
                        <c:if test="${form.side == 'SELL'}"><ui:fieldError field="price"/></c:if>

                        <label for="sell-amount">Количество, ${ticker}</label>
                        <input id="sell-amount" name="amount" value="${form.side == 'SELL' ? form.amount : ''}">
                        <c:if test="${form.side == 'SELL'}"><ui:fieldError field="amount"/></c:if>

                        <div class="order-total">Получите <span id="sell-total">0</span> USDT</div>
                        <button type="submit" class="btn-sell">Продать ${ticker}</button>
                    </form>
                </c:otherwise>
            </c:choose>
        </section>

        <aside class="trade-side">
            <div class="block-title">Последние сделки</div>

            <table class="book-table">
                <tr>
                    <th>Цена</th>
                    <th class="num">Кол-во</th>
                    <th class="num">Время</th>
                </tr>
                <c:forEach items="${book.lastTrades}" var="t">
                    <tr>
                        <td><ui:money value="${t.price}"/></td>
                        <td class="num"><ui:money value="${t.amount}"/></td>
                        <td class="num muted">${t.createdAt.hour}:${t.createdAt.minute < 10 ? '0' : ''}${t.createdAt.minute}</td>
                    </tr>
                </c:forEach>
            </table>

            <c:if test="${empty book.lastTrades}">
                <p class="empty">Сделок ещё не было</p>
            </c:if>
        </aside>

    </div>
    <script src="${ctx}/js/trade.js"></script>
</ui:layout>
