<%--
  Created by IntelliJ IDEA.
  User: peace
  Date: 12.11.2026
  Time: 16:22
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<ui:layout title="P2P ${ticker}">

    <div class="page-head">
        <h1>P2P-торговля</h1>
        <span class="admin-count">оплата переводом вне биржи, монеты продавца заморожены до подтверждения</span>
        <c:if test="${not empty sessionScope.user}">
            <a class="p2p-deals-link" href="${ctx}/p2p/deals">Мои сделки P2P</a>
        </c:if>
    </div>

    <div class="p2p-cols">

        <section class="p2p-main">
            <div class="coin-tabs">
                <c:forEach items="${currencies}" var="cur">
                    <a class="coin-tab ${cur.ticker == ticker ? 'active' : ''}" href="${ctx}/p2p?currency=${cur.ticker}">
                        <ui:coinIcon ticker="${cur.ticker}" iconFileId="${cur.iconFileId}"/>
                        ${cur.ticker}
                    </a>
                </c:forEach>
            </div>

            <c:if test="${empty offers}">
                <p class="empty">Объявлений о продаже ${ticker} пока нет</p>
            </c:if>

            <c:if test="${not empty offers}">
                <div class="p2p-table-wrap">
                    <table class="p2p-table">
                        <tr>
                            <th>Продавец</th>
                            <th class="num">Цена за 1 ${ticker}</th>
                            <th class="num">Доступно</th>
                            <th>Оплата</th>
                            <th><span class="visually-hidden">Купить</span></th>
                        </tr>
                        <c:forEach items="${offers}" var="o">
                            <tr>
                                <td>
                                    <div class="rating-user">
                                        <span class="avatar">${fn:toUpperCase(fn:substring(usersMap[o.userId], 0, 1))}</span>
                                        <b>${usersMap[o.userId]}</b>
                                    </div>
                                </td>
                                <td class="num strong"><ui:money value="${o.price}" digits="2"/> ₽</td>
                                <td class="num"><ui:money value="${o.remaining}"/> ${ticker}</td>
                                <td><span class="pay-tag">${paymentMethods[o.paymentMethodId]}</span></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty sessionScope.user}">
                                            <span class="muted"><a href="${ctx}/login">Войдите</a>, чтобы купить</span>
                                        </c:when>
                                        <c:when test="${o.userId == sessionScope.user.id}">
                                            <span class="muted">Ваше объявление</span>
                                        </c:when>
                                        <c:otherwise>
                                            <form class="deal-form" method="post" action="${ctx}/p2p/deal/open">
                                                <input type="hidden" name="offerId" value="${o.id}">
                                                <input name="amount" placeholder="Кол-во ${ticker}" aria-label="Количество ${ticker}" inputmode="decimal">
                                                <button type="submit" class="btn-buy">Купить</button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
            </c:if>

            <c:if test="${not empty myOffers}">
                <h2 class="p2p-subtitle">Мои объявления</h2>
                <div class="p2p-table-wrap">
                    <table class="p2p-table">
                        <tr>
                            <th>Монета</th>
                            <th class="num">Цена</th>
                            <th class="num">Осталось</th>
                            <th>Оплата</th>
                            <th>Статус</th>
                            <th><span class="visually-hidden">Действие</span></th>
                        </tr>
                        <c:forEach items="${myOffers}" var="m">
                            <tr>
                                <td class="strong">
                                    <c:forEach items="${currencies}" var="cur">
                                        <c:if test="${cur.id == m.currencyId}">${cur.ticker}</c:if>
                                    </c:forEach>
                                </td>
                                <td class="num"><ui:money value="${m.price}" digits="2"/> ₽</td>
                                <td class="num"><ui:money value="${m.remaining}"/> <span class="muted">из <ui:money value="${m.amount}"/></span></td>
                                <td>${paymentMethods[m.paymentMethodId]}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${m.status == 'ACTIVE'}"><span class="badge badge-open">Активно</span></c:when>
                                        <c:otherwise><span class="badge badge-cancelled">Закрыто</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${m.status == 'ACTIVE'}">
                                        <form class="inline-form" method="post" action="${ctx}/p2p/offers/close">
                                            <input type="hidden" name="id" value="${m.id}">
                                            <button type="submit">Закрыть</button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
            </c:if>
        </section>

        <aside class="p2p-aside">
            <c:choose>
                <c:when test="${empty sessionScope.user}">
                    <p class="hint"><a href="${ctx}/login">Войдите</a>, чтобы разместить объявление.</p>
                </c:when>

                <c:otherwise>
                    <c:set var="formTicker" value="${empty form ? ticker : form.currency}"/>

                    <form class="order-form" method="post" action="${ctx}/p2p/offers/new">
                        <div class="order-form-head">
                            <b>Продать за рубли</b>
                        </div>

                        <label for="offer-currency">Монета</label>
                        <select id="offer-currency" name="currency">
                            <c:forEach items="${currencies}" var="cur">
                                <option value="${cur.ticker}" ${cur.ticker == formTicker ? 'selected' : ''}>${cur.ticker} — ${cur.name}</option>
                            </c:forEach>
                        </select>
                        <ui:fieldError field="currency"/>

                        <label for="offer-price">Цена за 1 монету, ₽</label>
                        <input id="offer-price" name="price" value="${form.price}" inputmode="decimal">
                        <ui:fieldError field="price"/>

                        <label for="offer-amount">Количество</label>
                        <input id="offer-amount" name="amount" value="${form.amount}" inputmode="decimal">
                        <ui:fieldError field="amount"/>

                        <label for="offer-pay">Способ оплаты</label>
                        <select id="offer-pay" name="paymentMethodId">
                            <c:forEach items="${paymentMethods}" var="pm">
                                <c:set var="pmId">${pm.key}</c:set>
                                <option value="${pm.key}" ${form.paymentMethodId == pmId ? 'selected' : ''}>${pm.value}</option>
                            </c:forEach>
                        </select>
                        <ui:fieldError field="paymentMethodId"/>

                        <p class="p2p-note">Монеты сразу заморозятся на вашем кошельке. Когда покупатель откроет сделку, отправьте ему реквизиты в чате сделки, а после поступления денег подтвердите её.</p>

                        <button type="submit">Опубликовать</button>
                    </form>
                </c:otherwise>
            </c:choose>
        </aside>

    </div>

</ui:layout>
