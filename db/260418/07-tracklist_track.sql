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
