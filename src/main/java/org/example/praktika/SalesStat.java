package org.example.praktika;

public class SalesStat {
    private String productName;
    private Integer count;

    public SalesStat(String productName, Integer count) {
        this.productName = productName;
        this.count = count;
    }

    public String getProductName() { return productName; }
    public Integer getCount() { return count; }
}