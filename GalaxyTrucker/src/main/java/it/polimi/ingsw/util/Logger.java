package it.polimi.ingsw.util;

import static it.polimi.ingsw.view.cli.ANSI.*;

public class Logger {

	public enum LoggerLevel {
		INFO, WARN, ERROR
	}

	// Default logger level
	private static LoggerLevel currentLevel = LoggerLevel.INFO;

	// Setter for logger level
	public static void setLevel(LoggerLevel level) {
		currentLevel = level;
	}

	// Static method for INFO logs
	public static void info(String message) {
		if (currentLevel.ordinal() <= LoggerLevel.INFO.ordinal()) {
			System.out.println(GREEN + "[INFO] " + message + RESET);
		}
	}

	// Static method for WARN logs
	public static void warn(String message) {
		if (currentLevel.ordinal() <= LoggerLevel.WARN.ordinal()) {
			System.out.println(YELLOW + "[WARN] " + message + RESET);
		}
	}

	// Static method for ERROR logs
	public static void error(String message) {
		if (currentLevel.ordinal() <= LoggerLevel.ERROR.ordinal()) {
			System.out.println(RED + "[ERROR] " + message + RESET);
		}
	}

	// Static method for ERROR logs
	public static void error(String message, Exception e) {
		if (currentLevel.ordinal() <= LoggerLevel.ERROR.ordinal()) {
			System.out.println(RED + "[ERROR] " + message + RESET);
			e.printStackTrace();
		}
	}
}
