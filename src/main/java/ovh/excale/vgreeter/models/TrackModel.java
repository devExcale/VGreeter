package ovh.excale.vgreeter.models;

import lombok.*;
import org.gagravarr.ogg.OggPacketReader;

import javax.persistence.*;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "track")
public class TrackModel {

	@Id
	@Column(name = "track_id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TRACK_ID_SEQ_GEN")
	@SequenceGenerator(name = "TRACK_ID_SEQ_GEN", sequenceName = "track_track_id_seq", allocationSize = 1)
	private Long id;

	@Basic
	private String name;

	@Basic
	private Long size;

	@Basic
	private Timestamp uploadDate = Timestamp.from(Instant.now());

	@ToString.Exclude
	@Basic(fetch = FetchType.LAZY)
	private byte[] data;

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "uploader_id")
	private UserModel uploader;

	@Column(name = "uploader_id", insertable = false, updatable = false)
	private Long uploaderId;

	public OggPacketReader getPacketReader() {
		return new OggPacketReader(new ByteArrayInputStream(getData()));
	}

	@SuppressWarnings("unused")
	public static class TrackModelBuilder {

		@SuppressWarnings({ "FieldMayBeFinal", "unused" })
		private Timestamp uploadDate = Timestamp.from(Instant.now());

	}

}
