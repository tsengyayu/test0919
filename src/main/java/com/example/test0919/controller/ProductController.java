package com.example.test0919.controller;

import com.example.test0919.dto.CreateDataRequest;
import com.example.test0919.model.AppUser;
import com.example.test0919.model.Person;
import com.example.test0919.model.Product;
import com.example.test0919.service.PersonService;
import com.example.test0919.service.ProductService;
import com.example.test0919.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private PersonService personService;

    @GetMapping("/api/getData")
    public ResponseEntity<List<Product>> getData(){

        List<Product> productList = productService.getData();
        return ResponseEntity.status(HttpStatus.OK).body(productList);
    }

    @GetMapping("/api/getDataById/{productId}")
    public ResponseEntity<Product> getDataById(@PathVariable Integer productId){
        Product product = productService.getDataById(productId);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/createData")
    public ResponseEntity<Product> createData(@RequestBody CreateDataRequest createDataRequest){
        Integer productId = productService.createData(createDataRequest);
        Product product = productService.getDataById(productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/deleteData/{productName}")
    public ResponseEntity<?> deleteData(@PathVariable String productName){
        productService.deleteData(productName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/updateData/{productId}")
    public ResponseEntity<Product> updateData(@PathVariable Integer productId,
                                              @RequestBody CreateDataRequest createDataRequest){
        Product product = productService.getDataById(productId);
        if(product == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        productService.updateProduct(productId, createDataRequest);
        Product updateProduct = productService.getDataById(productId);
        return ResponseEntity.status(HttpStatus.OK).body(updateProduct);
    }

    @PostMapping("/createAppUser")
    public ResponseEntity<?> createAppUser(@RequestBody AppUser appUser){
        productService.createAppUser(appUser);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

//    @GetMapping("/getPerson")
//    public ResponseEntity<List<Person>> getPerson(){
//        List<Person> personList = personService.getPerson();
//        return ResponseEntity.status(HttpStatus.OK).body(personList);
//    }
//
//    public ResponseEntity<?> insertPersons(){
//
//        personService.insertPersons();
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }
}
