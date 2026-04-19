CREATE TABLE guild_track (

	guild_id BIGINT NOT NULL,
	track_id BIGINT NOT NULL,

	PRIMARY KEY (guild_id, track_id),
	FOREIGN KEY (guild_id) REFERENCES guild (discord_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	FOREIGN KEY (track_id) REFERENCES track (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);
