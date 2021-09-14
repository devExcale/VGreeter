package ovh.excale.vgreeter;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

@Log4j2
@SpringBootApplication
public class VGreeterApplication implements CommandLineRunner, ApplicationContextAware {

	private static Boolean maintenance;
	private static ConfigurableApplicationContext ctx;

	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(VGreeterApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);

	}

	// TODO: DON'T CONNECT TO DB DURING MAINTENANCE

	// keep previous maintenance state
	public static void restart(@Nullable final Runnable then, @Nullable final Boolean maintenance) {

		log.info("Restarting app");

		Thread thread = new Thread(() -> {
			ctx.close();

			VGreeterApplication.maintenance = maintenance != null ? maintenance : VGreeterApplication.maintenance;

			SpringApplication app = new SpringApplication(VGreeterApplication.class);
			app.setBannerMode(Banner.Mode.OFF);
			app.run();

			if(then != null)
				then.run();

		});

		thread.setDaemon(false);
		thread.start();

	}

	public static void restart(@NotNull Runnable then) {
		restart(then, null);
	}

	public static void restart(boolean maintenance) {
		restart(null, maintenance);
	}

	public static boolean isInMaintenance() {
		return maintenance;
	}

	public static ApplicationContext getApplicationContext() {
		return ctx;
	}

	public final String version;

	public VGreeterApplication(@Value("${application.version}") String version) {
		this.version = version;
	}

	@Override
	public void run(String[] args) {
		log.info("Running on version {}", version);
	}

	@Override
	public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
		ctx = (ConfigurableApplicationContext) context;
	}

}
