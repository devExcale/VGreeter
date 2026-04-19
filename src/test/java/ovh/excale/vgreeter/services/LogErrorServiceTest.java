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
		assertThat(logError.getLevel()).isEqualTo("error");
		assertThat(logError.getMessage()).isEqualTo(message);
		assertThat(logError.getCause()).isEqualTo("boom");
		assertThat(logError.getStackTrace()).contains("RuntimeException: boom");
		assertThat(logError.getStackTrace()).contains("givenMessageAndThrowable_whenError_thenSavedAsErrorLevel");

	}

	@Test
	void givenMessageOnly_whenError_thenCauseAndStackTraceAreNull() {

		// Prepare test data
		String message = "Something failed";

		// Save error entry
		LogErrorService service = new LogErrorService(logErrorRepo);
		when(logErrorRepo.save(any(LogErrorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		LogErrorEntity logError = service.error(message);

		// Verify saved error
		verify(logErrorRepo).save(logErrorCaptor.capture());
		assertThat(logError).isSameAs(logErrorCaptor.getValue());
		assertThat(logError.getLevel()).isEqualTo("error");
		assertThat(logError.getMessage()).isEqualTo(message);
		assertThat(logError.getCause()).isNull();
		assertThat(logError.getStackTrace()).isNull();

	}

	@Test
	void givenThrowableOnly_whenError_thenMessageAndCauseComeFromThrowable() {

		// Prepare test data
		IllegalStateException throwable = new IllegalStateException("bad state");

		// Save error entry
		LogErrorService service = new LogErrorService(logErrorRepo);
		when(logErrorRepo.save(any(LogErrorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		LogErrorEntity logError = service.error(throwable);

		// Verify saved error
		verify(logErrorRepo).save(logErrorCaptor.capture());
		assertThat(logError).isSameAs(logErrorCaptor.getValue());
		assertThat(logError.getLevel()).isEqualTo("error");
		assertThat(logError.getMessage()).isEqualTo("bad state");
		assertThat(logError.getCause()).isEqualTo("bad state");
		assertThat(logError.getStackTrace()).contains("IllegalStateException: bad state");

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
		assertThat(logError.getLevel()).isEqualTo("warn");
		assertThat(logError.getMessage()).isEqualTo(message);
		assertThat(logError.getCause()).isEqualTo("warned");
		assertThat(logError.getStackTrace()).contains("RuntimeException: warned");

	}

	@Test
	void givenMessageOnly_whenWarn_thenCauseAndStackTraceAreNull() {

		// Prepare test data
		String message = "Something suspicious";

		// Save warning entry
		LogErrorService service = new LogErrorService(logErrorRepo);
		when(logErrorRepo.save(any(LogErrorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		LogErrorEntity logError = service.warn(message);

		// Verify saved warning
		verify(logErrorRepo).save(logErrorCaptor.capture());
		assertThat(logError).isSameAs(logErrorCaptor.getValue());
		assertThat(logError.getLevel()).isEqualTo("warn");
		assertThat(logError.getMessage()).isEqualTo(message);
		assertThat(logError.getCause()).isNull();
		assertThat(logError.getStackTrace()).isNull();

	}

	@Test
	void givenThrowableOnly_whenWarn_thenMessageAndCauseComeFromThrowable() {

		// Prepare test data
		IllegalArgumentException throwable = new IllegalArgumentException("warn state");

		// Save warning entry
		LogErrorService service = new LogErrorService(logErrorRepo);
		when(logErrorRepo.save(any(LogErrorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		LogErrorEntity logError = service.warn(throwable);

		// Verify saved warning
		verify(logErrorRepo).save(logErrorCaptor.capture());
		assertThat(logError).isSameAs(logErrorCaptor.getValue());
		assertThat(logError.getLevel()).isEqualTo("warn");
		assertThat(logError.getMessage()).isEqualTo("warn state");
		assertThat(logError.getCause()).isEqualTo("warn state");
		assertThat(logError.getStackTrace()).contains("IllegalArgumentException: warn state");

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




