package ovh.excale.fainabot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Er Faina <i>(ma versione bot)</i> che non fa catcalling quando una donna entra in vocale.
 * <br/>
 * <a href="https://discord.com/api/oauth2/authorize?client_id=829982567217233920&permissions=2150632448&scope=bot%20applications.commands">Invitalo ora!</a>
 */
@SpringBootApplication
public class FainaBotApplication implements CommandLineRunner {

	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(FainaBotApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);

	}

	public final Logger logger;
	public final String version;

	public FainaBotApplication(@Value("${application.version}") String version) {
		this.version = version;
		logger = LoggerFactory.getLogger(FainaBotApplication.class);
	}

	@Override
	public void run(String[] args) {
		logger.info("Running on version {}", version);
	}

}
