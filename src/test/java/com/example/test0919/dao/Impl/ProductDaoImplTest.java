package com.example.test0919.dao.Impl;

import com.example.test0919.dao.ProductDao;
import com.example.test0919.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductDaoImplTest {

    @Autowired
    private ProductDao productDao;

    @Test
    public void getDataById(){
        Product product = productDao.getDataById(11);
        assertNotNull(product);
        assertEquals("apple", product.getName());
        assertEquals(70, product.getPrice());
    }

    @Test
    @Transactional
    public void insert(){
        Product product = new Product();
        product.setName("Alier");
        product.setPrice(80);
        Integer productId = productDao.createData(product);
        Product result = productDao.getDataById(productId);
        assertNotNull(result);
        assertEquals("Alier", result.getName());
        assertEquals(80, result.getPrice());
    }
    @Test
    @Transactional
    public void update(){
        Product product = productDao.getDataById(3);
        product.setName("pick");
        productDao.updateProduct(product);
        Product result = productDao.getDataById(3);
        assertNotNull(result);
        assertEquals("pick", result.getName());
    }

    @Test
    @Transactional
    public void deleteById(){
        productDao.deleteData("Alier");
        Product product = productDao.getDataById(3);
        assertNull(product);
    }
}