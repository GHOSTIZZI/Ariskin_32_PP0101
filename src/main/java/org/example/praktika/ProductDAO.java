package org.example.praktika;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProductDAO extends SupabaseDAO {

    public ProductDAO() {
        super();
    }


    public CompletableFuture<List<Product>> getAllProducts() {
        String path = "/products?select=*,product_variants(*)&order=id.asc";
        HttpRequest request = createBaseRequestBuilder(path).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<Product>>() {}, "getAllProducts");
    }


    public CompletableFuture<List<Product>> searchProducts(String query) {
        String encodedQuery = encodeUrlParameter("%" + query + "%");
        String path = "/products?select=*,product_variants(*)&name=ilike." + encodedQuery;
        HttpRequest request = createBaseRequestBuilder(path).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<Product>>() {}, "searchProducts");
    }

    public CompletableFuture<List<Product>> getProductsByCategory(String category) {
        String encodedCategory = encodeUrlParameter(category);
        String path = "/products?select=*,product_variants(*)&category=eq." + encodedCategory;
        HttpRequest request = createBaseRequestBuilder(path).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<Product>>() {}, "getProductsByCategory");
    }


    public CompletableFuture<List<ProductVariant>> getProductVariants(int productId) {
        String path = "/product_variants?product_id=eq." + productId + "&select=*&order=id.asc";
        HttpRequest request = createBaseRequestBuilder(path).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<ProductVariant>>() {}, "getProductVariants");
    }

    public CompletableFuture<Boolean> deleteProduct(int productId) {
        return sendDeleteRequest("/products?id=eq." + productId, "deleteProduct");
    }


    public CompletableFuture<Boolean> updateProduct(int id, Product product) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("name", product.getName());
        json.put("price", product.getPrice());
        json.put("old_price", product.getOldPrice());
        json.put("brand", product.getBrand());
        json.put("description", product.getDescription());
        json.put("material", product.getMaterial());
        json.put("season", product.getSeason());
        json.put("gender", product.getGender());
        json.put("category", product.getCategory());
        json.set("images", objectMapper.valueToTree(product.getImages()));

        HttpRequest request = createBaseRequestBuilder("/products?id=eq." + id)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> res.statusCode() == 200 || res.statusCode() == 204)
                .exceptionally(ex -> false);
    }

    public CompletableFuture<Boolean> addProduct(Product p) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("name", p.getName());
        json.put("price", p.getPrice());
        json.put("old_price", p.getOldPrice()); // 🔥 ФИКС ДЛЯ СКИДОК
        json.put("brand", p.getBrand());
        json.put("description", p.getDescription());
        json.put("category", p.getCategory());
        json.put("gender", p.getGender());
        json.put("material", p.getMaterial());
        json.put("season", p.getSeason());
        json.set("images", objectMapper.valueToTree(p.getImages()));

        return sendPostRequest("/products", json, "addProduct");
    }


    public CompletableFuture<List<CartItem>> getCartItems(long userId) {
        System.out.println("DEBUG DAO: Запрос корзины для пользователя ID = " + userId); // Сразу увидим, не слетел ли ID
        String path = "/cart_items?user_id=eq." + userId + "&select=*,product_variants(*,products(*))";
        HttpRequest request = createBaseRequestBuilder(path).GET().build();

        return sendAndDeserializeList(request, new TypeReference<List<CartItem>>() {}, "getCartItems")
                .thenApply(items -> {
                    System.out.println("DEBUG DAO: Сервер вернул элементов корзины: " + (items != null ? items.size() : "null"));
                    return items;
                });
    }

    public CompletableFuture<Boolean> addToCart(long userId, int productVariantId, int quantity) {
        String checkPath = "/cart_items?user_id=eq." + userId + "&product_variant_id=eq." + productVariantId + "&select=*";
        HttpRequest checkReq = createBaseRequestBuilder(checkPath).GET().build();

        return sendAndDeserializeList(checkReq, new TypeReference<List<CartItem>>() {}, "checkCart")
                .thenCompose(items -> {
                    if (items != null && !items.isEmpty()) {
                        CartItem existing = items.get(0);
                        int newQuantity = existing.getQuantity() + quantity;
                        return updateCartItemQuantity(existing.getId(), newQuantity);
                    } else {
                        ObjectNode json = objectMapper.createObjectNode();
                        json.put("user_id", userId);
                        json.put("product_variant_id", productVariantId);
                        json.put("quantity", quantity);
                        return sendPostRequest("/cart_items", json, "addToCart");
                    }
                });
    }

    public CompletableFuture<Boolean> updateCartItemQuantity(int cartItemId, int newQuantity) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("quantity", newQuantity);

        HttpRequest request = createBaseRequestBuilder("/cart_items?id=eq." + cartItemId)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 200 || response.statusCode() == 204)
                .exceptionally(e -> {
                    System.err.println("Ошибка при обновлении количества в корзине: " + e.getMessage());
                    return false;
                });
    }

    public CompletableFuture<Boolean> removeFromCart(int cartItemId) {
        return sendDeleteRequest("/cart_items?id=eq." + cartItemId, "removeFromCart");
    }

    public CompletableFuture<Boolean> clearCart(long userId) {
        return sendDeleteRequest("/cart_items?user_id=eq." + userId, "clearCart");
    }
    


    public CompletableFuture<Boolean> createOrder(long userId, int productVariantId, int quantity, double totalPrice) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("user_id", userId);
        json.put("product_variant_id", productVariantId);
        json.put("quantity", quantity);
        json.put("total_price", totalPrice);

        return sendPostRequest("/orders", json, "createOrder");
    }

    public CompletableFuture<List<Order>> getUserOrders(long userId) {
        String path = "/orders?user_id=eq." + userId + "&select=*,product_variants(*,products(*))&order=created_at.desc";
        HttpRequest request = createBaseRequestBuilder(path).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<Order>>() {}, "getUserOrders");
    }

    public CompletableFuture<List<Order>> getAllOrders() {
        String path = "/orders?select=*,product_variants(*,products(*))&order=created_at.desc";
        HttpRequest request = createBaseRequestBuilder(path).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<Order>>() {}, "getAllOrders");
    }

    public CompletableFuture<Boolean> updateVariantStock(long variantId, int newStock) {
        try {
            ObjectNode json = objectMapper.createObjectNode();
            json.put("stock", newStock);

            HttpRequest request = createBaseRequestBuilder("/product_variants?id=eq." + variantId)
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() == 200 || response.statusCode() == 204)
                    .exceptionally(e -> {
                        System.err.println("Ошибка при обновлении остатков на складе: " + e.getMessage());
                        return false;
                    });
        } catch (Exception e) {
            System.err.println("Не удалось сформировать запрос обновления склада: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    public CompletableFuture<Boolean> addOrUpdateVariantStock(int productId, String size, String color, int quantityToAdd) {
        String encodedSize = encodeUrlParameter(size);
        String encodedColor = encodeUrlParameter(color);

        String path = "/product_variants?product_id=eq." + productId + "&size=eq." + encodedSize + "&color=eq." + encodedColor + "&select=*";
        HttpRequest checkRequest = createBaseRequestBuilder(path).GET().build();

        return sendAndDeserializeList(checkRequest, new TypeReference<List<ProductVariant>>() {}, "checkVariantExist")
                .thenCompose(variants -> {
                    if (variants != null && !variants.isEmpty()) {

                        ProductVariant existingVariant = variants.get(0);
                        int newStock = existingVariant.getStock() + quantityToAdd;

                        return updateVariantStock(existingVariant.getId(), newStock);
                    } else {

                        ObjectNode json = objectMapper.createObjectNode();
                        json.put("product_id", productId);
                        json.put("size", size);
                        json.put("color", color);
                        json.put("stock", quantityToAdd);

                        return sendPostRequest("/product_variants", json, "createNewVariant");
                    }
                }).exceptionally(ex -> {
                    System.err.println("Ошибка при выполнении безопасного апсерта склада: " + ex.getMessage());
                    return false;
                });
    }

    public CompletableFuture<Boolean> createOrderWithDetails(long userId, int productVariantId, int quantity, double totalPrice, String email, String address) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("user_id", userId);
        json.put("product_variant_id", productVariantId);
        json.put("quantity", quantity);
        json.put("total_price", totalPrice);
        json.put("email", email);
        json.put("delivery_address", address);

        return sendPostRequest("/orders", json, "createOrder");
    }


}