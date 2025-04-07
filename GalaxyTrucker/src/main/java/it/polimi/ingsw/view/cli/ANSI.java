package it.polimi.ingsw.view.cli;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ANSI {

	public static class Helper {
		// regex ANSI: ESC + [ + numbers + m
		private static final String ANSI_REGEX = "\u001B\\[([0-9;]+)m";
		private static final Pattern ANSI_PATTERN = Pattern.compile(ANSI_REGEX);

		public static List<String> splitAnsiAndUnicode(String s) {
			List<String> result = new ArrayList<>();
			Matcher m = ANSI_PATTERN.matcher(s);
			int i = 0;

			while (i < s.length()) {
				// if found an ANSI sequence here
				if (m.find(i) && m.start() == i) {
					result.add(m.group());
					i = m.end();
				} else {
					// takes a single code point (even emoji)
					int cp = s.codePointAt(i);
					result.add(new String(Character.toChars(cp)));
					i += Character.charCount(cp);
				}
			}

			return result;
		}

		public static Integer ansiToCode(String ansi) {
			Matcher matcher = ANSI_PATTERN.matcher(ansi);
			if (matcher.matches()) {
				return Integer.parseInt(matcher.group(1));
			}
			return null;
		}

		public static String codeToAnsi(short code) {
			return "\u001B[" + code + "m";
		}

		public static String stripAnsi(String input) {
			return input.replaceAll(ANSI_REGEX, "");
		}
	}

	public static final String RESET = "\u001B[0m";

	public static final String BLACK = "\u001B[30m";
	public static final String RED = "\u001B[31m";
	public static final String GREEN = "\u001B[32m";
	public static final String YELLOW = "\u001B[33m";
	public static final String BLUE = "\u001B[34m";
	public static final String PURPLE = "\u001B[35m";
	public static final String CYAN = "\u001B[36m";
	public static final String WHITE = "\u001B[97m";

	public static final String BACKGROUND_BLACK = "\u001B[40m";
	public static final String BACKGROUND_RED = "\u001B[41m";
	public static final String BACKGROUND_GREEN = "\u001B[42m";
	public static final String BACKGROUND_YELLOW = "\u001B[43m";
	public static final String BACKGROUND_BLUE = "\u001B[44m";
	public static final String BACKGROUND_PURPLE = "\u001B[45m";
	public static final String BACKGROUND_CYAN = "\u001B[46m";
	public static final String BACKGROUND_WHITE = "\u001B[107m";

	private static final Set<String> FOREGROUND_SET = new HashSet<>();
	private static final Set<String> BACKGROUND_SET = new HashSet<>();

	static {
		// build foreground set
		FOREGROUND_SET.add(BLACK);
		FOREGROUND_SET.add(RED);
		FOREGROUND_SET.add(GREEN);
		FOREGROUND_SET.add(YELLOW);
		FOREGROUND_SET.add(BLUE);
		FOREGROUND_SET.add(PURPLE);
		FOREGROUND_SET.add(CYAN);
		FOREGROUND_SET.add(WHITE);

		// build background set
		BACKGROUND_SET.add(BACKGROUND_BLACK);
		BACKGROUND_SET.add(BACKGROUND_RED);
		BACKGROUND_SET.add(BACKGROUND_GREEN);
		BACKGROUND_SET.add(BACKGROUND_YELLOW);
		BACKGROUND_SET.add(BACKGROUND_BLUE);
		BACKGROUND_SET.add(BACKGROUND_PURPLE);
		BACKGROUND_SET.add(BACKGROUND_CYAN);
		BACKGROUND_SET.add(BACKGROUND_WHITE);
	}

	/**
	 * Returns true if the given character represents a foreground ANSI code.
	 */
	public static boolean isForeground(String ch) {
		return FOREGROUND_SET.contains(ch);
	}

	/**
	 * Returns true if the given character represents a background ANSI code.
	 */
	public static boolean isBackground(String ch) {
		return BACKGROUND_SET.contains(ch);
	}
}
