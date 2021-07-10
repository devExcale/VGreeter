package ovh.excale.fainabot.utilities;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.excale.fainabot.models.Track;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

public class TrackPlayer implements AudioSendHandler {

	private final static Logger logger = LoggerFactory.getLogger(TrackPlayer.class);

	private final Track track;
	private final Iterator<OggPacket> packetIterator;
	private Runnable trackEndAction;

	public TrackPlayer(Track track) {
		this.track = track;
		this.trackEndAction = () -> { };
		List<OggPacket> packetList = new LinkedList<>();

		if(track != null) {
			OggPacketReader packetReader = track.getPacketReader();

			try {
				OggPacket packet;
				while((packet = packetReader.getNextPacket()) != null)
					packetList.add(packet);
			} catch(IOException e) {
				logger.error(e.getMessage(), e);
			}

		}

		packetIterator = packetList.iterator();
	}

	public Track getTrack() {
		return track;
	}

	public void setTrackEndAction(Runnable trackEndAction) {
		this.trackEndAction = trackEndAction;
	}

	@Override
	public boolean canProvide() {
		return packetIterator.hasNext();
	}

	@Override
	public @Nullable ByteBuffer provide20MsAudio() {

		ByteBuffer buffer = null;

		if(packetIterator.hasNext())
			buffer = ByteBuffer.wrap(packetIterator.next()
					.getData());
		else
			Executors.newSingleThreadExecutor()
					.execute(trackEndAction);

		return buffer;
	}

	@Override
	public boolean isOpus() {
		return true;
	}

}
