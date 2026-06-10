package nullsteam.mods.artwork.csv;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TablePatching {
	@SuppressWarnings("Java8ListSort")
	public static void applyPatch(DataTable table, JSONObject patch) {
		List<String> keys = new ArrayList<>(patch.keySet());
		Collections.sort(keys, (a, b) -> {
			int aIdx = patch.getJSONObject(a).optInt("@index", 0);
			int bIdx = patch.getJSONObject(b).optInt("@index", 0);
			if (aIdx != bIdx) {
				return Integer.compare(aIdx, bIdx);
			}
			return a.compareTo(b);
		});
		for (String name : keys) {
			JSONObject data = patch.getJSONObject(name);

			int index;
			if (table.names.containsKey(name)) {
				index = table.names.get(name);
			} else {
				if (data.has("@index")) {
					index = (Integer) data.remove("@index");
				} else {
					index = table.data.size();
				}
				List<Object> row = table.insertDataRow(index);
				if (table.hasNames()) {
					row.set(0, name);
					table.names.put(name, index);
				}
			}

			int size = table.countRowsOfName(name);
			for (String column : data.keySet()) {
				JSONArray array = data.optJSONArray(column);
				if (array != null) {
					while (size < array.length()) {
						table.insertDataRow(index + size);
						size += 1;
					}
				}
			}

			for (String columnName : data.keySet()) {
				int column = table.columns.indexOf(columnName);
				if (column < 0) {
					throw new RuntimeException("column not found: " + columnName);
				}
				Object valueJson = data.get(columnName);
				List<Object> objects = new ArrayList<>();
				if (valueJson instanceof JSONArray) {
					JSONArray valueArrayJson = (JSONArray) valueJson;
					for (int i = 0; i < valueArrayJson.length(); i++) {
						objects.add(valueArrayJson.get(i));
					}
				} else {
					objects.add(valueJson);
				}
				for (int i = 0; i < size; i++) {
					List<Object> row = table.data.get(index + i);
					if (i < objects.size()) {
						Object value = objects.get(i);
						row.set(column, table.cast(column, value));
					} else {
						row.set(column, null);
					}
				}
			}
		}
	}

	public static JSONObject resolveWildcards(JSONObject patch, DataTable table) {
		JSONObject result = new JSONObject();
		Set<String> keys = new HashSet<>(patch.keySet());
		// Process wildcards:
		for (String key : new ArrayList<>(keys)) {
			Collection<String> names = getWildcards(key, table);
			if (names != null) {
				JSONObject rows = patch.getJSONObject(key);
				for (String name : names) {
					JSONObject newRows = new JSONObject();
					merge(newRows, rows);
					result.put(name, newRows);
				}
				keys.remove(key);
			}
		}
		for (String key : keys) {
			JSONObject rows = patch.getJSONObject(key);
			if (result.has(key)) {
				merge(result.getJSONObject(key), rows);
			} else {
				result.put(key, rows);
			}
		}
		return result;
	}

	private static Collection<String> getWildcards(String key, DataTable table) {
		if (key.equals("*")) {
			return table.names.keySet();
		}
		if (key.startsWith("{") && key.endsWith("}")) {
			boolean invertValue;
			String expression;
			if (key.startsWith("{!")) {
				invertValue = true;
				expression = key.substring(2, key.length() - 1);
			} else {
				invertValue = false;
				expression = key.substring(1, key.length() - 1);
			}
			int column = table.columns.indexOf(expression);
			if (column < 0) {
				throw new RuntimeException("column not found: " + expression);
			}
			List<String> result = new ArrayList<>();
			for (String name : table.names.keySet()) {
				int index = table.names.get(name);
				Object data = table.data.get(index).get(column);
				boolean value;
				if (data instanceof Boolean bool) {
					value = bool;
				} else if (data instanceof Integer integer) {
					value = (integer != 0);
				} else {
					value = (data != null);
				}
				if (value ^ invertValue) {
					result.add(name);
				}
			}
			return result;
		}
		return null;
	}

	public static void merge(JSONObject base, JSONObject second) {
		merge(base, second, second.keySet());
	}

	public static void merge(JSONObject base, JSONObject second, Iterable<String> keys) {
		for (String key : keys) {
			Object value = second.get(key);
			Object valueBase = base.opt(key);
			if (valueBase instanceof JSONObject && value instanceof JSONObject) {
				merge((JSONObject) valueBase, (JSONObject) value);
			} else {
				base.put(key, value);
			}
		}
	}
}
