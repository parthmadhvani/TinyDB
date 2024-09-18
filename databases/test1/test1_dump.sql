CREATE DATABASE test1;
USE test1;

CREATE TABLE student (
    id int pk,
    name varchar 
);

INSERT INTO student (id, name) VALUES (1, 's');
INSERT INTO student (id, name) VALUES (2, 'Mirza');
INSERT INTO student (id, name) VALUES (3, 'Mirza');
INSERT INTO student (id, name) VALUES (4, 'u');


CREATE TABLE professor (
    id int pk,
    name varchar 
);

INSERT INTO professor (id, name) VALUES (1, 'Dr. Smiley');
INSERT INTO professor (id, name) VALUES (2, 'Dr.Dickey');
INSERT INTO professor (id, name) VALUES (3, 'Mr. H');


