package com.example.test0919.service;

import com.example.test0919.dto.CreateDataRequest;
import com.example.test0919.model.Product;

import java.util.List;

public interface ProductService {

    public List<Product> getData();

    public Product getDataById(Integer productId);

    public  Integer createData(CreateDataRequest createDataRequest);

    public void deleteData(String productName);

    public void updateProduct(Integer productId, CreateDataRequest createDataRequest);
}
