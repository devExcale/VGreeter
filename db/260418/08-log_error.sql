CREATE TABLE log_error (

	id          UUID PRIMARY KEY,
	level       VARCHAR(12) NOT NULL,
	message     TEXT,
	cause       TEXT,
	stack_trace TEXT,
	created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	user_id     BIGINT,
	guild_id    BIGINT

);
