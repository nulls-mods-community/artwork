package nullsteam.mods.artwork.csv;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import nullsteam.mods.artwork.lzma.LZMA;

public class DataTable {
	public final List<String> columns;
	public final List<String> types;
	public final List<List<Object>> data;
	public final Map<String, Integer> names;

	public DataTable(List<String> columns, List<String> types) {
		this.columns = columns;
		this.types = types;
		if (columns.size() != types.size()) {
			throw new IllegalArgumentException("names and types must have the same length");
		}
		data = new ArrayList<>();
		names = new HashMap<>();
	}

	public Object cast(int column, Object value) {
		if (value != null && value != JSONObject.NULL) {
			if (value instanceof String string && string.isEmpty()) {
				return null;
			}
			switch (types.get(column).toLowerCase()) {
				case "int":
				case "intarray":
					return Integer.parseInt(value.toString());
				case "boolean":
				case "booleanarray":
					return Boolean.parseBoolean(value.toString());
				default:
					return value.toString();
			}
		}
		return null;
	}

	public boolean hasNames() {
		switch (columns.get(0).toLowerCase()) {
			case "name":
			case "tid":
			case "tr":
			case "map":
				return true;
			default:
				return false;
		}
	}

	public List<Object> insertDataRow(int index) {
		List<Object> objects = new ArrayList<>(columns.size());
		for (int i = 0; i < columns.size(); i++) {
			objects.add(null);
		}
		for (String key : names.keySet()) {
			int tmp = names.get(key);
			if (tmp >= index) {
				names.put(key, (tmp + 1));
			}
		}
		data.add(index, objects);
		return objects;
	}

	public int countRowsOfName(String name) {
		Integer start = names.get(name);
		if (start != null) {
			int i = start + 1;
			while (i < data.size() && data.get(i).get(0) == null) {
				i++;
			}
			return (i - start);
		}
		return 0;
	}

	@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
	public void save(Writer writer) throws IOException {
		writer.append(TableParsing.printRow(columns) + "\n");
		writer.append(TableParsing.printRow(types) + "\n");
		for (List<Object> row : data) {
			writer.append(TableParsing.printRow(row) + "\n");
			writer.flush();
		}
	}

	public static DataTable load(byte[] data) throws DataFormatException {
		if (data.length >= 68 && data[0] == 'S' && data[1] == 'i' && data[2] == 'g' && data[3] == ':') {
			data = Arrays.copyOfRange(data, 68, data.length);
		}
		if (data.length > 0 && data[0] >= 0x5A && data[0] <= 0x5F) {
			data = LZMA.decompress(data);
		}
		String table = new String(data, StandardCharsets.UTF_8);
		String[] rows = table.split("\r?\n");
		if (rows.length < 2) {
			throw new DataFormatException("headers has to be present");
		}
		List<String> columns = TableParsing.parseRow(rows[0]);
		List<String> types = TableParsing.parseRow(rows[1]);
		DataTable result = new DataTable(columns, types);
		int columnCount = columns.size();
		if (types.size() != columnCount) {
			throw new IllegalArgumentException("names and types must have the same length");
		}
		for (int i = 2; i < rows.length; i++) {
			List<String> rowString = TableParsing.parseRow(rows[i]);
			List<Object> row = new ArrayList<>(columnCount);
			if (rowString.size() != columnCount) {
				// Some original .csv files can be actually broken (example: locales.csv of CR:13b4ec79b082f69a3c0fd1035d4a97d386a2435e)
				String text = String.format(Locale.US, "row (%s) has different length (%d) than the header (%d)", (i - 2), rowString.size(), columnCount);
				Logger.getLogger("DataTable.load()").warning(text);
			}
			for (int j = 0; j < columnCount; j++) {
				if (j < rowString.size()) {
					Object value = rowString.get(j);
					row.add(result.cast(j, value));
				} else {
					row.add(null);
				}
			}
			String name = rowString.get(0);
			if (result.hasNames() && !name.isEmpty()) {
				result.names.put(name, result.data.size());
			}
			result.data.add(row);
		}
		return result;
	}
}
