package ovh.excale.fainabot.models;

import org.gagravarr.ogg.OggPacketReader;

import javax.persistence.*;
import java.io.ByteArrayInputStream;

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
	private byte[] data;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "uploader_id")
	private UserModel uploader;

	public TrackModel() {

	}

	public Long getId() {
		return id;
	}

	public TrackModel setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public TrackModel setName(String name) {
		this.name = name;
		return this;
	}

	public Long getSize() {
		return size;
	}

	public TrackModel setSize(Long size) {
		this.size = size;
		return this;
	}

	public byte[] getData() {
		return data;
	}

	public TrackModel setData(byte[] data) {
		this.data = data;
		return this;
	}

	public UserModel getUploader() {
		return uploader;
	}

	public TrackModel setUploader(UserModel uploader) {
		this.uploader = uploader;
		return this;
	}

	public OggPacketReader getPacketReader() {
		return new OggPacketReader(new ByteArrayInputStream(data));
	}

}
