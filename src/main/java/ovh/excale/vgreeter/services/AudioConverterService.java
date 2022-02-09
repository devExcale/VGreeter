package ovh.excale.vgreeter.services;

import lombok.extern.log4j.Log4j2;
import org.bytedeco.ffmpeg.ffmpeg;
import org.bytedeco.javacpp.Loader;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import static java.lang.ProcessBuilder.Redirect.PIPE;

@Log4j2
@Service
public class AudioConverterService {

	private static final String FFMPEG_PATH = Loader.load(ffmpeg.class);
	private static final List<String> command = new LinkedList<>();

	static {
		command.add(FFMPEG_PATH);
		command.add("-i");
		command.add("-");
		command.add("-c:a");
		command.add("libopus");
		command.add("-b:a");
		command.add("48k");
		command.add("-vbr");
		command.add("on");
		command.add("-application");
		command.add("voip");
		command.add("-frame_duration");
		command.add("20");
		command.add("-compression_level");
		command.add("10");
		command.add("-f");
		command.add("opus");
		command.add("-");
	}

	private final ProcessBuilder pb;

	public AudioConverterService() {

		pb = new ProcessBuilder(command);
		pb.redirectErrorStream(false)
				.redirectInput(PIPE)
				.redirectOutput(PIPE);

	}

	/**
	 * Converts any media that has an audio stream to an opus-encoded audio file.
	 * <br><br>
	 * Some notes:
	 * <ul>
	 *     <li>The track will be converted using FFMPEG;</li>
	 *     <li>The thread executing this method will be blocked until FFMPEG process ends;</li>
	 *     <li>In case of a generic error, the method will return an empty array;</li>
	 *     <li>The method will automatically close the InputStream audioTrack.</li>
	 * </ul>
	 *
	 * @param audioTrack An {@link InputStream} possibly reading from an audio stream
	 * @return A byte array containing the data of the encoded audio stream
	 * @throws IllegalArgumentException if audioTrack isn't a valid Stream or
	 *                                  if its content doesn't contain a convertible audio stream
	 */
	public byte[] toOpus(@NotNull InputStream audioTrack) throws IllegalArgumentException {

		Process p;
		try {

			p = pb.start();

		} catch(IOException e) {
			return new byte[0];
		}

		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();

		// Transfer from audio Stream to FFMPEG in
		new Thread(() -> {
			try(OutputStream pOut = p.getOutputStream()) {

				pipe(audioTrack, pOut, 4096);
				audioTrack.close();

			} catch(IOException e) {
				log.warn("Error while piping audio stream to FFMPEG", e);
			}
			try {

				audioTrack.close();

			} catch(IOException ignored) {
			}
		}).start();

		// Transfer from FFMPEG out to ByteArray
		try {

			pipe(p.getInputStream(), byteOutStream, 4096);

		} catch(IOException ignored) {
			// Transferring from FFMPEG to in-memory ByteArray, there shouldn't be any IOEx.
			// Even in the case, I'm checking the process exit code.
		}

		int exitCode;
		try {

			exitCode = p.waitFor();

		} catch(InterruptedException e) {
			return new byte[0];
		}

		if(exitCode != 0)
			throw new IllegalArgumentException("The provided file doesn't have a valid audio stream");

		return byteOutStream.toByteArray();

	}

	private static void pipe(InputStream in, OutputStream out,
			@SuppressWarnings("SameParameterValue") int bufferSize) throws IOException {

		byte[] buffer = new byte[bufferSize];
		int read;
		while((read = in.read(buffer)) != -1)
			out.write(buffer, 0, read);

	}

}
