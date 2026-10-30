<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="title" required="true" %>
<%@ attribute name="active" required="false" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>${title} — админка Tradance</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:wght@400;500;600;700&amp;display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="admin-body">

    <header class="admin-top">
        <a class="brand" href="${ctx}/admin/currencies">
            <svg width="26" height="26" viewBox="0 0 26 26" aria-hidden="true">
                <rect x="3" y="9" width="5" height="12" rx="1" fill="#FF8A1E"/>
                <rect x="5" y="5" width="1" height="18" fill="#FF8A1E"/>
                <rect x="12" y="4" width="5" height="10" rx="1" fill="#EAECEF"/>
                <rect x="14" y="2" width="1" height="15" fill="#EAECEF"/>
                <rect x="20" y="11" width="4" height="8" rx="1" fill="#FF8A1E"/>
            </svg>
            Tradance
        </a>
        <span class="admin-badge">ADMIN</span>

        <div class="admin-top-right">
            <a href="${ctx}/market">← На биржу</a>
            <a href="${ctx}/logout">Выход</a>
        </div>
    </header>

    <div class="admin-shell">
        <aside class="admin-side">
            <ui:adminMenu active="${active}"/>
        </aside>

        <main class="admin-main">
            <ui:flash/>
            <jsp:doBody/>
        </main>
    </div>

</body>
</html>
