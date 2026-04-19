package ovh.excale.vgreeter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@ToString
@Entity(name = "Guild")
@Table(name = "guild")
public class GuildEntity {

	public static final float DEFAULT_GREET_PROBAB = .15f;

	@Id
	private Long discordId;

	@Basic
	private String name;

	@Basic
	private Float greetProbab;

	@ToString.Exclude
	@Builder.Default
	@ManyToMany
	@JoinTable(
		name = "guild_track",
		joinColumns = @JoinColumn(name = "guild_id"),
		inverseJoinColumns = @JoinColumn(name = "track_id")
	)
	private Set<TrackEntity> sharedTracks = new HashSet<>();

}
