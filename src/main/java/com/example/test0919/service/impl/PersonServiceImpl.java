package com.example.test0919.service.impl;

import com.example.test0919.dao.PersonDao;
import com.example.test0919.model.Person;
import com.example.test0919.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PersonServiceImpl implements PersonService {
    @Autowired
    PersonDao personDao;

    @Override
    public List<Person> getPerson() {
        return personDao.getPerson();
    }
}
