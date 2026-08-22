WITH ordered_questions AS (
    SELECT question.id,
           row_number() OVER (
               PARTITION BY question.project_role_id
               ORDER BY question.uuid
           ) AS position
    FROM question
    WHERE question.uuid LIKE 'seed-%'
),
questions_with_levels AS (
    SELECT ordered_questions.id AS question_id,
           CASE
               WHEN ordered_questions.position = 1 THEN 'INTERN'
               WHEN ordered_questions.position = 2 THEN 'JUNIOR'
               WHEN ordered_questions.position = 3 THEN 'JUNIOR+'
               WHEN ordered_questions.position IN (4, 5) THEN 'MIDDLE'
               WHEN ordered_questions.position IN (6, 7) THEN 'MIDDLE+'
               WHEN ordered_questions.position IN (8, 9) THEN 'SENIOR'
               WHEN ordered_questions.position = 10 THEN 'INDUSTRY_EXPERT'
           END AS career_level_code
    FROM ordered_questions
)
INSERT INTO assessment_project_question(project_id, question_id, career_level_id)
SELECT assessment_project.id,
       questions_with_levels.question_id,
       career_level.id
FROM assessment_project
JOIN users AS project_owner ON project_owner.id = assessment_project.owner_id
CROSS JOIN questions_with_levels
JOIN career_level ON career_level.code = questions_with_levels.career_level_code
WHERE project_owner.username IN ('manager1', 'manager2')
  AND assessment_project.name IN (
    'Система управления заказами',
    'Платформа клиентских данных',
    'Корпоративный портал',
    'Сервис электронных платежей'
);
