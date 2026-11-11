<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="uk">
<head>
  <meta charset="UTF-8">
  <title>Помилка ${requestScope['jakarta.servlet.error.status_code']} — Реєстр укриттів</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main id="main">
  <h1>Помилка ${requestScope['jakarta.servlet.error.status_code']}</h1>
  <p><c:out value="${requestScope['jakarta.servlet.error.message']}" default="Щось пішло не так."/></p>
  <p><a href="${pageContext.request.contextPath}/shelters">До списку укриттів</a></p>
</main>
</body>
</html>
