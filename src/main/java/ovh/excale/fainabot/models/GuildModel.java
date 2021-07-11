package ovh.excale.fainabot.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "guild")
public class GuildModel {

	@Id
	@Column(name = "id_guild")
	private Long id;

	@Column(name = "join_probability")
	private Integer joinProbability;

	public GuildModel() {
	}

	public GuildModel(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public GuildModel setId(Long id) {
		this.id = id;
		return this;
	}

	public Integer getJoinProbability() {
		return joinProbability;
	}

	public GuildModel setJoinProbability(Integer joinProbability) {
		this.joinProbability = joinProbability;
		return this;
	}

}
