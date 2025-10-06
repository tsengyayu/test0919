package com.example.test0919.service;

import com.example.test0919.dto.CreateDataRequest;
import com.example.test0919.model.AppUser;
import com.example.test0919.model.Product;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ProductService {

    public List<Product> getData();

    public Product getDataById(Integer productId);

    public Product getDataByIdDao(Integer productId);

    public Product getDataByName(String productName);

    public  Integer createData(CreateDataRequest createDataRequest);

    public void deleteData(String productName);

    public void updateProduct(Integer productId, CreateDataRequest createDataRequest);

    public void createAppUser(AppUser appUser);

    public void deleteDataByLogic(String productName);

    public Map<String, Object> batchInsert(MultipartFile file) throws IOException;
}
