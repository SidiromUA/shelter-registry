-- ЛР2. Тестові дані (навчальні, не відповідають реальним об’єктам)

INSERT INTO districts (name) VALUES
    ('Галицький'), ('Залізничний'), ('Личаківський'),
    ('Сихівський'), ('Франківський'), ('Шевченківський');

-- Паролі: admin / admin, inspector / inspector (BCrypt знадобиться в ЛР8; поки що — заглушка)
INSERT INTO users (username, password_hash, full_name, role) VALUES
    ('admin',     '{noop}admin',     'Адміністратор системи', 'ADMIN'),
    ('inspector', '{noop}inspector', 'Петренко Олена',        'INSPECTOR'),
    ('kovalenko', '{noop}inspector', 'Коваленко Андрій',      'INSPECTOR');

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
