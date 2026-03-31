package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaginationConfig {

    @Value("${pagination.default.page:0}")
    private int defaultPage;

    @Value("${pagination.default.size:10}")
    private int defaultSize;

    public int getDefaultPage() {
        return defaultPage;
    }

    public int getDefaultSize() {
        return defaultSize;
    }
}