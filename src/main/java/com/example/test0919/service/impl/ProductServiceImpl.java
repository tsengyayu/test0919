package com.example.test0919.service.impl;

import com.example.test0919.aop.Auditable;
import com.example.test0919.dao.ProductDao;
import com.example.test0919.dto.CreateDataRequest;
import com.example.test0919.dto.CreateDataRequest;
import com.example.test0919.error.NotFoundException;
import com.example.test0919.model.AppUser;
import com.example.test0919.model.Product;
import com.example.test0919.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductDao productDao;

    @Auditable(action = "READ", fixedEntityId = "ALL_PRODUCTS")
    @Override
    public List<Product> getData() {
        return productDao.getData();
    }
    @Auditable(action = "READ", idArg = "productId")
    @Override
    public Product getDataById(Integer productId) {
        Product p = productDao.getDataById(productId);
        if (p == null) throw new NotFoundException("Product %d not found".formatted(productId));
        return p;
    }

    @Override
    public Product getDataByIdDao(Integer productId) {
        return productDao.getDataById(productId);
    }

    @Transactional
    @Auditable(action = "CREATE", idFromReturn = true, loader = "getDataById")
    @Override
    public Integer createData(CreateDataRequest createDataRequest) {
        Integer id = productDao.createData(createDataRequest);
        if (id == null) throw new RuntimeException("Create failed");
        return id;
    }

    @Override
    public Product getDataByName(String productName) {
        return productDao.getDataByName(productName);
    }

    @Transactional
    @Auditable(action = "DELETE", idArg = "productName", loader = "getDataByName")
    @Override
    public void deleteData(String productName) {
        productDao.deleteData(productName);
    }

    @Transactional
    @Auditable(action = "UPDATE", idArg = "productId", loader = "getDataByIdDao")
    @Override
    public void updateProduct(Integer productId, CreateDataRequest createDataRequest) {
        Product before = productDao.getDataByIdDao(productId);
        System.out.println(before);
        if (before == null) throw new NotFoundException("Product %d not found".formatted(productId));
        productDao.updateProduct(productId, createDataRequest);
    }

    @Transactional
    @Override
    public void createAppUser(AppUser appUser) {
        productDao.createAppUser(appUser);
    }

    @Override
    public void deleteDataByLogic(String productName) {
        productDao.deleteDataByLogic(productName);
    }

    @Override
    public Map<String, Object> batchInsert(MultipartFile file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = file.getInputStream();
        List<Map<String, Object>> productList = mapper.readValue(
                inputStream, new TypeReference<List<Map<String, Object>>>() {}
        );
        return productDao.batchInsert(productList);
    }
}
