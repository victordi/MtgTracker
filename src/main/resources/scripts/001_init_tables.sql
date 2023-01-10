--liquibase formatted sql
--changeset mtg:1

CREATE TABLE PLAYER (
    NAME VARCHAR(255) PRIMARY KEY
);

CREATE TABLE DECK (
    NAME VARCHAR(255) PRIMARY KEY,
    TIER VARCHAR(255) NOT NULL,
    PLAYER_NAME VARCHAR(255) NOT NULL,
    CONSTRAINT FK_PLAYER_NAME FOREIGN KEY (PLAYER_NAME) REFERENCES PLAYER(NAME)
);

CREATE TABLE SEASON (
    ID SERIAL PRIMARY KEY,
    PLAYER1 VARCHAR(255) NOT NULL,
    POINTS1 INTEGER NOT NULL,
    PLAYER2 VARCHAR(255) NOT NULL,
    POINTS2 INTEGER NOT NULL,
    PLAYER3 VARCHAR(255) NOT NULL,
    POINTS3 INTEGER NOT NULL,
    PLAYER4 VARCHAR(255) NOT NULL,
    POINTS4 INTEGER NOT NULL,
    CONSTRAINT FK_PLAYER1 FOREIGN KEY (PLAYER1) REFERENCES PLAYER(NAME),
    CONSTRAINT FK_PLAYER2 FOREIGN KEY (PLAYER2) REFERENCES PLAYER(NAME),
    CONSTRAINT FK_PLAYER3 FOREIGN KEY (PLAYER3) REFERENCES PLAYER(NAME),
    CONSTRAINT FK_PLAYER4 FOREIGN KEY (PLAYER4) REFERENCES PLAYER(NAME)
);

CREATE TABLE GAME_RESULT (
    ID SERIAL PRIMARY KEY,
    SEASON_ID INTEGER NOT NULL,
    PLAYER_NAME VARCHAR(255) NOT NULL,
    DECK_NAME VARCHAR(255) NOT NULL,
    PLACE INTEGER NOT NULL,
    START_ORDER INTEGER NOT NULL,
    KILLS INTEGER NOT NULL,
    COMMANDER_KILLS INTEGER NOT NULL,
    INFINITE BIT NOT NULL,
    BODY_GUARD INTEGER NOT NULL,
    PENALTY INTEGER NOT NULL,
    POINTS INTEGER NOT NULL,
    CONSTRAINT FK_SEASON_ID FOREIGN KEY (SEASON_ID) REFERENCES SEASON(ID),
    CONSTRAINT FK_PLAYER_NAME FOREIGN KEY (PLAYER_NAME) REFERENCES PLAYER(NAME),
    CONSTRAINT FK_DECK_NAME FOREIGN KEY (DECK_NAME) REFERENCES DECK(NAME)
);

CREATE TABLE POINTS_SYSTEM (
    ID SERIAL PRIMARY KEY,
    SEASON_ID INTEGER NOT NULL,
    FIRST_PLACE INTEGER NOT NULL,
    SECOND_PLACE INTEGER NOT NULL,
    THIRD_PLACE INTEGER NOT NULL,
    FOURTH_PLACE INTEGER NOT NULL,
    KILL INTEGER NOT NULL,
    COMMANDER_KILL INTEGER NOT NULL,
    INFINITE INTEGER NOT NULL,
    BODY_GUARD INTEGER NOT NULL,
    CONSTRAINT FK_SEASON_ID FOREIGN KEY (SEASON_ID) REFERENCES SEASON(ID)
);