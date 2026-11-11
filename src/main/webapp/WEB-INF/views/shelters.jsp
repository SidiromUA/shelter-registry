<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="uk">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Укриття — Реєстр укриттів</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="header.jspf" %>

<main id="main">
  <h1>Укриття міста</h1>

  <c:if test="${not empty message}">
    <p class="message" role="status"><c:out value="${message}"/></p>
  </c:if>

  <table>
    <caption>Зареєстровано укриттів: ${shelters.size()}</caption>
    <thead>
    <tr>
      <th scope="col">Назва</th><th scope="col">Адреса</th><th scope="col">Район</th>
      <th scope="col">Тип</th><th scope="col">Місткість, осіб</th>
      <th scope="col">Доступність</th><th scope="col">Стан</th>
      <c:if test="${sessionScope.user.role() == 'ADMIN'}"><th scope="col">Дії</th></c:if>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="s" items="${shelters}">
      <tr>
        <%-- c:out екранує HTML-символи і захищає від XSS --%>
        <td><c:out value="${s.name()}"/></td>
        <td><c:out value="${s.address()}"/></td>
        <td><c:out value="${s.district()}"/></td>
        <td><c:out value="${s.type()}"/></td>
        <td>${s.capacity()}</td>
        <td><c:out value="${s.accessibility()}"/></td>
        <td><span class="status status-${s.statusCode().toLowerCase().replace('_', '-')}"><c:out value="${s.status()}"/></span></td>
        <c:if test="${sessionScope.user.role() == 'ADMIN'}">
          <td>
            <form action="${pageContext.request.contextPath}/shelters/delete" method="post" class="inline">
              <input type="hidden" name="id" value="${s.id()}">
              <button type="submit" class="danger" aria-label="Видалити <c:out value='${s.name()}'/>">Видалити</button>
            </form>
          </td>
        </c:if>
      </tr>
    </c:forEach>
    </tbody>
  </table>

  <h2>Додати укриття</h2>
  <c:choose>
    <c:when test="${empty sessionScope.user}">
      <p><a href="${pageContext.request.contextPath}/login">Увійдіть</a>, щоб додавати укриття.</p>
    </c:when>
    <c:otherwise>
      <c:if test="${not empty errors}">
        <div class="errors" role="alert">
          <h3>Виправте помилки</h3>
          <ul><c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach></ul>
        </div>
      </c:if>

      <form action="${pageContext.request.contextPath}/shelters" method="post" novalidate>
        <div class="field">
          <label for="name">Назва</label>
          <input id="name" name="name" type="text" required value="<c:out value='${form.name()}'/>">
        </div>
        <div class="field">
          <label for="address">Адреса</label>
          <input id="address" name="address" type="text" required value="<c:out value='${form.address()}'/>">
        </div>
        <div class="field">
          <label for="district">Район</label>
          <select id="district" name="districtId" required>
            <option value="">— оберіть район —</option>
            <c:forEach var="d" items="${districts}">
              <option value="${d.id()}" ${form.districtId() == d.id() ? 'selected' : ''}><c:out value="${d.name()}"/></option>
            </c:forEach>
          </select>
        </div>
        <fieldset class="field">
          <legend>Тип укриття</legend>
          <label><input type="radio" name="type" value="BASEMENT" ${form.type() == 'BASEMENT' ? 'checked' : ''}> Підвал</label>
          <label><input type="radio" name="type" value="PARKING" ${form.type() == 'PARKING' ? 'checked' : ''}> Паркінг</label>
          <label><input type="radio" name="type" value="CIVIL_DEFENCE" ${form.type() == 'CIVIL_DEFENCE' ? 'checked' : ''}> Захисна споруда ЦЗ</label>
          <label><input type="radio" name="type" value="METRO" ${form.type() == 'METRO' ? 'checked' : ''}> Метрополітен</label>
        </fieldset>
        <div class="field">
          <label for="capacity">Місткість, осіб</label>
          <input id="capacity" name="capacity" type="number" min="1" required value="${form.capacity()}">
        </div>
        <div class="field">
          <label><input type="checkbox" name="accessible" value="true" ${form.accessible() ? 'checked' : ''}>
            Доступне для маломобільних груп</label>
        </div>
        <button type="submit">Зберегти</button>
      </form>
    </c:otherwise>
  </c:choose>
</main>
</body>
</html>
