package ovh.excale.vgreeter.utilities;

import com.fasterxml.uuid.Generators;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.util.UUID;

/**
 * A custom Hibernate identifier generator that produces UUIDv7 values.
 * This generator uses the com.fasterxml.uuid library to create time-based UUIDs.
 */
public class UUIDv7Generator implements IdentifierGenerator {

	/**
	 * Generates a UUIDv7 using the Generators utility from the com.fasterxml.uuid library.
	 *
	 * @param session the Hibernate session, not used in this implementation
	 * @param object the entity for which the ID is being generated, not used in this implementation
	 * @return a new UUIDv7
	 */
	@Override
	public UUID generate(SharedSessionContractImplementor session, Object object) {
		return Generators.timeBasedEpochGenerator()
			.generate();
	}

}