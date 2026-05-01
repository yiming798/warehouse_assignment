package com.warehouse.gui.controller;

import com.warehouse.gui.viewmodel.ItemRowView;
import com.warehouse.gui.service.WarehouseDashboardService;
import com.warehouse.gui.service.WarehouseDashboardService.DashboardSnapshot;
import com.warehouse.manager.ExpiredItemCleaner;
import com.warehouse.manager.Warehouse;
import com.warehouse.models.Item;
import com.warehouse.models.consumable.Drink;
import com.warehouse.models.consumable.Food;
import com.warehouse.models.tool.Toolbox;
import com.warehouse.models.weapon.Bomb;
import com.warehouse.models.weapon.Gun;
import com.warehouse.service.WarehouseService;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainController {
	@FXML
	private TableView<ItemRowView> table;
	@FXML
	private TableColumn<ItemRowView, String> colType;
	@FXML
	private TableColumn<ItemRowView, String> colName;
	@FXML
	private TableColumn<ItemRowView, String> colWeight;
	@FXML
	private TableColumn<ItemRowView, String> colExpiration;
	@FXML
	private TableColumn<ItemRowView, String> colDurability;
	@FXML
	private TableColumn<ItemRowView, String> colBullets;
	@FXML
	private TableColumn<ItemRowView, String> colStatus;
	@FXML
	private TableColumn<ItemRowView, Number> colDurabilityBar;

	@FXML
	private ComboBox<String> typeCombo;
	@FXML
	private TextField nameField;
	@FXML
	private TextField weightField;
	@FXML
	private TextField shelfDaysField;
	@FXML
	private TextField bulletsField;

	@FXML
	private TextField searchField;
	@FXML
	private ComboBox<String> sortFieldCombo;
	@FXML
	private ComboBox<String> sortOrderCombo;
	@FXML
	private Label capacityLabel;
	@FXML
	private ProgressBar capacityBar;
	@FXML
	private Label totalItemsLabel;
	@FXML
	private Label totalWeightLabel;
	@FXML
	private Label expiredItemsLabel;
	@FXML
	private Label lowDurabilityLabel;
	@FXML
	private Label consumableLabel;
	@FXML
	private Label weaponLabel;
	@FXML
	private Label toolboxLabel;
	@FXML
	private TextArea logArea;
	@FXML
	private ComboBox<String> repairToolboxCombo;
	@FXML
	private ComboBox<String> repairGunCombo;
	@FXML
	private PieChart itemTypeChart;
	@FXML
	private BarChart<String, Number> healthChart;

	private final Warehouse warehouse = new Warehouse();
	private final WarehouseService service = new WarehouseService(warehouse);
	private final WarehouseDashboardService dashboardService = new WarehouseDashboardService();
	private final ObservableList<ItemRowView> tableData = FXCollections.observableArrayList();
	private final ObservableList<PieChart.Data> itemTypeData = FXCollections.observableArrayList();
	private final XYChart.Series<String, Number> healthSeries = new XYChart.Series<>();
	private ExpiredItemCleaner cleaner;
	private String currentSearch = "";
	private String currentSortField = "Weight";
	private String currentSortOrder = "Ascending";

	@FXML
	public void initialize() {
		initColumns();
		initSelectors();
		initCharts();
		initializeCleaner();

		try {
			service.loadFromDisk();
		} catch (Exception e) {
			showError("Load Failed", e.getMessage());
		}
		refreshView();
		appendLog("Loaded historical warehouse data.");
	}

	@FXML
	public void onAddClicked() {
		try {
			Item item = buildItemFromForm();
			WarehouseService.AddItemResult result = service.addItemWithFeedback(item);
			refreshView();
			clearAddForm();
			appendLog("Added item: " + item.getName());
			if (result.hasCapacityReplacement()) {
				Item removed = result.removedByCapacity();
				String removalRule = result.evictionReason() == com.warehouse.manager.Warehouse.CapacityEvictionReason.FIRST_USED_OR_CONSUMED_ITEM
						? "Removed the first already consumed/used item in storage order"
						: "Removed the lightest item";
				String message = "Warehouse reached full capacity (10).\n"
						+ removalRule + ": " + removed.getName() + " (" + String.format(Locale.ROOT, "%.2f", removed.getWeight()) + " kg)\n"
						+ "Added: " + result.addedItem().getName() + " (" + String.format(Locale.ROOT, "%.2f", result.addedItem().getWeight()) + " kg)";
				showInfo("Capacity Replacement", message);
				appendLog("Capacity replacement: removed " + removed.getName() + ", added " + result.addedItem().getName());
			}
		} catch (Exception e) {
			showError("Add Item Failed", e.getMessage());
		}
	}

	@FXML
	public void onRemoveClicked() {
		ItemRowView row = table.getSelectionModel().getSelectedItem();
		if (row == null) {
			showError("Remove Failed", "Please select an item first.");
			return;
		}
		boolean removed = service.removeByName(row.getName());
		if (!removed) {
			showError("Remove Failed", "Item not found.");
			return;
		}
		refreshView();
		appendLog("Removed item: " + row.getName());
	}

	@FXML
	public void onSearchClicked() {
		currentSearch = safe(searchField.getText());
		refreshView();
	}

	@FXML
	public void onSortClicked() {
		currentSortField = sortFieldCombo.getValue();
		currentSortOrder = sortOrderCombo.getValue();
		refreshView();
	}

	@FXML
	public void onUseClicked() {
		try {
			ItemRowView row = requireSelection();
			service.useItem(row.getName());
			refreshView();
			appendLog("Used item: " + row.getName());
		} catch (Exception e) {
			showError("Use Failed", e.getMessage());
		}
	}

	@FXML
	public void onEatClicked() {
		try {
			ItemRowView row = requireSelection();
			service.eatFood(row.getName());
			refreshView();
			appendLog("Consumed food: " + row.getName());
		} catch (Exception e) {
			showError("Eat Failed", e.getMessage());
		}
	}

	@FXML
	public void onDrinkClicked() {
		try {
			ItemRowView row = requireSelection();
			service.drink(row.getName());
			refreshView();
			appendLog("Drank item: " + row.getName());
		} catch (Exception e) {
			showError("Drink Failed", e.getMessage());
		}
	}

	@FXML
	public void onRepairClicked() {
		try {
			String toolboxName = repairToolboxCombo.getValue();
			String gunName = repairGunCombo.getValue();
			if (toolboxName == null || gunName == null) {
				throw new IllegalArgumentException("Select both a toolbox and a gun before repairing.");
			}
			service.repair(toolboxName, gunName);
			refreshView();
			appendLog("Repaired gun '" + gunName + "' using toolbox '" + toolboxName + "'.");
		} catch (Exception e) {
			showError("Repair Failed", e.getMessage());
		}
	}

	@FXML
	public void onSaveClicked() {
		try {
			service.saveToDisk();
			appendLog("Saved warehouse data to disk.");
			showInfo("Saved", "Warehouse data has been saved.");
		} catch (Exception e) {
			showError("Save Failed", e.getMessage());
		}
	}

	@FXML
	public void onLoadClicked() {
		try {
			service.loadFromDisk();
			refreshView();
			appendLog("Loaded warehouse data from disk.");
			showInfo("Loaded", "Warehouse data has been loaded.");
		} catch (Exception e) {
			showError("Load Failed", e.getMessage());
		}
	}

	@FXML
	public void onResetFilterClicked() {
		searchField.clear();
		currentSearch = "";
		refreshView();
	}

	private void initColumns() {
		colType.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getType()));
		colName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));
		colWeight.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getWeightDisplay()));
		colExpiration.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getExpiration()));
		colDurability.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDurabilityDisplay()));
		colBullets.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getBulletsDisplay()));
		colStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus()));
		colDurabilityBar.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getDurabilityProgress()));
		colDurabilityBar.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
			private final ProgressBar bar = new ProgressBar();

			@Override
			protected void updateItem(Number item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
					return;
				}
				double progress = item.doubleValue();
				if (progress <= 0) {
					setGraphic(null);
					return;
				}
				bar.setProgress(progress);
				setGraphic(bar);
			}
		});

		table.setItems(tableData);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
		table.setRowFactory(tv -> new TableRow<>() {
			@Override
			protected void updateItem(ItemRowView item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setStyle("");
				} else if ("Expired".equals(item.getStatus())) {
					setStyle("-fx-background-color: #ffe6e6;");
				} else {
					setStyle("");
				}
			}
		});
	}

	private void initSelectors() {
		typeCombo.setItems(FXCollections.observableArrayList("Food", "Drink", "Gun", "Bomb", "Toolbox"));
		typeCombo.getSelectionModel().selectFirst();
		typeCombo.valueProperty().addListener((obs, oldType, newType) -> refreshFormByType(newType));

		sortFieldCombo.setItems(FXCollections.observableArrayList("Weight", "Name", "Type"));
		sortFieldCombo.getSelectionModel().selectFirst();
		currentSortField = sortFieldCombo.getValue();

		sortOrderCombo.setItems(FXCollections.observableArrayList("Ascending", "Descending"));
		sortOrderCombo.getSelectionModel().selectFirst();
		currentSortOrder = sortOrderCombo.getValue();

		repairToolboxCombo.setConverter(new StringConverter<>() {
			@Override
			public String toString(String value) {
				return value == null ? "" : value;
			}

			@Override
			public String fromString(String string) {
				return string;
			}
		});
		refreshFormByType(typeCombo.getValue());
	}

	private void initCharts() {
		if (itemTypeChart != null) {
			itemTypeChart.setData(itemTypeData);
		}
		if (healthChart != null) {
			healthSeries.setName("Status Overview");
			healthChart.getData().setAll(healthSeries);
		}
	}

	private void initializeCleaner() {
		cleaner = new ExpiredItemCleaner(warehouse, removedItems -> Platform.runLater(() -> {
			refreshView();
			appendLog("Auto-clean removed " + removedItems.size() + " expired item(s).");
		}));
		cleaner.start();
	}

	private Item buildItemFromForm() {
		String type = safe(typeCombo.getValue());
		String name = safe(nameField.getText());
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be empty.");
		}
		double weight;
		try {
			weight = Double.parseDouble(safe(weightField.getText()));
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Weight must be a valid number.");
		}
		if (weight <= 0) {
			throw new IllegalArgumentException("Weight must be greater than 0.");
		}

		return switch (type) {
			case "Food" -> new Food(name, weight, LocalDate.now().plusDays(parsePositiveInt(shelfDaysField.getText(), "shelf life days")));
			case "Drink" -> new Drink(name, weight, LocalDate.now().plusDays(parsePositiveInt(shelfDaysField.getText(), "shelf life days")));
			case "Gun" -> new Gun(name, weight, parseNonNegativeInt(bulletsField.getText(), "bullets"));
			case "Bomb" -> new Bomb(name, weight);
			case "Toolbox" -> new Toolbox(name, weight);
			default -> throw new IllegalArgumentException("Unknown item type: " + type);
		};
	}

	private int parsePositiveInt(String text, String fieldName) {
		int value;
		try {
			value = Integer.parseInt(safe(text));
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException(fieldName + " must be an integer.");
		}
		if (value <= 0) {
			throw new IllegalArgumentException(fieldName + " must be greater than 0.");
		}
		return value;
	}

	private int parseNonNegativeInt(String text, String fieldName) {
		int value;
		try {
			value = Integer.parseInt(safe(text));
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException(fieldName + " must be an integer.");
		}
		if (value < 0) {
			throw new IllegalArgumentException(fieldName + " cannot be negative.");
		}
		return value;
	}

	private ItemRowView requireSelection() {
		ItemRowView row = table.getSelectionModel().getSelectedItem();
		if (row == null) {
			throw new IllegalArgumentException("Please select one item first.");
		}
		return row;
	}

	private void refreshView() {
		List<Item> allItems = new ArrayList<>(service.getAllItems());
		List<Item> sorted = dashboardService.filterAndSort(allItems, currentSearch, currentSortField, currentSortOrder);
		DashboardSnapshot snapshot = dashboardService.snapshot(allItems);

		tableData.setAll(sorted.stream().map(ItemRowView::new).toList());
		updateCapacity(allItems);
		updateAnalytics(snapshot);
		refreshRepairCombos(snapshot.toolboxNames(), snapshot.gunNames());
	}

	private void updateCapacity(List<Item> items) {
		int usage = items.size();
		int max = service.getMaxCapacity();
		double progress = max <= 0 ? 0 : Math.min(1.0, usage / (double) max);
		if (capacityLabel != null) {
			capacityLabel.setText("Capacity: " + usage + "/" + max);
		}
		if (capacityBar != null) {
			capacityBar.setProgress(progress);
		}
	}

	private void updateAnalytics(DashboardSnapshot snapshot) {
		if (totalItemsLabel == null) {
			return;
		}

		totalItemsLabel.setText(String.valueOf(snapshot.totalItems()));
		totalWeightLabel.setText(String.format(Locale.ROOT, "%.2f kg", snapshot.totalWeight()));
		expiredItemsLabel.setText(String.valueOf(snapshot.expiredItems()));
		lowDurabilityLabel.setText(String.valueOf(snapshot.lowDurabilityItems()));
		consumableLabel.setText(String.valueOf(snapshot.consumableItems()));
		weaponLabel.setText(String.valueOf(snapshot.weaponItems()));
		toolboxLabel.setText(String.valueOf(snapshot.toolboxItems()));

		if (itemTypeChart != null) {
			itemTypeData.setAll(
					new PieChart.Data("Food", snapshot.foodItems()),
					new PieChart.Data("Drink", snapshot.drinkItems()),
					new PieChart.Data("Gun", snapshot.gunItems()),
					new PieChart.Data("Bomb", snapshot.bombItems()),
					new PieChart.Data("Toolbox", snapshot.toolboxItems())
			);
			applyPieSliceColors();
		}

		if (healthChart != null) {
			healthSeries.getData().setAll(
					new XYChart.Data<>("Expired", snapshot.expiredItems()),
					new XYChart.Data<>("Low Durability", snapshot.lowDurabilityItems()),
					new XYChart.Data<>("Consumables", snapshot.consumableItems()),
					new XYChart.Data<>("Weapons", snapshot.weaponItems())
			);
		}
	}

	private void applyPieSliceColors() {
		for (PieChart.Data data : itemTypeData) {
			String style = "-fx-pie-color: " + pieColorByType(data.getName()) + ";";
			if (data.getNode() != null) {
				data.getNode().setStyle(style);
			}
			data.nodeProperty().addListener((obs, oldNode, newNode) -> {
				if (newNode != null) {
					newNode.setStyle(style);
				}
			});
		}
	}

	private String pieColorByType(String itemType) {
		if ("Food".equals(itemType)) {
			return "#e76f51";
		}
		if ("Drink".equals(itemType)) {
			return "#f4a62a";
		}
		if ("Gun".equals(itemType)) {
			return "#4caf50";
		}
		if ("Bomb".equals(itemType)) {
			return "#3ba2c7";
		}
		if ("Toolbox".equals(itemType)) {
			return "#7a56d8";
		}
		return "#94a3b8";
	}

	private void refreshRepairCombos(List<String> toolboxes, List<String> guns) {

		repairToolboxCombo.setItems(FXCollections.observableArrayList(toolboxes));
		repairGunCombo.setItems(FXCollections.observableArrayList(guns));
		if (!toolboxes.isEmpty()) {
			repairToolboxCombo.getSelectionModel().selectFirst();
		} else {
			repairToolboxCombo.getSelectionModel().clearSelection();
		}
		if (!guns.isEmpty()) {
			repairGunCombo.getSelectionModel().selectFirst();
		} else {
			repairGunCombo.getSelectionModel().clearSelection();
		}
	}

	private void refreshFormByType(String type) {
		boolean foodOrDrink = "Food".equals(type) || "Drink".equals(type);
		boolean gun = "Gun".equals(type);
		shelfDaysField.setDisable(!foodOrDrink);
		bulletsField.setDisable(!gun);
		if (!foodOrDrink) {
			shelfDaysField.clear();
		}
		if (!gun) {
			bulletsField.clear();
		}
	}

	private void clearAddForm() {
		nameField.clear();
		weightField.clear();
		if (!shelfDaysField.isDisable()) {
			shelfDaysField.clear();
		}
		if (!bulletsField.isDisable()) {
			bulletsField.clear();
		}
	}

	private void appendLog(String line) {
		if (logArea == null) {
			return;
		}
		if (!logArea.getText().isEmpty()) {
			logArea.appendText(System.lineSeparator());
		}
		logArea.appendText(line);
	}

	private String safe(String text) {
		return text == null ? "" : text.trim();
	}

	private void showError(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.showAndWait();
	}

	private void showInfo(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.showAndWait();
	}

	public void shutdown() {
		if (cleaner != null) {
			cleaner.stop();
		}
		service.saveToDisk();
		System.out.println("The program has exited and files are saved!");
	}
}
