package ovh.excale.vgreeter;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.event.EventListener;

@RequiredArgsConstructor
@Log4j2
@SpringBootApplication
public class VGreeterApplication {

	@SuppressWarnings("UnnecessaryModifier")
	public static void main(String[] args) {
		SpringApplication.run(VGreeterApplication.class, args);
	}

	public final BuildProperties buildProperties;

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		log.info("Running on version {}", buildProperties.getVersion());
	}

}
