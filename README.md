# Assistant Manager

Веб-приложение для проведения оценки сотрудников: администратор настраивает
справочники и оценочные кампании, менеджер управляет проектами и наборами
вопросов, сотрудники проходят опросы, а руководители проверяют ответы.

Проект состоит из четырёх отдельных Spring Boot-приложений, которые используют
общий модуль `core`. Бизнес-приложения работают с PostgreSQL, а модуль интеграции
получает почтовые сообщения из Kafka и отправляет их через SMTP.

## Возможности

- управление пользователями, компетенциями, карьерными уровнями и проектными
  ролями;
- создание и импорт вопросов;

- управление проектами, доступом менеджеров и вопросами проектов;
- прохождение сотрудниками анкет;
- проверка ответов руководителями;
- авторизация через JWT в cookie;
- автоматическое создание структуры БД и тестовых данных через Flyway.

## Структура проекта

```text
assistant-manager/
├── administration/       # административное приложение
├── profile/              # профиль сотрудников
├── core/                 # общая бизнес-логика и инфраструктура
├── integration/          # отправка email из Kafka
├── manager/              # приложение менеджера
├── gradle/               # Gradle Wrapper и каталог версий
├── build.gradle.kts      # общая конфигурация сборки
├── settings.gradle.kts   # состав multi-module проекта
└── .dockerignore         # исключения из Docker build context
```


## Модули

| Модуль           | Назначение                                                                                                                       | Роли |   Порт |
|------------------|----------------------------------------------------------------------------------------------------------------------------------| --- |-------:|
| `administration` | Администрирование справочников, пользователей, вопросов и настройка оценок                                                       | `ADMIN` | `8889` |
| `profile`        | Профиль сотрудника                                                                                                               | `USER`, `TEAM_LEAD` |  `8890` |
| `manager`        | Управление проектами для подбора сотрудников и фильтрацией сотрудников                                                           | `MANAGER` | `8891` |
| `integration`    | Чтение сообщений из `email-topic` и отправка email через SMTP                                                                    | — | — |
| `core`           | Общие сущности, репозитории, сервисы, безопасность, ресурсы и миграции  (является библиотечным модулем и отдельно не запускается) | — |      — |

## Технологии

- Java 17;
- Gradle 9.3;
- Spring Boot 3.5.14;
- Spring MVC и Thymeleaf;
- Spring Data JPA и Hibernate;
- Spring Security и JWT;
- PostgreSQL и Flyway;
- Apache Kafka и SMTP;
- Lombok, OpenCSV и Apache Commons;

## Требования для локального запуска
- JDK 17;
- PostgreSQL;
- Docker с плагином Docker Compose — опционально, для контейнерного запуска.

## Подготовка базы данных

По умолчанию приложения используют следующие параметры:

```text
URL:      jdbc:postgresql://localhost:5433/assistant_db
Username: postgres
Password: postgres
Schema:   assistant_db
```

Быстрее всего запустить PostgreSQL через Docker из docker-compose:

```bash
docker compose up -d postgres
```

При первом запуске приложения Flyway автоматически применит миграции из
`core/src/main/resources/db/migration` и добавит демонстрационные данные.
Первый модуль рекомендуется запустить отдельно и дождаться окончания миграций,
после чего можно запускать остальные.

## Локальный запуск

Запустите нужные приложения в отдельных терминалах из корня проекта:

```bash
./gradlew :profile:bootRun
```

```bash
./gradlew :administration:bootRun
```

```bash
./gradlew :manager:bootRun
```

```bash
./gradlew :integration:bootRun
```

После запуска приложения доступны по адресам:

- администрирование: [http://localhost:8889](http://localhost:8889);
- анкеты: [http://localhost:8890](http://localhost:8890);
- кабинет менеджера: [http://localhost:8891](http://localhost:8891).

## Тестовые пользователи

Миграции добавляют демонстрационных сотрудников и менеджеров с паролем `1111`.
Администратор `admin` с паролем `admin` создаётся автоматически при запуске приложения,
если такой пользователь ещё отсутствует.

| Приложение     | Логин       | Пароль  | Роль |
|----------------|-------------|---------| --- |
| Administration | `admin`     | `admin` | `ADMIN` |
| Manager        | `manager1`  | `1111`  | `MANAGER` |
| Manager        | `manager2`  | `1111`  | `MANAGER` |
| Profile        | `backend1`  | `1111`  | `USER` |
| Profile         | `backend2`  | `1111`  | `USER`, `TEAM_LEAD` |
| Profile         | `analyst1`  | `1111`  | `USER`|
| Profile         | `analyst2`  | `1111`  | `USER`, `TEAM_LEAD`|
| Profile         | `devops1`   | `1111`  | `USER`|
| Profile         | `devops2`   | `1111`  | `USER`, `TEAM_LEAD`|
| Profile         | `frontend1` | `1111`  | `USER`|
| Profile         | `frontend2` | `1111`  | `USER`, `TEAM_LEAD`|
| Profile         | `autoqa1`   | `1111`  | `USER`|
| Profile         | `autoqa2`   | `1111`  | `USER`, `TEAM_LEAD`|
| Profile         | `qa1`       | `1111`  | `USER`|
| Profile         | `qa2`       | `1111`  | `USER`, `TEAM_LEAD`|

Учётные записи предназначены только для локальной разработки.

## Сборка проекта

Собрать все исполняемые jar без запуска тестов:

```bash
./gradlew \
  :administration:bootJar \
  :profile:bootJar \
  :integration:bootJar \
  :manager:bootJar \
  -x test
```

Запустить все проверки:

```bash
./gradlew check
```

## Docker

Для каждого запускаемого модуля предусмотрен отдельный multi-stage Dockerfile.
На первом этапе приложение собирается в образе с Gradle и JDK 17, а итоговый
образ содержит только Java Runtime и исполняемый jar.

### Запуск через Docker Compose

Из корня проекта соберите образы и запустите PostgreSQL и все три приложения:

```bash
docker compose up --build -d
```

При первом запуске сборка образов может занять несколько минут. Проверить
состояние контейнеров и посмотреть логи приложений можно командами:

```bash
docker compose ps
docker compose logs -f
```

После запуска будут доступны:

- Кабинет администратора: [http://localhost:8889](http://localhost:8889);
- Профиль сотрудника: [http://localhost:8890](http://localhost:8890);
- Кабинет менеджера: [http://localhost:8891](http://localhost:8891);
- PostgreSQL: `localhost:5433`.
- Kafka: `localhost:9092`;
- тестовые письма Mailpit: [http://localhost:8025](http://localhost:8025).

Чтобы запустить только PostgreSQL или отдельное приложение вместе с его
зависимостями, укажите имя сервиса:

```bash
docker compose up -d postgres
docker compose up -d profile
```

### Ручная сборка образов

Все команды сборки необходимо выполнять из корня проекта, поскольку приложения
зависят от общего модуля `core`:

```bash
docker build -f profile/Dockerfile -t assistant-profile .
docker build -f administration/Dockerfile -t assistant-administration .
docker build -f integration/Dockerfile -t assistant-integration .
docker build -f manager/Dockerfile -t assistant-manager .
```
## Конфигурация

Настройки каждого приложения находятся в его `src/main/resources/application.yaml`.
Spring Boot позволяет переопределять их переменными окружения:

| Переменная | Назначение |
| --- | --- |
| `SERVER_PORT` | HTTP-порт приложения |
| `SPRING_DATASOURCE_URL` | JDBC URL PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Пользователь PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | Пароль PostgreSQL |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Адрес Kafka, например `localhost:9092` |
| `SPRING_MAIL_HOST` | SMTP-сервер для модуля `integration` |
| `SPRING_MAIL_PORT` | SMTP-порт для модуля `integration` |
| `APP_JWT_SECRET` | Секрет подписи JWT, не менее 32 символов |
| `APP_JWT_EXPIRATION_TIMES` | Время жизни JWT, например `30m` |
| `APP_DEFAULT_PASSWORD` | Начальный пароль для создаваемых пользователей |
