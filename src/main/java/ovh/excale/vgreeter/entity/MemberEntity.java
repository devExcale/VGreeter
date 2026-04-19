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
@Entity(name = "Member")
@Table(name = "member")
public class MemberEntity {

	@Id
	private Long discordId;

	@Basic
	private String discordUsername;

	@Builder.Default
	@Generated
	private Long trackMaxSize = 64 * 1024L;

	@ToString.Exclude
	@Builder.Default
	@OneToMany(mappedBy = "owner")
	private Set<TrackEntity> tracks = new HashSet<>();

	public void addTrack(TrackEntity track) {
		tracks.add(track);
		track.setOwner(this);
	}

	public void removeTrack(TrackEntity track) {
		tracks.remove(track);
		track.setOwner(null);
	}

}
