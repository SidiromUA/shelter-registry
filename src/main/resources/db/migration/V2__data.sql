-- ЛР8. Міграція Flyway V2: тестові дані (навчальні, не відповідають реальним об’єктам)

INSERT INTO districts (name) VALUES
    ('Галицький'), ('Залізничний'), ('Личаківський'),
    ('Сихівський'), ('Франківський'), ('Шевченківський');

-- Паролі: admin / admin, inspector / inspector, kovalenko / inspector. Зберігаються лише хеші BCrypt.
INSERT INTO users (username, password_hash, full_name, role) VALUES
    ('admin',     '{bcrypt}$2y$10$oaUim9D15hJEaW7JdO8qqumKFr5MD8o0TKPNNU0qBEdBUQQdVTrZ2',     'Адміністратор системи', 'ADMIN'),
    ('inspector', '{bcrypt}$2y$10$.VPcW6TwHvQIV6GOWkToOe5sy3ZLYu10cVpbbhOdXGGD6ijqk4.TW', 'Петренко Олена',        'INSPECTOR'),
    ('kovalenko', '{bcrypt}$2y$10$.VPcW6TwHvQIV6GOWkToOe5sy3ZLYu10cVpbbhOdXGGD6ijqk4.TW', 'Коваленко Андрій',      'INSPECTOR');

INSERT INTO shelters (name, address, district_id, type, capacity, accessible, status, last_inspection_date) VALUES
    ('Укриття № 1',          'вул. Клепарівська, 35',      6, 'BASEMENT',      120, TRUE,  'READY',     '2027-01-15'),
    ('Підземний паркінг ТЦ', 'вул. Городоцька, 120',       2, 'PARKING',       450, TRUE,  'READY',     '2027-01-20'),
    ('Школа № 12',           'вул. Наукова, 7',            5, 'CIVIL_DEFENCE', 300, FALSE, 'LIMITED',   '2027-01-25'),
    ('Укриття № 4',          'просп. Червоної Калини, 60', 4, 'BASEMENT',       80, FALSE, 'NOT_READY', '2026-11-28'),
    ('Ліцей № 3',            'вул. Личаківська, 85',       3, 'CIVIL_DEFENCE', 250, TRUE,  'READY',     '2027-01-05'),
    ('Укриття № 6',          'пл. Ринок, 10',              1, 'BASEMENT',       60, FALSE, 'LIMITED',   NULL);

INSERT INTO inspections (shelter_id, inspector_id, inspection_date, result, notes) VALUES
    (1, 2, '2027-01-15', 'PASSED',  'Освітлення та вентиляція в нормі'),
    (2, 3, '2027-01-20', 'PASSED',  NULL),
    (3, 2, '2026-12-10', 'FAILED',  'Немає запасу питної води'),
    (4, 3, '2026-11-28', 'FAILED',  'Затоплення приміщення'),
    (5, 2, '2027-01-05', 'PASSED',  'Пандус і ліфт працюють'),
    (3, 3, '2027-01-25', 'PENDING', 'Повторна перевірка після усунення недоліків');
