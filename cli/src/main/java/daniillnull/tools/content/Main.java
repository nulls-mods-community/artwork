package daniillnull.tools.content;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

import nullsteam.mods.artwork.csv.DataTable;
import nullsteam.mods.artwork.csv.TablePatching;

public class Main {
	public static int FILE_SIZE_LIMIT = 5_000_000;

	public static void main(String[] args) throws IOException, DataFormatException {
		if (args.length < 3) {
			System.err.println("Usage: <content json> <output directory> <input directories...>");
			System.exit(1);
		}

		JSONObject contentJson;
		try (FileReader reader = new FileReader(args[0], StandardCharsets.UTF_8)) {
			contentJson = new JSONObject(new JSONTokener(reader));
		}

		String outputPath = args[1];
		List<File> inputPaths = new ArrayList<>();
		for (int i = 2; i < args.length; i++) {
			inputPaths.add(0, new File(args[i]));
		}

		for (String key : contentJson.keySet()) {
			if (!key.startsWith("@")) {
				System.out.println("Processing: " + key);

				LookupFile lookup = lookup(inputPaths, key + ".csv");
				File inputFile = lookup.getFile();
				File outputFile = new File(outputPath, lookup.file);

				File outputParent = outputFile.getParentFile();
				if (outputParent != null && !outputParent.isDirectory() && !outputParent.mkdirs()) {
					throw new IOException("could not create directory: " + outputParent);
				}

				DataTable table;
				try (InputStream is = new FileInputStream(inputFile)) {
					table = DataTable.load(is.readAllBytes());
				}

				JSONObject patch = TablePatching.resolveWildcards(contentJson.getJSONObject(key), table);
				TablePatching.applyPatch(table, patch);

				try (FileOutputStream stream = new FileOutputStream(outputFile)) {
					FiniteFileWriter writer = new FiniteFileWriter(stream, FILE_SIZE_LIMIT);
					table.save(writer);
				}

				if (isColorfulTerm()) {
					System.out.println("\033[FSucessfully patched: " + key);
				} else {
					System.out.println("Successfully patched: " + key);
				}
			}
		}
	}

	private static LookupFile lookup(List<File> args, String base) throws IOException {
		for (File arg : args) {
			Optional<Path> result = lookup(arg, base);
			if (result.isPresent()) {
				Path relative = arg.toPath().relativize(result.get());
				return new LookupFile(arg, relative.toString());
			}
		}
		throw new IOException("could not find " + base + " in paths specified");
	}

	private static Optional<Path> lookup(File arg, String base) throws IOException {
		try (Stream<Path> walkStream = Files.walk(arg.toPath())) {
			return walkStream.filter((path) -> {
				return (Files.isRegularFile(path) && path.getFileName().toString().equals(base));
			}).findAny();
		}
	}

	private static boolean isColorfulTerm() {
		String term = System.getenv("TERM");
		return (term != null && term.contains("xterm"));
	}

	private static class LookupFile {
		File directory;
		String file;

		public LookupFile(File directory, String file) {
			this.directory = directory;
			this.file = file;
		}

		public File getFile() {
			return new File(directory, file);
		}
	}
}
