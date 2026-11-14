<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="ticker" value="${currencies[deal.currencyId]}"/>
<c:set var="isBuyer" value="${deal.buyerId == sessionScope.user.id}"/>
<c:set var="partnerId" value="${isBuyer ? deal.sellerId : deal.buyerId}"/>

<c:set var="t" value="${deal.createdAt}"/>
<c:set var="createdTxt">${t.dayOfMonth < 10 ? '0' : ''}${t.dayOfMonth}.${t.monthValue < 10 ? '0' : ''}${t.monthValue} ${t.hour < 10 ? '0' : ''}${t.hour}:${t.minute < 10 ? '0' : ''}${t.minute}</c:set>
<c:if test="${not empty deal.paidAt}">
    <c:set var="t" value="${deal.paidAt}"/>
    <c:set var="paidTxt">${t.dayOfMonth < 10 ? '0' : ''}${t.dayOfMonth}.${t.monthValue < 10 ? '0' : ''}${t.monthValue} ${t.hour < 10 ? '0' : ''}${t.hour}:${t.minute < 10 ? '0' : ''}${t.minute}</c:set>
</c:if>
<c:if test="${not empty deal.closedAt}">
    <c:set var="t" value="${deal.closedAt}"/>
    <c:set var="closedTxt">${t.dayOfMonth < 10 ? '0' : ''}${t.dayOfMonth}.${t.monthValue < 10 ? '0' : ''}${t.monthValue} ${t.hour < 10 ? '0' : ''}${t.hour}:${t.minute < 10 ? '0' : ''}${t.minute}</c:set>
</c:if>

<ui:layout title="Сделка P2P №${deal.id}">

    <div class="page-head">
        <h1>Сделка P2P №${deal.id}</h1>
        <c:choose>
            <c:when test="${deal.status == 'CREATED'}"><span class="deal-status">Ждёт оплаты</span></c:when>
            <c:when test="${deal.status == 'PAID'}"><span class="deal-status deal-status-paid">Покупатель оплатил</span></c:when>
            <c:when test="${deal.status == 'COMPLETED'}"><span class="deal-status deal-status-done">Завершена</span></c:when>
            <c:otherwise><span class="deal-status deal-status-cancelled">Отменена</span></c:otherwise>
        </c:choose>
        <a class="p2p-deals-link" href="${ctx}/p2p/deals">Все мои сделки</a>
    </div>

    <a class="deal-back" href="${ctx}/p2p?currency=${ticker}">← Объявления ${ticker}</a>

    <c:if test="${deal.status != 'CANCELLED'}">
        <ol class="deal-steps">
            <li class="deal-step done">
                <span class="deal-step-num">Шаг 1</span>
                <b>Сделка открыта</b>
                <span class="muted">${createdTxt}</span>
            </li>
            <li class="deal-step ${deal.status == 'CREATED' ? 'current' : 'done'}">
                <span class="deal-step-num">Шаг 2</span>
                <b>Покупатель оплатил</b>
                <span class="muted">${empty paidTxt ? 'ожидается' : paidTxt}</span>
            </li>
            <li class="deal-step ${deal.status == 'PAID' ? 'current' : (deal.status == 'COMPLETED' ? 'done' : '')}">
                <span class="deal-step-num">Шаг 3</span>
                <b>Продавец подтвердил</b>
                <span class="muted">${deal.status == 'COMPLETED' ? closedTxt : 'ожидается'}</span>
            </li>
            <li class="deal-step ${deal.status == 'COMPLETED' ? 'done' : ''}">
                <span class="deal-step-num">Шаг 4</span>
                <b>${ticker} у покупателя</b>
                <span class="muted">${deal.status == 'COMPLETED' ? closedTxt : '—'}</span>
            </li>
        </ol>
    </c:if>

    <div class="deal-cols">

        <section class="deal-card">
            <div class="deal-title">
                <ui:coinIcon ticker="${ticker}"/>
                <div>
                    <div class="deal-title-main">
                        ${isBuyer ? 'Вы покупаете' : 'Вы продаёте'}
                        <span class="${isBuyer ? 'buy' : 'sell'}"><ui:money value="${deal.amount}"/> ${ticker}</span>
                    </div>
                    <div class="muted">${isBuyer ? 'продавец' : 'покупатель'} ${usersMap[partnerId]}</div>
                </div>
            </div>

            <div class="deal-facts">
                <div>
                    <span class="stat-label">Цена за 1 ${ticker}</span>
                    <b><ui:money value="${deal.price}" digits="2"/> ₽</b>
                </div>
                <div>
                    <span class="stat-label">${isBuyer ? 'Сумма к переводу' : 'Сумма к получению'}</span>
                    <b class="buy"><ui:money value="${deal.price * deal.amount}" digits="2"/> ₽</b>
                </div>
                <div>
                    <span class="stat-label">Способ оплаты</span>
                    <b>${paymentMethods[deal.paymentMethodId]}</b>
                </div>
            </div>

            <c:choose>
                <c:when test="${deal.status == 'CREATED' and isBuyer}">
                    <p class="deal-note">Продавец пришлёт реквизиты в чате сделки. Переведите <b><ui:money value="${deal.price * deal.amount}" digits="2"/> ₽</b> через ${paymentMethods[deal.paymentMethodId]} и нажмите «Я оплатил». Монеты уже заморожены у продавца и никуда не денутся.</p>
                </c:when>
                <c:when test="${deal.status == 'CREATED'}">
                    <p class="deal-note">Отправьте покупателю реквизиты для перевода в чате сделки. Монеты заморожены на вашем кошельке до конца сделки.</p>
                </c:when>
                <c:when test="${deal.status == 'PAID' and isBuyer}">
                    <p class="deal-note">Вы отметили перевод. Ждём, пока продавец проверит поступление и отправит монеты.</p>
                </c:when>
                <c:when test="${deal.status == 'PAID'}">
                    <p class="deal-note">Покупатель отметил, что перевёл <b><ui:money value="${deal.price * deal.amount}" digits="2"/> ₽</b>. Проверьте поступление в банке и только после этого подтвердите.</p>
                </c:when>
                <c:when test="${deal.status == 'COMPLETED'}">
                    <p class="deal-note deal-note-ok">Сделка завершена: <ui:money value="${deal.amount}"/> ${ticker} зачислены покупателю.</p>
                </c:when>
                <c:otherwise>
                    <p class="deal-note deal-note-off">Сделку отменил ${usersMap[deal.cancelledBy]}. Монеты вернулись продавцу.</p>
                </c:otherwise>
            </c:choose>

            <div class="deal-actions">
                <c:if test="${deal.status == 'CREATED' and isBuyer}">
                    <form method="post" action="${ctx}/p2p/deal/paid">
                        <input type="hidden" name="id" value="${deal.id}">
                        <button type="submit" class="btn-buy">Я оплатил</button>
                    </form>
                </c:if>
                <c:if test="${deal.status == 'PAID' and not isBuyer}">
                    <form method="post" action="${ctx}/p2p/deal/confirm">
                        <input type="hidden" name="id" value="${deal.id}">
                        <button type="submit" class="btn-buy">Деньги получены, отправить ${ticker}</button>
                    </form>
                </c:if>
                <c:if test="${deal.status == 'CREATED' or (deal.status == 'PAID' and isBuyer)}">
                    <form method="post" action="${ctx}/p2p/deal/cancel">
                        <input type="hidden" name="id" value="${deal.id}">
                        <button type="submit" class="btn-ghost">Отменить сделку</button>
                    </form>
                </c:if>
            </div>
        </section>

        <aside class="deal-card">
            <div class="block-title deal-history-title">История</div>
            <ul class="deal-history">
                <li>
                    <span class="deal-dot done"></span>
                    <div>${usersMap[deal.buyerId]} открыл сделку на <ui:money value="${deal.amount}"/> ${ticker}<span class="muted">${createdTxt}</span></div>
                </li>
                <c:if test="${not empty paidTxt}">
                    <li>
                        <span class="deal-dot done"></span>
                        <div>${usersMap[deal.buyerId]} отметил перевод <ui:money value="${deal.price * deal.amount}" digits="2"/> ₽<span class="muted">${paidTxt}</span></div>
                    </li>
                </c:if>
                <c:if test="${deal.status == 'COMPLETED'}">
                    <li>
                        <span class="deal-dot done"></span>
                        <div>${usersMap[deal.sellerId]} подтвердил получение, монеты переведены<span class="muted">${closedTxt}</span></div>
                    </li>
                </c:if>
                <c:if test="${deal.status == 'CANCELLED'}">
                    <li>
                        <span class="deal-dot off"></span>
                        <div>${usersMap[deal.cancelledBy]} отменил сделку<span class="muted">${closedTxt}</span></div>
                    </li>
                </c:if>
                <c:if test="${deal.status == 'CREATED' or deal.status == 'PAID'}">
                    <li>
                        <span class="deal-dot wait"></span>
                        <div>${deal.status == 'CREATED' ? 'Ожидается оплата' : 'Ожидается подтверждение продавца'}</div>
                    </li>
                </c:if>
            </ul>
        </aside>

    </div>

</ui:layout>
