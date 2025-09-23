package com.example.test0919.controller;

import com.example.test0919.dto.CreateDataRequest;
import com.example.test0919.model.Person;
import com.example.test0919.model.Product;
import com.example.test0919.service.PersonService;
import com.example.test0919.service.ProductService;
import com.example.test0919.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private PersonService personService;

    @GetMapping("/getData")
    public ResponseEntity<List<Product>> getData(){

        List<Product> productList = productService.getData();
        return ResponseEntity.status(HttpStatus.OK).body(productList);
    }

    @GetMapping("getDataById/{productId}")
    public ResponseEntity<Product> getDataById(@PathVariable Integer productId){
        Product product = productService.getDataById(productId);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

//    public  ResponseEntity<Page<Product>> getProductsBylimit(){
//
//    }

    @PostMapping("/createData")
    public ResponseEntity<Product> createData(@RequestBody CreateDataRequest createDataRequest){
        Integer productId = productService.createData(createDataRequest);
        Product product = productService.getDataById(productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @DeleteMapping("/deleteData/{productName}")
    public ResponseEntity<?> deleteData(@PathVariable String productName){
        productService.deleteData(productName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/updateData/{productId}")
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

    @GetMapping("/getPerson")
    public ResponseEntity<List<Person>> getPerson(){
        List<Person> personList = personService.getPerson();
        return ResponseEntity.status(HttpStatus.OK).body(personList);
    }

    public ResponseEntity<?> insertPersons(){

        personService.insertPersons();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
