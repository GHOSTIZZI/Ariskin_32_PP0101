package org.example.praktika;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class AccController {

    @FXML private Label loginLabel;
    @FXML private ImageView avatarImage;
    @FXML private VBox ordersContainer;

    private Form1 mainController;
    private final ProductDAO productDAO = new ProductDAO();
    private long currentUserId = 1;

    public void setMainController(Form1 controller) {
        this.mainController = controller;

        if (controller != null) {
            this.currentUserId = controller.getCurrentUserId();
        }
        loadAccountInfo();
    }

    private void loadAccountInfo() {
        loadAvatarImage();
        if (mainController != null) {
            String login = mainController.getCurrentUserLogin();
            if (login != null) {
                loginLabel.setText(login);
            }
        }

        handleShowHistory();
    }

    private void loadAvatarImage() {
        try {
            Image defaultAvatar = new Image(getClass().getResourceAsStream("default_avatar.png"));
            avatarImage.setImage(defaultAvatar);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображения аккаунта: " + e.getMessage());
        }
    }


    @FXML
    private void handleShowHistory() {
        ordersContainer.getChildren().clear();

        Label loadingLabel = new Label("Загрузка истории заказов...");
        loadingLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 14px;");
        ordersContainer.getChildren().add(loadingLabel);


        productDAO.getUserOrders(currentUserId).thenAccept(orders -> {
            Platform.runLater(() -> {
                ordersContainer.getChildren().clear();

                if (orders == null || orders.isEmpty()) {
                    Label emptyLabel = new Label("Вы еще ничего не приобрели.");
                    emptyLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
                    ordersContainer.getChildren().add(emptyLabel);
                    return;
                }

                for (Order order : orders) {
                    ordersContainer.getChildren().add(createOrderCard(order));
                }
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> {
                ordersContainer.getChildren().clear();
                Label errorLabel = new Label("Не удалось загрузить историю покупок.");
                errorLabel.setStyle("-fx-text-fill: #e57373; -fx-font-size: 14px;");
                ordersContainer.getChildren().add(errorLabel);
            });
            return null;
        });
    }


    private HBox createOrderCard(Order order) {
        HBox card = new HBox();
        card.setSpacing(15);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #E0E0E0; -fx-border-radius: 10;");
        String productName = "Товар удален или недоступен";
        String productDetails = "Параметры неизвестны";
        String imageUrl = null;

        if (order.getVariant() != null && order.getVariant().getProduct() != null) {
            Product product = order.getVariant().getProduct();
            ProductVariant variant = order.getVariant();

            productName = product.getName();
            productDetails = String.format("Бренд: %s | Размер: %s | Цвет: %s", product.getBrand(), variant.getSize(), variant.getColor());
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                imageUrl = product.getImages().get(0);
            }
        }

        ImageView itemImage = new ImageView();
        itemImage.setFitWidth(60);
        itemImage.setFitHeight(60);
        itemImage.setPreserveRatio(true);
        try {
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                itemImage.setImage(new Image(imageUrl, 60, 60, true, true, true));
            } else {

                itemImage.setImage(new Image("https://via.placeholder.com/60/E3E3E3/888888?text=No+Data", true));
            }
        } catch (Exception e) {
            itemImage.setImage(new Image("https://via.placeholder.com/60/E3E3E3/b388ff?text=Error", true));
        }

        VBox textBlock = new VBox(4);
        HBox.setHgrow(textBlock, Priority.ALWAYS);

        Label titleLabel = new Label(productName);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #212121;");
        titleLabel.setWrapText(true);

        Label descLabel = new Label(productDetails);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");

        String rawDate = order.getCreatedAt();
        String formattedDate = (rawDate != null && rawDate.length() >= 10) ? rawDate.substring(0, 10) : "Не указана";
        Label dateLabel = new Label("Дата покупки: " + formattedDate);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9E9E9E;");

        Label emailLabel = new Label("Email: " + (order.getEmail() != null ? order.getEmail() : "—"));
        emailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #b388ff;");

        Label addrLabel = new Label("Адрес: " + (order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "—"));
        addrLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #757575;");
        addrLabel.setWrapText(true);

        textBlock.getChildren().addAll(titleLabel, descLabel, dateLabel, emailLabel, addrLabel);

        VBox priceBlock = new VBox(2);
        priceBlock.setAlignment(Pos.CENTER_RIGHT);
        priceBlock.setPrefWidth(120);

        Label qtyLabel = new Label(order.getQuantity() + " шт.");
        qtyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #212121;");

        Label costLabel = new Label(String.format("%.2f ₽", order.getTotalPrice()));
        costLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #212121;");

        priceBlock.getChildren().addAll(qtyLabel, costLabel);

        card.getChildren().addAll(itemImage, textBlock, priceBlock);
        return card;
    }

    @FXML
    private void handleLogout() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) avatarImage.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.showMainView();
        }
    }
}