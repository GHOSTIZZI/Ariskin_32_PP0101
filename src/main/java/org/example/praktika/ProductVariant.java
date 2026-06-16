package org.example.praktika;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductVariant {
    private int id;

    @JsonProperty("product_id")
    private int productId;

    private String color;
    private String size;
    private int stock;

    @JsonProperty("sales_count")
    private int salesCount;

    @JsonProperty("products")
    private Product product;

    public ProductVariant() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getSalesCount() { return salesCount; }
    public void setSalesCount(int salesCount) { this.salesCount = salesCount; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}