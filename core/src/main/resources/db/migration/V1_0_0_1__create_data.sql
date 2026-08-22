INSERT INTO skill(enabled, code, name, position)
VALUES (true, 'HARD', 'Технические навыки', 1),
       (true, 'SOFT', 'Личностные навыки', 2),
       (true, 'PROJECT', 'Проектные навыки', 3);

INSERT INTO project_role(enabled, code, name, position)
VALUES (true, 'ANALYST', 'Аналитик', 1),
       (true, 'BE', 'Back-End', 2),
       (true, 'DEVOPS', 'DevOps', 3),
       (true, 'FE', 'Front-End', 4),
       (true, 'LOAD_QA', 'AutoQA', 5),
       (true, 'QA', 'QA', 6);

INSERT INTO career_level(enabled, code, name, position)
VALUES (true, 'INTERN', 'Intern', 1),
       (true, 'JUNIOR', 'Junior', 2),
       (true, 'JUNIOR+', 'Junior+', 3),
       (true, 'MIDDLE', 'Middle', 4),
       (true, 'MIDDLE+', 'Middle+', 5),
       (true, 'SENIOR', 'Senior', 6),
       (true, 'INDUSTRY_EXPERT', 'Industry Expert', 7);
