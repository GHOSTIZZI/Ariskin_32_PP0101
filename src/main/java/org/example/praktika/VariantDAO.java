package org.example.praktika;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VariantDAO extends SupabaseDAO {

    public CompletableFuture<Boolean> addVariant(int productId, String color, String size, int stock) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("product_id", productId);
        json.put("color", color);
        json.put("size", size);
        json.put("stock", stock);
        return sendPostRequest("/product_variants", json, "addVariant");
    }

    public CompletableFuture<List<ProductVariant>> getVariantsByProductId(int productId) {
        String path = "/product_variants?product_id=eq." + productId;
        HttpRequest request = createBaseRequestBuilder(path).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<ProductVariant>>() {}, "getVariants");
    }

    public CompletableFuture<Boolean> deleteVariant(int variantId) {
        return sendDeleteRequest("/product_variants?id=eq." + variantId, "deleteVariant");
    }




}