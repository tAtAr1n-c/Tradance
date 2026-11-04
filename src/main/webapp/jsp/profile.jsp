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
<c:set var="months" value="${['января','февраля','марта','апреля','мая','июня','июля','августа','сентября','октября','ноября','декабря']}"/>


<ui:layout title="Профиль">
    <div class="profile">

        <div class="profile-head">
            <c:choose>
                <c:when test="${not empty profile.avatarFileId}">
                    <img class="avatar avatar-big" src="${ctx}/files?id=${profile.avatarFileId}" alt="">
                </c:when>
                <c:otherwise>
                    <span class="avatar avatar-big">${fn:toUpperCase(fn:substring(profile.username, 0, 1))}</span>
                </c:otherwise>
            </c:choose>

            <div>
                <h1>${profile.username}</h1>
                <span class="muted">
                    ${profile.role == 'ADMIN' ? 'Администратор' : 'Пользователь'}
                    · с ${profile.createdAt.dayOfMonth} ${months[profile.createdAt.monthValue - 1]} ${profile.createdAt.year}
                </span>
            </div>
        </div>

        <div class="profile-row">
            <span>Логин</span>
            <span>${profile.username}</span>
        </div>
        <div class="profile-row">
            <span>Email</span>
            <span>${profile.email}</span>
        </div>
        <div class="profile-row">
            <span>Роль</span>
            <span>${profile.role}</span>
        </div>
        <div class="profile-row">
            <span>Секретная фраза</span>
            <span>10 слов, сохранены при регистрации</span>
        </div>

        <h2>Аватар</h2>
        <form class="avatar-form" method="post" action="${ctx}/profile/avatar" enctype="multipart/form-data">
            <label for="avatar">PNG, JPG или WEBP до 2 МБ</label>
            <input type="file" name="avatar" id="avatar" accept="image/png,image/jpeg,image/webp">
            <button type="submit">Загрузить</button>
        </form>

    </div>
</ui:layout>
