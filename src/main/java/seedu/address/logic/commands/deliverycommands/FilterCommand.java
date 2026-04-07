package seedu.address.logic.commands.deliverycommands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMPANY;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.company.CompanyNameContainsKeywordsPredicate;
import seedu.address.model.delivery.Delivery;

/**
 * Sorts a company's deliveries by deadline, with the earliest deadline shown first.
 */
public class FilterCommand extends Command {

    public static final String COMMAND_WORD = "filter";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Filters deliveries for a company by deadline, "
            + "with the earliest deadline first.\n"
            + "Parameters: " + PREFIX_COMPANY + "COMPANY\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_COMPANY + "Dell";

    public static final String MESSAGE_SORT_SUCCESS = "Filtered %1$d delivery(s) for company: %2$s";
    public static final String MESSAGE_NO_DELIVERIES_FOR_COMPANY = "No deliveries found for company: %1$s";

    private final List<CompanyNameContainsKeywordsPredicate> name;

    /**
     * Creates a FilterCommand to filter deliveries for the specified company.
     */
    public FilterCommand(List<CompanyNameContainsKeywordsPredicate> name) {
        requireNonNull(name);
        this.name = name;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<String> companyName = getCompanyName();
        Predicate<Delivery> matchesCompany = delivery -> false;
        List<String> empty= new ArrayList<>();
        for (String name : companyName) {
            System.out.println(name);
            Predicate<Delivery> match = delivery -> delivery.getCompany().getName().toString().equalsIgnoreCase(name);
            boolean hasMatchingDelivery = model.getDeliveryBook().getDeliveryList().stream()
                    .anyMatch(match);
            if (!hasMatchingDelivery) {
                empty.add(name);
            }
            matchesCompany = matchesCompany.or(match);
        }
        model.sortDeliveriesByDeadline(matchesCompany);
        model.updateFilteredDeliveryList(matchesCompany);
        if (!empty.isEmpty()){
            throw new CommandException(String.format(MESSAGE_NO_DELIVERIES_FOR_COMPANY, companyName));
        }

        return new CommandResult(
                String.format(MESSAGE_SORT_SUCCESS, model.getFilteredDeliveryList().size(),
                        String.join(" ", companyName)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof FilterCommand)) {
            return false;
        }

        FilterCommand otherFilterCommand = (FilterCommand) other;
        return name.equals(otherFilterCommand.name);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("company", getCompanyName())
                .toString();
    }

    private List<String> getCompanyName() {
        return name.stream().map(x -> x.getKeywords().get(0)).toList();
    }
}