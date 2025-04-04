package it.polimi.ingsw.view.cli;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ANSI {
	public static final String ANSI_RESET = "Ϟ";
	public static final String ANSI_BLACK = "Ϡ";
	public static final String ANSI_RED = "ϡ";
	public static final String ANSI_GREEN = "Ϣ";
	public static final String ANSI_YELLOW = "ϣ";
	public static final String ANSI_BLUE = "Ϥ";
	public static final String ANSI_PURPLE = "ϥ";
	public static final String ANSI_CYAN = "Ϧ";
	public static final String ANSI_WHITE = "ϧ";

	public static final String ANSI_BLACK_BACKGROUND = "Ϩ";
	public static final String ANSI_RED_BACKGROUND = "ϩ";
	public static final String ANSI_GREEN_BACKGROUND = "Ϫ";
	public static final String ANSI_YELLOW_BACKGROUND = "ϫ";
	public static final String ANSI_BLUE_BACKGROUND = "Ϭ";
	public static final String ANSI_PURPLE_BACKGROUND = "ϭ";
	public static final String ANSI_CYAN_BACKGROUND = "Ϯ";
	public static final String ANSI_WHITE_BACKGROUND = "ϯ";

	private static final Map<String, String> PLACEHOLDER_MAP = new HashMap<>();
	private static final Set<String> FOREGROUND_SET = new HashSet<>();
	private static final Set<String> BACKGROUND_SET = new HashSet<>();

	static {
		PLACEHOLDER_MAP.put(ANSI_RESET, "\u001B[0m");
		PLACEHOLDER_MAP.put(ANSI_BLACK, "\u001B[30m");
		PLACEHOLDER_MAP.put(ANSI_RED, "\u001B[31m");
		PLACEHOLDER_MAP.put(ANSI_GREEN, "\u001B[32m");
		PLACEHOLDER_MAP.put(ANSI_YELLOW, "\u001B[33m");
		PLACEHOLDER_MAP.put(ANSI_BLUE, "\u001B[34m");
		PLACEHOLDER_MAP.put(ANSI_PURPLE, "\u001B[35m");
		PLACEHOLDER_MAP.put(ANSI_CYAN, "\u001B[36m");
		PLACEHOLDER_MAP.put(ANSI_WHITE, "\u001B[97m");

		PLACEHOLDER_MAP.put(ANSI_BLACK_BACKGROUND, "\u001B[40m");
		PLACEHOLDER_MAP.put(ANSI_RED_BACKGROUND, "\u001B[41m");
		PLACEHOLDER_MAP.put(ANSI_GREEN_BACKGROUND, "\u001B[42m");
		PLACEHOLDER_MAP.put(ANSI_YELLOW_BACKGROUND, "\u001B[43m");
		PLACEHOLDER_MAP.put(ANSI_BLUE_BACKGROUND, "\u001B[44m");
		PLACEHOLDER_MAP.put(ANSI_PURPLE_BACKGROUND, "\u001B[45m");
		PLACEHOLDER_MAP.put(ANSI_CYAN_BACKGROUND, "\u001B[46m");
		PLACEHOLDER_MAP.put(ANSI_WHITE_BACKGROUND, "\u001B[107m");

		// Build foreground set
		FOREGROUND_SET.add(ANSI_RESET);
		FOREGROUND_SET.add(ANSI_BLACK);
		FOREGROUND_SET.add(ANSI_RED);
		FOREGROUND_SET.add(ANSI_GREEN);
		FOREGROUND_SET.add(ANSI_YELLOW);
		FOREGROUND_SET.add(ANSI_BLUE);
		FOREGROUND_SET.add(ANSI_PURPLE);
		FOREGROUND_SET.add(ANSI_CYAN);
		FOREGROUND_SET.add(ANSI_WHITE);

		// Build background set
		BACKGROUND_SET.add(ANSI_BLACK_BACKGROUND);
		BACKGROUND_SET.add(ANSI_RED_BACKGROUND);
		BACKGROUND_SET.add(ANSI_GREEN_BACKGROUND);
		BACKGROUND_SET.add(ANSI_YELLOW_BACKGROUND);
		BACKGROUND_SET.add(ANSI_BLUE_BACKGROUND);
		BACKGROUND_SET.add(ANSI_PURPLE_BACKGROUND);
		BACKGROUND_SET.add(ANSI_CYAN_BACKGROUND);
		BACKGROUND_SET.add(ANSI_WHITE_BACKGROUND);
	}

	public static String replacePlaceholders(String input) {
		String output = input;
		for (Map.Entry<String, String> entry : PLACEHOLDER_MAP.entrySet()) {
			output = output.replace(entry.getKey(), entry.getValue());
		}
		return output;
	}

	public static boolean isAnsi(Character c) {
		return PLACEHOLDER_MAP.containsKey(c.toString());
	}

	/**
	 * Returns true if the given character represents a foreground ANSI code.
	 */
	public static boolean isForeground(Character c) {
		return FOREGROUND_SET.contains(c.toString());
	}

	/**
	 * Returns true if the given character represents a background ANSI code.
	 */
	public static boolean isBackground(Character c) {
		return BACKGROUND_SET.contains(c.toString());
	}

	public static String stripAnsi(String input) {
		StringBuilder output = new StringBuilder();
		for (char c : input.toCharArray()) {
			if (!isAnsi(c)) {
				output.append(c);
			}
		}
		return output.toString();
	}
}
