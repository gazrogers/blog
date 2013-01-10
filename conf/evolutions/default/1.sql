# --- !Ups

CREATE TABLE articles (
	id serial,
	title varchar(255),
	"date" date,
	article text
)

# --- !Downs

DROP TABLE IF EXISTS articles;