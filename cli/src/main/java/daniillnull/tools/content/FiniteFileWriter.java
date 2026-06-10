package daniillnull.tools.content;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class FiniteFileWriter extends OutputStreamWriter {
	private final FileChannel channel;
	private final long limitSize;

	public FiniteFileWriter(FileOutputStream stream, long limitSize) {
		super(stream, StandardCharsets.UTF_8);
		this.limitSize = limitSize;
		channel = stream.getChannel();
	}

	@Override
	public void flush() throws IOException {
		if (channel.position() >= limitSize) {
			channel.truncate(0);
			super.close();
			throw new IOException("output file size limit exceeded");
		}

		super.flush();
	}
}
