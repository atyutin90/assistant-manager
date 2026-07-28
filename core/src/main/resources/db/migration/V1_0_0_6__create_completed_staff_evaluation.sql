INSERT INTO staff_evaluation(status, date_from, date_to, name)
VALUES (
    'COMPLETED',
    DATE '2026-01-01',
    DATE '2026-12-31',
    'Комплексная оценка по всем проектным ролям'
);

INSERT INTO staff_evaluation_question(staff_evaluation_id, question_id, position)
SELECT staff_evaluation.id,
       question.id,
       row_number() OVER (
           ORDER BY project_role.position, question.uuid
       )::INTEGER
FROM staff_evaluation
CROSS JOIN question
JOIN project_role ON project_role.id = question.project_role_id
WHERE staff_evaluation.name = 'Комплексная оценка по всем проектным ролям'
  AND question.uuid LIKE 'seed-%';

INSERT INTO staff_evaluation_user(
    feedback_message,
    staff_evaluation_id,
    user_id,
    status,
    verified_by
)
SELECT 'Ответы на вопросы заполнены и переданы ответственному на проверку.',
       staff_evaluation.id,
       employee.id,
       'COMPLETED',
       responsible.id
FROM (VALUES
    ('analyst1', 'analyst2'),
    ('backend1', 'backend2'),
    ('devops1', 'devops2'),
    ('frontend1', 'frontend2'),
    ('autoqa1', 'autoqa2'),
    ('qa1', 'qa2')
) AS assignments(employee_username, responsible_username)
JOIN users AS employee ON employee.username = assignments.employee_username
JOIN users AS responsible ON responsible.username = assignments.responsible_username
CROSS JOIN staff_evaluation
WHERE staff_evaluation.name = 'Комплексная оценка по всем проектным ролям';

WITH answers AS (
    SELECT question.id AS question_id,
           staff_evaluation_user.id AS staff_evaluation_user_id,
           CASE
               WHEN right(question.uuid, 2)::INTEGER <= 7 THEN 'YES'
               WHEN right(question.uuid, 2)::INTEGER <= 10 THEN 'NO'
           END AS response
    FROM staff_evaluation_user
    JOIN staff_evaluation
        ON staff_evaluation.id = staff_evaluation_user.staff_evaluation_id
    JOIN users
        ON users.id = staff_evaluation_user.user_id
    JOIN question
        ON question.project_role_id = users.project_role_id
    WHERE staff_evaluation.name = 'Комплексная оценка по всем проектным ролям'
      AND question.uuid LIKE 'seed-%'
)
INSERT INTO staff_evaluation_answer(
    response,
    question_id,
    staff_evaluation_user_id,
    verified_response,
    verification_comment
)
SELECT answers.response,
       answers.question_id,
       answers.staff_evaluation_user_id,
       answers.response,
       'Ответ подтвержден ответственным.'
FROM answers;
