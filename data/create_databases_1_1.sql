DROP DATABASE IF EXISTS fusrodah_db;
CREATE DATABASE IF NOT EXISTS fusrodah_db;

USE fusrodah_db;

CREATE TABLE shouts1
(
	id 			bigint 			NOT NULL PRIMARY KEY AUTO_INCREMENT,
	templateID 	bigint 			NOT NULL,
	created 	varchar(255) 	NOT NULL,
	shouterIDs	TEXT			NOT NULL,
	location 	varchar(255) 	NOT NULL
);

CREATE TABLE templates1
(
	id 			bigint 			NOT NULL PRIMARY KEY AUTO_INCREMENT,
	message 	TEXT 			NOT NULL,
	startLocation varchar(255) 	NOT NULL,
	endLocation varchar(255),
	senderID 	bigint 			NOT NULL,
	receiverID 	bigint,
	lastShoutTime varchar(255) 	NOT NULL,
	completed 	varchar(10) 	NOT NULL
);

CREATE TABLE users1
(
	id 			bigint 			NOT NULL PRIMARY KEY AUTO_INCREMENT,
	location 	varchar(255),
	points 		bigint,
	userName 	varchar(255) 	NOT NULL,
	lastShoutTime varchar(255) 	NOT NULL
);

CREATE TABLE secure1
(
	id 			bigint 			NOT NULL PRIMARY KEY,
	passwordHash varchar(255) 	NOT NULL
);

CREATE TABLE victories1
(
	id 			bigint 			NOT NULL PRIMARY KEY AUTO_INCREMENT,
	receivedPoints varchar(255) NOT NULL,
	receiverIDs TEXT 			NOT NULL,
	created 	varchar(255) 	NOT NULL,
	templateID 	bigint 			NOT NULL
);


DROP DATABASE IF EXISTS fusrodah_management_db;
CREATE DATABASE IF NOT EXISTS fusrodah_management_db;

USE fusrodah_management_db;

CREATE TABLE loginKeys1
(
	userID 		bigint 			NOT NULL PRIMARY KEY,
	userKey 	varchar(64) 	NOT NULL
);

CREATE TABLE tableamounts
(
	tableName 	varchar(32) 	NOT NULL PRIMARY KEY,
	latestIndex int 			NOT NULL
);