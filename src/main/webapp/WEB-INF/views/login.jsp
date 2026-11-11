<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="uk">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Вхід — Реєстр укриттів</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="header.jspf" %>

<main id="main">
  <h1>Вхід до системи</h1>

  <c:if test="${not empty error}">
    <p class="errors" role="alert"><c:out value="${error}"/></p>
  </c:if>

  <form action="${pageContext.request.contextPath}/login" method="post">
    <div class="field">
      <label for="username">Логін</label>
      <input id="username" name="username" type="text" required autocomplete="username"
             value="<c:out value='${param.username}'/>">
    </div>
    <div class="field">
      <label for="password">Пароль</label>
      <input id="password" name="password" type="password" required autocomplete="current-password">
    </div>
    <button type="submit">Увійти</button>
  </form>
  <p class="hint">Навчальні облікові записи: admin / admin, inspector / inspector.</p>
</main>
</body>
</html>
