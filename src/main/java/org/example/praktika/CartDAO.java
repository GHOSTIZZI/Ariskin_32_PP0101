package org.example.praktika;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CartDAO extends SupabaseDAO {

    public CompletableFuture<List<CartItem>> getCartItems(long userId) {
        String path = "/cart_items?user_id=eq." + userId + "&select=*,product_variants(*,products(*))";
        HttpRequest request = createBaseRequestBuilder(path).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<CartItem>>() {}, "getCartItems");
    }

    public CompletableFuture<Boolean> updateCartItemQuantity(int cartItemId, int newQuantity) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("quantity", newQuantity);
        HttpRequest request = createBaseRequestBuilder("/cart_items?id=eq." + cartItemId)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();
        return httpClient.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> res.statusCode() == 200 || res.statusCode() == 204)
                .exceptionally(e -> false);
    }

    public CompletableFuture<Boolean> removeFromCart(int cartItemId) {
        return sendDeleteRequest("/cart_items?id=eq." + cartItemId, "removeFromCart");
    }

    public CompletableFuture<Boolean> clearCart(long userId) {
        return sendDeleteRequest("/cart_items?user_id=eq." + userId, "clearCart");
    }
}