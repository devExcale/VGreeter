package ovh.excale.vgreeter.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

public class UploadCommand extends AbstractCommand {

	public UploadCommand() {
		super("upload", "Show help to upload a track");
	}

	@Override
	public ReplyAction execute(SlashCommandEvent event) {

		//noinspection StringBufferReplaceableByString
		StringBuilder sb = new StringBuilder();

		sb.append("To upload a new track, you first have to set an altname with `/altname`.")
				.append('\n')
				.append("Then send me (DM) a file with the following requirements:")
				.append('\n')
				.append(" • The filename can only contain alphanumeric characters, dashes `-` and underscores `_`, ")
				.append("followed by `.opus`. (ex. `faina-catcall.opus`)")
				.append('\n')
				.append(" • The track must be opus-encoded (48KHz, 20ms packets)")
				.append('\n')
				.append(" • As now, the file size must be under 40KB and you can only upload 3 tracks")
				.append('\n')
				.append("In the future there might be loyalty programs to increase these limitations.");

		return event.reply(sb.toString());

	}

}
