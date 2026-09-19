package com.sthouts.backend.service;

import com.sthouts.backend.config.TenantContext;
import com.sthouts.backend.dto.CategoryDto;
import com.sthouts.backend.model.Category;
import com.sthouts.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getAllCategories() {
        String tenantEmail = TenantContext.getTenantEmail();
        if (tenantEmail == null || tenantEmail.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<Category> categories = categoryRepository.findByTenantEmail(tenantEmail);
        return categories.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CategoryDto createCategory(CategoryDto categoryDto) {
        String tenantEmail = TenantContext.getTenantEmail();
        if (tenantEmail != null && !tenantEmail.isEmpty()) {
            if (categoryRepository.existsByNameAndTenantEmail(categoryDto.getName(), tenantEmail)) {
                throw new IllegalArgumentException("Category with name " + categoryDto.getName() + " already exists.");
            }
        } else if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new IllegalArgumentException("Category with name " + categoryDto.getName() + " already exists.");
        }

        Category category = Category.builder()
                .name(categoryDto.getName())
                .tenantEmail(tenantEmail)
                .build();
        Category savedCategory = categoryRepository.save(category);
        return mapToDto(savedCategory);
    }

    public void deleteCategory(String name) {
        String tenantEmail = TenantContext.getTenantEmail();
        Category category;
        if (tenantEmail != null && !tenantEmail.isEmpty()) {
            category = categoryRepository.findByNameAndTenantEmail(name, tenantEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        } else {
            category = categoryRepository.findByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        }
        categoryRepository.delete(category);
    }

    private CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
