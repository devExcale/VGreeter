package ovh.excale.vgreeter.entity;

import jakarta.persistence.*;
import lombok.*;

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

	@Column(columnDefinition = "TEXT")
	private String message;

	@Column(columnDefinition = "TEXT")
	@ToString.Exclude
	private String cause;

	@Column(columnDefinition = "TEXT")
	@ToString.Exclude
	private String stackTrace;

	@Column(insertable = false, updatable = false)
	private Timestamp createdAt;

	@Basic
	private Long userId;

	@Basic
	private Long guildId;

}
