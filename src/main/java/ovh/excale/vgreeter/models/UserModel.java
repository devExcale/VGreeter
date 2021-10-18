package ovh.excale.vgreeter.models;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "\"user\"")
public class UserModel {

	@Id
	private Long snowflake;

	@Basic
	private String altname;

	@Basic
	@Column(name = "tracks_max")
	private Integer trackMaxSize = 64 * 1024;

	@ToString.Exclude
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "uploader")
	private Set<TrackModel> tracks;

	@SuppressWarnings("unused")
	public static class UserModelBuilder {

		@SuppressWarnings({ "FieldMayBeFinal", "unused" })
		private Integer trackMaxSize = 64 * 1024;

	}

}
