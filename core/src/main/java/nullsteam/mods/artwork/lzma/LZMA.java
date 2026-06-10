package nullsteam.mods.artwork.lzma;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;

import lzma.sdk.lzma.Decoder;
import lzma.sdk.lzma.Encoder;

public class LZMA {
	public static byte[] decompress(byte[] data) throws DataFormatException {
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Decoder d = new Decoder();
		long len = 0;

		try {
			byte[] props = new byte[5];
			if (is.read(props) != props.length) {
				throw new RuntimeException();
			}
			if (!d.setDecoderProperties(props)) {
				throw new RuntimeException();
			}

			for (int i = 0; i < 4; i++) {
				int v = is.read();
				if (v < 0) {
					throw new DataFormatException("can't read stream size");
				}
				len |= ((long) v) << (8 * i);
			}

			if (!d.code(is, os, len)) {
				throw new DataFormatException("error in data stream");
			}
		} catch (IOException e) {
			throw new DataFormatException(e.getMessage());
		}

		return os.toByteArray();
	}

	public static byte[] compress(byte[] data) {
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Encoder d = new Encoder();

		try {
			d.setAlgorithm(3);
			d.writeCoderProperties(os);
			for (int i = 0; i < 4; i++) {
				os.write(data.length >>> (8 * i) & 0xFF);
			}
			d.code(is, os, -1, -1, null);
		} catch (IOException e) {
			// This should never happen!
			throw new InternalError();
		}

		return os.toByteArray();
	}
}
