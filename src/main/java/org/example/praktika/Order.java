package org.example.praktika;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private int id;

    @JsonProperty("user_id")
    private long userId;

    @JsonProperty("product_variant_id")
    private int productVariantId;

    private int quantity;

    @JsonProperty("total_price")
    private double totalPrice;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("product_variants")
    private ProductVariant variant;

    @JsonProperty("email")
    private String email;

    @JsonProperty("delivery_address")
    private String deliveryAddress;

    public Order() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public int getProductVariantId() { return productVariantId; }
    public void setProductVariantId(int productVariantId) { this.productVariantId = productVariantId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public ProductVariant getVariant() { return variant; }
    public void setVariant(ProductVariant variant) { this.variant = variant; }

    public String getEmail() { return email; }
    public void setEmail(String email){ this.email=email;}

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress){ this.deliveryAddress=deliveryAddress;}
}