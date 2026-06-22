package org.example.praktika;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CartController {
    private Form1 mainController;

    @FXML private VBox cartItemsContainer;
    @FXML private ScrollPane cartScrollPane;
    @FXML private VBox checkoutFormPane;
    @FXML private Label totalLabel;
    @FXML private Label itemsCountLabel;
    @FXML private Button checkoutActionButton;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> paymentMethodComboBox;

    private final CartDAO cartDAO = new CartDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ObservableList<CartItem> cartData = FXCollections.observableArrayList();
    private long currentUserId = -1;
    public void setCurrentUserId(long id) {
        this.currentUserId = id;
    }

    @FXML
    public void initialize() {
        paymentMethodComboBox.setItems(FXCollections.observableArrayList("При получении", "Имитация онлайн-оплаты"));
        paymentMethodComboBox.setPromptText("Выберите способ оплаты");
    }



    public void initCart(long userId, Form1 mainController) {
        this.currentUserId = userId;
        this.mainController = mainController;
        System.out.println("DEBUG: Инициализация корзины для пользователя ID = " + this.currentUserId);
        if (checkoutFormPane != null) checkoutFormPane.setVisible(false);
        if (cartScrollPane != null) cartScrollPane.setVisible(true);
        if (checkoutActionButton != null) checkoutActionButton.setDisable(false);
        loadCart();
    }

    public void loadCart() {
        if (currentUserId == -1) {
            System.err.println("DEBUG ПРЕДОХРАНИТЕЛЬ: Попытка загрузить корзину без указания ID пользователя!");
            return;
        }

        cartItemsContainer.setAlignment(Pos.TOP_LEFT);
        cartItemsContainer.getChildren().clear();

        Label loadingLabel = new Label("Загрузка вашей корзины...");
        loadingLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 16px;");
        cartItemsContainer.setAlignment(Pos.CENTER);
        cartItemsContainer.getChildren().add(loadingLabel);

        cartDAO.getCartItems(currentUserId).thenAccept(items -> {
            Platform.runLater(() -> {
                cartItemsContainer.getChildren().clear();
                if (items != null && !items.isEmpty()) {
                    cartData.setAll(items);
                    renderCartCards();
                    calculateTotal();
                } else {
                    cartData.clear();
                    showEmptyCartMessage();
                    calculateTotal();
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                System.err.println("Ошибка при получении корзины из БД: " + ex.getMessage());
                showError("Не удалось связаться с сервером для загрузки корзины.");
            });
            return null;
        });
    }

    private void renderCartCards() {
        cartItemsContainer.getChildren().clear();

        if (cartData.isEmpty()) {
            showEmptyCartMessage();
            return;
        }

        cartItemsContainer.setAlignment(Pos.TOP_LEFT);

        for (CartItem item : cartData) {
            Product product = item.getVariant().getProduct();
            ProductVariant variant = item.getVariant();

            boolean isOutOfStock = variant.getStock() <= 0;

            HBox card = new HBox();
            card.setSpacing(15);
            card.setPadding(new Insets(15));
            card.setAlignment(Pos.CENTER_LEFT);

            if (isOutOfStock) {
                card.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 12; -fx-border-color: #DDDDDD; -fx-border-radius: 12; -fx-opacity: 0.7;");
            } else {
                card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-border-color: #E0E0E0; -fx-border-radius: 12;");
            }
            card.setPrefHeight(100);

            if (!isOutOfStock) {
                card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + " -fx-border-color: #b388ff;"));
                card.setOnMouseExited(e -> card.setStyle(card.getStyle().replaceAll(" -fx-border-color:.*?;", " -fx-border-color: #E0E0E0;")));
            }

            card.setOnMouseClicked(e -> {
                if (e.getTarget() instanceof Button) return;
                openProductDetail(product);
            });
            card.setStyle(card.getStyle() + " -fx-cursor: hand;");

            ImageView imageView = new ImageView();
            imageView.setFitWidth(80);
            imageView.setFitHeight(80);
            imageView.setPreserveRatio(true);

            String imageUrl = (product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : null;
            try {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    imageView.setImage(new Image(imageUrl, 80, 80, true, true, true));
                } else {
                    imageView.setImage(new Image("https://via.placeholder.com/80/E3E3E3/888888?text=No+Photo", true));
                }
            } catch (Exception e) {
                imageView.setImage(new Image("https://via.placeholder.com/80/E3E3E3/b388ff?text=Error", true));
            }

            VBox infoBox = new VBox();
            infoBox.setSpacing(4);
            infoBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            Label nameLabel = new Label(product.getName());
            nameLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #212121;");
            nameLabel.setWrapText(true);

            String stockStatusText = isOutOfStock ? " [НЕТ НА СКЛАДЕ]" : "";
            Label detailsLabel = new Label(product.getBrand() + " • Размер: " + variant.getSize() + " • Цвет: " + variant.getColor() + stockStatusText);
            detailsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: " + (isOutOfStock ? "#d32f2f" : "#757575") + ";");

            infoBox.getChildren().addAll(nameLabel, detailsLabel);

            HBox qtyBox = new HBox(5);
            qtyBox.setAlignment(Pos.CENTER);
            qtyBox.setStyle("-fx-background-color: #E3E3E3; -fx-background-radius: 6; -fx-padding: 4 8;");

            Button btnMinus = new Button("-");
            btnMinus.setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-cursor: hand;");
            btnMinus.setOnAction(e -> changeQty(item, -1));

            Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
            qtyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #212121; -fx-font-weight: bold; -fx-min-width: 20; -fx-alignment: center;");

            Button btnPlus = new Button("+");
            btnPlus.setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-cursor: hand;");
            btnPlus.setOnAction(e -> changeQty(item, 1));

            if (isOutOfStock) btnPlus.setDisable(true);

            qtyBox.getChildren().addAll(btnMinus, qtyLabel, btnPlus);

            VBox priceBox = new VBox();
            priceBox.setAlignment(Pos.CENTER_RIGHT);
            priceBox.setPrefWidth(110);

            if (isOutOfStock) {
                Label noStockLabel = new Label("Нет в наличии");
                noStockLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
                priceBox.getChildren().add(noStockLabel);
            } else {
                double positionPrice = product.getPrice() * item.getQuantity();
                Label priceLabel = new Label(String.format("%.2f ₽", positionPrice));
                priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #212121;");
                priceBox.getChildren().add(priceLabel);
            }

            Button btnDelete = new Button("×");
            btnDelete.setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-text-fill: #d32f2f; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 0 5 0 5;");
            btnDelete.setOnAction(e -> deleteItem(item));

            card.getChildren().addAll(imageView, infoBox, qtyBox, priceBox, btnDelete);
            cartItemsContainer.getChildren().add(card);
        }
    }

    private void showEmptyCartMessage() {
        Label message = new Label("Ваша корзина пуста");
        message.setStyle("-fx-text-fill: #888888; -fx-font-size: 16px;");
        cartItemsContainer.setAlignment(Pos.CENTER);
        cartItemsContainer.getChildren().add(message);
    }

    private void changeQty(CartItem item, int delta) {
        int newQty = item.getQuantity() + delta;
        if (newQty <= 0) {
            deleteItem(item);
            return;
        }

        if (delta > 0 && item.getVariant() != null) {
            int maxAvailable = item.getVariant().getStock();
            if (newQty > maxAvailable) {
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        String.format("Нельзя добавить больше! На складе доступно всего %d шт.", maxAvailable));
                alert.show();
                return;
            }
        }

        cartDAO.updateCartItemQuantity(item.getId(), newQty).thenAccept(success -> {
            if (success) Platform.runLater(this::loadCart);
        });
    }

    private void deleteItem(CartItem item) {
        cartDAO.removeFromCart(item.getId()).thenAccept(success -> {
            if (success) Platform.runLater(this::loadCart);
        });
    }

    private void calculateTotal() {
        double total = cartData.stream()
                .filter(i -> i.getVariant() != null && i.getVariant().getStock() > 0)
                .mapToDouble(i -> i.getVariant().getProduct().getPrice() * i.getQuantity())
                .sum();

        int totalItems = cartData.stream()
                .filter(i -> i.getVariant() != null && i.getVariant().getStock() > 0)
                .mapToInt(CartItem::getQuantity)
                .sum();

        totalLabel.setText(String.format("%.2f ₽", total));
        itemsCountLabel.setText(totalItems + " " + getProductWord(totalItems));
    }

    private String getProductWord(int count) {
        if (count % 100 >= 11 && count % 100 <= 19) return "товаров";
        int lastDigit = count % 10;
        if (lastDigit == 1) return "товар";
        if (lastDigit >= 2 && lastDigit <= 4) return "товара";
        return "товаров";
    }

    private void openProductDetail(Product product) {
        if (mainController != null) {
            mainController.openProductDetail(product);
        } else {
            System.err.println("Ошибка: Ссылка на Form1 (mainController) не установлена in CartController!");
        }
    }

    @FXML
    private void handleCheckout() {
        if (cartData.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Ваша корзина пуста!");
            alert.show();
            return;
        }

        boolean hasOutOfStockItems = cartData.stream()
                .anyMatch(item -> item.getVariant() == null || item.getVariant().getStock() <= 0);

        if (hasOutOfStockItems) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Внимание");
            alert.setHeaderText("Недоступные товары в корзине");
            alert.setContentText("Удалите из корзины товары со статусом «Нет в наличии» перед оформлением заказа.");
            alert.showAndWait();
            return;
        }

        cartScrollPane.setVisible(false);
        checkoutFormPane.setVisible(true);
        checkoutActionButton.setDisable(true);
    }

    @FXML
    private void cancelCheckout() {
        checkoutFormPane.setVisible(false);
        cartScrollPane.setVisible(true);
        checkoutActionButton.setDisable(false);
    }

    @FXML
    private void confirmPurchase() {
        if (cartData.isEmpty()) return;

        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String paymentMethod = paymentMethodComboBox.getValue();

        if (address.isEmpty()) {
            showError("Введите адрес доставки!");
            return;
        }
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showError("Введите корректный Email!");
            return;
        }
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            showError("Выберите способ оплаты!");
            return;
        }

        checkoutActionButton.setDisable(true);

        List<CompletableFuture<String>> validationFutures = cartData.stream()
                .map(item -> productDAO.getProductVariants(item.getVariant().getProductId())
                        .thenApply(freshVariants -> {
                            if (freshVariants == null) return "Ошибка связи с сервером";

                            ProductVariant freshVariant = freshVariants.stream()
                                    .filter(v -> v.getId() == item.getVariant().getId())
                                    .findFirst().orElse(null);

                            if (freshVariant == null || freshVariant.getStock() < item.getQuantity()) {
                                return String.format("• %s — осталось: %d", item.getVariant().getProduct().getName(),
                                        freshVariant == null ? 0 : freshVariant.getStock());
                            }
                            item.getVariant().setStock(freshVariant.getStock());
                            return null;
                        })
                ).collect(java.util.stream.Collectors.toList());

        CompletableFuture.allOf(validationFutures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    List<String> errors = validationFutures.stream()
                            .map(CompletableFuture::join)
                            .filter(java.util.Objects::nonNull)
                            .collect(java.util.stream.Collectors.toList());

                    if (!errors.isEmpty()) {
                        Platform.runLater(() -> {
                            showError("Некоторые товары уже раскупили:\n" + String.join("\n", errors));
                            cancelCheckout();
                            loadCart();
                        });
                        return;
                    }

                    CompletableFuture<?>[] orderFutures = cartData.stream()
                            .map(item -> {
                                double positionPrice = item.getVariant().getProduct().getPrice() * item.getQuantity();
                                return productDAO.createOrderWithDetails(
                                        currentUserId,
                                        item.getVariant().getId(),
                                        item.getQuantity(),
                                        positionPrice,
                                        email,
                                        address
                                );
                            })
                            .toArray(CompletableFuture[]::new);

                    CompletableFuture<?>[] stockFutures = cartData.stream()
                            .map(item -> productDAO.updateVariantStock(item.getVariant().getId(),
                                    item.getVariant().getStock() - item.getQuantity()))
                            .toArray(CompletableFuture[]::new);

                    CompletableFuture.allOf(orderFutures)
                            .thenCombine(CompletableFuture.allOf(stockFutures), (v1, v2) -> null)
                            .thenCompose(vx -> cartDAO.clearCart(currentUserId))
                            .thenAccept(success -> Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Заказ успешно оформлен!");
                                alert.showAndWait();


                                emailField.clear();
                                addressField.clear();
                                paymentMethodComboBox.setValue(null);

                                handleBack();
                            }))
                            .exceptionally(ex -> {
                                Platform.runLater(() -> {
                                    showError("Ошибка при оформлении заказа.");
                                    checkoutActionButton.setDisable(false);
                                });
                                return null;
                            });
                });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.show();
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }
}