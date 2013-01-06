# --- !Ups

CREATE TABLE articles (
	id int auto_increment,
	title varchar(255),
	`date` date,
	article text
)

# --- !Downs

DROP TABLE IF EXISTS articles;