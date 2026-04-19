package ovh.excale.vgreeter.services;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import ovh.excale.vgreeter.entity.LogErrorEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(
	properties = {
		"spring.datasource.url=jdbc:h2:mem:vgreeter;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=member",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.sql.init.mode=always",
		"spring.sql.init.schema-locations=classpath:h2/schema.sql"
	}
)
@EntityScan(basePackageClasses = LogErrorEntity.class)
@Import(LogErrorService.class)
class LogErrorServiceH2Test {

	private static final String ERROR_LEVEL = "error";
	private static final String WARN_LEVEL = "warn";

	@Autowired
	private LogErrorService logErrorService;

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private EntityManager em;

	@Test
	void givenErrorEntry_whenSave_thenPersistedInDatabase() {

		// Prepare test data
		String message = "Something failed";
		RuntimeException throwable = new RuntimeException("boom");

		// Save error entry
		LogErrorEntity saved = logErrorService.error(message, throwable);
		entityManager.flush();
		entityManager.clear();

		// Verify persisted error
		LogErrorEntity reloaded = em.find(LogErrorEntity.class, saved.getId());
		assertNotNull(reloaded);
		assertEquals(ERROR_LEVEL, reloaded.getLevel());
		assertEquals(message, reloaded.getMessage());
		assertEquals("boom", reloaded.getCause());
		assertTrue(reloaded.getStackTrace().contains("RuntimeException: boom"));
		assertTrue(reloaded.getStackTrace().contains("givenErrorEntry_whenSave_thenPersistedInDatabase"));
		assertNotNull(reloaded.getCreatedAt());

	}

	@Test
	void givenWarnEntry_whenSave_thenPersistedInDatabase() {

		// Prepare test data
		String message = "Something suspicious";

		// Save warning entry
		LogErrorEntity saved = logErrorService.warn(message);
		entityManager.flush();
		entityManager.clear();

		// Verify persisted warning
		LogErrorEntity reloaded = em.find(LogErrorEntity.class, saved.getId());
		assertNotNull(reloaded);
		assertEquals(WARN_LEVEL, reloaded.getLevel());
		assertEquals(message, reloaded.getMessage());
		assertNull(reloaded.getCause());
		assertNull(reloaded.getStackTrace());
		assertNotNull(reloaded.getCreatedAt());

	}

}


