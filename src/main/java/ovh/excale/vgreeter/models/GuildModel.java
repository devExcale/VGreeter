package ovh.excale.vgreeter.models;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "guild")
public class GuildModel {

	public static final int DEFAULT_JOIN_PROBABILITY = 15;

	@Id
	@Column(name = "id_guild")
	private Long id;

	@Builder.Default
	@Column(name = "join_probability")
	private Integer joinProbability = DEFAULT_JOIN_PROBABILITY;

}

