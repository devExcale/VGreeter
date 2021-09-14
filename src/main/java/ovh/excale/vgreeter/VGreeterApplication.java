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
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class VGreeterApplication implements CommandLineRunner, ApplicationContextAware {

	@Value("${env.VGREETER_MAINTENANCE:false}")
	private static boolean maintenance;
	private static ConfigurableApplicationContext ctx;

	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(VGreeterApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);

	}

	public static void restart(boolean maintenance) {

		Thread thread = new Thread(() -> {
			ctx.close();
			ctx = SpringApplication.run(VGreeterApplication.class);
			// TODO: MAINTENANCE AS APPLICATION ARGUMENT
			VGreeterApplication.setMaintenance(maintenance);
		});

		thread.setDaemon(false);
		thread.start();
	}

	public static boolean isInMaintenance() {
		return maintenance;
	}

	public static void setMaintenance(boolean maintenanceOn) {
		maintenance = maintenanceOn;
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
		ctx = (ConfigurableApplicationContext) context;
	}

}
