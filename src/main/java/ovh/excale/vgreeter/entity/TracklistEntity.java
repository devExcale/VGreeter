package ovh.excale.vgreeter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@ToString
@Entity(name = "Tracklist")
@Table(name = "tracklist")
public class TracklistEntity {

	@Id
	@GenerateUUIDv7
	private UUID id;

	@Basic
	private String name;

	@Basic
	private Long memberId;

	@Basic
	private Long guildId;

	@ToString.Exclude
	@Builder.Default
	@ManyToMany
	@JoinTable(
		name = "tracklist_track",
		joinColumns = @JoinColumn(name = "tracklist_id"),
		inverseJoinColumns = @JoinColumn(name = "track_id")
	)
	private Set<TrackEntity> tracks = new HashSet<>();

}
