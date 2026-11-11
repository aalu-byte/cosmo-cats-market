package com.cosmocats.market.repository;

import com.cosmocats.market.TestcontainersConfiguration;
import com.cosmocats.market.repository.entity.CategoryEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class CategoryRepositoryIT {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testSeededCategoriesExist() {
        // дві категорії додає
        assertThat(categoryRepository.count()).isEqualTo(2);
    }

    @Test
    void testCategoryCrud() {
        CategoryEntity category = new CategoryEntity();
        category.setName("Moon Gear");
        CategoryEntity saved = categoryRepository.save(category);

        assertThat(categoryRepository.findById(saved.getId())).isPresent();

        saved.setName("Moon Gear Pro");
        entityManager.flush();
        entityManager.clear();
        assertThat(categoryRepository.findById(saved.getId()).orElseThrow().getName()).isEqualTo("Moon Gear Pro");

        categoryRepository.deleteById(saved.getId());
        assertThat(categoryRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void testDuplicateCategoryNameIsRejected() {
        CategoryEntity duplicate = new CategoryEntity();
        duplicate.setName("Cosmic Toys");

        assertThatThrownBy(() -> categoryRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}