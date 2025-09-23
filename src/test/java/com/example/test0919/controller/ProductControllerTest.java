package com.example.test0919.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getById() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/students/3");

        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(3))
                .andDo(print())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString();
    }

    public void create() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n"+
                        " \"name\":  \"HANK\",\n" +
                        " \"score\":  14.6,\n" +
                        " \"graduate\": false\n" +
                        "}");
        mockMvc.perform(requestBuilder)
                .andExpect(status().is(201));
    }


}