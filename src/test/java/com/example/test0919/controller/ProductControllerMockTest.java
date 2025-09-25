package com.example.test0919.controller;

import com.example.test0919.dao.ProductDao;
import com.example.test0919.model.Product;
import com.example.test0919.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.parameters.P;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductControllerMockTest {
    @MockitoBean
    private ProductService productService;
    @Autowired
    private ProductDao productDao;

    @BeforeEach
    public void beforeEach(){
        Product mockProduct = new Product();
        mockProduct.setId(100);
        mockProduct.setName("piapple");

        Mockito.when(productDao.getDataById(Mockito.any())).thenReturn(mockProduct)
                .thenThrow(new RuntimeException());
    }

    @Test
    public void getDataById(){

        Product product = productService.getDataById(100);
        assertNotNull(product);
        assertEquals(100, product.getId());
    }
}