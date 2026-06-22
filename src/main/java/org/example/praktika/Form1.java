package org.example.praktika;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

public class Form1 {

    private Timeline priceFilterTimer = null;

    private final ProductDAO productDAO = new ProductDAO();

    @FXML private ComboBox<String> sortBox;
    @FXML private Label adminPanelBtn;
    @FXML private Node filterBtn;
    @FXML private ImageView accImage;
    @FXML private ImageView cartImage;

    @FXML private FlowPane productCardsPane;
    @FXML private VBox mainContent;
    @FXML private BorderPane rootContainer;
    @FXML private TextField searchField;


    @FXML private HBox paginationContainer;
    private final int ITEMS_PER_PAGE = 20;
    private int currentPage = 1;
    private List<Product> currentFilteredProducts = new ArrayList<>();

    private Timeline debounceTimer = null;

    private String currentUserRole;
    private String currentUserLogin;
    private int currentUserId = 0;
    private VBox searchContent = new VBox();

    public int getCurrentUserId() { return currentUserId; }
    public String getCurrentUserLogin() { return currentUserLogin; }

    public void setCurrentUser(int id, String login, String role) {
        this.currentUserId = id;
        this.currentUserLogin = login;
        this.currentUserRole = role;

        if (adminPanelBtn != null) {
            adminPanelBtn.setVisible("admin".equals(role));
        }
    }

    @FXML private VBox filterSidebar;
    @FXML private Slider priceSlider;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<String> genderFilter;
    @FXML private ComboBox<String> colorFilter;
    @FXML private ComboBox<String> seasonFilter;
    @FXML private ComboBox<String> materialFilter;
    @FXML private ComboBox<String> categoryFilter;
    private List<Product> cachedProducts = new ArrayList<>();


    @FXML
    public void initialize() {
        createProductCards();
        loadAccImage();
        loadCartImage();
        genderFilter.getItems().addAll("Мужская", "Женская", "Детская");


        minPriceField.textProperty().addListener((obs, old, val) -> setupPriceDebounce());
        maxPriceField.textProperty().addListener((obs, old, val) -> setupPriceDebounce());


        genderFilter.valueProperty().addListener(e -> applyFilters());
        colorFilter.valueProperty().addListener(e -> applyFilters());
        seasonFilter.valueProperty().addListener(e -> applyFilters());
        materialFilter.valueProperty().addListener(e -> applyFilters());
        categoryFilter.valueProperty().addListener(e -> applyFilters());

        categoryFilter.valueProperty().addListener(e -> applyFilters());

        sortBox.getItems().addAll("По умолчанию", "Цена: по возрастанию", "Цена: по убыванию");
        sortBox.setValue("По умолчанию");
        sortBox.valueProperty().addListener((obs, old, val) -> applySorting());


        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (debounceTimer != null) {
                debounceTimer.stop();
            }
            if (newValue == null || newValue.trim().isEmpty()) {
                handleSearchInput("");
                return;
            }
            debounceTimer = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                handleSearchInput(newValue);
            }));
            debounceTimer.play();
        });
    }

    private void setupPriceDebounce() {
        if (priceFilterTimer != null) priceFilterTimer.stop();

        priceFilterTimer = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            applyFilters();
        }));
        priceFilterTimer.play();
    }

    private void renderMainProducts() {
        productCardsPane.getChildren().clear();

        if (currentFilteredProducts.isEmpty()) {
            renderPaginationControls(0);
            return;
        }

        int totalItems = currentFilteredProducts.size();
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);


        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;


        int fromIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, totalItems);


        List<Product> pageSlice = currentFilteredProducts.subList(fromIndex, toIndex);

        for (Product p : pageSlice) {
            productCardsPane.getChildren().add(createProductCard(p));
        }

        renderPaginationControls(totalPages);
    }


    private void renderPaginationControls(int totalPages) {
        paginationContainer.getChildren().clear();

        if (totalPages <= 1) return;

        Button prevBtn = new Button("←");
        prevBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: black; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 15;");
        if (currentPage == 1) {
            prevBtn.setDisable(true);
            prevBtn.setStyle(prevBtn.getStyle() + " -fx-opacity: 0.3;");
        } else {
            prevBtn.setOnAction(e -> {
                currentPage--;
                renderMainProducts();
            });
        }
        paginationContainer.getChildren().add(prevBtn);


        for (int i = 1; i <= totalPages; i++) {
            int pageNum = i;
            Button pageBtn = new Button(String.valueOf(pageNum));

            if (pageNum == currentPage) {

                pageBtn.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 15;");
            } else {
                pageBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: black; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 15;");
                pageBtn.setOnAction(e -> {
                    currentPage = pageNum;
                    renderMainProducts();
                });
            }
            paginationContainer.getChildren().add(pageBtn);
        }


        Button nextBtn = new Button("→");
        nextBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: black; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 15;");
        if (currentPage == totalPages) {
            nextBtn.setDisable(true);
            nextBtn.setStyle(nextBtn.getStyle() + " -fx-opacity: 0.3;");
        } else {
            nextBtn.setOnAction(e -> {
                currentPage++;
                renderMainProducts();
            });
        }
        paginationContainer.getChildren().add(nextBtn);
    }

    public StackPane createProductCard(Product product) {
        StackPane card = new StackPane();
        card.setPrefSize(220, 300);
        card.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #f0f0f0; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4);"
        );


        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace("0.08", "0.15") + "-fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("0.15", "0.08")));

        VBox content = new VBox();
        content.setSpacing(6);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.TOP_LEFT);


        ImageView cover = new ImageView();
        cover.setFitWidth(190);
        cover.setFitHeight(190);
        cover.setPreserveRatio(true);


        StackPane imageContainer = new StackPane(cover);
        imageContainer.setPrefHeight(190);

        String imageUrl = (product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : null;
        try {
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                cover.setImage(new Image(imageUrl, 190, 190, true, true, true));
            } else {
                cover.setImage(new Image("https://via.placeholder.com/190x190/f5f5f5/cccccc?text=Нет+фото", true));
            }
        } catch (Exception e) {
            cover.setImage(new Image("https://via.placeholder.com/190x190/f5f5f5/cccccc?text=Error", true));
        }


        Text titleText = new Text(product.getName() != null ? product.getName() : "Без названия");
        titleText.setWrappingWidth(190);

        titleText.setStyle("-fx-font-size: 16px; -fx-fill: #212121; -fx-font-weight: bold;");


        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);


        HBox priceBox = new HBox(8);
        priceBox.setAlignment(Pos.BOTTOM_LEFT);

        if (product.getOldPrice() != null && product.getOldPrice() > product.getPrice()) {
            Text newPriceText = new Text(String.format("%.0f ₽", product.getPrice()));
            newPriceText.setStyle("-fx-font-size: 18px; -fx-fill: #e53935; -fx-font-weight: bold;");

            Text oldPriceText = new Text(String.format("%.0f ₽", product.getOldPrice()));
            oldPriceText.setStyle("-fx-font-size: 13px; -fx-fill: #9e9e9e;");
            oldPriceText.setStrikethrough(true);

            priceBox.getChildren().addAll(newPriceText, oldPriceText);
        } else {
            Text regularPriceText = new Text(String.format("%.0f ₽", product.getPrice()));
            regularPriceText.setStyle("-fx-font-size: 18px; -fx-fill: #424242; -fx-font-weight: bold;");
            priceBox.getChildren().add(regularPriceText);
        }


        Text brandText = new Text((product.getBrand() != null ? product.getBrand() : "") + " • " + (product.getSeason() != null ? product.getSeason() : ""));
        brandText.setStyle("-fx-font-size: 12px; -fx-fill: #757575;");

        content.getChildren().addAll(imageContainer, titleText, spacer, priceBox, brandText);
        card.getChildren().add(content);
        card.setOnMouseClicked(event -> openProductDetail(product));

        return card;
    }

    public void openProductDetail(Product product) {
        hideFilters();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("product_detail.fxml"));
            Parent detailView = loader.load();

            ProductDetailController controller = loader.getController();
            controller.setMainController(this);
            controller.setCurrentUserId(this.currentUserId);
            controller.setProduct(product);

            rootContainer.setCenter(detailView);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть карточку товара: " + product.getName());
        }
    }

    public void showMainView() {
        rootContainer.setCenter(mainContent);
        if (filterBtn != null) {
            filterBtn.setVisible(true);
            filterBtn.setManaged(true);
        }
    }

    @FXML
    private void logout() {
        Stage stage = (Stage) rootContainer.getScene().getWindow();
        stage.close();
    }

    public void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    public void openAdminPanel() {

        if (!"admin".equals(currentUserRole)) {
            showError("У вас нет прав для доступа к админ-панели.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminPanel.fxml"));
            Parent root = loader.load();
            AdminPanelController adminController = loader.getController();
            adminController.setMainController(this);
            Stage stage = new Stage();
            stage.setTitle("Админ-панель");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть админ-панель.");
        }
    }

    @FXML
    public void openAccount() {
        hideFilters();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Acc.fxml"));
            Parent content = loader.load();
            AccController controller = loader.getController();
            if (controller != null) {
                controller.setMainController(this);
            }
            rootContainer.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть «Аккаунт».");
        }
    }

    @FXML
    private void loadAccImage() {
        try {
            Image defaultAvatar = new Image(getClass().getResourceAsStream("account.png"));
            accImage.setImage(defaultAvatar);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображения аккаунта " + e.getMessage());
        }
    }

    @FXML
    private void loadCartImage() {
        try {
            Image defaultAvatar = new Image(getClass().getResourceAsStream("cart.png"));
            cartImage.setImage(defaultAvatar);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображения избранное: " + e.getMessage());
        }
    }

    @FXML
    private void openCart() {
        hideFilters();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("cart.fxml"));
            Parent cartView = loader.load();
            CartController controller = loader.getController();
            long userId = this.getCurrentUserId();
            controller.initCart(userId, this);
            rootContainer.setCenter(cartView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Ошибка при загрузке корзины: " + e.getMessage());
        }
    }

    private void openSearchViewPending() {
        searchContent.getChildren().clear();
        searchContent.setSpacing(24);
        searchContent.setPadding(new Insets(20));

        Label title = new Label("Поиск товаров...");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setPrefSize(50, 50);

        searchContent.getChildren().addAll(title, indicator);
        rootContainer.setCenter(searchContent);
    }

    private void openSearchView(String query, List<Product> allProducts) {
        hideFilters();
        String q = query.toLowerCase().trim();

        List<Product> filteredProducts = allProducts.stream()
                .filter(product -> {
                    if (product == null) return false;
                    boolean matchName = product.getName() != null && product.getName().toLowerCase().contains(q);
                    boolean matchBrand = product.getBrand() != null && product.getBrand().toLowerCase().contains(q);
                    return matchName || matchBrand;
                })
                .collect(Collectors.toList());

        searchContent.getChildren().clear();
        searchContent.setSpacing(24);
        searchContent.setPadding(new Insets(20));

        Label title = new Label("Результаты поиска: \"" + query + "\"");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: black;");

        FlowPane resultPane = new FlowPane();
        resultPane.setHgap(20);
        resultPane.setVgap(20);
        resultPane.setPrefWrapLength(1000);

        if (!filteredProducts.isEmpty()) {
            for (Product product : filteredProducts) {
                resultPane.getChildren().add(createProductCard(product));
            }
        } else {
            Label notFound = new Label("По вашему запросу товаров не найдено.");
            notFound.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");
            resultPane.getChildren().add(notFound);
        }

        searchContent.getChildren().addAll(title, resultPane);
        rootContainer.setCenter(searchContent);
    }

    private CompletableFuture<List<Product>> currentSearchFuture;

    private void handleSearchInput(String query) {
        if (query == null || query.trim().isEmpty()) {
            showMainView();
            return;
        }

        if (currentSearchFuture != null && !currentSearchFuture.isDone()) {
            currentSearchFuture.cancel(true);
        }

        currentSearchFuture = productDAO.searchProducts(query);
        openSearchViewPending();

        currentSearchFuture.thenAccept(results -> {
            Platform.runLater(() -> openSearchView(query, results));
        }).exceptionally(ex -> {
            if (ex.getCause() instanceof CancellationException) return null;
            ex.printStackTrace();
            Platform.runLater(() -> showError("Ошибка при поиске товаров."));
            return null;
        });
    }

    public void createProductCards() {
        if (productCardsPane == null) return;

        productCardsPane.getChildren().clear();
        productCardsPane.getChildren().add(new ProgressIndicator());

        productDAO.getAllProducts()
                .thenAccept(allProducts -> Platform.runLater(() -> {
                    this.cachedProducts = allProducts;

                    Set<String> colors = allProducts.stream()
                            .flatMap(p -> p.getVariants() != null ? p.getVariants().stream() : null)
                            .filter(Objects::nonNull)
                            .map(ProductVariant::getColor)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    colorFilter.getItems().setAll(colors);

                    Set<String> seasons = allProducts.stream()
                            .map(Product::getSeason)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toSet());
                    seasonFilter.getItems().setAll(seasons);

                    Set<String> materials = allProducts.stream()
                            .map(Product::getMaterial)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(m -> !m.isEmpty())
                            .collect(Collectors.toSet());
                    materialFilter.getItems().setAll(materials);


                    Set<String> categories = allProducts.stream()
                            .map(Product::getCategory)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toSet());
                    categoryFilter.getItems().setAll(categories);


                    this.currentFilteredProducts = new ArrayList<>(allProducts);
                    this.currentPage = 1;
                    renderMainProducts();
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        productCardsPane.getChildren().clear();
                        productCardsPane.getChildren().add(new Label("Ошибка загрузки товаров."));
                        showError("Не удалось загрузить товары.");
                    });
                    return null;
                });
    }

    private void applyFilters() {
        List<Product> filtered = cachedProducts.stream().filter(p -> {

            double min = 0;
            double max = Double.MAX_VALUE;

            try {
                if (!minPriceField.getText().trim().isEmpty())
                    min = Double.parseDouble(minPriceField.getText().trim());
                if (!maxPriceField.getText().trim().isEmpty())
                    max = Double.parseDouble(maxPriceField.getText().trim());
            } catch (NumberFormatException e) {

            }
            boolean priceMatch = p.getPrice() >= min && p.getPrice() <= max;

            boolean genderMatch = genderFilter.getValue() == null ||
                    (p.getGender() != null && p.getGender().equalsIgnoreCase(genderFilter.getValue()));

            boolean colorMatch = colorFilter.getValue() == null ||
                    (p.getVariants() != null && p.getVariants().stream()
                            .anyMatch(v -> v.getColor() != null && v.getColor().equalsIgnoreCase(colorFilter.getValue())));

            boolean seasonMatch = seasonFilter.getValue() == null ||
                    (p.getSeason() != null && p.getSeason().equalsIgnoreCase(seasonFilter.getValue()));

            boolean materialMatch = materialFilter.getValue() == null ||
                    (p.getMaterial() != null && p.getMaterial().equalsIgnoreCase(materialFilter.getValue()));

            boolean categoryMatch = categoryFilter.getValue() == null ||
                    (p.getCategory() != null && p.getCategory().equalsIgnoreCase(categoryFilter.getValue()));

            return priceMatch && genderMatch && colorMatch && seasonMatch && materialMatch && categoryMatch;
        }).collect(Collectors.toList());


        this.currentFilteredProducts = filtered;
        this.currentPage = 1;
        renderMainProducts();
        applySorting();
    }

    @FXML
    private void resetFilters() {
        genderFilter.setValue(null);
        colorFilter.setValue(null);
        seasonFilter.setValue(null);
        materialFilter.setValue(null);
        categoryFilter.setValue(null);
        minPriceField.clear();
        maxPriceField.clear();


        this.currentFilteredProducts = new ArrayList<>(cachedProducts);
        this.currentPage = 1;
        renderMainProducts();
    }

    @FXML
    private void toggleFilters() {
        boolean isNowVisible = !filterSidebar.isVisible();
        filterSidebar.setVisible(isNowVisible);
        filterSidebar.setManaged(isNowVisible);
    }

    private void hideFilters() {
        filterSidebar.setVisible(false);
        filterSidebar.setManaged(false);

        if (filterBtn != null) {
            filterBtn.setVisible(false);
            filterBtn.setManaged(false);
        }
    }

    private void applySorting() {
        String sortType = sortBox.getValue();
        if (sortType == null || sortType.equals("По умолчанию")) {
            renderMainProducts();
            return;
        }

        if (sortType.equals("Цена: по возрастанию")) {
            currentFilteredProducts.sort(Comparator.comparingDouble(Product::getPrice));
        } else if (sortType.equals("Цена: по убыванию")) {
            currentFilteredProducts.sort(Comparator.comparingDouble(Product::getPrice).reversed());
        }

        currentPage = 1;
        renderMainProducts();
    }
}