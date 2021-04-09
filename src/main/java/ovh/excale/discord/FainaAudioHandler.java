package ovh.excale.discord;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FainaAudioHandler implements AudioSendHandler {

	private static final List<OggPacket> packetList = new LinkedList<>();

	static {

		try(InputStream fainaIS = FainaAudioHandler.class.getClassLoader()
				.getResourceAsStream("erfaina-encoded.opus")) {

			OggPacketReader packetReader = new OggPacketReader(fainaIS);

			OggPacket packet;
			while((packet = packetReader.getNextPacket()) != null)
				packetList.add(packet);

		} catch(Exception e) {
			// TODO: LOG
			e.printStackTrace();
		}

	}

	private final Iterator<OggPacket> packetIterator;
	private Runnable onTrackEnd;

	public FainaAudioHandler() {
		packetIterator = packetList.iterator();
		onTrackEnd = null;
	}

	public void onTrackEnd(Runnable action) {
		onTrackEnd = action;
	}

	@Override
	public boolean canProvide() {
		return packetIterator.hasNext();
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
