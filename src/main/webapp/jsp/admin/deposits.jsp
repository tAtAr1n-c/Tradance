<%--
  Created by IntelliJ IDEA.
  User: peace
  Date: 08.11.2026
  Time: 01:04
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<ui:adminLayout title="Пополнения" active="deposits">
  <div class="page-head">
    <h1>Пополнения</h1>
    <span class="admin-count">${fn:length(deposits)} всего</span>
  </div>

  <c:if test="${empty deposits}">
    <p class="empty">Пополнений пока нет</p>
  </c:if>

  <c:if test="${not empty deposits}">
    <table class="admin-table">
      <tr>
        <th>№</th>
        <th>Время</th>
        <th>Пользователь</th>
        <th>Монета</th>
        <th class="num">Количество</th>
        <th>Кто пополнил</th>
      </tr>

      <c:forEach items="${deposits}" var="d">
        <tr>
          <td class="muted">#${d.id}</td>
          <td>${d.createdAt.dayOfMonth < 10 ? '0' : ''}${d.createdAt.dayOfMonth}.${d.createdAt.monthValue < 10 ? '0' : ''}${d.createdAt.monthValue} ${d.createdAt.hour}:${d.createdAt.minute < 10 ? '0' : ''}${d.createdAt.minute}</td>
          <td>${usersMap[d.userId]}</td>
          <td>${currencyMap[d.currencyId]}</td>
          <td class="num">+<ui:money value="${d.amount}"/></td>
          <td>${usersMap[d.adminId]}</td>
        </tr>

      </c:forEach>
    </table>
  </c:if>
</ui:adminLayout>