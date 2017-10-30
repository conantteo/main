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
