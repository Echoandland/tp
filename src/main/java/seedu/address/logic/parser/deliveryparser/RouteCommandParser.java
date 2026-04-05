package seedu.address.logic.parser.deliveryparser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.deliverycommands.RouteCommand;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@link RouteCommand}.
 */
public class RouteCommandParser implements Parser<RouteCommand> {

    @Override
    public RouteCommand parse(String args) throws ParseException {
        if (!args.trim().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RouteCommand.MESSAGE_USAGE));
        }
        return new RouteCommand();
    }
}
