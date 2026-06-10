package nullsteam.mods.artwork.csv;

import java.util.ArrayList;
import java.util.List;

public class TableParsing {
	public static final char QUOTE = '\"';
	public static final char DELIMITER = ',';

	public static List<String> parseRow(String input) {
		List<String> words = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		QuotationStatus status = QuotationStatus.NONE;
		for (int i = 0; i < input.length(); i++) {
			char code = input.charAt(i);
			if (code == QUOTE) {
				switch (status) {
					case NONE:
						status = QuotationStatus.SET;
						break;
					case SET:
						status = QuotationStatus.ENDING;
						break;
					case ENDING: // Double quote!
						status = QuotationStatus.SET;
						sb.append(QUOTE);
						break;
				}
			} else {
				// Reset quotation:
				if (status == QuotationStatus.ENDING) {
					status = QuotationStatus.NONE;
				}
				if (code == DELIMITER && status == QuotationStatus.NONE) {
					words.add(sb.toString());
					sb.setLength(0);
				} else {
					sb.append(code);
				}
			}
		}
		words.add(sb.toString());
		return words;
	}

	public static String printRow(List<?> words) {
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (Object row : words) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(DELIMITER);
			}

			if (row != null) {
				if ((row instanceof Boolean) || (row instanceof Number)) {
					sb.append(row);
				} else if (row instanceof String) {
					String string = (String) row;
					if (!string.isEmpty()) {
						sb.append(QUOTE);
						sb.append(string.replace("\"", "\"\"")); // Replace any quote to double quote
						sb.append(QUOTE);
					}
				}
			}
		}
		return sb.toString();
	}

	enum QuotationStatus {
		NONE, SET, ENDING;
	}
}
