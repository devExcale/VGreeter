package ovh.excale.fainabot.models;

import javax.persistence.*;

@Entity
@Table(name = "user")
public class UserModel {

	@Id
	private Long snowflake;

	@Basic
	@Column(name = "tracks_max")
	private Integer maxTracks;

	public UserModel() {
	}

	public Long getSnowflake() {
		return snowflake;
	}

	public UserModel setSnowflake(Long snowflake) {
		this.snowflake = snowflake;
		return this;
	}

	public Integer getMaxTracks() {
		return maxTracks;
	}

	public UserModel setMaxTracks(Integer maxTracks) {
		this.maxTracks = maxTracks;
		return this;
	}

}
