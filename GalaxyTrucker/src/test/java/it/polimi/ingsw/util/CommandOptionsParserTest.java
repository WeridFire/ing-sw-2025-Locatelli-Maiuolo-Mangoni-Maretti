package it.polimi.ingsw.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandOptionsParserTest {

    private final List<CommandOptionsParser.OptionFinder> finders = List.of(
            new CommandOptionsParser.OptionFinder(List.of("-h", "--help"), "help", "false"),
            new CommandOptionsParser.OptionFinder(List.of("-t", "--text"), "text", ""),
            new CommandOptionsParser.OptionFinder(List.of("-d", "--default"), "default", "def")
    );

    @Test
    public void testParseCorrect() throws CommandOptionsParser.IllegalFormatException {
        String command = "command arg1 --help true --text hello\tI'm a text";
        HashMap<String, String> options = CommandOptionsParser.parse(command, finders);

        HashMap<String, String> expectedResult = new HashMap<>();
        expectedResult.put(CommandOptionsParser.COMMAND_REMINDER, "command arg1");
        expectedResult.put("help", "true");
        expectedResult.put("text", "hello I'm a text");
        expectedResult.put("default", "def");

        assertEquals(expectedResult, options);
    }

    @Test
    public void testParseOptionRepeated() {
        assertThrows(CommandOptionsParser.IllegalFormatException.class, () -> CommandOptionsParser.parse(
                "command arg1 --help true -h true", finders));
        assertThrows(CommandOptionsParser.IllegalFormatException.class, () -> CommandOptionsParser.parse(
                "command arg1 --text ciao -t hello", finders));
    }

    @Test
    public void testMissingValue() throws CommandOptionsParser.IllegalFormatException {
        assertEquals("", CommandOptionsParser.parse("command arg1 arg2 -t cool text --help",
                finders).get("help"));
        assertEquals("", CommandOptionsParser.parse("command arg1 arg2 -h -t cool text",
                finders).get("help"));
        assertDoesNotThrow(() -> CommandOptionsParser.parse("command arg1 arg2", finders));
    }

    @Test
    public void testOptionFindersWithSameAliasOrID() {
        assertThrows(IllegalArgumentException.class, () -> CommandOptionsParser.parse(
                "command arg1 --help true --text hello\tI'm a text", List.of(
                        new CommandOptionsParser.OptionFinder(List.of("-h", "--help"), "help", "false"),
                        new CommandOptionsParser.OptionFinder(List.of("-t", "--text"), "text", ""),
                        new CommandOptionsParser.OptionFinder(List.of("-d", "--default"), "default", "def"),
                        new CommandOptionsParser.OptionFinder("-h", "help2", "false")
                )));
        assertThrows(IllegalArgumentException.class, () -> CommandOptionsParser.parse(
                "command arg1 --help true --text hello\tI'm a text", List.of(
                        new CommandOptionsParser.OptionFinder(List.of("-h", "--help"), "help", "false"),
                        new CommandOptionsParser.OptionFinder(List.of("-t", "--text"), "text", ""),
                        new CommandOptionsParser.OptionFinder(List.of("-d", "--default"), "default", "def"),
                        new CommandOptionsParser.OptionFinder(List.of("-h2"), "help", "false")
                )));
    }
}