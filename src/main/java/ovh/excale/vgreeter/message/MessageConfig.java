package ovh.excale.vgreeter.message;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:messages.properties")
@EnableConfigurationProperties({
	ErrorMessages.class,
	TrackMessages.class,
	ProbabilityMessages.class
})
public class MessageConfig {

}