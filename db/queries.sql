-- ЛР2. Приклади SQL-запитів

-- 1. Усі готові укриття, від найбільших
SELECT name, address, capacity
FROM shelters
WHERE status = 'READY'
ORDER BY capacity DESC;

-- 2. З’єднання таблиць: укриття з назвою району
SELECT s.name, s.address, d.name AS district, s.status
FROM shelters s
JOIN districts d ON d.id = s.district_id
ORDER BY d.name, s.name;

-- 3. Групування: загальна місткість і кількість укриттів за районами
SELECT d.name AS district,
       COUNT(s.id)                 AS shelters,
       COALESCE(SUM(s.capacity), 0) AS total_capacity
FROM districts d
LEFT JOIN shelters s ON s.district_id = d.id
GROUP BY d.name
ORDER BY total_capacity DESC;

-- 4. HAVING: райони, де менше двох готових укриттів
SELECT d.name, COUNT(s.id) AS ready_count
FROM districts d
LEFT JOIN shelters s ON s.district_id = d.id AND s.status = 'READY'
GROUP BY d.name
HAVING COUNT(s.id) < 2;

-- 5. Підзапит: укриття, місткість яких більша за середню
SELECT name, capacity
FROM shelters
WHERE capacity > (SELECT AVG(capacity) FROM shelters);

-- 6. Три таблиці: остання перевірка кожного укриття та інспектор
SELECT s.name, i.inspection_date, i.result, u.full_name AS inspector
FROM inspections i
JOIN shelters s ON s.id = i.shelter_id
JOIN users u    ON u.id = i.inspector_id
WHERE i.inspection_date = (SELECT MAX(i2.inspection_date)
                           FROM inspections i2
                           WHERE i2.shelter_id = i.shelter_id);

-- 7. Доступні для маломобільних груп укриття
SELECT name, address FROM shelters WHERE accessible;

-- 8. Оновлення та видалення
UPDATE shelters SET status = 'LIMITED' WHERE id = 4;
DELETE FROM inspections WHERE result = 'PENDING' AND inspection_date < CURRENT_DATE - INTERVAL '1 year';
