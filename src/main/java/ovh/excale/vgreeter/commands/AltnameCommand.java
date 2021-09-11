package ovh.excale.vgreeter.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.models.UserModel;
import ovh.excale.vgreeter.repositories.UserRepository;

import java.util.Optional;
import java.util.regex.Pattern;

public class AltnameCommand extends AbstractCommand {

	private static final Pattern ALTNAME_PATTERN = Pattern.compile("[\\w\\d-_]{3,}");

	private final UserRepository userRepo;

	public AltnameCommand() {
		super("altname", "Manage your altname used for uploading tracks");
		this.getBuilder()
				.addOption("username", "New username", OptionType.STRING);

		userRepo = VGreeterApplication
				.getApplicationContext()
				.getBean(UserRepository.class);

	}

	@Override
	public ReplyAction execute(SlashCommandEvent event) {

		ReplyAction reply;
		User user = event.getUser();

		String name = Optional.ofNullable(event.getOption("username"))
				.map(OptionMapping::getAsString)
				.orElse(null);

		if(name != null) {

			if(ALTNAME_PATTERN.matcher(name)
					.matches()) {

				if(!userRepo.existsByAltname(name)) {

					UserModel userModel = userRepo.findById(user.getIdLong())
							.orElseGet(() -> new UserModel(user.getIdLong()));

					userModel.setAltname(name);
					userRepo.save(userModel);

					reply = event.reply("Altname successfully saved")
							.setEphemeral(true);
				} else
					reply = event.reply("A user with that altname already exists")
							.setEphemeral(true);

			} else
				reply = event.reply(
						"Invalid name format (a minimum of 3 alphanumeric characters, dashes `-` or underscores `_`)")
						.setEphemeral(true);

		} else
			reply = userRepo.findById(user.getIdLong())
					.map(UserModel::getAltname)
					.map(s -> event.reply("Your altname is `" + s + "`"))
					.orElseGet(() -> event.reply("You don't have an altname yet"));

		return reply;
	}

}
