package com.cosmocats.market.web;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.PageResponse;
import com.cosmocats.market.dto.ProductRequestDto;
import com.cosmocats.market.dto.ProductResponseDto;
import com.cosmocats.market.mapper.ProductMapper;
import com.cosmocats.market.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    public PageResponse<ProductResponseDto> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<ProductResponseDto> content = productService.getAll(page, size).stream()
                .map(productMapper::toDto)
                .toList();

        long total = productService.count();
        int totalPages = (int) Math.ceil((double) total / size);

        return new PageResponse<>(content, page, size, total, totalPages);
    }

    @GetMapping("/{id}")
    public ProductResponseDto getById(@PathVariable Long id) {
        return productMapper.toDto(productService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponseDto> create(@Valid @RequestBody ProductRequestDto request) {
        Product created = productService.create(productMapper.toDomain(request));
        URI location= URI.create("/api/products/" + created.getId());
        return ResponseEntity.created(location).body(productMapper.toDto(created));
    }

    @PutMapping("/{id}")
    public ProductResponseDto update(@PathVariable Long id,
                                     @Valid @RequestBody ProductRequestDto request) {
        Product updated = productService.update(id, productMapper.toDomain(request));
        return productMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}