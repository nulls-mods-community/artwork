package nullsteam.mods.artwork.csv;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;

import nullsteam.mods.artwork.IOUtilsBase;
import nullsteam.mods.artwork.Mutator;

public class TableMutator implements Mutator<DataTable, JSONObject> {
	private static final TableMutator INSTANCE = new TableMutator();

	private TableMutator() {
	}

	@Override
	public DataTable load(InputStream is) throws IOException {
		try {
			byte[] bytes = IOUtilsBase.readAllBytes(is);
			return DataTable.load(bytes);
		} catch (DataFormatException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void save(DataTable instance, OutputStream os) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
		instance.save(writer);
		writer.flush();
	}

	@Override
	public void merge(JSONObject base, JSONObject extension) {
		TablePatching.merge(base, extension);
	}

	@Override
	public void apply(DataTable instance, JSONObject patch) {
		JSONObject resolvePatch = TablePatching.resolveWildcards(patch, instance);
		TablePatching.applyPatch(instance, resolvePatch);
	}

	public static TableMutator getInstance() {
		return INSTANCE;
	}
}
