package seedu.address.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.DeliveryBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyDeliveryBook;
import seedu.address.model.company.Address;
import seedu.address.model.company.Company;
import seedu.address.model.company.Email;
import seedu.address.model.company.Name;
import seedu.address.model.company.Phone;
import seedu.address.model.delivery.Address;
import seedu.address.model.delivery.Delivery;
import seedu.address.model.delivery.Product;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} and {@code DeliveryBook} with sample data.
 */
public class SampleDataUtil {
    public static SampleData getSampleDatas() {
        Company c1 = new Company(new Name("Apple"), new Phone("87438807"), new Email("apple@example.com"),
                new seedu.address.model.company.Address("Blk 30 Geylang Street 29, #06-40"),
                getTagSet("important"));
        Company c2 = new Company(new Name("Dell"), new Phone("99272758"), new Email("dell@example.com"),
                new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18"), getTagSet(""));
        new Company[] {
            new Company(new Name("Alex Yeoh"), new Phone("87438807"), new Email("alexyeoh@example.com"),
                new Address("Blk 30 Geylang Street 29, #06-40"),
                getTagSet("friends")),
            new Company(new Name("Bernice Yu"), new Phone("99272758"), new Email("berniceyu@example.com"),
                new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18"),
                getTagSet("colleagues", "friends")),
            new Company(new Name("Charlotte Oliveiro"), new Phone("93210283"), new Email("charlotte@example.com"),
                new Address("Blk 11 Ang Mo Kio Street 74, #11-04"),
                getTagSet("neighbours")),
            new Company(new Name("David Li"), new Phone("91031282"), new Email("lidavid@example.com"),
                new Address("Blk 436 Serangoon Gardens Street 26, #16-43"),
                getTagSet("family")),
            new Company(new Name("Irfan Ibrahim"), new Phone("92492021"), new Email("irfan@example.com"),
                new Address("Blk 47 Tampines Street 20, #17-35"),
                getTagSet("classmates")),
            new Company(new Name("Roy Balakrishnan"), new Phone("92624417"), new Email("royb@example.com"),
                new Address("Blk 45 Aljunied Street 85, #11-31"),
                getTagSet("colleagues"))
        };

        Delivery[] deliveries = new Delivery[] {
                new Delivery(new Product("laptop"), c1, new seedu.address.model.delivery.Address("Blk 30 Geylang Street 29, #06-40"),
                        getTagSet("fragile"))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Company sampleCompany : getCompanies()) {
            sampleAb.addCompany(sampleCompany);
        }
        return sampleAb;
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
