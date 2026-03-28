package seedu.address.ui;

import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.json.JSONArray;
import org.json.JSONObject;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.delivery.Delivery;
import seedu.address.routing.model.RouteResult;
import seedu.address.routing.service.DeliveryRouterService;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final int ROUTES_TAB_INDEX = 2;
    private static final int DEFAULT_NUM_VEHICLES = 2;

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private final Logic logic;
    private final Model model;

    private final DeliveryRouterService routerService = new DeliveryRouterService();
    private WebView mapView;
    private WebEngine webEngine;

    private CompanyListPanel companyListPanel;
    private DeliveryListPanel deliveryListPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;

    @FXML private StackPane commandBoxPlaceholder;
    @FXML private MenuItem helpMenuItem;
    @FXML private TabPane listTabPane;
    @FXML private StackPane companyListPanelPlaceholder;
    @FXML private StackPane deliveryListPanelPlaceholder;
    @FXML private StackPane resultDisplayPlaceholder;
    @FXML private StackPane statusbarPlaceholder;
    @FXML private StackPane mapPlaceholder;
    @FXML private Button planRoutesButton;
    @FXML private Label routeStatusLabel;

    public MainWindow(Stage primaryStage, Logic logic, Model model) {
        super(FXML, primaryStage);
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.model = model;

        setWindowDefaultSize(logic.getGuiSettings());
        configureListTabs();
        setAccelerators();
        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    void fillInnerParts() {
        companyListPanel = new CompanyListPanel(model.getFilteredCompanyList());
        companyListPanelPlaceholder.getChildren().add(companyListPanel.getRoot());

        deliveryListPanel = new DeliveryListPanel(model.getFilteredDeliveryList());
        deliveryListPanelPlaceholder.getChildren().add(deliveryListPanel.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(logic.getAddressBookFilePath());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());

        initMap();
        syncSelectedTabWithMode();
    }

    private void initMap() {
        mapView = new WebView();
        webEngine = mapView.getEngine();
        URL mapUrl = getClass().getResource("/view/route-map.html");
        if (mapUrl != null) {
            webEngine.load(mapUrl.toExternalForm());
        } else {
            logger.warning("route-map.html not found in resources");
        }
        mapPlaceholder.getChildren().add(mapView);
    }

    @FXML
    private void handlePlanRoutes() {
        List<Delivery> deliveries = model.getFilteredDeliveryList()
                .stream()
                .collect(Collectors.toList());

        if (deliveries.isEmpty()) {
            routeStatusLabel.setText("No deliveries to route. Add some deliveries first.");
            return;
        }

        planRoutesButton.setDisable(true);
        routeStatusLabel.setText("Planning routes... please wait.");

        Task<RouteResult> task = new Task<>() {
            @Override
            protected RouteResult call() throws Exception {
                return routerService.planRoutes(deliveries, DEFAULT_NUM_VEHICLES);
            }
        };

        task.setOnSucceeded(e -> {
            RouteResult result = task.getValue();
            drawRoutesOnMap(result);
            planRoutesButton.setDisable(false);

            int totalStops = result.routes.stream().mapToInt(r -> r.stops.size()).sum();
            String status = String.format("Done! %d vehicles, %d stops assigned.",
                    result.routes.size(), totalStops);
            if (!result.unassigned.isEmpty()) {
                status += String.format(" (%d deliveries could not be assigned)",
                        result.unassigned.size());
            }
            routeStatusLabel.setText(status);
            listTabPane.getSelectionModel().select(ROUTES_TAB_INDEX);
        });

        task.setOnFailed(e -> {
            planRoutesButton.setDisable(false);
            String err = task.getException().getMessage();
            routeStatusLabel.setText("Failed: " + err);
            logger.warning("Route planning failed: " + err);
            webEngine.executeScript("showError('" + escapeJs(err) + "')");
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void drawRoutesOnMap(RouteResult result) {
        JSONArray routesJson = new JSONArray();
        for (RouteResult.VehicleRoute vehicle : result.routes) {
            JSONObject vObj = new JSONObject();
            vObj.put("vehicleId", vehicle.vehicleId);
            JSONArray stopsArr = new JSONArray();
            for (RouteResult.Stop stop : vehicle.stops) {
                JSONObject sObj = new JSONObject();
                sObj.put("address", stop.address);
                sObj.put("lat", stop.lat);
                sObj.put("lon", stop.lon);
                sObj.put("arrivalTime", stop.arrivalTimeFormatted);
                stopsArr.put(sObj);
            }
            vObj.put("stops", stopsArr);
            routesJson.put(vObj);
        }
        String json = escapeJs(routesJson.toString());
        webEngine.executeScript("drawRoutes('" + json + "')");
    }

    private String escapeJs(String s) {
        return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private void configureListTabs() {
        listTabPane.getSelectionModel().selectedIndexProperty().addListener((unused, oldValue, newValue) -> {
            if (newValue == null) return;
            model.setCompanyPackage(newValue.intValue() == 0);
        });
        syncSelectedTabWithMode();
    }

    private void syncSelectedTabWithMode() {
        listTabPane.getSelectionModel().select(model.getCompanyPackage() ? 0 : 1);
    }

    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public CompanyListPanel getCompanyListPanel() {
        return companyListPanel;
    }

    public DeliveryListPanel getDeliveryListPanel() {
        return deliveryListPanel;
    }

    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            if (commandResult.isShowHelp()) handleHelp();
            if (commandResult.isExit()) handleExit();
            syncSelectedTabWithMode();
            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        }
    }
}
