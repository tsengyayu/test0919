package com.example.test0919.service.impl;

import com.example.test0919.dao.ProductDao;
import com.example.test0919.dto.CreateDataRequest;
import com.example.test0919.model.AppUser;
import com.example.test0919.model.Product;
import com.example.test0919.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductDao productDao;

    @Override
    public List<Product> getData() {
        return productDao.getData();
    }

    @Override
    public Product getDataById(Integer productId) {
        return productDao.getDataById(productId);
    }

    @Override
    public Integer createData(CreateDataRequest createDataRequest) {
        return productDao.createData(createDataRequest);
    }

    @Override
    public void deleteData(String productName) {
        productDao.deleteData(productName);
    }

    @Override
    public void updateProduct(Integer productId, CreateDataRequest createDataRequest) {
        productDao.updateProduct(productId, createDataRequest);
    }

    @Override
    public void createAppUser(AppUser appUser) {
        productDao.createAppUser(appUser);
    }
}
