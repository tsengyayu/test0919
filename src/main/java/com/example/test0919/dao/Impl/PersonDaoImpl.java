package com.example.test0919.dao.Impl;

import com.example.test0919.dao.PersonDao;
import com.example.test0919.model.Person;
import com.example.test0919.model.Product;
import com.example.test0919.rowMapper.PersonRowMapper;
import com.example.test0919.rowMapper.ProductRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PersonDaoImpl implements PersonDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<Person> getPerson() {
        String sql = "SELECT id, name, age, isMarried, friends, family FROM person";
//        Map<String, Objects> map = new HashMap<>();

        List<Person> personList = namedParameterJdbcTemplate.query(sql, new PersonRowMapper());
        return personList;
    }
}
