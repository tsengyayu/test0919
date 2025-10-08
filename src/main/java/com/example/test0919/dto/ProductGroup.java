package com.example.test0919.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProductGroup(@JsonProperty("group") String name, List<ProductSlim> items) {}
