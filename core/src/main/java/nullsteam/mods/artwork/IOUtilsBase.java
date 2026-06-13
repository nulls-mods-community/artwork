package nullsteam.mods.artwork;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class IOUtilsBase {
	private static final int BUFFER_SIZE = 4096;

	protected IOUtilsBase() {
	}

	public static void streamCopy(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		int read;
		while ((read = is.read(buffer)) > 0) {
			os.write(buffer, 0, read);
		}
	}

	public static byte[] readAllBytes(InputStream is) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(is.available());
		streamCopy(is, os);
		return os.toByteArray();
	}

	public static String readAllBytesAsString(InputStream is) throws IOException {
		return new String(readAllBytes(is), StandardCharsets.UTF_8);
	}
}
