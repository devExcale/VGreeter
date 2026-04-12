package ovh.excale.vgreeter.models;

import lombok.*;

import jakarta.persistence.*;
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

	@Builder.Default
	@Basic
	@Column(name = "tracks_max")
	private Integer trackMaxSize = 64 * 1024;

	@ToString.Exclude
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "uploader")
	private Set<TrackModel> tracks;


}
