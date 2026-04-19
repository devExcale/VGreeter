package ovh.excale.vgreeter.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.excale.vgreeter.entity.LogErrorEntity;
import ovh.excale.vgreeter.repository.LogErrorRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogErrorServiceTest {

	@Mock
	private LogErrorRepository logErrorRepo;

	@Captor
	private ArgumentCaptor<LogErrorEntity> logErrorCaptor;

	private static void assertSavedLogError(
		LogErrorEntity logError,
		String level,
		String message,
		String cause,
		Long userId,
		Long guildId,
		String stackTracePart
	) {

		assertThat(logError.getLevel()).isEqualTo(level);
		assertThat(logError.getMessage()).isEqualTo(message);
		assertThat(logError.getCause()).isEqualTo(cause);
		assertThat(logError.getUserId()).isEqualTo(userId);
		assertThat(logError.getGuildId()).isEqualTo(guildId);

		if(stackTracePart == null)
			assertThat(logError.getStackTrace()).isNull();
		else
			assertThat(logError.getStackTrace()).contains(stackTracePart);

	}

	@Test
	void givenNullMessageAndThrowableAndIds_whenError_thenMessageFallsBackAndIdsAreSaved() {

		// Prepare test data
		RuntimeException throwable = new RuntimeException("boom");
		Long userId = 6001L;
		Long guildId = 7001L;

		// Save error entry
		LogErrorService service = new LogErrorService(logErrorRepo);
		when(logErrorRepo.save(any(LogErrorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		LogErrorEntity logError = service.error(null, throwable, userId, guildId);

		// Verify saved error
		verify(logErrorRepo).save(logErrorCaptor.capture());
		assertThat(logError).isSameAs(logErrorCaptor.getValue());
		assertSavedLogError(
			logError,
			"error",
			"boom",
			"boom",
			userId,
			guildId,
			"RuntimeException: boom"
		);

	}

	@Test
	void givenMessageAndThrowable_whenError_thenSavedAsErrorLevel() {

		// Prepare test data
		String message = "Something failed";
		RuntimeException throwable = new RuntimeException("boom");

		// Save error entry
		LogErrorService service = new LogErrorService(logErrorRepo);
		when(logErrorRepo.save(any(LogErrorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		LogErrorEntity logError = service.error(message, throwable);

		// Verify saved error
		verify(logErrorRepo).save(logErrorCaptor.capture());
		assertThat(logError).isSameAs(logErrorCaptor.getValue());
		assertSavedLogError(
			logError,
			"error",
			message,
			"boom",
			null,
			null,
			"RuntimeException: boom"
		);

	}

	@Test
	void givenThrowableAndIds_whenError_thenMessageFallsBackAndIdsAreSaved() {

		// Prepare test data
		IllegalStateException throwable = new IllegalStateException("bad state");
		Long userId = 6002L;
		Long guildId = 7002L;

		// Save error entry
		LogErrorService service = new LogErrorService(logErrorRepo);
		when(logErrorRepo.save(any(LogErrorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		LogErrorEntity logError = service.error(throwable, userId, guildId);

		// Verify saved error
		verify(logErrorRepo).save(logErrorCaptor.capture());
		assertThat(logError).isSameAs(logErrorCaptor.getValue());
		assertSavedLogError(
			logError,
			"error",
			"bad state",
			"bad state",
			userId,
			guildId,
			"IllegalStateException: bad state"
		);

	}

	@Test
	void givenThrowableOnly_whenError_thenMessageFallsBackAndIdsAreNull() {

		// Prepare test data
		IllegalStateException throwable = new IllegalStateException("bad state");

		// Save error entry
		LogErrorService service = new LogErrorService(logErrorRepo);
		when(logErrorRepo.save(any(LogErrorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		LogErrorEntity logError = service.error(throwable);

		// Verify saved error
		verify(logErrorRepo).save(logErrorCaptor.capture());
		assertThat(logError).isSameAs(logErrorCaptor.getValue());
		assertSavedLogError(
			logError,
			"error",
			"bad state",
			"bad state",
			null,
			null,
			"IllegalStateException: bad state"
		);

	}

	@Test
	void givenMessageAndThrowableAndIds_whenWarn_thenSavedAsWarnLevel() {

		// Prepare test data
		String message = "Something suspicious";
		RuntimeException throwable = new RuntimeException("warned");
		Long userId = 6003L;
		Long guildId = 7003L;

		// Save warning entry
		LogErrorService service = new LogErrorService(logErrorRepo);
		when(logErrorRepo.save(any(LogErrorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		LogErrorEntity logError = service.warn(message, throwable, userId, guildId);

		// Verify saved warning
		verify(logErrorRepo).save(logErrorCaptor.capture());
		assertThat(logError).isSameAs(logErrorCaptor.getValue());
		assertSavedLogError(
			logError,
			"warn",
			message,
			"warned",
			userId,
			guildId,
			"RuntimeException: warned"
		);

	}

	@Test
	void givenMessageAndThrowable_whenWarn_thenSavedAsWarnLevel() {

		// Prepare test data
		String message = "Something suspicious";
		RuntimeException throwable = new RuntimeException("warned");

		// Save warning entry
		LogErrorService service = new LogErrorService(logErrorRepo);
		when(logErrorRepo.save(any(LogErrorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		LogErrorEntity logError = service.warn(message, throwable);

		// Verify saved warning
		verify(logErrorRepo).save(logErrorCaptor.capture());
		assertThat(logError).isSameAs(logErrorCaptor.getValue());
		assertSavedLogError(
			logError,
			"warn",
			message,
			"warned",
			null,
			null,
			"RuntimeException: warned"
		);

	}

	@Test
	void givenThrowableAndIds_whenWarn_thenMessageFallsBackAndIdsAreSaved() {

		// Prepare test data
		IllegalArgumentException throwable = new IllegalArgumentException("warn state");
		Long userId = 6004L;
		Long guildId = 7004L;

		// Save warning entry
		LogErrorService service = new LogErrorService(logErrorRepo);
		when(logErrorRepo.save(any(LogErrorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		LogErrorEntity logError = service.warn(throwable, userId, guildId);

		// Verify saved warning
		verify(logErrorRepo).save(logErrorCaptor.capture());
		assertThat(logError).isSameAs(logErrorCaptor.getValue());
		assertSavedLogError(
			logError,
			"warn",
			"warn state",
			"warn state",
			userId,
			guildId,
			"IllegalArgumentException: warn state"
		);

	}

	@Test
	void givenThrowableOnly_whenWarn_thenMessageFallsBackAndIdsAreNull() {

		// Prepare test data
		IllegalArgumentException throwable = new IllegalArgumentException("warn state");

		// Save warning entry
		LogErrorService service = new LogErrorService(logErrorRepo);
		when(logErrorRepo.save(any(LogErrorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		LogErrorEntity logError = service.warn(throwable);

		// Verify saved warning
		verify(logErrorRepo).save(logErrorCaptor.capture());
		assertThat(logError).isSameAs(logErrorCaptor.getValue());
		assertSavedLogError(
			logError,
			"warn",
			"warn state",
			"warn state",
			null,
			null,
			"IllegalArgumentException: warn state"
		);

	}

	@Test
	void givenNullThrowable_whenSerializeStackTrace_thenNull() {

		// Verify null input
		assertThat(LogErrorService.serializeStackTrace(null)).isNull();
	}

	@Test
	void givenThrowable_whenSerializeStackTrace_thenContainsThrowableDetails() {

		// Verify throwable serialization
		String stackTrace = LogErrorService.serializeStackTrace(new RuntimeException("boom"));

		assertThat(stackTrace)
			.contains("RuntimeException: boom")
			.contains("at ");

	}

}




