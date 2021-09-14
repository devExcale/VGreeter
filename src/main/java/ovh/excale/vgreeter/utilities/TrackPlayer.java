package ovh.excale.vgreeter.utilities;

import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.jetbrains.annotations.Nullable;
import ovh.excale.vgreeter.models.TrackModel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
public class TrackPlayer implements AudioSendHandler {

	private final TrackModel track;
	private final Iterator<OggPacket> packetIterator;
	private Runnable trackEndAction;

	public TrackPlayer(TrackModel track) {
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
				log.error(e.getMessage(), e);
			}

		}

		packetIterator = packetList.iterator();
	}

	public TrackModel getTrack() {
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

		if(packetIterator.hasNext()) {

			buffer = ByteBuffer.wrap(packetIterator.next()
					.getData());

			if(!packetIterator.hasNext())
				Executors.newSingleThreadScheduledExecutor()
						.schedule(trackEndAction, 40, TimeUnit.MILLISECONDS);
		}

		return buffer;
	}

	@Override
	public boolean isOpus() {
		return true;
	}

}
