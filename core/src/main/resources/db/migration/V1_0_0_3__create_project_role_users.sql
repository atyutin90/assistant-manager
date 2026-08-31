-- BCrypt strength 10 hash for the password "1111".
INSERT INTO users(
    first_name,
    last_name,
    middle_name,
    password,
    username,
    email,
    labor_code_position,
    current_level_id
)
SELECT source.first_name,
       source.last_name,
       source.middle_name,
       '$2y$10$AP2Xn.91biyDni63brPeau9KToLu/lcIWKyQVe2VqDtUMefYnzny6',
       source.username,
       source.email,
       source.labor_code_position,
       career_level.id
FROM (VALUES
    ('Алексей', 'Аналитиков', 'Игоревич', 'analyst1', 'analyst1@example.com', 'Системный аналитик', 'JUNIOR', 'ANALYST'),
    ('Мария', 'Требования', 'Олеговна', 'analyst2', 'analyst2@example.com', 'Бизнес-аналитик', 'MIDDLE', 'ANALYST'),
    ('Иван', 'Бэкендов', 'Сергеевич', 'backend1', 'backend1@example.com', 'Back-End разработчик', 'JUNIOR', 'BE'),
    ('Елена', 'Сервисова', 'Андреевна', 'backend2', 'backend2@example.com', 'Back-End разработчик', 'MIDDLE', 'BE'),
    ('Павел', 'Инфраструктурин', 'Дмитриевич', 'devops1', 'devops1@example.com', 'DevOps-инженер', 'JUNIOR', 'DEVOPS'),
    ('Ольга', 'Пайплайнова', 'Романовна', 'devops2', 'devops2@example.com', 'DevOps-инженер', 'MIDDLE', 'DEVOPS'),
    ('Денис', 'Фронтендов', 'Максимович', 'frontend1', 'frontend1@example.com', 'Front-End разработчик', 'JUNIOR', 'FE'),
    ('Анна', 'Интерфейсова', 'Викторовна', 'frontend2', 'frontend2@example.com', 'Front-End разработчик', 'MIDDLE', 'FE'),
    ('Никита', 'Автотестов', 'Алексеевич', 'autoqa1', 'autoqa1@example.com', 'Инженер по автоматизации тестирования', 'JUNIOR', 'LOAD_QA'),
    ('Светлана', 'Нагрузкина', 'Ильинична', 'autoqa2', 'autoqa2@example.com', 'Инженер по автоматизации тестирования', 'MIDDLE', 'LOAD_QA'),
    ('Артем', 'Тестировщиков', 'Петрович', 'qa1', 'qa1@example.com', 'Инженер по тестированию', 'JUNIOR', 'QA'),
    ('Ирина', 'Качествова', 'Сергеевна', 'qa2', 'qa2@example.com', 'Инженер по тестированию', 'MIDDLE', 'QA')
) AS source(
    first_name,
    last_name,
    middle_name,
    username,
    email,
    labor_code_position,
    career_level_code,
    project_role_code
)
JOIN career_level ON career_level.code = source.career_level_code
JOIN project_role ON project_role.code = source.project_role_code;

INSERT INTO users(first_name, last_name, middle_name, password, username, email, labor_code_position)
VALUES
    ('Михаил', 'Проектов', 'Александрович',
     '$2y$10$AP2Xn.91biyDni63brPeau9KToLu/lcIWKyQVe2VqDtUMefYnzny6',
     'manager1', 'manager1@example.com', 'Менеджер проектов'),
    ('Наталья', 'Управленцева', 'Игоревна',
     '$2y$10$AP2Xn.91biyDni63brPeau9KToLu/lcIWKyQVe2VqDtUMefYnzny6',
     'manager2', 'manager2@example.com', 'Менеджер проектов');

INSERT INTO user_role(user_id, role)
SELECT users.id, 'USER'
FROM users
WHERE users.username IN (
    'analyst1', 'analyst2',
    'backend1', 'backend2',
    'devops1', 'devops2',
    'frontend1', 'frontend2',
    'autoqa1', 'autoqa2',
    'qa1', 'qa2'
);

INSERT INTO user_role(user_id, role)
SELECT users.id, 'TEAM_LEAD'
FROM users
WHERE users.username IN ('analyst2', 'backend2', 'devops2', 'frontend2', 'autoqa2', 'qa2');

INSERT INTO user_role(user_id, role)
SELECT users.id, 'MANAGER'
FROM users
WHERE users.username IN ('manager1', 'manager2');

INSERT INTO user_project_role(user_id, project_role_id)
SELECT users.id, project_role.id
FROM
    (VALUES ('analyst1', 'ANALYST'),
            ('analyst2', 'ANALYST'),
            ('backend1', 'BE'),
            ('backend2', 'BE'),
            ('devops1', 'DEVOPS'),
            ('devops2', 'DEVOPS'),
            ('frontend1', 'FE'),
            ('frontend2', 'FE'),
            ('autoqa1', 'LOAD_QA'),
            ('autoqa2', 'LOAD_QA'),
            ('qa1', 'QA'),
            ('qa2', 'QA')) AS data(username, project_role_code), users, project_role
WHERE data.username = users.username AND project_role.code = data.project_role_code;

UPDATE users AS employee
SET responsible_id = responsible.id
    FROM users AS responsible,
     (VALUES
         ('analyst1', 'analyst2'),
         ('backend1', 'backend2'),
         ('devops1', 'devops2'),
         ('frontend1', 'frontend2'),
         ('autoqa1', 'autoqa2'),
         ('qa1', 'qa2')
     ) AS assignments(employee_username, responsible_username)
WHERE employee.username = assignments.employee_username
  AND responsible.username = assignments.responsible_username;