INSERT INTO assessment_project(active, name, owner_id)
SELECT true,
       source.name,
       users.id
FROM (VALUES
    ('manager1', 'Система управления заказами'),
    ('manager1', 'Платформа клиентских данных'),
    ('manager2', 'Корпоративный портал'),
    ('manager2', 'Сервис электронных платежей')
) AS source(owner_username, name)
JOIN users ON users.username = source.owner_username;
