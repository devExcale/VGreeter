-- 1. No extensions needed in H2
-- CREATE EXTENSION IF NOT EXISTS lo; 

CREATE TABLE guild (
	discord_id   BIGINT PRIMARY KEY,
	name         VARCHAR(255) NOT NULL,
	greet_probab FLOAT        NOT NULL DEFAULT 0.15
		CHECK (greet_probab BETWEEN 0 AND 1)
);

CREATE TABLE member (
	discord_id       BIGINT PRIMARY KEY,
	discord_username VARCHAR(255) NOT NULL,
	track_max_size   BIGINT       NOT NULL DEFAULT 65536
);


CREATE SEQUENCE track_id_seq START WITH 1 INCREMENT BY 1;


CREATE TABLE track (
	id         BIGINT                DEFAULT NEXT VALUE FOR track_id_seq PRIMARY KEY, -- H2's version of BIGSERIAL
	title      VARCHAR(255) NOT NULL,
	owner_id   BIGINT       NOT NULL,
	created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

	-- 2. Use BLOB instead of OID
	opus_oid   BLOB         NOT NULL,

	-- 3. Replace the PL/pgSQL trigger with a simple computed column
	opus_size  BIGINT GENERATED ALWAYS AS (OCTET_LENGTH(opus_oid)),

	FOREIGN KEY (owner_id) REFERENCES member (discord_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

-- 4. ALL TRIGGERS AND FUNCTIONS ARE OMITTED! 

CREATE TABLE guild_track (
	guild_id BIGINT NOT NULL,
	track_id BIGINT NOT NULL,
	-- owner_id BIGINT NOT NULL, TODO

	PRIMARY KEY (guild_id, track_id),
	FOREIGN KEY (guild_id) REFERENCES guild (discord_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	FOREIGN KEY (track_id) REFERENCES track (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE tracklist (
	id        UUID PRIMARY KEY,
	name      VARCHAR(255) NOT NULL,
	member_id BIGINT       NOT NULL,
	guild_id  BIGINT       NOT NULL,

	FOREIGN KEY (member_id) REFERENCES member (discord_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	FOREIGN KEY (guild_id) REFERENCES guild (discord_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE tracklist_track (
	tracklist_id UUID   NOT NULL,
	track_id     BIGINT NOT NULL,

	PRIMARY KEY (tracklist_id, track_id),
	FOREIGN KEY (tracklist_id) REFERENCES tracklist (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	FOREIGN KEY (track_id) REFERENCES track (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE log_error (
	id          UUID PRIMARY KEY,
	level       VARCHAR(16) NOT NULL,
	message     CLOB,
	cause       CLOB,
	stack_trace CLOB,
	created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	user_id     BIGINT,
	guild_id    BIGINT
);
