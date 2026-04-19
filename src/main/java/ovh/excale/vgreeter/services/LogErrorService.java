package ovh.excale.vgreeter.services;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import ovh.excale.vgreeter.entity.LogErrorEntity;
import ovh.excale.vgreeter.repository.LogErrorRepository;

import java.io.PrintWriter;
import java.io.StringWriter;

@RequiredArgsConstructor
@Service
public class LogErrorService {

	private final LogErrorRepository logErrorRepo;

	public LogErrorEntity error(
		@Nullable String message,
		@NotNull Throwable throwable,
		@Nullable Long userId,
		@Nullable Long guildId
	) {

		LogErrorEntity logError = LogErrorEntity.builder()
			.level("error")
			.message(message != null ? message : throwable.getMessage())
			.cause(throwable.getMessage())
			.stackTrace(serializeStackTrace(throwable))
			.userId(userId)
			.guildId(guildId)
			.build();

		return logErrorRepo.save(logError);
	}

	public LogErrorEntity error(
		@Nullable String message,
		@NotNull Throwable throwable
	) {
		return error(message, throwable, null, null);
	}

	public LogErrorEntity error(
		@NotNull Throwable throwable
	) {
		return error(null, throwable, null, null);
	}

	public LogErrorEntity error(
		@NotNull Throwable throwable,
		@Nullable Long userId,
		@Nullable Long guildId
	) {
		return error(null, throwable, userId, guildId);
	}

	public LogErrorEntity warn(
		@Nullable String message,
		@NotNull Throwable throwable,
		@Nullable Long userId,
		@Nullable Long guildId
	) {

		LogErrorEntity logError = LogErrorEntity.builder()
			.level("warn")
			.message(message != null ? message : throwable.getMessage())
			.cause(throwable.getMessage())
			.stackTrace(serializeStackTrace(throwable))
			.userId(userId)
			.guildId(guildId)
			.build();

		return logErrorRepo.save(logError);
	}

	public LogErrorEntity warn(
		@Nullable String message,
		@NotNull Throwable throwable
	) {
		return warn(message, throwable, null, null);
	}

	public LogErrorEntity warn(
		@NotNull Throwable throwable
	) {
		return warn(null, throwable, null, null);
	}

	public LogErrorEntity warn(
		@NotNull Throwable throwable,
		@Nullable Long userId,
		@Nullable Long guildId
	) {
		return warn(null, throwable, userId, guildId);
	}

	public static String serializeStackTrace(Throwable throwable) {

		if(throwable == null)
			return null;

		StringWriter writer = new StringWriter();
		try(PrintWriter printWriter = new PrintWriter(writer)) {
			throwable.printStackTrace(printWriter);
		}

		return writer.toString();
	}

}


