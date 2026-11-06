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

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<ui:adminLayout title="Пользователи" active="users">
    <div class="page-head">
        <h1>Пользователи</h1>
        <span class="admin-count">${fn:length(users)} всего</span>
    </div>

    <div class="admin-cols">
        <div class="users-view">
            <c:if test="${empty users}">
                <p class="empty">Пользователей пока нет</p>
            </c:if>
            <c:if test="${not empty users}">
                <table class="admin-table">
                    <tr>
                    <th>Ник</th>
                    <th>Email</th>
                    <th>Роль</th>
                    <th>Статус</th>
                    <th>Действия</th>
                    </tr>


                    <c:forEach items="${users}" var="u">
                        <tr>
                            <td>
                                <b>${u.username}</b>
                            </td>
                            <td>
                                ${u.email}
                            </td>
                            <td>${u.role == 'ADMIN' ? 'Админ' : 'Пользователь'}</td>
                            <td class="${u.blocked ? 'state-off' : 'state-on'}">${u.blocked ? 'Заблокирован' : 'Активен'}</td>

                            <td>
                                <c:if test="${u.id != sessionScope.user.id}">
                                    <form class="inline-form ${u.blocked ? 'toggle-form' : ''}" method="post" action="${ctx}/admin/users/block">
                                        <input type="hidden" name="id" value="${u.id}">
                                        <input type="hidden" name="blocked" value="${not u.blocked}">
                                        <button type="submit">${u.blocked ? 'Разблокировать' : 'Заблокировать'}</button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </table>


            </c:if>
        </div>

        <aside class="panel">
            <h2>Пополнить баланс</h2>
            <form method="post" action="${ctx}/admin/users/deposit">
                <label for="userId">Пользователь</label>
                <select name="userId" id="userId">
                    <c:forEach items="${users}" var="u">
                        <option value="${u.id}">${u.username}</option>
                    </c:forEach>
                </select>

                <label for="currency">Монета</label>
                <select name="currency" id="currency">
                    <c:forEach items="${currencies}" var="cur">
                        <option value="${cur.ticker}">${cur.ticker} — ${cur.name}</option>
                    </c:forEach>
                </select>

                <label for="amount">Количество</label>
                <input name="amount" id="amount" placeholder="например, 1000">

                <button type="submit">Пополнить</button>
            </form>
        </aside>
    </div>
</ui:adminLayout>
