package ovh.excale.discord.audio;

import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Track {

	private final String name;
	private final List<OggPacket> packets;

	protected Track(String name, InputStream trackStream) throws IOException {
		this.name = name;
		packets = new LinkedList<>();

		OggPacketReader packetReader = new OggPacketReader(trackStream);

		OggPacket packet;
		while((packet = packetReader.getNextPacket()) != null)
			packets.add(packet);

	}

	public String getName() {
		return name;
	}

	public Iterator<OggPacket> getPacketIterator() {
		return packets.iterator();
	}

}
