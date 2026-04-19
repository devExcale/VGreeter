package ovh.excale.vgreeter.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ovh.excale.vgreeter.entity.LogErrorEntity;
import ovh.excale.vgreeter.repository.LogErrorRepository;

import java.io.PrintWriter;
import java.io.StringWriter;

@RequiredArgsConstructor
@Service
public class LogErrorService {

	private final LogErrorRepository logErrorRepo;

	public LogErrorEntity error(String message, Throwable throwable) {

		LogErrorEntity logError = LogErrorEntity.builder()
			.level("error")
			.message(message)
			.cause(throwable != null ? throwable.getMessage() : null)
			.stackTrace(serializeStackTrace(throwable))
			.build();

		return logErrorRepo.save(logError);
	}

	public LogErrorEntity error(String message) {
		return error(message, null);
	}

	public LogErrorEntity error(Throwable throwable) {
		return error(throwable.getMessage(), throwable);
	}

	public LogErrorEntity warn(String message, Throwable throwable) {

		LogErrorEntity logError = LogErrorEntity.builder()
			.level("warn")
			.message(message)
			.cause(throwable != null ? throwable.getMessage() : null)
			.stackTrace(serializeStackTrace(throwable))
			.build();

		return logErrorRepo.save(logError);
	}

	public LogErrorEntity warn(String message) {
		return warn(message, null);
	}

	public LogErrorEntity warn(Throwable throwable) {
		return warn(throwable.getMessage(), throwable);
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


