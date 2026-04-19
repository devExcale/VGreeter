package ovh.excale.vgreeter.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@Entity(name = "LogError")
@Table(name = "log_error")
public class LogErrorEntity {

	@Id
	@GenerateUUIDv7
	private UUID id;

	@Basic
	private String level;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String message;

	@Lob
	@Column(columnDefinition = "TEXT")
	@ToString.Exclude
	private String cause;

	@Lob
	@Column(columnDefinition = "TEXT")
	@ToString.Exclude
	private String stackTrace;

	@Column(insertable = false, updatable = false)
	private Timestamp createdAt;

}
