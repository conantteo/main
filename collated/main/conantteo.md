# conantteo
###### \java\seedu\address\commons\core\Messages.java
``` java
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The person index provided is invalid: ";
    public static final String MESSAGE_INVALID_PERSON_TO_EMAIL = "The person may have missing email address "
            + "at specified index provided: ";

}
```
###### \java\seedu\address\commons\events\ui\EmailRequestEvent.java
``` java
package seedu.address.commons.events.ui;

import seedu.address.commons.events.BaseEvent;

/**
 * An event to request for sending email to one or more email addresses
 */
public class EmailRequestEvent extends BaseEvent {

    private final String allEmailAddresses;

    public EmailRequestEvent(String allEmailAddresses) {
        this.allEmailAddresses = allEmailAddresses;
    }

    public String getAllEmailAddresses() {
        return allEmailAddresses;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
```
###### \java\seedu\address\commons\events\ui\ExportRequestEvent.java
``` java
package seedu.address.commons.events.ui;

import seedu.address.commons.events.BaseEvent;

/**
 * An event requesting to export one or more contacts as a Vcard.
 */
public class ExportRequestEvent extends BaseEvent {

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
```
###### \java\seedu\address\commons\util\IndexArrayUtil.java
``` java
package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;

/**
 * Helper functions for handling arrays containing one or more Index.
 */
public class IndexArrayUtil {

    /**
     * Compare two arrays {@code arr1} & {@code arr2} if they have the same Index number(s).
     * Index in both arrays do not have to be in ascending or descending order.
     * @param arr1 cannot be null but can be empty
     * @param arr2 cannot be null but can be empty
     * @return true if two arrays have the same Index number(s)
     */
    public static boolean compareIndexArrays(Index[] arr1, Index[] arr2) {
        requireNonNull(arr1);
        requireNonNull(arr2);

        if (arr1.length != arr2.length) {
            return false;
        }

        Index[] sortedArr1 = sortArray(arr1);
        Index[] sortedArr2 = sortArray(arr2);

        for (int i = 0; i < sortedArr1.length; i++) {
            if (!sortedArr1[i].equals(sortedArr2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Swap elements in an array by its position.
     * @param arr is a given array, it cannot be null
     * @param pos is a valid position to be swap with the next element.
     */
    public static void swapElements(Index[] arr, int pos) {

        Index temp = arr[pos];
        arr[pos] = arr[pos + 1];
        arr[pos + 1] = temp;
    }

    /**
     * Sort Index elements in a Index array by its index value in one-based.
     * @param arr is a valid Index array.
     * @return a sorted Index array.
     */
    private static Index[] sortArray(Index[] arr) {

        for (int k = 0; k < arr.length; k++) {
            for (int i = 0; i < arr.length - 1; i++) {
                if (arr[i].getOneBased() > arr[i + 1].getOneBased()) {
                    swapElements(arr, i);
                }
            }
        }
        return arr;
    }
}
```
###### \java\seedu\address\logic\commands\EmailCommand.java
``` java
package seedu.address.logic.commands;

import java.util.List;

import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.events.ui.EmailRequestEvent;
import seedu.address.commons.util.IndexArrayUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.person.ReadOnlyPerson;

/**
 * Email one or more person identified using it's last displayed index from the address book.
 */
public class EmailCommand extends Command {

    public static final String COMMAND_WORD = "email";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Email one or more person identified by the index number used in the last person listing.\n"
            + "Parameters: INDEX [INDEX]... (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1" + " [2]" + " [3]";

    public static final String MESSAGE_EMAIL_PERSON_SUCCESS = "Email Person: %1$s";

    private final Index[] targetIndices;

    public EmailCommand(Index[] targetIndexes) {
        this.targetIndices = targetIndexes;
    }

    @Override
    public CommandResult execute() throws CommandException {

        List<ReadOnlyPerson> lastShownList = model.getFilteredPersonList();
        StringBuilder addresses = new StringBuilder();
        StringBuilder persons = new StringBuilder();
        for (Index targetIndex : targetIndices) {
            if (targetIndex.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX
                        + targetIndex.getOneBased());
            }
            ReadOnlyPerson personToEmail = lastShownList.get(targetIndex.getZeroBased());
            persons.append(", ");
            persons.append(personToEmail.getName().toString());
            addresses.append(" ");
            if (personToEmail.getEmail().toString().isEmpty()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_TO_EMAIL + targetIndex.getOneBased());
            }
            addresses.append(personToEmail.getEmail().toString());
        }

        String allPersons = persons.toString().trim().substring(2, persons.length());
        String allEmailAddresses = addresses.toString().trim().replaceAll(" ", ",");

        EventsCenter.getInstance().post(new EmailRequestEvent(allEmailAddresses));
        return new CommandResult(String.format(MESSAGE_EMAIL_PERSON_SUCCESS, allPersons));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof EmailCommand // instanceof handles nulls
                && IndexArrayUtil.compareIndexArrays(this.targetIndices, (
                        (EmailCommand) other).targetIndices)); // state check
    }
}
```
###### \java\seedu\address\logic\commands\ExportCommand.java
``` java
package seedu.address.logic.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.events.ui.ExportRequestEvent;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.IndexArrayUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.person.ReadOnlyPerson;
import seedu.address.model.person.Vcard;

/**
 * Exports all contacts or specified person identified using it's last displayed index from the address book.
 */
public class ExportCommand extends Command {

    public static final String COMMAND_WORD = "export";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Export all contacts or specified person identified by the index number "
            + "used in the last person listing into a VCard file.\n"
            + "Parameters: all or INDEX [INDEX]... (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1 2" + " or " + COMMAND_WORD + " all";

    public static final String MESSAGE_EXPORT_PERSON_SUCCESS = "Export Person: %1$s\n"
            + "Please close the app before moving the vcf file to another location.";

    public static final String DEFAULT_FILE_DIR = "./data/";
    public static final String DEFAULT_FILE_NAME = "contacts.vcf";

    private final Index[] targetIndices;

    public ExportCommand() {
        this.targetIndices = new Index[0];
    }

    public ExportCommand(Index[] targetIndexes) {
        this.targetIndices = targetIndexes;
    }

    @Override
    public CommandResult execute() throws CommandException {
        List<ReadOnlyPerson> fullList = model.getAddressBook().getPersonList();
        List<ReadOnlyPerson> lastShownList = model.getFilteredPersonList();
        StringBuilder persons = new StringBuilder();
        ArrayList<Vcard> listOfvCards = new ArrayList<>();
        if (targetIndices.length > 0) {
            for (Index targetIndex : targetIndices) {
                if (targetIndex.getZeroBased() >= lastShownList.size()) {
                    throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX
                            + targetIndex.getOneBased());
                }
                ReadOnlyPerson personToExport = lastShownList.get(targetIndex.getZeroBased());
                persons.append(", ");
                persons.append(personToExport.getName().toString());
                Vcard vCard = new Vcard(personToExport);
                listOfvCards.add(vCard);
            }
        } else {
            for (ReadOnlyPerson person : fullList) {
                persons.append(", ");
                persons.append(person.getName().toString());
                Vcard vCard = new Vcard(person);
                listOfvCards.add(vCard);
            }
        }
        String allPersons = persons.toString().trim().substring(2, persons.length());

        //Create a new VCard format file to store all the VCard information.
        File file = new File(DEFAULT_FILE_DIR, DEFAULT_FILE_NAME);
        try {
            FileUtil.createIfMissing(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Write information from a list of VCards into the VCard format file.
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            assert false :  "File is always created when exporting";
        }
        for (Vcard vCard : listOfvCards) {
            try {
                fos.write(vCard.getCardDetails().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        EventsCenter.getInstance().post(new ExportRequestEvent());
        return new CommandResult(String.format(MESSAGE_EXPORT_PERSON_SUCCESS, allPersons));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ExportCommand // instanceof handles nulls
                && IndexArrayUtil.compareIndexArrays(this.targetIndices, (
                (ExportCommand) other).targetIndices)); // state check
    }
}
```
###### \java\seedu\address\logic\Logic.java
``` java
    /** Returns an unmodifiable view of all tags in the address book */
    ObservableList<Tag> getAllTags();
```
###### \java\seedu\address\logic\LogicManager.java
``` java
    @Override
    public ObservableList<Tag> getAllTags() {
        return model.getAddressBook().getTagList();
    }
```
###### \java\seedu\address\logic\parser\EmailCommandParser.java
``` java
package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.EmailCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new EmailCommand object
 */
public class EmailCommandParser implements Parser<EmailCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the EmailCommand
     * and returns an EmailCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public EmailCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] indexes = args.trim().split(" ");
        Index[] indexArray = new Index[indexes.length];

        try {
            for (int i = 0; i < indexes.length; i++) {
                Index index = ParserUtil.parseIndex(indexes[i]);
                indexArray[i] = index;
            }
            return new EmailCommand(indexArray);
        } catch (IllegalValueException ive) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, EmailCommand.MESSAGE_USAGE));
        }
    }

}
```
###### \java\seedu\address\logic\parser\ExportCommandParser.java
``` java
package seedu.address.logic.parser;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parsers input arguments and creates a new ExportCommand object
 */
public class ExportCommandParser implements Parser<ExportCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the EmailCommand
     * and returns an EmailCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    @Override
    public ExportCommand parse(String args) throws ParseException {

        String trimmedArgs = args.trim();
        if (trimmedArgs.startsWith("all")) {
            return new ExportCommand();
        } else {
            String[] indices = args.trim().split(" ");
            Index[] indexArray = new Index[indices.length];
            try {
                for (int i = 0; i < indices.length; i++) {
                    Index index = ParserUtil.parseIndex(indices[i]);
                    indexArray[i] = index;
                }
                return new ExportCommand(indexArray);
            } catch (IllegalValueException e) {
                throw new ParseException(
                        String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE));
            }
        }
    }
}
```
###### \java\seedu\address\model\AddressBook.java
``` java
    /**
     * Ensures that all tags of a {@code person} is deleted away from the master list.
     */
    private void deleteMasterTagListWith(Person person) {
        final UniqueTagList personTags = new UniqueTagList(person.getTags());
        tags.deleteFrom(personTags);
    }
```
###### \java\seedu\address\model\person\Birthday.java
``` java
package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.exceptions.IllegalValueException;

/**
 * Represents a Person's birthday in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidBirthday(String)}
 */
public class Birthday {

    public static final String MESSAGE_BIRTHDAY_CONSTRAINTS =
            "Birthday format should be 'DD/MM/YYYY', and it should not be blank";

    public static final String BIRTHDAY_VALIDATION_REGEX = "\\d{2}\\/\\d{2}\\/\\d{4}";

    public final String value;

    /**
     * Validates given birthday.
     *
     * @throws IllegalValueException if given birthday string is invalid.
     */
    public Birthday(String birthday) throws IllegalValueException {
        requireNonNull(birthday);
        String trimmedBirthday = birthday.trim();
        if (birthday.length() != 0 && !isValidBirthday(trimmedBirthday)) {
            throw new IllegalValueException(MESSAGE_BIRTHDAY_CONSTRAINTS);
        }
        this.value = trimmedBirthday;
    }

    /**
     * Returns true if a given string is a valid person phone number.
     */
    public static boolean isValidBirthday(String test) {
        return test.matches(BIRTHDAY_VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Birthday // instanceof handles nulls
                && this.value.equals(((Birthday) other).value)); // state check
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
```
###### \java\seedu\address\model\person\Person.java
``` java
    @Override
    public ObjectProperty<Birthday> birthdayProperty() {
        return birthday;
    }

    @Override
    public Birthday getBirthday() {
        return birthday.get();
    }

    public void setBirthday(Birthday birthday) {
        this.birthday.set(requireNonNull(birthday));
    }
```
###### \java\seedu\address\model\person\Vcard.java
``` java
package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents a Vcard of a Person in the address book.
 * Contains user information in a Vcard format stored in a String {@code cardDetails}
 * This Vcard is only created when user wants to export his/her contacts' information.
 */
public class Vcard {

    private String cardDetails;

    /**
     * Constructs empty Vcard.
     */
    public Vcard() {}

    /**
     * Creates a Vcard using a given person.
     * @param person enforces no nulls person.
     * Store information of a given person in a string {@code cardDetails}
     * Note that using Vcard version 3.0:
     * BEING, VERSION, FN, END fields in cardDetails are required.
     * The rest of the fields are not required and can be empty Strings.
     */
    public Vcard(ReadOnlyPerson person) {
        requireNonNull(person);
        String name = person.getName().toString();
        String phone = person.getPhone().toString();
        String address = person.getAddress().toString();
        String email = person.getEmail().toString();
        String birthday = person.getBirthday().toString();
        birthday = buildBirthdayString(birthday);
        cardDetails = "BEGIN:VCARD\n"
                + "VERSION:3.0\n"
                + "FN:" + name + "\n"
                + "TEL;TYPE=MOBILE:" + phone + "\n"
                + "EMAIL;TYPE=WORK:" + email + "\n"
                + "BDAY:" + birthday + "\n"
                + "ADR;TYPE=HOME:;;" + address + "\n"
                + "END:VCARD" + "\n";
    }

    public String getCardDetails() {
        return cardDetails;
    }

    /**
     * This method builds a valid birthday format for Vcard.
     * {@code bday} is a StringBuilder that appends the Year, followed by Month and Day
     * of a particular birthday separated by a dash.
     * @param birthday is a valid birthday string of a person in the format: DD/MM/YYYY.
     * @return a new birthday string format: YYYY-MM-DD
     */
    private String buildBirthdayString(String birthday) {
        if (!birthday.equals("")) {
            String[] birthdayField = birthday.split("/");
            StringBuilder bday = new StringBuilder();
            bday.append(birthdayField[2]);
            bday.append("-");
            bday.append(birthdayField[1]);
            bday.append("-");
            bday.append(birthdayField[0]);
            return bday.toString();
        } else {
            // If there is no birthday information, return empty string
            return "";
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Vcard // instanceof handles nulls
                && this.cardDetails.equals(((Vcard) other).cardDetails));
    }

    @Override
    public int hashCode() {
        return cardDetails.hashCode();
    }

}
```
###### \java\seedu\address\model\tag\UniqueTagList.java
``` java
    /**
     * Deletes the Tags in this list with those in the argument tag list.
     */
    public void deleteFrom(UniqueTagList from) {
        from.internalList.stream()
                .forEach(internalList::remove);
        assert CollectionUtil.elementsAreUnique(internalList);
    }
```
###### \java\seedu\address\storage\AddressBookStorage.java
``` java
    /**
    * Saves the given {@link ReadOnlyAddressBook} in a temporary location
    * @param addressBook cannot be null
    * @throws IOException if there was any problem writing to the file.
    */
    void backupAddressBook(ReadOnlyAddressBook addressBook) throws IOException;
}
```
###### \java\seedu\address\storage\StorageManager.java
``` java
    @Override
    public void backupAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, addressBookStorage.getAddressBookFilePath() + "-backup.xml");
    }
```
###### \java\seedu\address\storage\XmlAddressBookStorage.java
``` java
    @Override
    public void backupAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath + "-backup.xml");
    }

}
```
###### \java\seedu\address\ui\MainWindow.java
``` java
    /**
     * This method will invoke the user's default mail client and set the recipients field with all the
     * email addresses specified by the user.
     * @param allEmailAddresses is a string of all valid email addresses user request to email to.
     * @throws IOException when user's desktop cannot support Desktop operations.
     */
    public void handleEmail(String allEmailAddresses) {
        URI mailTo = null;
        try {
            mailTo = new URI(EMAIL_URI_PREFIX + allEmailAddresses);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (Desktop.isDesktopSupported()) {
            Desktop userDesktop = Desktop.getDesktop();
            logger.info("Showing user's default mail client");
            try {
                userDesktop.mail(mailTo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

```
###### \java\seedu\address\ui\MainWindow.java
``` java
    /**
     * Opens a file folder which shows the directory where contacts.vcf file is found.
     * Folder is is guaranteed to exist before showing.
     * @throws IOException when user's desktop cannot support Desktop operations.
     */
    public void handleExport() {
        File file = new File(EXPORT_FILE_PATH);
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop userDesktop = Desktop.getDesktop();
                logger.info("Showing user's folder for contacts.vcf");
                userDesktop.open(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```
###### \java\seedu\address\ui\MainWindow.java
``` java
    @Subscribe
    private void handleEmailRequestEvent(EmailRequestEvent event) throws Exception {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        handleEmail(event.getAllEmailAddresses());
    }

```
###### \java\seedu\address\ui\MainWindow.java
``` java
    @Subscribe
    private void handleExportRequestEvent(ExportRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        handleExport();
    }
}
```
###### \java\seedu\address\ui\TagBox.java
``` java
package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.tag.Tag;

/**
 * An UI component that display the name of a {@code Tag}.
 */
public class TagBox extends UiPart<Region> {

    private static final String FXML = "TagBox.fxml";
    public final Tag tag;

    @FXML
    private HBox cardPane;
    @FXML
    private Label tagsName;

    public TagBox(Tag tag) {
        super(FXML);
        this.tag = tag;
        initTags(tag);
    }

    private void initTags(Tag tag) {
        tagsName.setText(tag.tagName);
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }
        // instanceof handles nulls
        if (!(other instanceof TagBox)) {
            return false;
        }
        // state check
        return tag.equals(((TagBox) other).tag);
    }
}
```
###### \java\seedu\address\ui\TagListPanel.java
``` java
package seedu.address.ui;

import org.fxmisc.easybind.EasyBind;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.tag.Tag;

/**
 * Panel containing the list of unique tags
 */
public class TagListPanel extends UiPart<Region> {
    private static final String FXML = "GroupList.fxml";

    @FXML
    private ListView tagListView;

    public TagListPanel(ObservableList<Tag> allTagsList) {
        super(FXML);
        bindTags(allTagsList);
    }

    /**
     * Creating bindings for each tag to each ListCell in the ListView
     * @param allTagsList is a valid list of all unique tags to be displayed.
     */
    private void bindTags(ObservableList<Tag> allTagsList) {
        ObservableList<TagBox> mappedList = EasyBind.map(
                allTagsList, (tag) -> new TagBox(tag));
        tagListView.setItems(mappedList);
        tagListView.setCellFactory(listView -> new TagListViewCell());

    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code TagBox}.
     */
    class TagListViewCell extends ListCell<TagBox> {

        @Override
        protected void updateItem(TagBox tagBox, boolean empty) {
            super.updateItem(tagBox, empty);

            Platform.runLater(() -> {
                if (empty || tagBox == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(tagBox.getRoot());
                }

            });

        }
    }
}
```