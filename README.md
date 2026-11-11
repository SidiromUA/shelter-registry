# Реєстр укриттів міста

Навчальний наскрізний проєкт дисципліни **«WEB-технології та WEB-програмування»**
(ЛДУ БЖД, кафедра ІТтаСЕК, спеціальність F3 «Комп’ютерні науки», 3 курс).

Протягом семестру застосунок зростає від статичної сторінки до вебсервісу на Spring Boot.
Кожен етап зафіксовано окремим git-тегом — стан проєкту на момент відповідної лабораторної роботи.

| Тег | Лабораторна робота | Що додано |
|---|---|---|
| `lab-01` | Середовище, HTTP, статична сторінка | `web/index.html`, приклади HTTP-запитів |
| `lab-02` | БД і JDBC | SQL-схема, DAO на JDBC, транзакції |
| `lab-03` | MVC і доступність | шари model / repository / service, WCAG |
| `lab-04` | Servlet API, Tomcat | сервлети, JSP, сесії, фільтр авторизації |
| `lab-05` | Spring Boot | біни, впровадження залежностей, JdbcClient, перший REST-контролер |
| `lab-06` | Конфігурація | профілі, `application.yml`, логування, Docker |
| `lab-07` | Вебсторінки і REST API | Thymeleaf, CRUD API, валідація, Swagger |
| `lab-08` | JPA, безпека, тести | Spring Data JPA, Flyway, Spring Security, JUnit |

Переглянути стан етапу:

```bash
git clone https://github.com/SidiromUA/shelter-registry.git
cd shelter-registry
git checkout lab-02
```

## Предметна область

- **Район** (`District`) — адміністративний район міста.
- **Укриття** (`Shelter`) — назва, адреса, тип, місткість, доступність для маломобільних груп, стан готовності.
- **Перевірка** (`Inspection`) — дата, результат, примітки, інспектор.
- **Користувач** (`User`) — роль ADMIN або INSPECTOR.

> Дані навчальні та не відповідають реальним об’єктам.

## Запуск

### ЛР1 — статична сторінка

```bash
cd web
jwebserver -p 8000      # JDK 18+
```

Далі — http://localhost:8000. Приклади HTTP-запитів: [docs/http-examples.md](docs/http-examples.md).

### ЛР2 — база даних і JDBC

Потрібні JDK 21, Maven, PostgreSQL.

```bash
# 1. Створити користувача та БД (один раз)
psql -d postgres -c "CREATE ROLE shelter LOGIN PASSWORD 'shelter';"
psql -d postgres -c "CREATE DATABASE shelter_registry OWNER shelter;"

# 2. Схема і тестові дані (з ЛР6 файли лежать у src/main/resources/db/)
psql -h localhost -U shelter -d shelter_registry -f src/main/resources/db/schema.sql -f src/main/resources/db/data.sql

# 3. Консольний застосунок
mvn compile exec:java
```

Параметри підключення можна змінити змінними оточення `DB_URL`, `DB_USER`, `DB_PASSWORD`.
Приклади SQL-запитів — [db/queries.sql](db/queries.sql).

### ЛР3 — багаторівнева архітектура і доступність

```
controller  →  service  →  repository (інтерфейс)  →  repository.jdbc (реалізація)  →  БД
    ↓              ↓
  view          model, dto, exception
```

На етапі `lab-03` команда `mvn compile exec:java` генерує сторінки `web/shelters.html` і `web/shelters-errors.html`.
Порівняйте `web/inaccessible-example.html` з `web/index.html` і `web/add-shelter.html` —
розбір порушень WCAG у [docs/accessibility.md](docs/accessibility.md).

### ЛР4 — Servlet API та Apache Tomcat

| URL | Метод | Обробник | Доступ |
|---|---|---|---|
| `/shelters` | GET | `ShelterServlet` → `shelters.jsp` | усі |
| `/shelters` | POST | `ShelterServlet` (додавання, валідація, PRG) | після входу |
| `/shelters/delete` | POST | `ShelterDeleteServlet` | лише ADMIN |
| `/login` | GET, POST | `LoginServlet` (сесія) | усі |
| `/logout` | POST | `LogoutServlet` | усі |

`AuthFilter` перевіряє сесію, `EncodingFilter` встановлює UTF-8, `AppContextListener` створює сервіси під час запуску.

```bash
# Варіант 1: вбудований Tomcat (нічого встановлювати не потрібно)
mvn compile exec:exec                  # http://localhost:8080/shelters

# Варіант 2: окремий Apache Tomcat 10.1+
mvn package                            # target/shelters.war
cp target/shelters.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh          # http://localhost:8080/shelters/shelters
```

Облікові записи: `admin` / `admin` (може видаляти), `inspector` / `inspector`.

### ЛР5 — Spring Boot

Проєкт перенесено на **Spring Boot 4.1** (Java 21, Maven). Порівняйте з `lab-04`:

| Було (ЛР3–4) | Стало (ЛР5) |
|---|---|
| `new ShelterService(new JdbcShelterRepository(), …)` вручну | `@Service`, `@Repository` — об’єкти створює і зв’язує Spring (IoC/DI) |
| `DriverManager.getConnection(...)` | `DataSource` (пул HikariCP) з `application.properties` |
| `PreparedStatement`, `ResultSet`, `try-with-resources` | `JdbcClient` |
| `setAutoCommit(false)` / `commit()` / `rollback()` | `@Transactional` |
| `HttpServlet.doGet` + JSP | `@RestController` + JSON |
| WAR + окремий Tomcat | виконуваний jar з вбудованим Tomcat |

```bash
mvn spring-boot:run          # або: mvn package && java -jar target/shelter-registry-0.5.0.jar
```

| Запит | Відповідь |
|---|---|
| `GET /hello?name=Олена` | текст |
| `GET /api/shelters`, `GET /api/shelters?districtId=6` | список укриттів (JSON) |
| `GET /api/shelters/3` | одне укриття; неіснуюче — 404 |
| `GET /api/shelters/3/inspections` | перевірки укриття |
| `GET /api/districts` | райони |

### ЛР6 — конфігурація, профілі, збирання, Docker

| Файл | Призначення |
|---|---|
| `application.yml` | спільні налаштування, власний блок `registry:`, Actuator |
| `application-dev.yml` | H2 у пам’яті, схема і дані при кожному запуску, H2-консоль, журнал SQL |
| `application-prod.yml` | PostgreSQL зі змінних оточення `DB_URL`, `DB_USER`, `DB_PASSWORD`, журнал у файл |
| `RegistryProperties` | типізовані налаштування `@ConfigurationProperties` з перевіркою значень |

```bash
mvn spring-boot:run                                   # профіль dev — БД не потрібна
# http://localhost:8080/h2-console  (JDBC URL: jdbc:h2:mem:shelters, користувач sa, без пароля)

mvn package
SPRING_PROFILES_ACTIVE=prod java -jar target/shelter-registry-*.jar     # PostgreSQL
java -jar target/shelter-registry-*.jar --registry.inspection-interval-days=30 --server.port=9090

docker compose up --build                             # застосунок + PostgreSQL у контейнерах
```

Нові ендпоінти: `GET /api/shelters/overdue` (прострочені перевірки), `GET /actuator/health`, `GET /actuator/info`.

### ЛР7 — вебсторінки (Thymeleaf) і RESTful API

**Вебінтерфейс** (`ShelterWebController`, шаблони в `src/main/resources/templates/`):

| URL | Сторінка |
|---|---|
| `GET /shelters?districtId=5` | список з фільтром за районом (запам’ятовується в cookie) |
| `GET /shelters/{id}` | картка укриття, історія перевірок, реєстрація перевірки |
| `GET /shelters/new`, `POST /shelters` | створення: помилки валідації — біля полів форми |
| `GET /shelters/{id}/edit`, `POST /shelters/{id}` | редагування |
| `POST /shelters/{id}/delete` | видалення |

**REST API** (`ShelterApiController`), документація — http://localhost:8080/swagger-ui.html:

| Метод і шлях | Дія | Коди відповіді |
|---|---|---|
| `GET /api/shelters` | список | 200 |
| `GET /api/shelters/{id}` | одне укриття | 200, 404 |
| `POST /api/shelters` | створити | 201 + `Location`, 400 |
| `PUT /api/shelters/{id}` | оновити | 200, 400, 404 |
| `DELETE /api/shelters/{id}` | видалити | 204, 404 |
| `GET /api/shelters/{id}/inspections` | перевірки | 200, 404 |
| `POST /api/shelters/{id}/inspections` | зареєструвати перевірку | 200, 400, 404, 409 |

Валідація: анотації Bean Validation у `ShelterForm` + `@Valid`; бізнес-правила — у `ShelterService`.
Помилки API — у форматі Problem Details (RFC 9457), обробник `ApiExceptionHandler`:

```bash
curl -X POST localhost:8080/api/shelters -H 'Content-Type: application/json' \
     -d '{"name":"","districtId":4,"type":"BASEMENT","capacity":0}'
# {"title":"Некоректні дані","status":400,"errors":{"name":"Вкажіть назву укриття", ...}}
```
