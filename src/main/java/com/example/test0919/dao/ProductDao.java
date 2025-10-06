package com.example.test0919.dao;

import com.example.test0919.dto.CreateDataRequest;
import com.example.test0919.model.AppUser;
import com.example.test0919.model.Product;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

public interface ProductDao {
    public List<Product> getData();

    public Product getDataById(Integer productId);

    public Product getDataByIdDao(Integer productId);

    public Integer createData(CreateDataRequest createDataRequest);

    public void deleteData(String productName);

    public void updateProduct(Integer productId, CreateDataRequest createDataRequest);

    public void createAppUser(AppUser appUser);

    public void deleteDataByLogic(String productName);

    public Product getDataByName(String productName);

    public Map<String, Object> batchInsert(List<Map<String, Object>> createDataRequestsList) throws JsonProcessingException;
}

