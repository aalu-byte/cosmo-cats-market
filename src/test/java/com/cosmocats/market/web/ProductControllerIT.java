package com.cosmocats.market.web;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.mapper.ProductMapper;
import com.cosmocats.market.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тести рівня контролера: піднімається лише web-шар (контролер, маппер,
 * GlobalExceptionHandler, валідація), а сервіс замінено Mockito-моком.
 */
@WebMvcTest(ProductController.class)
@Import(ProductMapper.class)
class ProductControllerIT {

    private static final String URL = "/api/products";

    private static final String VALID_BODY = """
            {"name":"Galaxy Milk","description":"Milk from the Milky Way","price":4.50,"categoryId":2}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private static Product product(long id, String name) {
        return Product.builder()
                .id(id)
                .name(name)
                .description("desc")
                .price(new BigDecimal("9.99"))
                .categoryId(1L)
                .build();
    }

    // ---------- GET /api/products ----------

    @Test
    void getAll_returnsPagedEnvelope() throws Exception {
        when(productService.getAll(0, 10))
                .thenReturn(List.of(product(1, "Star Yarn"), product(2, "Galaxy Milk")));
        when(productService.count()).thenReturn(2L);

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getAll_usesPageAndSizeParameters() throws Exception {
        when(productService.getAll(1, 1)).thenReturn(List.of(product(2, "Galaxy Milk")));
        when(productService.count()).thenReturn(2L);

        mockMvc.perform(get(URL).param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void getAll_negativePage_returns400() throws Exception {
        mockMvc.perform(get(URL).param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getAll_zeroSize_returns400() throws Exception {
        mockMvc.perform(get(URL).param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void getAll_tooBigSize_returns400() throws Exception {
        mockMvc.perform(get(URL).param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void getAll_notNumericPage_returns400() throws Exception {
        mockMvc.perform(get(URL).param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("page")));
    }

    // ---------- GET /api/products/{id} ----------

    @Test
    void getById_existing_returns200() throws Exception {
        when(productService.getById(1L)).thenReturn(product(1, "Star Yarn"));

        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Star Yarn"));
    }

    @Test
    void getById_missing_returns404ProblemDetail() throws Exception {
        when(productService.getById(999L)).thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get(URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://cosmo-cats.market/errors/not-found"))
                .andExpect(jsonPath("$.title").value("Product not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.instance").value(URL + "/999"));
    }

    @Test
    void getById_unexpectedError_returns500WithoutInternalDetails() throws Exception {
        when(productService.getById(1L)).thenThrow(new IllegalStateException("secret boom"));

        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal server error"))
                .andExpect(jsonPath("$.detail", not(containsString("secret boom"))));
    }

    // ---------- POST /api/products ----------

    @Test
    void create_validBody_returns201WithLocation() throws Exception {
        when(productService.create(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/products/10")))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Galaxy Milk"));
    }

    @Test
    void create_blankNameAndNegativePrice_returns400WithFieldErrors() throws Exception {
        String body = """
                {"name":"","price":-5,"categoryId":1}
                """;

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://cosmo-cats.market/errors/validation"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.instance").value(URL))
                .andExpect(jsonPath("$.errors[*].field", hasItem("name")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("price")));

        verify(productService, never()).create(any());
    }

    @Test
    void create_nonCosmicName_returns400() throws Exception {
        String body = """
                {"name":"Simple Box","price":10,"categoryId":1}
                """;

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("name")));
    }

    @Test
    void create_priceWithThreeDecimals_returns400() throws Exception {
        String body = """
                {"name":"Star Dust","price":9.999,"categoryId":1}
                """;

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("price")));
    }

    @Test
    void create_missingPrice_returns400() throws Exception {
        String body = """
                {"name":"Star Dust","categoryId":1}
                """;

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("price")));
    }

    @Test
    void create_negativeCategoryId_returns400() throws Exception {
        String body = """
                {"name":"Star Box","price":5,"categoryId":-7}
                """;

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("categoryId")));
    }

    @Test
    void create_tooLongDescription_returns400() throws Exception {
        String body = """
                {"name":"Star Box","description":"%s","price":5,"categoryId":1}
                """.formatted("a".repeat(501));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("description")));
    }

    @Test
    void create_malformedJson_returns400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Star\", \"price\": }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed request"));
    }

    // ---------- PUT /api/products/{id} ----------

    @Test
    void update_validBody_returns200() throws Exception {
        when(productService.update(eq(1L), any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(1);
            p.setId(1L);
            return p;
        });

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Galaxy Milk"));
    }

    @Test
    void update_invalidBody_returns400() throws Exception {
        String body = """
                {"name":"","price":0,"categoryId":1}
                """;

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));

        verify(productService, never()).update(any(), any());
    }

    @Test
    void update_missing_returns404() throws Exception {
        when(productService.update(eq(999L), any(Product.class)))
                .thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(put(URL + "/999").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE /api/products/{id} ----------

    @Test
    void delete_existing_returns204() throws Exception {
        mockMvc.perform(delete(URL + "/1"))
                .andExpect(status().isNoContent());

        verify(productService).delete(1L);
    }

    @Test
    void delete_missing_returns404() throws Exception {
        doThrow(new ProductNotFoundException(999L)).when(productService).delete(999L);

        mockMvc.perform(delete(URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product not found"));
    }

    // ---------- Інші помилки ----------

    @Test
    void unsupportedMethod_returns405ProblemDetail() throws Exception {
        mockMvc.perform(patch(URL + "/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void unknownPath_returns404ProblemDetail() throws Exception {
        mockMvc.perform(get("/api/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
