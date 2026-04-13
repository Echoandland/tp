package seedu.address.logic.commands.companycommands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.company.Company;
import seedu.address.model.delivery.Delivery;

/**
 * Deletes a company identified using its displayed index from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the company identified by the index number used in the displayed company list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_COMPANY_SUCCESS = "Deleted Company: %1$s";
    public static final String MESSAGE_DELETE_CASCADED_DELIVERIES =
            "\nAlso deleted %1$d associated delivery/deliveries:\n%2$s";

    private final Index targetIndex;

    public DeleteCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Company> lastShownList = model.getFilteredCompanyList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_COMPANY_DISPLAYED_INDEX);
        }

        Company companyToDelete = lastShownList.get(targetIndex.getZeroBased());
        List<Delivery> cascadedDeliveries = new ArrayList<>();
        for (Delivery d: new ArrayList<>(model.getFilteredDeliveryList())) {
            if (d.getCompany().equals(companyToDelete)) {
                cascadedDeliveries.add(d);
                model.deleteDelivery(d);
            }
        }
        model.deleteCompany(companyToDelete);

        String message = String.format(MESSAGE_DELETE_COMPANY_SUCCESS, Messages.format(companyToDelete));
        if (!cascadedDeliveries.isEmpty()) {
            String deliveryList = cascadedDeliveries.stream()
                    .map(Messages::format)
                    .collect(Collectors.joining("\n"));
            message += String.format(MESSAGE_DELETE_CASCADED_DELIVERIES,
                    cascadedDeliveries.size(), deliveryList);
        }
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand)) {
            return false;
        }

        DeleteCommand otherDeleteCommand = (DeleteCommand) other;
        return targetIndex.equals(otherDeleteCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
