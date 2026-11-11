-- ЛР8. Міграція Flyway V1: початкова схема БД «Реєстр укриттів».
-- Flyway виконує кожну міграцію один раз і записує це в таблицю flyway_schema_history.
-- Змінювати вже застосовані міграції не можна — лише додавати нові (V3__..., V4__...).

CREATE TABLE districts (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE users (
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'INSPECTOR'))
);

CREATE TABLE shelters (
    id                   SERIAL PRIMARY KEY,
    name                 VARCHAR(150) NOT NULL,
    address              VARCHAR(255) NOT NULL,
    district_id          INT          NOT NULL REFERENCES districts (id),
    type                 VARCHAR(30)  NOT NULL
                         CHECK (type IN ('BASEMENT', 'PARKING', 'CIVIL_DEFENCE', 'METRO')),
    capacity             INT          NOT NULL CHECK (capacity > 0),
    accessible           BOOLEAN      NOT NULL DEFAULT FALSE,   -- доступність для маломобільних груп
    status               VARCHAR(20)  NOT NULL DEFAULT 'NOT_READY'
                         CHECK (status IN ('READY', 'LIMITED', 'NOT_READY')),
    last_inspection_date DATE,
    UNIQUE (address)
);

CREATE TABLE inspections (
    id              SERIAL PRIMARY KEY,
    shelter_id      INT         NOT NULL REFERENCES shelters (id) ON DELETE CASCADE,
    inspector_id    INT         NOT NULL REFERENCES users (id),
    inspection_date DATE        NOT NULL,
    result          VARCHAR(20) NOT NULL CHECK (result IN ('PASSED', 'FAILED', 'PENDING')),
    notes           TEXT
);

-- Індекси для частих запитів: пошук укриттів за районом і перевірок за укриттям
CREATE INDEX idx_shelters_district    ON shelters (district_id);
CREATE INDEX idx_inspections_shelter  ON inspections (shelter_id);
