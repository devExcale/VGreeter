package ovh.excale.vgreeter.models;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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

	@Column(name = "join_probability")
	private Integer joinProbability = DEFAULT_JOIN_PROBABILITY;

	@SuppressWarnings("unused")
	public static class GuildModelBuilder {

		@SuppressWarnings({ "FieldMayBeFinal", "unused" })
		private Integer joinProbability = DEFAULT_JOIN_PROBABILITY;

	}

}
