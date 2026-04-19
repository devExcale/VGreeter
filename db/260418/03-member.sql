CREATE TABLE member (

	discord_id       BIGINT PRIMARY KEY,
	discord_username VARCHAR(255) NOT NULL,
	track_max_size   BIGINT       NOT NULL DEFAULT 65536

);
