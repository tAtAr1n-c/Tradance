<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<%@ attribute name="title" required="true" %>

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>${title} — Tradance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <ui:navbar/>

    <main>
        <ui:flash/>
        <jsp:doBody/>
    </main>

    <ui:footer/>
</body>
</html>
