package it.polimi.ingsw.util;

import java.util.*;

/**
 * Utility class for parsing cmd-style command strings with custom options.
 * It extracts declared options and stores them in a helpful map.
 * Remaining parts of the command (the actual command name and positional arguments) are stored under a special key.
 */
public class CommandOptionsParser {

    public static class IllegalFormatException extends RuntimeException {
        protected IllegalFormatException(String message) {
            super(message);
        }
    }

    /**
     * Reserved key used in the resulting map to store the remaining non-option part of the command.
     * For example, if the input is "command argument --opt_name1 opt_value1 --opt_name2 opt_value2",
     * the reminder would be "command argument".
     */
    public static final String COMMAND_REMINDER = "__cmdrem__";

    /**
     * Describes how to find a single option in a command string.
     */
    public static class OptionFinder {
        private final Set<String> optionAlias;
        private final String optionID;
        private final String defaultValue;

        /**
         * Constructs an OptionFinder with multiple aliases.
         *
         * @param optionAlias all valid forms of the option (e.g., {"-f", "--force"})
         * @param optionID a unique identifier to be used as a key in the result map
         * @param defaultValue a value to use when the specified option is not found
         * @throws IllegalArgumentException if {@code optionID} is equal to {@link #COMMAND_REMINDER}
         */
        public OptionFinder(final Collection<String> optionAlias, final String optionID, final String defaultValue) {
            if (COMMAND_REMINDER.equals(optionID)) {
                throw new IllegalArgumentException("optionID can not be " + COMMAND_REMINDER);
            }
            this.optionAlias = new HashSet<>(optionAlias);
            this.optionID = optionID;
            this.defaultValue = defaultValue;
        }

        /**
         * Constructs an OptionFinder with a single alias.
         *
         * @param optionDeclaration the option name (e.g., "--force")
         * @param optionID a unique identifier to be used as a key in the result map
         */
        public OptionFinder(final String optionDeclaration, final String optionID, final String defaultValue) {
            this(Set.of(optionDeclaration), optionID, defaultValue);
        }

        /**
         * @return all valid forms of this option
         */
        protected Set<String> getOptionAlias() {
            return optionAlias;
        }

        /**
         * @return the key under which the parsed value will be stored
         */
        protected String getOptionID() {
            return optionID;
        }

        /**
         * @return the value used when this option is not found in the command
         */
        protected String getDefaultValue() {
            return defaultValue;
        }
    }

    /**
     * Parses a command string using the provided list of {@link OptionFinder} rules.
     * Recognized options are extracted and stored one by one,
     * and the rest of the command is stored under {@link #COMMAND_REMINDER}.
     *
     * @param command the full command string to parse (e.g. "command argument --opt_name1 opt_value1
     *                --opt_name2 opt_value2")
     * @param optionsFinder a list of rules to recognize all the possible options, with default values
     * @return a map of parsed values: option IDs as keys and parsed values as values,
     *         plus a {@link #COMMAND_REMINDER} entry for the rest of the command
     * @throws IllegalArgumentException if in the {@code optionsFinder} provided two (or more) different options
     * share a common alias
     * @throws IllegalFormatException if an option is listed twice in the command
     */
    public static HashMap<String, String> parse(String command, List<OptionFinder> optionsFinder)
            throws IllegalFormatException {
        StringBuilder reminder = new StringBuilder();
        String[] words = command.trim().split("\\s+");
        HashMap<String, String> options = new HashMap<>();

        // build a map from option alias to its key
        HashMap<String, String> aliasMap = new HashMap<>();
        for (OptionFinder opt : optionsFinder) {
            String optionID = opt.getOptionID();
            if (aliasMap.containsValue(optionID)) {
                throw new IllegalArgumentException("Option '" + optionID + "' already exists." +
                        " Attempted to use it from another OptionFinder.");
            }
            for (String alias : opt.getOptionAlias()) {
                if (aliasMap.containsKey(alias)) {
                    throw new IllegalArgumentException("Option alias '" + alias + "' already exists for the option '"
                            + aliasMap.get(alias) + "'. Attempted to use it again for the option '"
                            + optionID + "'.");
                }
                aliasMap.put(alias, opt.getOptionID());
            }
        }

        // iterate through the command words
        for (int i = 0; i < words.length; ) {
            String word = words[i];
            if (aliasMap.containsKey(word)) {
                if (options.containsKey(aliasMap.get(word))) {
                    throw new CommandOptionsParser.IllegalFormatException(
                            "Option '" + word + "' found twice in the command.");
                }

                String optionKey = aliasMap.get(word);

                // catch all the following values as content, until a new alias is found or the command ends
                StringBuilder value = new StringBuilder();
                i++;  // start from the next word
                while (i < words.length && !aliasMap.containsKey(words[i])) {
                    if (!value.isEmpty()) value.append(' ');
                    value.append(words[i++]);
                }

                // save the value - note: valid also for empty values as a sign of presence
                // e.g. if the command is "git push --force" the alias '--force' is saved with value ""
                // to notify its presence
                options.put(optionKey, value.toString());
            } else {
                // not a known option, append to the command reminder
                if (!reminder.isEmpty()) reminder.append(' ');
                reminder.append(word);
                i++;
            }
        }

        // add default values if needed
        for (OptionFinder opt : optionsFinder) {
            if (!options.containsKey(opt.getOptionID())) {
                options.put(opt.getOptionID(), opt.getDefaultValue());
            }
        }

        // add command reminder and return
        options.put(COMMAND_REMINDER, reminder.toString());
        return options;
    }

    /**
     * Like {@link #parse(String, List)}, but with command separated into commandName and args
     * @param commandName the word that identify the command
     * @param optionsFinder array of words representing all the content of a command after the first word
     *                      (which is {@code commandName})
     */
    public static HashMap<String, String> parse(String commandName, String[] args, List<OptionFinder> optionsFinder)
            throws IllegalFormatException {
        StringBuilder command = new StringBuilder(commandName);
        for (String arg : args) command.append(' ').append(arg);
        return parse(command.toString(), optionsFinder);
    }

    public static String getCommandName(HashMap<String, String> parsedCommandWithOptions) {
        String fullCommand = parsedCommandWithOptions.get(COMMAND_REMINDER);
        if (fullCommand == null) return null;
        else return fullCommand.split(" ")[0];
    }

    public static String[] getCommandArgs(HashMap<String, String> parsedCommandWithOptions) {
        String fullCommand = parsedCommandWithOptions.get(COMMAND_REMINDER);
        if (fullCommand == null) return new String[0];
        else {
            String[] commandArgs = fullCommand.split(" ");
            return Arrays.copyOfRange(commandArgs, 1, commandArgs.length);
        }
    }
}
