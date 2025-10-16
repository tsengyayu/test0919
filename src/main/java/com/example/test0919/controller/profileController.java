package com.example.test0919.controller;


import com.example.test0919.dto.EntitlementDto;
import com.example.test0919.dto.FunctionGroupDto;
import com.example.test0919.dto.ProfileDto;
import com.example.test0919.service.impl.ProfileAggreateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.test0919.service.impl.ProfileAggreateService.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.util.List;

@RestController
@Validated
@RequestMapping("/profiles")
public class profileController {
//    private record EntitlementReq(String tittName, List<String> de) {}
//    private record FunctionGroupReq(List<EntitlementReq> entitlement, String functionName, String description) {}
//    private record ProfileReq(String name, int age, List<FunctionGroupReq> functionGroup, boolean isContain) {}

    private final ProfileAggreateService service;
    private final ObjectMapper mapper;

    public profileController(ObjectMapper mapper, ProfileAggreateService service) {
        this.mapper = mapper;
        this.service = service;
    }


    @PostMapping("/createbatch")
    @PreAuthorize("hasRole('ADMIN')")
    public Long create(@RequestBody ProfileReq req) {
        ProfileDto p = new ProfileDto();
        p.setName(req.name());
        p.setAge(req.age());
        p.setContain(req.isContain());

        List<FunctionGroupDto> groups = req.functionGroup().stream().map(fgReq -> {
            FunctionGroupDto fg = new FunctionGroupDto();
            fg.setFunctionName(fgReq.functionName());
            fg.setDescription(fgReq.description());
            return fg;
        }).toList();

        List<List<EntitlementDto>> entPerGroup = req.functionGroup().stream().map(fgReq ->
                fgReq.entitlement().stream().map(eReq -> {
                    EntitlementDto e = new EntitlementDto();
                    e.setTittName(eReq.tittName());
                    e.setDe(eReq.de());
                    return e;
                }).toList()
        ).toList();

        return service.saveAggregate(p, groups, entPerGroup);
    }

//    @PostMapping("/batch")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ProfileAggreateService.ImportResult> createBatch(@RequestBody List<ProfileReq> reqs) {
//        ProfileAggreateService.ImportResult result = service.importProfiles(reqs);
//        boolean allOk = (result.total() == result.success());
//        return ResponseEntity.status(allOk ? HttpStatus.CREATED : HttpStatus.MULTI_STATUS).body(result);
//    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importJson(
            @RequestParam("file") MultipartFile file,
            UriComponentsBuilder uri) throws Exception {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "file is empty"));
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".json")) {
            // 非必需，但可防呆
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "only .json files are allowed"));
        }

        try (InputStream in = file.getInputStream()) {
            JsonNode root = mapper.readTree(in);

            // 1) 內容是「單筆物件」
            if (root.isObject()) {
                ProfileReq req = mapper.treeToValue(root, ProfileReq.class);
                Long id = service.saveAggregate(req);
                var location = uri.path("/profiles/{id}").build(id);
                return ResponseEntity.created(location).body(java.util.Map.of("id", id));
            }
            // 2) 內容是「陣列」→ 批次
            if (root.isArray()) {
                // 直接轉成 List<ProfileReq>
                List<ProfileReq> list = mapper.readerForListOf(ProfileReq.class).readValue(root);
                ImportResult result = service.importProfiles(list);
                boolean allOk = (result.total() == result.success());
                return ResponseEntity
                        .status(allOk ? HttpStatus.CREATED : HttpStatus.MULTI_STATUS)
                        .body(result);
            }
            // 3) 其他型態不接受
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "JSON must be an object or an array"));
        }
    }
}
