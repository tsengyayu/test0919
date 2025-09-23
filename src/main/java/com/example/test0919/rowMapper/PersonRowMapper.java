package com.example.test0919.rowMapper;

import com.example.test0919.model.Person;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PersonRowMapper implements RowMapper<Person> {
    @Override
    public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
        Person person = new Person();
        person.setId(rs.getString("id"));
        person.setName(rs.getString("name"));
        person.setAge(rs.getInt("age"));
        person.setMarried(rs.getBoolean("isMarried"));
        person.setFriedns(rs.getString("friends"));
        person.setFamily(rs.getString("family"));
        return person;
    }
}
