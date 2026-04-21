package ovh.excale.vgreeter.track;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrackPlayer implements AudioSendHandler {

	private final Iterator<OggPacket> packetIterator;

	private final Runnable trackEndAction;

	public TrackPlayer(
		@NotNull OggPacketReader trackPacketReader,
		@Nullable Runnable trackEndAction
	) throws IOException {

		// Read all packets from the OggPacketReader and store them in a list for iteration
		List<OggPacket> packetList = new LinkedList<>();
		OggPacket packet;
		while((packet = trackPacketReader.getNextPacket()) != null)
			packetList.add(packet);

		this.trackEndAction = trackEndAction;
		this.packetIterator = packetList.iterator();

	}

	@Override
	public boolean isOpus() {
		return true;
	}

	@Override
	public boolean canProvide() {
		return packetIterator.hasNext();
	}

	@Override
	public @Nullable ByteBuffer provide20MsAudio() {

		// If there are no more packets, return null to indicate the end of the track
		if(!packetIterator.hasNext())
			return null;

		// Retrieve new packet
		OggPacket packet = packetIterator.next();
		ByteBuffer buffer = ByteBuffer.wrap(packet.getData());

		// Schedule the track end action to be executed after the last packet is sent
		if(!packetIterator.hasNext())
			try(ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor()) {
				executor.schedule(trackEndAction, 40, TimeUnit.MILLISECONDS);
			}

		return buffer;
	}

}
