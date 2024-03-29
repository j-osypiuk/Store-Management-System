package com.shopapp.product.dto;

import com.shopapp.category.dto.ResponseCategoryDto;
import com.shopapp.discount.dto.ResponseDiscountDto;
import lombok.Builder;

import java.util.List;

@Builder
public record ResponseProductDto(
        Long id,
        String name,
        String description,
        int amount,
        double price,
        ResponseDiscountDto discount,
        List<ResponseCategoryDto> categories,
        List<String> productPhotos
) {
}
