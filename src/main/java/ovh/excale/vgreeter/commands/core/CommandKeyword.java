package ovh.excale.vgreeter.commands.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.StringKeySerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public enum CommandKeyword {

	TRACK_NAME("tn", "trackname"),
	USER_ID("u", "user");

	public static final String COMMAND = "cmd";
	public static final String SUBCOMMAND = "scmd";
	public static final String PAGE = "p";

	private static final Map<String, String> encodeRecord;
	private static final Map<String, String> decodeRecord;

	static {

		encodeRecord = new HashMap<>();
		decodeRecord = new HashMap<>();

		for(CommandKeyword keyword : values()) {
			encodeRecord.put(keyword.ext, keyword.key);
			decodeRecord.put(keyword.key, keyword.ext);
		}
	}

	public final String key;
	public final String ext;

	CommandKeyword(String key, String ext) {
		this.key = key;
		this.ext = ext;
	}

	public static class KeywordDecoder extends KeyDeserializer {

		@Override
		public Object deserializeKey(String key, DeserializationContext deserializationContext) {
			return decodeRecord.getOrDefault(key, key);
		}

	}

	public static class KeywordEncoder extends StringKeySerializer {

		@Override
		public void serialize(Object ext, JsonGenerator g, SerializerProvider provider) throws IOException {
			g.writeFieldName(encodeRecord.getOrDefault(ext.toString(), ext.toString()));
		}

	}
}
