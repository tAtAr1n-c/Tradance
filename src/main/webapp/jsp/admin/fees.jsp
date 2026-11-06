<%--
  Created by IntelliJ IDEA.
  User: peace
  Date: 06.11.2026
  Time: 21:10
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<ui:adminLayout title="Доход бииржи" active="fees">
  <div class="page-head">
    <h1>Доход биржи</h1>
    <span class="admin-count"><ui:money value="${totalFee}" digits="2"/> USDT</span>
  </div>

  <c:if test="${empty rows}">
    <p class="empty">Комиссий пока нет</p>
  </c:if>

  <c:if test="${not empty rows}">
    <table class="admin-table">
      <tr>
        <th>Монета</th>
        <th class="num">Сделок</th>
        <th class="num">Оборот USDT</th>
        <th class="num">Комиссия USDT</th>
      </tr>

      <c:forEach items="${rows}" var="row">
        <tr>
          <td>${currencyMap[row.currencyId]}/USDT</td>
          <td class="num">${row.tradesCount}</td>
          <td class="num"><ui:money value="${row.turnover}" digits="2"/></td>
          <td class="num"><ui:money value="${row.fee}" digits="2"/></td>
        </tr>

      </c:forEach>
    </table>

  </c:if>
</ui:adminLayout>