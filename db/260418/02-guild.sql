create TABLE guild (

	discord_id   BIGINT PRIMARY KEY,
	name         VARCHAR(255) NOT NULL,
	greet_probab FLOAT        NOT NULL DEFAULT 0.15
		CHECK ( greet_probab BETWEEN 0 AND 1 )

);
