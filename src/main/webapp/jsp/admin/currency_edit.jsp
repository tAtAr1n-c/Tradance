<%--
  Created by IntelliJ IDEA.
  User: peace
  Date: 29.10.2026
  Time: 18:19
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<ui:adminLayout title="Монета ${currency.ticker}" active="currencies">
    <div class="edit-head">
        <a class="back-link" href="${ctx}/admin/currencies">← Все монеты</a>
        <h1>Изменить монету ${currency.ticker}</h1>
    </div>

    <form class="edit-form" method="post" action="${ctx}/admin/currencies/edit" enctype="multipart/form-data">
        <input type="hidden" name="id" value="${currency.id}">

        <label for="ticker">Тикер</label>
        <input name="ticker" id="ticker" value="${empty form ? currency.ticker : form.ticker}">
        <ui:fieldError field="ticker"/>

        <label for="name">Название</label>
        <input name="name" id="name" value="${empty form ? currency.name : form.name}">
        <ui:fieldError field="name"/>

        <label>Иконка</label>
        <div class="icon-preview">
            <ui:coinIcon ticker="${currency.ticker}" iconFileId="${currency.iconFileId}"/>
            <span class="muted">${empty currency.iconFileId ? 'Файл не загружен, показываем букву' : 'Загружена своя иконка'}</span>
        </div>

        <label for="icon">Новая иконка, PNG, JPG или WEBP до 2 МБ</label>
        <input type="file" name="icon" id="icon" accept="image/png,image/jpeg,image/webp">

        <c:if test="${not currency.quote}">
            <div class="check">
                <input type="checkbox" name="active" id="active" ${(empty form and currency.active) or form.active == 'on' ? 'checked' : ''}>
                <label for="active">Торговля включена</label>
            </div>
        </c:if>
        <c:if test="${currency.quote}">
            <input type="hidden" name="active" value="on">
        </c:if>

        <div class="form-buttons">
            <button type="submit">Сохранить</button>
            <a class="btn btn-ghost" href="${ctx}/admin/currencies">Отмена</a>
        </div>
    </form>

    <p class="muted admin-hint edit-hint">
        Удалить монету можно в списке, если по ней ещё нет кошельков и заявок.
        Чтобы остановить торговлю, снимите галочку «Торговля включена».
    </p>

</ui:adminLayout>
