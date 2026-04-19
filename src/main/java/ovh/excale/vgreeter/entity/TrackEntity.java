package ovh.excale.vgreeter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.gagravarr.ogg.OggPacketReader;
import org.hibernate.annotations.Generated;
import org.hibernate.engine.jdbc.proxy.BlobProxy;

import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@ToString
@Entity(name = "Track")
@Table(name = "track")
public class TrackEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_id_seq_gen")
	@SequenceGenerator(name = "track_id_seq_gen", sequenceName = "track_id_seq", allocationSize = 1)
	private Long id;

	@Basic
	private String title;

	@Column(name = "owner_id", nullable = false, insertable = false, updatable = false)
	private Long ownerId;

	@Generated
	@Column(insertable = false, updatable = false)
	private Timestamp createdAt;

	@ToString.Exclude
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "opus_oid", nullable = false)
	private Blob opusBytes;

	@Generated
	@Column(insertable = false, updatable = false)
	private Long opusSize;

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	private MemberEntity owner;

	public void setOpusBytes(byte[] opusBytes) {
		this.opusBytes = BlobProxy.generateProxy(opusBytes);
		this.opusSize = (long) opusBytes.length;
	}

	public byte[] getOpusBytes() {
		try {
			return this.opusBytes.getBytes(1, (int) this.opusBytes.length());
		} catch (SQLException e) {
			throw new PersistenceException("Failed to read opus bytes from database", e);
		}
	}

	public OggPacketReader getOpusPacketReader() throws SQLException {
		return new OggPacketReader(opusBytes.getBinaryStream());
	}

	@SuppressWarnings("unused")
	public static class TrackEntityBuilder {

		public TrackEntityBuilder opusBytes(byte[] opusBytes) {
			this.opusBytes = BlobProxy.generateProxy(opusBytes);
			this.opusSize = (long) opusBytes.length;
			return this;
		}
	}

}
