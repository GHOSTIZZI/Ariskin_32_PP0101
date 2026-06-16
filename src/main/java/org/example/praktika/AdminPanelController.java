package org.example.praktika;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminPanelController {


    @FXML private Label ordersCountLabel;
    @FXML private Label avgCheckLabel;
    @FXML private BarChart<String, Integer> salesChart;

    @FXML private Label revenueLabel;
    @FXML private TableView<SalesStat> analyticsTable;
    @FXML private TableColumn<SalesStat, String> prodNameCol;
    @FXML private TableColumn<SalesStat, Integer> prodCountCol;

    @FXML private TableColumn<ProductVariant, String> colorCol;
    @FXML private TableColumn<ProductVariant, String> sizeCol;
    @FXML private TableColumn<ProductVariant, Integer> stockCol;

    //Таблица товаров
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idCol;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, Double> priceCol;
    @FXML private TableColumn  <Product, String> brandCol;
    @FXML private TableColumn <Product, String> materialCol;
    @FXML private TableColumn <Product, String> seasonCol;
    @FXML private TableColumn <Product, String> genderCol;
    @FXML private TableColumn <Product, String> categoryCol;
    @FXML private TableColumn <Product, String> imagesCol;
    @FXML private TableColumn <Product, String> descCol;

    //Таблица пользователей
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TableColumn<User, String> userLoginCol;
    @FXML private TableColumn<User, String> userRoleCol;

    //Таблица вариаций
    @FXML private TableView<ProductVariant> variantsTable;

    //Таблица заказов
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, Integer> orderIdCol;
    @FXML private TableColumn<Order, Long> orderUserIdCol;
    @FXML private TableColumn<Order, Integer> orderQuantityCol;
    @FXML private TableColumn<Order, Double> orderPriceCol;
    @FXML private TableColumn<Order, String> orderDateCol;
    @FXML private TableColumn<Order, String> emailCol;
    @FXML private TableColumn<Order, String> deliveryAddressColl;

    //Поля ввода
    @FXML private TextField nameField, priceField, oldPriceField, brandField, materialField, seasonField, genderField, categoryField;
    @FXML private TextArea descField;
    @FXML private TextField imagesField;
    @FXML private TextField colorField, sizeField, stockField;

    private final ProductDAO productDAO = new ProductDAO();
    private final UserDAO userDAO = new UserDAO();
    private final VariantDAO variantDAO = new VariantDAO();

    private Form1 mainController;

    public void setMainController(Form1 controller) {
        this.mainController = controller;
        initTables();
        loadData();
    }

    @FXML
    public void initialize() {
        salesChart.setAnimated(false);
        xAxis.setAnimated(true);
    }
    private void initTables() {
        // 1. Аналитика (Топ товаров)
        prodNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        prodCountCol.setCellValueFactory(new PropertyValueFactory<>("count"));
        prodNameCol.setSortable(true);
        prodCountCol.setSortable(true);


        // 2. Вариации
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colorCol.setSortable(true);
        sizeCol.setSortable(true);
        stockCol.setSortable(true);

        // 3. Товары
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        materialCol.setCellValueFactory(new PropertyValueFactory<>("material"));
        seasonCol.setCellValueFactory(new PropertyValueFactory<>("season"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        idCol.setSortable(true);
        nameCol.setSortable(true);
        priceCol.setSortable(true);
        brandCol.setSortable(true);
        materialCol.setSortable(true);
        seasonCol.setSortable(true);
        genderCol.setSortable(true);
        categoryCol.setSortable(true);

        // 4. Пользователи
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userLoginCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        userRoleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        userIdCol.setSortable(true);
        userLoginCol.setSortable(true);
        userRoleCol.setSortable(true);

        // 5. Заказы
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderUserIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        orderQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        orderPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        deliveryAddressColl.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        orderIdCol.setSortable(true);
        orderUserIdCol.setSortable(true);
        orderQuantityCol.setSortable(true);
        orderPriceCol.setSortable(true);
        orderDateCol.setSortable(true);
        emailCol.setSortable(true);
        deliveryAddressColl.setSortable(true);


        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                nameField.setText(newSel.getName() != null ? newSel.getName() : "");
                priceField.setText(String.valueOf(newSel.getPrice()));
                oldPriceField.setText(newSel.getOldPrice() != null ? String.valueOf(newSel.getOldPrice()) : "");
                brandField.setText(newSel.getBrand() != null ? newSel.getBrand() : "");
                materialField.setText(newSel.getMaterial() != null ? newSel.getMaterial() : "");
                seasonField.setText(newSel.getSeason() != null ? newSel.getSeason() : "");
                genderField.setText(newSel.getGender() != null ? newSel.getGender() : "");
                categoryField.setText(newSel.getCategory() != null ? newSel.getCategory() : "");
                descField.setText(newSel.getDescription() != null ? newSel.getDescription() : "");

                if (newSel.getImages() != null) {
                    imagesField.setText(String.join(",", newSel.getImages()));
                } else {
                    imagesField.clear();
                }

                clearVariantFields();
                loadVariantsForProduct(newSel.getId());
            }
        });

        variantsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                colorField.setText(newSel.getColor() != null ? newSel.getColor() : "");
                sizeField.setText(newSel.getSize() != null ? newSel.getSize() : "");
                stockField.setText(String.valueOf(newSel.getStock()));
            }
        });
    }

    private void loadData() {
        productDAO.getAllProducts().thenAccept(list ->
                Platform.runLater(() -> {
                    productTable.setItems(FXCollections.observableArrayList(list));
                    if (mainController != null) {
                        mainController.createProductCards();
                    }
                }));

        userDAO.getAllUsers().thenAccept(list ->
                Platform.runLater(() -> userTable.setItems(FXCollections.observableArrayList(list))));

        productDAO.getAllOrders().thenAccept(list ->
                Platform.runLater(() -> orderTable.setItems(FXCollections.observableArrayList(list))));
    }

    @FXML
    private void handleAddProduct() {
        Product p = createProductFromFields();
        setupProductImages(p);

        productDAO.addProduct(p).thenAccept(success -> {
            if (success) {
                Platform.runLater(() -> {
                    loadData();
                    clearFields();
                    if (mainController != null) {
                        mainController.showAlert("Успех", "Новый товар успешно добавлен!");
                    }
                });
            } else {
                Platform.runLater(() -> {
                    if (mainController != null) mainController.showError("Не удалось добавить товар.");
                });
            }
        });
    }

    @FXML
    private void handleUpdateProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (mainController != null) mainController.showError("Выберите товар в таблице слева!");
            return;
        }

        Product p = createProductFromFields();
        setupProductImages(p);

        productDAO.updateProduct(selected.getId(), p).thenAccept(success -> {
            if (success) {
                Platform.runLater(() -> {
                    selected.setName(p.getName());
                    selected.setPrice(p.getPrice());
                    selected.setOldPrice(p.getOldPrice());
                    selected.setBrand(p.getBrand());
                    selected.setMaterial(p.getMaterial());
                    selected.setSeason(p.getSeason());
                    selected.setGender(p.getGender());
                    selected.setCategory(p.getCategory());
                    selected.setDescription(p.getDescription());
                    selected.setImages(p.getImages());
                    productTable.refresh();
                    if (mainController != null) mainController.showAlert("Успех", "Данные товара обновлены!");
                });
            } else {
                Platform.runLater(() -> {
                    if (mainController != null) mainController.showError("Ошибка сохранения изменений.");
                });
            }
        });
    }


    @FXML
    private void handleAddVariant() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) return;

        try {
            String color = colorField.getText().trim();
            String size = sizeField.getText().trim();
            int quantityToAdd = Integer.parseInt(stockField.getText().trim());

            if (color.isEmpty() || size.isEmpty()) return;

            productDAO.addOrUpdateVariantStock(selectedProduct.getId(), size, color, quantityToAdd)
                    .thenAccept(success -> {
                        if (success) {
                            Platform.runLater(() -> {
                                loadVariantsForProduct(selectedProduct.getId());
                                clearVariantFields();
                            });
                        }
                    });
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат числа в поле 'Склад'");
        }
    }


    @FXML
    private void handleUpdateVariant() {
        ProductVariant selectedVariant = variantsTable.getSelectionModel().getSelectedItem();
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedVariant == null || selectedProduct == null) {
            if (mainController != null) mainController.showError("Выберите размер в таблице вариаций для изменения!");
            return;
        }

        try {
            int newStock = Integer.parseInt(stockField.getText().trim());

            productDAO.updateVariantStock(selectedVariant.getId(), newStock)
                    .thenAccept(success -> {
                        if (success) {
                            Platform.runLater(() -> {
                                loadVariantsForProduct(selectedProduct.getId());
                                clearVariantFields();
                            });
                        }
                    });
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат числа");
        }
    }


    @FXML
    private void handleDeleteVariant() {
        ProductVariant selectedVariant = variantsTable.getSelectionModel().getSelectedItem();
        Product currentProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedVariant != null && currentProduct != null) {

            productDAO.updateVariantStock(selectedVariant.getId(), 0)
                    .thenAccept(success -> {
                        if (success) {
                            Platform.runLater(() -> {
                                loadVariantsForProduct(currentProduct.getId());
                                clearVariantFields();
                            });
                        }
                    });
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            productDAO.deleteProduct(selected.getId()).thenAccept(s -> Platform.runLater(this::loadData));
        }
    }

    private void loadVariantsForProduct(int productId) {
        variantDAO.getVariantsByProductId(productId).thenAccept(list ->
                Platform.runLater(() -> variantsTable.setItems(FXCollections.observableArrayList(list))));
    }

    private void setupProductImages(Product p) {
        String imagesText = imagesField.getText();
        if (imagesText != null && !imagesText.trim().isEmpty()) {
            p.setImages(Arrays.stream(imagesText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        } else {
            p.setImages(new ArrayList<>());
        }
    }

    private Product createProductFromFields() {
        Product p = new Product();
        p.setName(nameField.getText());


        try {
            String priceText = priceField.getText().replace(",", ".").trim();
            p.setPrice(priceText.isEmpty() ? 0.0 : Double.parseDouble(priceText));
        } catch (NumberFormatException e) {
            if (mainController != null) mainController.showError("Ошибка в поле 'Цена': введите число!");
            p.setPrice(0.0);
        }


        try {
            String oldPriceText = oldPriceField.getText().replace(",", ".").trim();
            if (oldPriceText.isEmpty()) {
                p.setOldPrice(0.0);
            } else {
                p.setOldPrice(Double.parseDouble(oldPriceText));
            }
        } catch (NumberFormatException e) {

            if (mainController != null) mainController.showError("Ошибка в поле 'Старая цена': введите корректное число!");
            p.setOldPrice(0.0);
        }

        p.setBrand(brandField.getText());
        p.setMaterial(materialField.getText());
        p.setSeason(seasonField.getText());
        p.setGender(genderField.getText());
        p.setCategory(categoryField.getText());
        p.setDescription(descField.getText());
        return p;
    }

    @FXML
    private void clearFields() {
        nameField.clear();
        priceField.clear();
        oldPriceField.clear();
        brandField.clear();
        materialField.clear();
        seasonField.clear();
        genderField.clear();
        categoryField.clear();
        descField.clear();
        imagesField.clear();
        productTable.getSelectionModel().clearSelection();
        clearVariantFields();
    }

    private void clearVariantFields() {
        colorField.clear();
        sizeField.clear();
        stockField.clear();
        variantsTable.getSelectionModel().clearSelection();
    }



    @FXML private CategoryAxis xAxis;
    @FXML
    private void refreshAnalytics() {
        List<Order> allOrders = orderTable.getItems();
        if (allOrders == null || allOrders.isEmpty()) return;


        int totalOrders = allOrders.size();
        double totalRevenue = allOrders.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();
        double avgCheck = totalOrders > 0 ? totalRevenue / totalOrders : 0;


        ordersCountLabel.setText(String.valueOf(totalOrders));
        revenueLabel.setText(String.format("%.2f руб.", totalRevenue));
        avgCheckLabel.setText(String.format("%.2f руб.", avgCheck));


        Map<Product, Integer> statsMap = allOrders.stream()
                .filter(o -> o.getVariant() != null && o.getVariant().getProduct() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getVariant().getProduct(),
                        Collectors.summingInt(Order::getQuantity)
                ));

        List<Map.Entry<Product, Integer>> top10 = statsMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .collect(Collectors.toList());


        analyticsTable.setItems(FXCollections.observableArrayList(
                top10.stream().map(e -> new SalesStat(e.getKey().getName(), e.getValue())).collect(Collectors.toList())
        ));


        salesChart.getData().clear();
        xAxis.getCategories().clear();

        List<String> categories = top10.stream()
                .map(e -> e.getKey().getName())
                .collect(Collectors.toList());

        xAxis.setCategories(FXCollections.observableArrayList(categories));

        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("Продажи");

        for (Map.Entry<Product, Integer> entry : top10) {
            series.getData().add(new XYChart.Data<>(entry.getKey().getName(), entry.getValue()));
        }

        salesChart.getData().add(series);
    }

}