<%--
  Created by IntelliJ IDEA.
  User: peace
  Date: 09.11.2026
  Time: 08:14
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<ui:layout title="Рейтинг">
  <div class="page-head">
    <h1>Рейтинг</h1>
    <span class="admin-count">Заработок = портфель − вложено</span>
  </div>

  <c:if test="${empty rows}">
    <p class="empty">Рейтинга пока нет</p>
  </c:if>

  <c:if test="${not empty rows}">
    <div class="rating-top">
      <c:forEach items="${rows}" var="r" varStatus="st" end="2">
        <div class="rating-card rating-place-${st.count}">
          <div class="rating-card-head">
            <span class="rating-place">#${st.count}</span>
            <c:choose>
              <c:when test="${not empty r.avatarFileId}">
                <img class="avatar" src="${ctx}/files?id=${r.avatarFileId}" alt="">
              </c:when>
              <c:otherwise>
                <span class="avatar">${fn:toUpperCase(fn:substring(r.username, 0, 1))}</span>
              </c:otherwise>
            </c:choose>
            <b>${r.username}</b>
          </div>

          <div class="rating-card-profit ${r.profit >= 0 ? 'buy' : 'sell'}">
            <span class="rating-card-sum">${r.profit > 0 ? '+' : ''}<ui:money value="${r.profit}" digits="2"/></span>
            <span>${r.profitPct > 0 ? '+' : ''}<ui:money value="${r.profitPct}" digits="2"/>%</span>
          </div>

          <span class="muted rating-card-note">портфель <ui:money value="${r.portfolio}" digits="2"/> USDT · сделок: ${r.tradesCount}</span>
        </div>
      </c:forEach>
    </div>

    <table class="data-table">
      <tr>
        <th>Место</th>
        <th>Трейдер</th>
        <th class="num">Портфель</th>
        <th class="num">Вложено</th>
        <th class="num">Заработок</th>
        <th class="num">Доходность</th>
        <th class="num">Сделок</th>
      </tr>

      <c:forEach items="${rows}" var="r" varStatus="st">
        <tr  class="${r.userId == sessionScope.user.id ? 'rating-me' : ''}">
          <td>#${st.count}</td>
          <td>
            <div class="rating-user">
              <c:choose>
                <c:when test="${not empty r.avatarFileId}">
                  <img class="avatar" src="${ctx}/files?id=${r.avatarFileId}" alt="">
                </c:when>
                <c:otherwise>
                  <span class="avatar">${fn:toUpperCase(fn:substring(r.username, 0, 1))}</span>
                </c:otherwise>
              </c:choose>
              <b>${r.username}</b>
            </div>
          </td>
          <td class="num"><ui:money value="${r.portfolio}" digits="2"/></td>
          <td class="num"><ui:money value="${r.invested}" digits="2"/></td>
          <td class="num ${r.profit >= 0 ? 'buy' : 'sell'}">${r.profit > 0 ? '+' : ''}<ui:money value="${r.profit}" digits="2"/></td>
          <td class="num ${r.profit >= 0 ? 'buy' : 'sell'}"><ui:money value="${r.profitPct}" digits="2"/>%</td>
          <td class="num">${r.tradesCount}</td>
        </tr>
      </c:forEach>
    </table>
  </c:if>
</ui:layout>
