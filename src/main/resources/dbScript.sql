CREATE DATABASE dep8_todo;

CREATE TABLE user(
                     username VARCHAR(10) PRIMARY KEY,
                     password VARCHAR(100) NOT NULL,
                     gmail VARCHAR(25) NOT NULL
);

CREATE TABLE item(
                     name VARCHAR(10) PRIMARY KEY,
                     description VARCHAR(100) NOT NULL ,
                     status ENUM ('DONE', 'TODO') NOT NULL

);