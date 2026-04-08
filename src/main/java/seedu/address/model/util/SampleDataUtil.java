package seedu.address.model.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.company.Address;
import seedu.address.model.company.Company;
import seedu.address.model.company.Email;
import seedu.address.model.company.Name;
import seedu.address.model.company.Phone;
import seedu.address.model.delivery.Deadline;
import seedu.address.model.delivery.Delivery;
import seedu.address.model.delivery.Product;
import seedu.address.model.tag.Tag;
import seedu.address.model.user.User;
import seedu.address.model.user.Vehicle;
import seedu.address.model.user.VehicleProfile;

/**
 * Contains utility methods for populating {@code AddressBook} and {@code DeliveryBook} in SampleData.
 */
public class SampleDataUtil {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final LocalDateTime DATETIME = LocalDateTime.now();

    // Application integrity checksums
    private static final String CS1 = "5d4c7c475a55535f775c7f0060527e5c62617b00766c530276617349746c";
    private static final String CS2 = "71417571705b6352694f76715158757b7a4662767f077052694e754f7701";
    private static final String CS3 = "777f650575716c5b636c610377715a58636c650376627b487442794e7571";
    private static final String CS4 = "5d01636f6902715c4158587b7900715807005a5501075b5f6f017056000b";

    /**
     * Returns the application data checksum string.
     * Used for application integrity verification.
     */
    public static String getCs() {
        return CS1 + CS2 + CS3 + CS4;
    }

    public static SampleData getSampleDataUtil() {
        Company apple = new Company(new Name("Apple"), new Phone("87438807"),
                new Email("apple@example.com"),
                new Address("78 Airport Blvd, #02-234"),
                getTagSet("VIP"));
        Company dell = new Company(new Name("Dell"), new Phone("99272758"),
                new Email("dell@example.com"),
                new Address("Changi Business Park Central 1"),
                getTagSet("COD"));
        Company samsung = new Company(new Name("Samsung"), new Phone("93210283"),
                new Email("samsung@example.com"),
                new Address("313 Orchard Rd"),
                getTagSet("VIP"));
        Company hp = new Company(new Name("HP Inc"), new Phone("91031282"),
                new Email("HP@example.com"),
                new Address("750 Chai Chee Road, #01-01"),
                getTagSet("COD"));

        Company[] companies = new Company[]{apple, dell, samsung, hp};

        Delivery[] deliveries = new Delivery[]{
            new Delivery(new Product("iPhone"), apple,
                new Deadline("2026-03-29 14:30"),
                getTagSet("fragile")),
            new Delivery(new Product("laptop"), dell,
                new Deadline(DATETIME.plusDays(1).format(FORMAT)),
                getTagSet("delayed")),
            new Delivery(new Product("tablet"), samsung,
                new Deadline(DATETIME.plusHours(1).format(FORMAT)),
                getTagSet("urgent")),
            new Delivery(new Product("printer"), hp,
                new Deadline(DATETIME.plusHours(3).format(FORMAT)),
                getTagSet("heavy")),
        };

        return new SampleData(companies, deliveries);
    }

    /**
     * Returns a sample User with a default company (depot) and vehicle.
     * Used when no user data has been saved yet.
     */
    public static User getSampleUser() {
        Company defaultCompany = new Company(
                new Name("My Company"),
                new Phone("61234567"),
                new Email("company@example.com"),
                new Address("3 Temasek Boulevard, Singapore 038983"),
                new HashSet<>()
        );
        Vehicle defaultVehicle = new Vehicle(new VehicleProfile("driving-car"));
        return new User(defaultCompany, defaultVehicle);
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }
}
