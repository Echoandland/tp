package seedu.address.logic.commands.deliverycommands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.delivery.Delivery;

/**
 * Plans routes for all deliveries currently selected (same as the "Map selected deliveries" button).
 */
public class RouteCommand extends Command {

    public static final String COMMAND_WORD = "route";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Plans routes on the map for all currently selected deliveries.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Opened Routes tab; planning routes for %1$d delivery(s).";

    public static final String MESSAGE_NO_SELECTION =
            "No deliveries selected. Use checkboxes or the select command first.";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Delivery> selected = model.getSelectedDeliveriesInDisplayOrder();
        if (selected.isEmpty()) {
            throw new CommandException(MESSAGE_NO_SELECTION);
        }
        String feedback = String.format(MESSAGE_SUCCESS, selected.size());
        return new CommandResult(feedback, false, false, List.copyOf(selected));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RouteCommand;
    }

    @Override
    public int hashCode() {
        return RouteCommand.class.hashCode();
    }
}
