package seedu.address.ui;

import java.util.List;

import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.Model;
import seedu.address.model.delivery.Delivery;

/**
 * Panel containing the list of deliveries.
 */
public class DeliveryListPanel extends UiPart<Region> {
    private static final String FXML = "DeliveryListPanel.fxml";

    private final Model model;
    private final Runnable onSelectionChanged;

    @FXML
    private ListView<Delivery> deliveryListView;

    /**
     * Creates a {@code DeliveryListPanel} with the given {@code ObservableList}.
     */
    public DeliveryListPanel(ObservableList<Delivery> deliveryList, Model model, Runnable onSelectionChanged) {
        super(FXML);
        this.model = model;
        this.onSelectionChanged = onSelectionChanged;
        deliveryListView.setItems(deliveryList);
        deliveryListView.setCellFactory(listView -> new DeliveryListViewCell());
        model.getDeliverySelection().addListener((SetChangeListener<Delivery>) c -> {
            deliveryListView.refresh();
            onSelectionChanged.run();
        });
    }

    /**
     * Returns selected deliveries in display order
     * (same as {@link Model#getSelectedDeliveriesInDisplayOrder()}).
     */
    public List<Delivery> getSelectedDeliveries() {
        return model.getSelectedDeliveriesInDisplayOrder();
    }

    private void setSelected(Delivery delivery, boolean isSelected) {
        model.setDeliverySelected(delivery, isSelected);
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Delivery} using a {@code DeliveryCard}.
     */
    class DeliveryListViewCell extends ListCell<Delivery> {
        @Override
        protected void updateItem(Delivery delivery, boolean empty) {
            super.updateItem(delivery, empty);

            if (empty || delivery == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new DeliveryCard(delivery, getIndex() + 1,
                        model.getDeliverySelection().contains(delivery),
                        isSelected -> setSelected(delivery, isSelected)).getRoot());
            }
        }
    }
}
