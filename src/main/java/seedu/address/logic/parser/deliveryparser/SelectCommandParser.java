package seedu.address.logic.parser.deliveryparser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.deliverycommands.SelectCommand;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@link SelectCommand}.
 */
public class SelectCommandParser implements Parser<SelectCommand> {

    private static final String NONE_KEYWORD = "none";

    @Override
    public SelectCommand parse(String args) throws ParseException {
        String trimmed = args.trim();
        if (trimmed.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SelectCommand.MESSAGE_USAGE));
        }

        if (trimmed.equalsIgnoreCase(NONE_KEYWORD)) {
            return new SelectCommand(true, List.of());
        }

        String[] tokens = trimmed.split("\\s+");
        List<Index> indices = new ArrayList<>();
        for (String token : tokens) {
            indices.add(ParserUtil.parseIndex(token));
        }
        return new SelectCommand(false, indices);
    }
}
