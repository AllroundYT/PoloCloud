package de.polocloud.base.console;

import de.polocloud.base.Base;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.*;

public final class ConsoleCompleter implements Completer {

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        final var input = parsedLine.line();

        List<String> suggestions = null;
        var arguments = input.split(" ");
        if (input.isEmpty() || input.indexOf(' ') == -1) {
            final Collection<String> registeredCommands = Base.getInstance().getCommandManager().getCachedCloudCommands().keySet();
            final var toTest = arguments[arguments.length - 1];
            final List<String> result = new LinkedList<>();
            for (final String s : registeredCommands) {
                if (s != null && (toTest.trim().isEmpty() || s.toLowerCase().contains(toTest.toLowerCase()))) {
                    result.add(s);
                }
            }

            if (result.isEmpty() && !registeredCommands.isEmpty()) {
                result.addAll(registeredCommands);
            }

            suggestions = result;
        } else {
            final var command = Base.getInstance().getCommandManager().getCachedCloudCommands().get(arguments[0]);
            if (arguments.length > 1) {
                if (input.endsWith(" ")) {
                    arguments = Arrays.copyOfRange(arguments, 1, arguments.length + 1);
                    arguments[arguments.length - 1] = "";
                } else {
                    arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
                }
            }
            if (command != null) {
                suggestions = command.tabComplete(arguments);
            }
        }

        if (suggestions == null || suggestions.isEmpty()) return;

        suggestions.stream().map(Candidate::new).forEach(list::add);
    }

}
