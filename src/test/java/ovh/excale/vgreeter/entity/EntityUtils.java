package ovh.excale.vgreeter.entity;

public class EntityUtils {

	public static final long DEFAULT_TRACK_MAX_SIZE = 64 * 1024L;

	public static final byte[] EMPTY_OPUS = new byte[0];

	public static MemberEntity validMember(Long discordId, String discordUsername) {
		return MemberEntity.builder()
			.discordId(discordId)
			.discordUsername(discordUsername)
			.trackMaxSize(DEFAULT_TRACK_MAX_SIZE)
			.build();
	}

	public static GuildEntity validGuild(Long discordId, String name, float greetProbab) {
		return GuildEntity.builder()
			.discordId(discordId)
			.name(name)
			.greetProbab(greetProbab)
			.build();
	}

	public static TrackEntity validTrack(MemberEntity owner, String title, byte[] opusBytes) {
		return TrackEntity.builder()
			.owner(owner)
			.title(title)
			.opusBytes(opusBytes)
			.build();
	}

	public static TracklistEntity validTracklist(Long memberId, Long guildId, String name) {
		return TracklistEntity.builder()
			.memberId(memberId)
			.guildId(guildId)
			.name(name)
			.build();
	}

}
