package com.cosmocats.market.repository;

public interface TopProductProjection {

    Long getProductId();

    String getProductName();

    Long getTotalQuantity();
}