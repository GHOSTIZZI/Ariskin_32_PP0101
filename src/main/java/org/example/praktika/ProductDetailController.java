package org.example.praktika;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailController {

    private final ProductDAO productDAO = new ProductDAO();

    @FXML private ImageView productImage;
    @FXML private Label imageCounterLabel;
    @FXML private Button btnPrevImg;
    @FXML private Button btnNextImg;
    @FXML private HBox thumbnailsContainer;

    @FXML private Label categoryLabel;
    @FXML private Label genderLabel;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label stockLabel;
    @FXML private Label brandLabel;
    @FXML private Label materialLabel;
    @FXML private Label seasonLabel;
    @FXML private Label descriptionLabel;

    @FXML private FlowPane variantsPane;
    @FXML private Button addToCartButton;

    private Form1 mainController;
    private Product currentProduct;
    private ProductVariant selectedVariant = null;
    private int currentImageIndex = 0;

    private List<CartItem> cartItems = new ArrayList<>();

    private long currentUserId = -1;

    public void setMainController(Form1 mainController) {
        this.mainController = mainController;
    }


    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
    }

    public void setProduct(Product product) {
        this.currentProduct = product;
        this.selectedVariant = null;

        fillData();
        refreshProductDataFromDB();
    }

    private void refreshProductDataFromDB() {
        if (currentProduct == null) return;

        productDAO.getProductVariants(currentProduct.getId()).thenAccept(freshVariants -> {
            Platform.runLater(() -> {
                if (freshVariants != null && !freshVariants.isEmpty()) {
                    this.currentProduct.setVariants(freshVariants);
                }
                loadCartData();
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(this::loadCartData);
            return null;
        });
    }


    private void loadCartData() {
        productDAO.getCartItems(currentUserId).thenAccept(items -> {
            Platform.runLater(() -> {
                this.cartItems = items != null ? items : new ArrayList<>();
                renderVariants();
                updateAddToCartButtonState();
            });
        });
    }

    private void updateAddToCartButtonState() {
        if (selectedVariant == null) {
            addToCartButton.setText("Выберите размер");
            addToCartButton.setDisable(true);
            addToCartButton.setStyle("-fx-background-color: #eeeeee; -fx-text-fill: #888888; -fx-background-radius: 16;");
            return;
        }


        int inCartQuantity = cartItems.stream()
                .filter(item -> item.getProductVariantId() == selectedVariant.getId())
                .mapToInt(CartItem::getQuantity)
                .sum();


        int availableStock = selectedVariant.getStock() - inCartQuantity;

        if (availableStock <= 0) {
            addToCartButton.setText("В корзине (макс.)");
            addToCartButton.setDisable(true);
            addToCartButton.setStyle("-fx-background-color: #9e9e9e; -fx-text-fill: white; -fx-background-radius: 16;");

            stockLabel.setText("❌ Достигнут лимит (все " + selectedVariant.getStock() + " шт. в вашей корзине)");
            stockLabel.setStyle("-fx-text-fill: #e57373; -fx-font-weight: bold;");
        } else if (inCartQuantity > 0) {
            addToCartButton.setText("Добавить еще (" + inCartQuantity + " в корзине)");
            addToCartButton.setDisable(false);
            addToCartButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-background-radius: 16;");

            stockLabel.setText("✔ Доступно для добавления: " + availableStock + " шт. (На складе: " + selectedVariant.getStock() + ")");
            stockLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
        } else {
            addToCartButton.setText("Добавить в корзину");
            addToCartButton.setDisable(false);
            addToCartButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-background-radius: 16;");

            stockLabel.setText("✔ В наличии: " + availableStock + " шт.");
            stockLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
        }
    }

    private void renderVariants() {
        variantsPane.getChildren().clear();
        ToggleGroup sizeGroup = new ToggleGroup();


        String baseStyle = "-fx-background-radius: 16; -fx-font-size: 16px; -fx-padding: 8 12; ";
        String disabledStyle = baseStyle + "-fx-background-color: #e0e0e0; -fx-text-fill: #a0a0a0; -fx-opacity: 0.5;";
        String selectedStyle = baseStyle + "-fx-background-color: #2196f3; -fx-text-fill: white;";
        String defaultStyle = baseStyle + "-fx-background-color: #f0f0f0; -fx-text-fill: black;";

        if (currentProduct.getVariants() != null && !currentProduct.getVariants().isEmpty()) {
            int selectedId = (selectedVariant != null) ? selectedVariant.getId() : -1;

            for (ProductVariant variant : currentProduct.getVariants()) {
                ToggleButton sizeBtn = new ToggleButton(variant.getSize() + " / " + variant.getColor());
                sizeBtn.setToggleGroup(sizeGroup);
                sizeBtn.setUserData(variant);

                // Фильтр остатков
                int inCartQuantity = cartItems.stream()
                        .filter(item -> item.getProductVariantId() == variant.getId())
                        .mapToInt(CartItem::getQuantity)
                        .sum();
                int availableStock = variant.getStock() - inCartQuantity;

                if (availableStock <= 0) {
                    sizeBtn.setDisable(true);
                    sizeBtn.setStyle(disabledStyle);
                } else {
                    if (variant.getId() == selectedId) {
                        sizeBtn.setSelected(true);
                        sizeBtn.setStyle(selectedStyle);
                    } else {
                        sizeBtn.setStyle(defaultStyle);
                    }
                }

                variantsPane.getChildren().add(sizeBtn);
                addToCartButton.setVisible(true);
            }

            sizeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
                if (newToggle != null) {
                    selectedVariant = (ProductVariant) newToggle.getUserData();
                    ((ToggleButton) newToggle).setStyle(selectedStyle);
                } else {
                    selectedVariant = null;
                }

                if (oldToggle != null && !((ToggleButton) oldToggle).isDisabled()) {
                    ((ToggleButton) oldToggle).setStyle(defaultStyle);
                }

                updateAddToCartButtonState();
            });
        }
    }

    @FXML
    private void handleAddToCart() {
        if (selectedVariant == null) return;

        int inCartQuantity = cartItems.stream()
                .filter(item -> item.getProductVariantId() == selectedVariant.getId())
                .mapToInt(CartItem::getQuantity)
                .sum();
        int availableStock = selectedVariant.getStock() - inCartQuantity;

        if (availableStock <= 0) {
            new Alert(Alert.AlertType.ERROR, "Доступный лимит товара исчерпан!").show();
            return;
        }

        addToCartButton.setDisable(true);
        addToCartButton.setText("Добавление...");

        // 🔥 ИСПРАВЛЕНИЕ: Отправляем запрос с ID текущего юзера
        productDAO.addToCart(currentUserId, selectedVariant.getId(), 1)
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        if (success) {
                            new Alert(Alert.AlertType.INFORMATION, "Товар добавлен в корзину!").show();
                            refreshProductDataFromDB();
                        } else {
                            new Alert(Alert.AlertType.ERROR, "Ошибка добавления.").show();
                            updateAddToCartButtonState();
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        new Alert(Alert.AlertType.ERROR, "Ошибка соединения.").show();
                        updateAddToCartButtonState();
                    });
                    return null;
                });
    }

    private void fillData() {
        System.out.println("DEBUG: Товар " + currentProduct.getName() +
                " | Цена: " + currentProduct.getPrice() +
                " | Старая цена: " + currentProduct.getOldPrice());
        nameLabel.setText(currentProduct.getName());


        if (currentProduct.getOldPrice() != null && currentProduct.getOldPrice() > currentProduct.getPrice()) {
            priceLabel.setText("");

            HBox priceBox = new HBox(12);
            priceBox.setAlignment(Pos.BASELINE_LEFT);


            Text newPriceText = new Text(String.format("%.2f ₽", currentProduct.getPrice()));
            newPriceText.setStyle("-fx-font-size: 22px; -fx-fill: #ff5252; -fx-font-weight: bold;");


            Text oldPriceText = new Text(String.format("%.2f ₽", currentProduct.getOldPrice()));
            oldPriceText.setStyle("-fx-font-size: 15px; -fx-fill: #888888;");
            oldPriceText.setStrikethrough(true);

            priceBox.getChildren().addAll(newPriceText, oldPriceText);
            priceLabel.setGraphic(priceBox);
        } else {

            priceLabel.setGraphic(null);
            priceLabel.setText(String.format("%.2f ₽", currentProduct.getPrice()));
            priceLabel.setStyle("-fx-text-fill: #b388ff; -fx-font-size: 20px; -fx-font-weight: bold;");
        }
        // -----------------------------------------------

        brandLabel.setText("Бренд: " + currentProduct.getBrand());
        materialLabel.setText("Материал: " + currentProduct.getMaterial());
        seasonLabel.setText("Сезон: " + currentProduct.getSeason());
        genderLabel.setText("Пол: " + currentProduct.getGender());
        categoryLabel.setText("Категория: " + currentProduct.getCategory());
        descriptionLabel.setText(currentProduct.getDescription());

        stockLabel.setText("Выберите размер");
        stockLabel.setStyle("-fx-text-fill: #666666; -fx-font-weight: bold;");

        currentImageIndex = 0;
        updateImage();
        renderThumbnails();
    }

    private void renderThumbnails() {
        thumbnailsContainer.getChildren().clear();
        if (currentProduct.getImages() == null || currentProduct.getImages().isEmpty()) return;

        for (int i = 0; i < currentProduct.getImages().size(); i++) {
            final int index = i;
            String url = currentProduct.getImages().get(i);

            ImageView thumbView = new ImageView();
            thumbView.setFitWidth(65);
            thumbView.setFitHeight(65);
            thumbView.setPreserveRatio(true);

            try {
                thumbView.setImage(new Image(url, 65, 65, true, true, true));
            } catch (Exception e) {
                thumbView.setImage(new Image("https://via.placeholder.com/65?text=Error", true));
            }

            HBox wrapper = new HBox(thumbView);
            wrapper.setStyle("-fx-border-color: " + (i == currentImageIndex ? "#2196f3" : "#dddddd") + "; -fx-border-width: 2; -fx-padding: 2; -fx-background-color: white;");
            wrapper.setOnMouseClicked(e -> {
                currentImageIndex = index;
                updateImage();
            });

            thumbnailsContainer.getChildren().add(wrapper);
        }
    }

    private void updateImage() {
        if (currentProduct.getImages() == null || currentProduct.getImages().isEmpty()) {
            productImage.setImage(new Image("https://via.placeholder.com/350x420/eeeeee/888888?text=Нет+фото", true));
            imageCounterLabel.setText("0 / 0");
            return;
        }

        String imageUrl = currentProduct.getImages().get(currentImageIndex);
        try {
            productImage.setImage(new Image(imageUrl, true));
        } catch (Exception e) {
            System.err.println("Ошибка отображения основной картинки: " + e.getMessage());
        }

        imageCounterLabel.setText((currentImageIndex + 1) + " / " + currentProduct.getImages().size());
        btnPrevImg.setDisable(currentImageIndex == 0);
        btnNextImg.setDisable(currentImageIndex == currentProduct.getImages().size() - 1);

        if (thumbnailsContainer != null && !thumbnailsContainer.getChildren().isEmpty()) {
            for (int i = 0; i < thumbnailsContainer.getChildren().size(); i++) {
                HBox wrapper = (HBox) thumbnailsContainer.getChildren().get(i);
                wrapper.setStyle("-fx-border-color: " + (i == currentImageIndex ? "#2196f3" : "#dddddd") + "; -fx-border-width: 2; -fx-padding: 2; -fx-background-color: white;");
            }
        }
    }

    @FXML private void handleNextImage() {
        if (currentProduct.getImages() != null && currentImageIndex < currentProduct.getImages().size() - 1) {
            currentImageIndex++;
            updateImage();
        }
    }

    @FXML private void handlePrevImage() {
        if (currentImageIndex > 0) {
            currentImageIndex--;
            updateImage();
        }
    }

    @FXML private void handleBack() {
        if (mainController != null) mainController.showMainView();
    }
}