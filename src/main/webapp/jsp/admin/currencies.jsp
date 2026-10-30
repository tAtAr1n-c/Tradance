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

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<ui:adminLayout title="Монеты" active="currencies">
    <div class="page-head">
        <h1>Монеты</h1>
        <span class="admin-count">${total} всего · ${tradable} торгуются</span>
    </div>


    <div class="admin-cols">
        <div class="currencies_view">
            <c:if test="${empty currencies}">
                <p class="empty">Монет пока нет</p>
            </c:if>
            <c:if test="${not empty currencies}">
                <table class="admin-table">
                    <tr>
                        <th>Монета</th>
                        <th>Тип</th>
                        <th>Статус</th>
                        <th>Действия</th>
                    </tr>

                    <c:forEach items="${currencies}" var="cur">
                        <tr>
                            <td>
                                <ui:coinIcon ticker="${cur.ticker}" iconPath="${cur.iconPath}"/>
                                <b>${cur.ticker}</b>
                                <span class="muted">${cur.name}</span>
                            </td>

                            <td class="${cur.quote ? 'type-quote' : 'type-base'}">${cur.quote ? 'Котируемая' : 'Торговая'}</td>
                            <td class="${cur.active ? 'state-on' : 'state-off'}">${cur.active ? 'Активна' : 'Выключена'}</td>

                            <td>
                                <a href="${ctx}/admin/currencies/edit?id=${cur.id}">Изменить</a>
                                <c:if test="${not cur.quote}">
                                    <form class="inline-form toggle-form" method="post" action="${ctx}/admin/currencies/toggle">
                                        <input type="hidden" name="id" value="${cur.id}">
                                        <input type="hidden" name="active" value="${not cur.active}">
                                        <button type="submit">${cur.active ? 'Выключить' : 'Включить'}</button>
                                    </form>
                                </c:if>
                                <c:if test="${not cur.quote}">
                                    <form class="inline-form" method="post" action="${ctx}/admin/currencies/delete">
                                        <input type="hidden" name="id" value="${cur.id}">
                                        <button type="submit">Удалить</button>
                                    </form>
                                </c:if>
                            </td>

                        </tr>
                    </c:forEach>
                </table>
            </c:if>
        </div>

        <aside class="panel">
            <h2>Новая монета</h2>
            <form method="post" action="${ctx}/admin/currencies/new">
                <label for="ticker">Тикер</label>
                <input name="ticker" id="ticker" value="${form.ticker}">
                <ui:fieldError field="ticker"/>
                <label for="name" >Название</label>
                <input name="name" id="name" value="${form.name}">
                <ui:fieldError field="name"/>


                <div class="check">
                    <input type="checkbox" name="active" id="active" ${form == null or form.active == 'on' ? 'checked' : ''}>
                    <label for="active">Торговля включена</label>
                </div>

                <button type="submit">Добавить монету</button>
            </form>
        </aside>
    </div>


</ui:adminLayout>
