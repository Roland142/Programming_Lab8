-- Пользователи
CREATE TABLE IF NOT EXISTS users (
    login    VARCHAR(100) PRIMARY KEY,
    password VARCHAR(64)  NOT NULL  -- SHA-224 hex = 56 символов
);

-- Sequence для генерации id объектов коллекции
CREATE SEQUENCE IF NOT EXISTS human_being_id_seq START 1;

-- Коллекция
CREATE TABLE IF NOT EXISTS human_beings (
    id                   BIGINT       PRIMARY KEY DEFAULT nextval('human_being_id_seq'),
    map_key              BIGINT       NOT NULL UNIQUE,
    name                 VARCHAR      NOT NULL,
    coord_x              DOUBLE PRECISION NOT NULL,
    coord_y              INTEGER      NOT NULL,
    creation_date        DATE         NOT NULL,
    real_hero            BOOLEAN      NOT NULL,
    has_toothpick        BOOLEAN,
    impact_speed         DOUBLE PRECISION NOT NULL,
    soundtrack_name      VARCHAR      NOT NULL,
    minutes_of_waiting   INTEGER,
    mood                 VARCHAR,
    car_name             VARCHAR,
    owner_login          VARCHAR(100) NOT NULL REFERENCES users(login)
);
