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
