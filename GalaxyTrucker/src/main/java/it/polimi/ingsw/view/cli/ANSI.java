package it.polimi.ingsw.view.cli;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ANSI {
	public static final String RESET = "Ϟ";

	public static final String BLACK = "Ϡ";
	public static final String RED = "ϡ";
	public static final String GREEN = "Ϣ";
	public static final String YELLOW = "ϣ";
	public static final String BLUE = "Ϥ";
	public static final String PURPLE = "ϥ";
	public static final String CYAN = "Ϧ";
	public static final String WHITE = "ϧ";

	public static final String BACKGROUND_BLACK = "Ϩ";
	public static final String BACKGROUND_RED = "ϩ";
	public static final String BACKGROUND_GREEN = "Ϫ";
	public static final String BACKGROUND_YELLOW = "ϫ";
	public static final String BACKGROUND_BLUE = "Ϭ";
	public static final String BACKGROUND_PURPLE = "ϭ";
	public static final String BACKGROUND_CYAN = "Ϯ";
	public static final String BACKGROUND_WHITE = "ϯ";

	private static final Map<String, String> PLACEHOLDER_MAP = new HashMap<>();
	private static final Set<String> FOREGROUND_SET = new HashSet<>();
	private static final Set<String> BACKGROUND_SET = new HashSet<>();

	static {
		PLACEHOLDER_MAP.put(RESET, "\u001B[0m");
		PLACEHOLDER_MAP.put(BLACK, "\u001B[30m");
		PLACEHOLDER_MAP.put(RED, "\u001B[31m");
		PLACEHOLDER_MAP.put(GREEN, "\u001B[32m");
		PLACEHOLDER_MAP.put(YELLOW, "\u001B[33m");
		PLACEHOLDER_MAP.put(BLUE, "\u001B[34m");
		PLACEHOLDER_MAP.put(PURPLE, "\u001B[35m");
		PLACEHOLDER_MAP.put(CYAN, "\u001B[36m");
		PLACEHOLDER_MAP.put(WHITE, "\u001B[97m");

		PLACEHOLDER_MAP.put(BACKGROUND_BLACK, "\u001B[40m");
		PLACEHOLDER_MAP.put(BACKGROUND_RED, "\u001B[41m");
		PLACEHOLDER_MAP.put(BACKGROUND_GREEN, "\u001B[42m");
		PLACEHOLDER_MAP.put(BACKGROUND_YELLOW, "\u001B[43m");
		PLACEHOLDER_MAP.put(BACKGROUND_BLUE, "\u001B[44m");
		PLACEHOLDER_MAP.put(BACKGROUND_PURPLE, "\u001B[45m");
		PLACEHOLDER_MAP.put(BACKGROUND_CYAN, "\u001B[46m");
		PLACEHOLDER_MAP.put(BACKGROUND_WHITE, "\u001B[107m");

		// Build foreground set
		FOREGROUND_SET.add(RESET);
		FOREGROUND_SET.add(BLACK);
		FOREGROUND_SET.add(RED);
		FOREGROUND_SET.add(GREEN);
		FOREGROUND_SET.add(YELLOW);
		FOREGROUND_SET.add(BLUE);
		FOREGROUND_SET.add(PURPLE);
		FOREGROUND_SET.add(CYAN);
		FOREGROUND_SET.add(WHITE);

		// Build background set
		BACKGROUND_SET.add(BACKGROUND_BLACK);
		BACKGROUND_SET.add(BACKGROUND_RED);
		BACKGROUND_SET.add(BACKGROUND_GREEN);
		BACKGROUND_SET.add(BACKGROUND_YELLOW);
		BACKGROUND_SET.add(BACKGROUND_BLUE);
		BACKGROUND_SET.add(BACKGROUND_PURPLE);
		BACKGROUND_SET.add(BACKGROUND_CYAN);
		BACKGROUND_SET.add(BACKGROUND_WHITE);
	}

	public static String applyColors(String input) {
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
