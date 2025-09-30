//package com.example.test0919.controller;
//
//import com.example.test0919.dto.CreateDataRequest;
//import com.example.test0919.error.NotFoundException;
//import com.example.test0919.model.AppUser;
//import com.example.test0919.model.Person;
//import com.example.test0919.model.Product;
//import com.example.test0919.service.PersonService;
//import com.example.test0919.service.ProductService;
//import com.example.test0919.util.Page;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//public class ProductController {
//    @Autowired
//    private ProductService productService;
//    @Autowired
//    private PersonService personService;
//
//    ObjectMapper mapper = new ObjectMapper();
//
//    @GetMapping("/api/getData")
//    public ResponseEntity<List<Product>> getData(){
//
//        List<Product> productList = productService.getData();
//        return ResponseEntity.status(HttpStatus.OK).body(productList);
//    }
//
//    @GetMapping("/api/getDataById/{productId}")
//    public ResponseEntity<Product> getDataById(@PathVariable Integer productId){
//        Product product = productService.getDataById(productId);
//        return ResponseEntity.status(HttpStatus.OK).body(product);
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping("/api/createData")
//    public ResponseEntity<Product> createData(@RequestBody CreateDataRequest createDataRequest){
//        Integer productId = productService.createData(createDataRequest);
//        Product product = productService.getDataByIdDao(productId);
//        return ResponseEntity.status(HttpStatus.CREATED).body(product);
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @DeleteMapping("/api/deleteData/{productName}")
//    public ResponseEntity<?> deleteData(@PathVariable String productName){
//        productService.deleteData(productName);
//
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @DeleteMapping("/api/deleteDataByLogic/{productName}")
//    public ResponseEntity<?> deleteDataByLogic(@PathVariable String productName){
//        productService.deleteDataByLogic(productName);
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @PutMapping("/api/updateData/{productId}")
//    public ResponseEntity<Product> updateData(@PathVariable Integer productId,
//                                              @RequestBody CreateDataRequest createDataRequest){
////        Product product = productService.getDataByIdDao(productId);
//        productService.updateProduct(productId, createDataRequest);
//        Product updateProduct = productService.getDataByIdDao(productId);
//        return ResponseEntity.status(HttpStatus.OK).body(updateProduct);
//    }
//
//    @PostMapping("/createAppUser")
//    public ResponseEntity<?> createAppUser(@RequestBody AppUser appUser){
//        productService.createAppUser(appUser);
//        return ResponseEntity.status(HttpStatus.CREATED).build();
//    }
//
////    @GetMapping("/getPerson")
////    public ResponseEntity<List<Person>> getPerson(){
////        List<Person> personList = personService.getPerson();
////        return ResponseEntity.status(HttpStatus.OK).body(personList);
////    }
////
////    public ResponseEntity<?> insertPersons(){
////
////        personService.insertPersons();
////        return ResponseEntity.status(HttpStatus.OK).build();
////    }
//}


package com.example.test0919.controller;

import com.example.test0919.dto.CreateDataRequest;
import com.example.test0919.error.NotFoundException;
import com.example.test0919.model.AppUser;
import com.example.test0919.model.Product;
import com.example.test0919.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Slf4j
@Validated
@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "取得所有產品")
    @GetMapping("/api/getData")
    public ResponseEntity<List<Product>> getData() {
        List<Product> productList = productService.getData();
        log.info("list_products count={}", productList.size());
        return ResponseEntity.ok(productList);
    }

    @Operation(
            summary = "依 ID 取得產品",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = Product.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            }
    )
    @GetMapping("/api/getDataById/{productId}")
    public ResponseEntity<Product> getById(
            @Parameter(description = "產品 ID") @PathVariable Integer productId) {

        Product product = productService.getDataById(productId);
        if (product == null) {
            log.info("get_product_not_found id={}", productId);
            throw new NotFoundException("Product not found: " + productId);
        }
        // 依你的實體欄位命名可自行加上名稱等資訊
        log.info("get_product id={}", productId);
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "新增產品（需 ADMIN）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/createData")
    public ResponseEntity<Product> createData(@Valid @RequestBody CreateDataRequest createDataRequest) {
        Integer productId = productService.createData(createDataRequest);
        Product product = productService.getDataByIdDao(productId);
        URI location = URI.create("/api/getDataById/" + productId);
        log.info("create_product id={}", productId);
        return ResponseEntity.created(location).body(product);
    }

    @Operation(summary = "刪除產品（實體刪除，依名稱，需 ADMIN）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/deleteData/{productName}")
    public ResponseEntity<Void> deleteData(@PathVariable String productName) {
        productService.deleteData(productName);
        log.info("delete_product_by_name name={}", productName);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "邏輯刪除產品（依名稱，需 ADMIN）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/deleteDataByLogic/{productName}")
    public ResponseEntity<Void> deleteDataByLogic(@PathVariable String productName) {
        productService.deleteDataByLogic(productName);
        log.info("logical_delete_product_by_name name={}", productName);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "更新產品（需 ADMIN）")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/updateData/{productId}")
    public ResponseEntity<Product> updateData(@PathVariable Integer productId,
                                              @Valid @RequestBody CreateDataRequest createDataRequest) {

        Product existing = productService.getDataByIdDao(productId);
        if (existing == null) {
            log.info("update_product_not_found id={}", productId);
        }
        productService.updateProduct(productId, createDataRequest);
        Product updateProduct = productService.getDataByIdDao(productId);
        log.info("update_product id={}", productId);
        return ResponseEntity.ok()
                .header(HttpHeaders.LOCATION, "/api/getDataById/" + productId)
                .body(updateProduct);
    }

    @Operation(summary = "新增 AppUser（示範）")
    @PostMapping("/createAppUser")
    public ResponseEntity<Void> createAppUser(@Valid @RequestBody AppUser appUser) {
        productService.createAppUser(appUser);
        log.info("create_app_user username={}", appUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
