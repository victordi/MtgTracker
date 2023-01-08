--liquibase formatted sql
--changeset mtg:1

CREATE TABLE PLAYER (
    NAME VARCHAR(255) PRIMARY KEY
);

CREATE TABLE DECK (
    NAME VARCHAR(255) PRIMARY KEY,
    PLAYER_NAME VARCHAR(255) NOT NULL,
    CONSTRAINT FK_PLAYER_NAME FOREIGN KEY (PLAYER_NAME) REFERENCES PLAYER(NAME)
);

CREATE TABLE SEASON (
    ID SERIAL PRIMARY KEY,
    PLAYERS VARCHAR(4000) NOT NULL
);

CREATE TABLE GAME_RESULT (
    ID SERIAL PRIMARY KEY,
    SEASON_ID INTEGER NOT NULL,
    DECK_NAME VARCHAR(255) NOT NULL,
    PLACE INTEGER NOT NULL,
    START_ORDER INTEGER NOT NULL,
    KILLS INTEGER NOT NULL,
    COMMANDER_KILLS INTEGER NOT NULL,
    INFINITE BIT NOT NULL,
    BODY_GUARD INTEGER NOT NULL,
    PENALTY INTEGER NOT NULL,
    CONSTRAINT FK_SEASON_ID FOREIGN KEY (SEASON_ID) REFERENCES SEASON(ID),
    CONSTRAINT DECK_NAME FOREIGN KEY (DECK_NAME) REFERENCES DECK(NAME)
);