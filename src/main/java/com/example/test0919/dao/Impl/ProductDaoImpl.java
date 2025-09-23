package com.example.test0919.dao.Impl;

import com.example.test0919.dao.ProductDao;
import com.example.test0919.dto.CreateDataRequest;
import com.example.test0919.model.Product;
import com.example.test0919.rowMapper.ProductRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ProductDaoImpl implements ProductDao {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<Product> getData() {
        String sql = "SELECT id, name, price FROM products";
//        Map<String, Objects> map = new HashMap<>();

        List<Product> productList = namedParameterJdbcTemplate.query(sql, new ProductRowMapper());
        return productList;
    }

    @Override
    public Product getDataById(Integer productId) {
        String sql = "SELECT id, name, price FROM products WHERE id=:productId";
        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);

        List<Product> productList = namedParameterJdbcTemplate.query(sql, map, new ProductRowMapper());

        if(productList.size() > 0){
            return productList.get(0);
        }
        else{
            return null;
        }
    }

    @Override
    public Integer createData(CreateDataRequest createDataRequest) {
        String sql = "INSERT INTO products(name, price) VALUES (:name, :price)";
        Map<String, Object> map = new HashMap<>();
        map.put("name", createDataRequest.getName());
        map.put("price", createDataRequest.getPrice());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);
        Integer productId = keyHolder.getKey().intValue();
        return productId;
    }

    @Override
    public void deleteData(String productName) {
        String sql = "DELETE FROM products WHERE name=:productName";
        Map<String, Object> map = new HashMap<>();
        map.put("productName", productName);
        namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public void updateProduct(Integer productId, CreateDataRequest createDataRequest) {
        String sql = "UPDATE products SET name=:productName, price=:productPrice " +
                "WHERE id=:productId";
        Map<String, Object> map = new HashMap<>();
        map.put("productName", createDataRequest.getName());
        map.put("productPrice", createDataRequest.getPrice());
        map.put("productId", productId);
        namedParameterJdbcTemplate.update(sql, map);
    }
}
