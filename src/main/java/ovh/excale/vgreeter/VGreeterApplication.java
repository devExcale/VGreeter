package ovh.excale.vgreeter;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@SpringBootApplication
public class VGreeterApplication implements CommandLineRunner, ApplicationContextAware {

	private static ApplicationContext ctx;

	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(VGreeterApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);

	}

	public static ApplicationContext getApplicationContext() {
		return ctx;
	}

	public final Logger logger;
	public final String version;

	public VGreeterApplication(@Value("${application.version}") String version) {
		this.version = version;
		logger = LoggerFactory.getLogger(VGreeterApplication.class);
	}

	@Override
	public void run(String[] args) {
		logger.info("Running on version {}", version);
	}

	@Override
	public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
		ctx = context;
	}

}
