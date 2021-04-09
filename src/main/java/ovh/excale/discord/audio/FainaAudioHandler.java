package ovh.excale.discord.audio;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.gagravarr.ogg.OggPacket;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FainaAudioHandler implements AudioSendHandler {

	private final static Logger logger = LoggerFactory.getLogger(FainaAudioHandler.class);
	private final static Map<String, Track> trackMap = new HashMap<>();
	private final static String[] tracklist;

	static {

		ClassLoader cloader = FainaAudioHandler.class.getClassLoader();

		try(InputStream is = cloader.getResourceAsStream("tracklist.txt")) {

			Objects.requireNonNull(is, "Cannot read tracklist.txt");
			BufferedReader isr = new BufferedReader(new InputStreamReader(is));

			String line;
			while((line = isr.readLine()) != null)
				try {

					InputStream trackStream = cloader.getResourceAsStream("tracks/" + line + ".opus");
					Objects.requireNonNull(trackStream);

					Track track = new Track(line, trackStream);
					trackMap.put(track.getName(), track);

				} catch(IOException | NullPointerException e) {
					logger.error("Cannot read track [" + line + "]", e);
				}

		} catch(IOException e) {
			logger.error("Cannot read tracklist.txt", e);
		} catch(NullPointerException e) {
			logger.error(e.getMessage(), e);
		}

		if(trackMap.isEmpty())
			logger.warn("Empty tracklist!");

		tracklist = trackMap.keySet()
				.toArray(new String[0]);

	}

	public static boolean hasTracks() {
		return !trackMap.isEmpty();
	}

	private final Iterator<OggPacket> packetIterator;
	private Runnable onTrackEnd;

	public FainaAudioHandler() {
		this(null);
	}

	public FainaAudioHandler(String trackName) {
		onTrackEnd = null;

		if(tracklist.length == 0)
			packetIterator = null;
		else {

			if(trackName == null)
				trackName = tracklist[new Random().nextInt(tracklist.length)];

			Track track = trackMap.get(trackName);
			packetIterator = track.getPacketIterator();

		}

	}

	public void onTrackEnd(Runnable action) {
		onTrackEnd = action;
	}

	@Override
	public boolean canProvide() {
		return packetIterator != null && packetIterator.hasNext();
	}

	@Override
	public @Nullable ByteBuffer provide20MsAudio() {

		ByteBuffer buffer = null;

		if(packetIterator.hasNext()) {

			OggPacket packet = packetIterator.next();
			buffer = ByteBuffer.wrap(packet.getData());

			if(!packetIterator.hasNext())
				Executors.newSingleThreadScheduledExecutor()
						.schedule(onTrackEnd, 40, TimeUnit.MILLISECONDS);

		}

		return buffer;
	}

	@Override
	public boolean isOpus() {
		return true;
	}

}
