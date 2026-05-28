# EventHub - NoSQL Database Project

[![EventHub](https://github.com/ElegantCone/itmo-nosql-esaraeva/actions/workflows/eventhub.yml/badge.svg)](https://github.com/ElegantCone/itmo-nosql-esaraeva/actions/workflows/eventhub.yml)

EventHub - backend-сервис платформы мероприятий, реализованный в рамках курса по NoSQL-базам данных. Приложение хранит пользователей и мероприятия, поддерживает авторизацию через cookie-сессии, реакции, отзывы, агрегированные кэши и рекомендации мероприятий на основе графа лайков.

## Стек

- Java 17
- Spring Boot 4
- Maven
- MongoDB - пользователи и мероприятия
- Redis - сессии, агрегированные реакции/отзывы, кэш рекомендаций
- Cassandra - реакции и отзывы
- Neo4j - граф пользователей, мероприятий и лайков для рекомендаций
- Docker Compose - локальная инфраструктура

## Архитектура БД внутри приложения

MongoDB хранит основные документы:

- `users` - профиль пользователя, содержащий следующие поля: 
  - `id`, 
  - `full_name`, 
  - `username`, 
  - `password_hash`, 
  - `created_at`
- `events` - данные мероприятия, содержащие следующие поля: 
  - `id`, 
  - `title`, 
  - `category`, 
  - `price`, 
  - `description`, 
  - `location` (вложенный документ с полями `city` и `address`), 
  - `created_at`, 
  - `created_by` (id пользователя-организатора), 
  - `started_at`, 
  - `finished_at`

Cassandra хранит отзывы и реакции на мероприятия:

- `event_reactions` - лайки и дизлайки мероприятий:
  - `event_id`,
  - `created_by`,
  - `like_value`,
  - `created_at`;
- `event_reviews` - отзывы и рейтинги:
  - `id`
  - `event_id`,
  - `rating`,
  - `comment`,
  - `created_by`,
  - `created_at`,
  - `updated_at`;

Redis используется в качестве кэша для:

- cookie-сессий пользователей
- агрегатов реакций по названию мероприятия
- агрегатов отзывов по названию мероприятия
- рекомендаций пользователя по ключу `user:{user_id}:recomms`

Neo4j хранит только рекомендательный граф:

```text
(u:User {id})-[:LIKED]->(e:Event {id, title})
```

Полные данные рекомендованных мероприятий для HTTP-ответа всегда берутся из MongoDB.

## Запуск

Требования:

- Docker и Docker Compose;
- JDK 17;
- Maven.

Запустить сервисы можно через Makefile:

```sh
make run
```

Это самый простой способ, который запустит все сервисы.

После запуска приложение доступно по адресу:

```text
http://localhost:8080
```

## Конфигурация

Основные параметры находятся в `.env.local`.

| Переменная                                                                                                 | Назначение                          |
|------------------------------------------------------------------------------------------------------------|-------------------------------------|
| `APP_PORT`                                                                                                 | HTTP-порт приложения                |
| `APP_HOST`                                                                                                 | host binding приложения             |
| `APP_USER_SESSION_TTL`                                                                                     | TTL пользовательской сессии в Redis |
| `APP_LIKE_TTL`                                                                                             | TTL кэша агрегированных реакций     |
| `APP_EVENT_REVIEWS_TTL`                                                                                    | TTL кэша агрегированных отзывов     |
| `APP_RECOMMENDATIONS_TTL`                                                                                  | TTL кэша рекомендаций               |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, `REDIS_DB`                                                   | подключение к Redis                 |
| `MONGODB_HOST`, `MONGODB_PORT`, `MONGODB_DATABASE`, `MONGODB_USER`, `MONGODB_PASSWORD`                     | подключение к MongoDB               |
| `CASSANDRA_HOSTS`, `CASSANDRA_PORT`, `CASSANDRA_KEYSPACE`, `CASSANDRA_CONSISTENCY`, `CASSANDRA_DATACENTER` | подключение к Cassandra             |
| `NEO4J_URL`, `NEO4J_USERNAME`, `NEO4J_PASSWORD`, `NEO4J_BOLT_PORT`                                         | подключение к Neo4j                 |

## API

Все ответы с ошибками возвращают JSON вида:

```json
{
  "message": "error message"
}
```

Авторизация работает через cookie:

```http
Cookie: X-Session-Id=<session_id>
```

### Служебные endpoint'ы

| Метод  | Путь       | Назначение                              |
|--------|------------|-----------------------------------------|
| `GET`  | `/health`  | Проверка доступности приложения         |
| `POST` | `/session` | Создание или обновление гостевой сессии |

### Пользователи и авторизация

| Метод  | Путь                 | Назначение                   |
|--------|----------------------|------------------------------|
| `POST` | `/users`             | Регистрация пользователя     |
| `GET`  | `/users`             | Поиск пользователей          |
| `GET`  | `/users/{id}`        | Получение пользователя по id |
| `GET`  | `/users/{id}/events` | Мероприятия организатора     |
| `POST` | `/auth/login`        | Вход пользователя            |
| `POST` | `/auth/logout`       | Выход пользователя           |

Пример регистрации:

```sh
curl -i -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"full_name":"Sam Sepiol","username":"sam","password":"password"}'
```

Пример входа:

```sh
curl -i -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"sam","password":"password"}'
```

### Мероприятия

| Метод   | Путь                                   | Назначение                                                                |
|---------|----------------------------------------|---------------------------------------------------------------------------|
| `POST`  | `/events`                              | Создание мероприятия, требуется авторизация                               |
| `PATCH` | `/events/{id}`                         | Обновление категории, цены или города, требуется авторизация организатора |
| `GET`   | `/events`                              | Поиск мероприятий                                                         |
| `GET`   | `/events/{id}`                         | Получение мероприятия по id                                               |
| `POST`  | `/events/{id}/like`                    | Лайк мероприятия, требуется авторизация                                   |
| `POST`  | `/events/{id}/dislike`                 | Дизлайк мероприятия, требуется авторизация                                |
| `POST`  | `/events/{id}/reviews`                 | Создание отзыва, требуется авторизация                                    |
| `GET`   | `/events/{id}/reviews`                 | Список отзывов мероприятия                                                |
| `PATCH` | `/events/{eventId}/reviews/{reviewId}` | Обновление собственного отзыва                                            |

Поиск мероприятий поддерживает параметры:

- `id`
- `title`
- `category`
- `city`
- `price_from`
- `price_to`
- `date_from`
- `date_to`
- `user`
- `limit`
- `offset`
- `include`

`include` может содержать `reactions`, `reviews` или оба значения через запятую.

Пример создания мероприятия:

```sh
curl -i -X POST http://localhost:8080/events \
  -H 'Content-Type: application/json' \
  -H 'Cookie: X-Session-Id=<session_id>' \
  -d '{
    "title": "Ночь в опере",
    "address": "Москва, ул. Большая Дмитровка, д. 6",
    "started_at": "2026-04-05T19:00:00Z",
    "finished_at": "2026-04-05T21:00:00Z",
    "description": "Гала-концерт лучших солистов Большого театра"
  }'
```

### Рекомендации

| Метод | Путь               | Назначение                                        |
|-------|--------------------|---------------------------------------------------|
| `GET` | `/recommendations` | Рекомендованные мероприятия текущего пользователя |

Endpoint доступен только авторизованному пользователю и возвращает рекомендации только для владельца текущей сессии.

Пример:

```sh
curl -i http://localhost:8080/recommendations \
  -H 'Cookie: X-Session-Id=<session_id>'
```

Ответ:

```json
{
  "events": [
    {
      "id": "4",
      "title": "Ночь в опере",
      "category": "concert",
      "price": 2500,
      "description": "Гала-концерт лучших солистов Большого театра",
      "location": {
        "city": "Москва",
        "address": "Москва, ул. Большая Дмитровка, д. 6"
      },
      "created_at": "2026-01-02T10:00:00Z",
      "created_by": "65e9c0b1a2b3c4d5e6f7a8b9",
      "started_at": "2026-04-05T19:00:00Z",
      "finished_at": "2026-04-05T21:00:00Z"
    }
  ]
}
```

Если рекомендаций нет:

```json
{
  "events": []
}
```

## Как работают рекомендации

Алгоритм для пользователя A:

1. Найти события, которые лайкнул A.
2. Найти других пользователей, лайкнувших эти же события.
3. Найти события, которые лайкнули эти пользователи.
4. Исключить события, которые A уже лайкал.
5. Отсортировать рекомендации по релевантности: от более популярных к менее популярным.
6. Если несколько рекомендованных событий имеют одинаковое название, оставить одно ближайшее по `started_at`.

Дизлайки не удаляют связь `LIKED` из Neo4j. Если пользователь лайкнул событие, а позже поставил дизлайк, этот факт лайка все равно участвует в рекомендациях.

Рекомендации строятся лениво:

1. `GET /recommendations` сначала проверяет Redis.
2. Если ключа `user:{user_id}:recomms` нет, сервис строит подборку через Neo4j и MongoDB.
3. Результат сохраняется в Redis hash через HSET.
4. На ключ ставится TTL `APP_RECOMMENDATIONS_TTL`.

Redis key:

```text
user:{user_id}:recomms
```

Redis hash field:

```text
events
```

Значение поля `events` - JSON-массив рекомендованных мероприятий.

## Структура проекта

```text
src/main/java/nosql
├── api          # REST-контроллеры и DTO
├── cassandra    # Cassandra repositories and entities
├── config       # Spring configuration
├── model        # request/search models
├── mongo        # MongoDB documents and repositories
├── neo4j        # Neo4j configuration and graph repository
├── redis        # Redis repositories
├── service      # бизнес-логика
└── utils        # validation and helper classes
```

Инфраструктурные файлы:

- `docker-compose.yml` - все сервисы и зависимости;
- `.env.local` - локальная конфигурация;
- `mongo/` - инициализация MongoDB sharded cluster;
- `cassandra/` - инициализация Cassandra keyspace/tables;
- `api/` - Postman collections для лабораторных работ;
- `.labrc` - номер лабораторной работы для CI.

## Полезные ссылки

- [Задания лабораторных работ](https://github.com/sitnikovik/ndbx/tree/main/docs/lab)
- [CONTRIBUTING.md](CONTRIBUTING.md)
- [Документация курса](https://github.com/sitnikovik/ndbx)
