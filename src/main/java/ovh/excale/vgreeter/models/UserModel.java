package ovh.excale.vgreeter.models;

import javax.persistence.*;

@Entity
@Table(name = "\"user\"")
public class UserModel {

	@Id
	private Long snowflake;

	@Basic
	private String altname;

	@Basic
	@Column(name = "tracks_max")
	private Integer trackMaxSize;

	public UserModel() {
		trackMaxSize = 64 * 1024;
	}

	public UserModel(long snowflake) {
		this.snowflake = snowflake;
		trackMaxSize = 64 * 1024;
	}

	public Long getSnowflake() {
		return snowflake;
	}

	public UserModel setSnowflake(Long snowflake) {
		this.snowflake = snowflake;
		return this;
	}

	public String getAltname() {
		return altname;
	}

	public UserModel setAltname(String altname) {
		this.altname = altname;
		return this;
	}

	public Integer getTrackMaxSize() {
		return trackMaxSize;
	}

	public UserModel setTrackMaxSize(Integer maxTracks) {
		this.trackMaxSize = maxTracks;
		return this;
	}

}
