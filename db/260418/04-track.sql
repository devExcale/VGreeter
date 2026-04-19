CREATE TABLE track (

	id         BIGSERIAL PRIMARY KEY,
	title      VARCHAR(255) NOT NULL,
	owner_id   BIGINT       NOT NULL,
	created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
	opus_oid   OID          NOT NULL,
	opus_size  BIGINT       NOT NULL,

	FOREIGN KEY (owner_id) REFERENCES member (discord_id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION compute_opus_size_track()
	RETURNS TRIGGER AS $$
DECLARE
	lo_fd INTEGER;
BEGIN
	-- Only compute if an OID actually exists
	IF NEW.opus_oid IS NOT NULL THEN
		-- Open the Large Object in read-only mode (262144 is the constant for INV_READ)
		lo_fd := lo_open(NEW.opus_oid, 262144);

		-- Seek exactly to the end of the file (2 is the constant for SEEK_END)
		-- lo_lseek64 returns the byte offset, which equals the total file size
		NEW.opus_size := lo_lseek64(lo_fd, 0, 2);

		-- Close the file pointer to prevent memory leaks
		PERFORM lo_close(lo_fd);

	ELSE
		NEW.opus_size := 0;
	END IF;

	RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER trg_compute_opus_size_track
	BEFORE INSERT OR UPDATE OF opus_oid
	ON track
	FOR EACH ROW
EXECUTE FUNCTION compute_opus_size_track();

CREATE TRIGGER trg_manage_opus_oid_track
	BEFORE UPDATE OR DELETE
	ON track
	FOR EACH ROW
EXECUTE FUNCTION lo_manage(opus_oid);
