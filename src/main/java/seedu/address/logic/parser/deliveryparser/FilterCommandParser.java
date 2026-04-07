package seedu.address.logic.parser.deliveryparser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMPANY;

import java.util.List;

import seedu.address.logic.commands.deliverycommands.FilterCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.Prefix;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.company.CompanyNameContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FilterCommand object.
 */
public class FilterCommandParser implements Parser<FilterCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FilterCommand
     * and returns a FilterCommand object for execution.
     *
     * @throws ParseException if the user input does not conform the expected format
     */
    public FilterCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_COMPANY);

        if (!arePrefixesPresent(argMultimap, PREFIX_COMPANY) || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }

        List<CompanyNameContainsKeywordsPredicate> companies = argMultimap.getAllValues(PREFIX_COMPANY).stream()
                .map(x -> {
                    try {
                        return ParserUtil.parseCompany(x);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        if (companies.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }
        return new FilterCommand(companies);
    }

    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        for (Prefix prefix : prefixes) {
            if (!argumentMultimap.getValue(prefix).isPresent()) {
                return false;
            }
        }
        return true;
    }
}
