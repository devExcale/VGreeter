package ovh.excale.vgreeter.track;

import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.jetbrains.annotations.Nullable;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.services.LogErrorService;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
public class TrackPlayer implements AudioSendHandler {

	private final TrackEntity track;
	private final Iterator<OggPacket> packetIterator;
	private Runnable trackEndAction;
	private final LogErrorService logErrorService;

	public TrackPlayer(TrackEntity track) {
		this(track, null);
	}

	public TrackPlayer(TrackEntity track, @Nullable LogErrorService logErrorService) {
		this.track = track;
		this.logErrorService = logErrorService;
		this.trackEndAction = () -> { };
		List<OggPacket> packetList = new LinkedList<>();

		if(track != null)
			try {

				OggPacketReader packetReader = track.getOpusPacketReader();
				OggPacket packet;
				while((packet = packetReader.getNextPacket()) != null)
					packetList.add(packet);

			} catch(Exception e) {
				if(this.logErrorService != null)
					this.logErrorService.error("Failed to read opus packets from track", e);
				log.error(e.getMessage(), e);
			}

		packetIterator = packetList.iterator();
	}

	public TrackEntity getTrack() {
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
