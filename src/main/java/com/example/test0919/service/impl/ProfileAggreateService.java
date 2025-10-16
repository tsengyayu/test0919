package com.example.test0919.service.impl;

import com.example.test0919.dao.Impl.EntitlementDaoImpl;
import com.example.test0919.dao.Impl.FunctionGroupDaoImpl;
import com.example.test0919.dao.Impl.ProfileDaoImpl;
import com.example.test0919.dto.EntitlementDto;
import com.example.test0919.dto.FunctionGroupDto;
import com.example.test0919.dto.ProfileDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProfileAggreateService {

    private final ProfileDaoImpl profileRepo;
    private final FunctionGroupDaoImpl fgRepo;
    private final EntitlementDaoImpl entRepo;

    public ProfileAggreateService(ProfileDaoImpl profileRepo,
                                   FunctionGroupDaoImpl fgRepo,
                                   EntitlementDaoImpl entRepo) {
        this.profileRepo = profileRepo;
        this.fgRepo = fgRepo;
        this.entRepo = entRepo;
    }

    public static record EntitlementReq(String tittName, List<String> de) {}
    public static record FunctionGroupReq(List<EntitlementReq> entitlement, String functionName, String description) {}
    public static record ProfileReq(String name, int age, List<FunctionGroupReq> functionGroup, boolean isContain) {}

    public ProfileDto toDomain(ProfileReq req) {
        ProfileDto p = new ProfileDto();
        p.setName(req.name());
        p.setAge(req.age());
        p.setContain(req.isContain());
        return p;
    }

    public List<FunctionGroupDto> toDomainGroups(ProfileReq req) {
        if (req.functionGroup() == null) return List.of();
        return req.functionGroup().stream().map(fgReq -> {
            FunctionGroupDto fg = new FunctionGroupDto();
            fg.setFunctionName(fgReq.functionName());
            fg.setDescription(fgReq.description());
            return fg;
        }).toList();
    }

    public List<List<EntitlementDto>> toDomainEnts(ProfileReq req) {
        if (req.functionGroup() == null) return List.of();
        return req.functionGroup().stream().map(fgReq -> {
            if (fgReq.entitlement() == null) return List.<EntitlementDto>of();
            return fgReq.entitlement().stream().map(eReq -> {
                EntitlementDto e = new EntitlementDto();
                e.setTittName(eReq.tittName());
                e.setDe(eReq.de());
                return e;
            }).toList();
        }).toList();
    }

    @Transactional
    public Long saveAggregate(ProfileReq req) {
        return saveAggregate(toDomain(req), toDomainGroups(req), toDomainEnts(req));
    }

//    建立單筆
    @Transactional
    public Long saveAggregate(ProfileDto root, List<FunctionGroupDto> groups, List<List<EntitlementDto>> entPerGroup) {
        Long profileId = profileRepo.create(root);

        for (int i = 0; i < groups.size(); i++) {
            FunctionGroupDto fg = groups.get(i);
            fg.setProfileId(profileId);

            Long fgId = fgRepo.create(fg);
            System.out.println(fgId);
            System.out.println("Success1");

            if (entPerGroup != null && i < entPerGroup.size()) {
                for (EntitlementDto e : entPerGroup.get(i)) {
                    System.out.println(e.getDe());
                    e.setFunctionGroupId(fgId);
                    entRepo.create(e);
                    System.out.println("success2");
                }
            }
        }
        return profileId;
    }

    @Transactional
    public void deleteProfileCascade(Long profileId) {
        // 外鍵已 ON DELETE CASCADE，其實只要刪 profile 即可
        profileRepo.deleteById(profileId);
    }

//    建立多筆
    public record ImportError(int index, String name, String message) {}
    public record ImportResult(int total, int success, List<ImportError> errors) {}

    public ImportResult importProfiles(List<ProfileReq> items) {
        int success = 0;
        List<ImportError> errors = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            ProfileReq req = items.get(i);
            try {
                saveAggregate(req);                  // 每筆為獨立交易（方法有 @Transactional）
                success++;
            } catch (Exception ex) {
                errors.add(new ImportError(i, req.name(), ex.getMessage()));
            }
        }
        return new ImportResult(items.size(), success, errors);
    }

    /* ---------- 查詢聚合（回傳巢狀） ---------- */

//    public static record EntitlementAgg(Long id, String tittName, List<String> de) {}
//    public static record FunctionGroupAgg(Long id, String functionName, String description,
//                                          List<EntitlementAgg> entitlement) {}
//    public static record ProfileAgg(Long id, String name, int age, boolean isContain,
//                                    List<FunctionGroupAgg> functionGroup) {}
//
//    @Transactional(readOnly = true)
//    public Optional<ProfileAgg> getAggregate(Long profileId) {
//        var pOpt = profileRepo.findById(profileId);
//        if (pOpt.isEmpty()) return Optional.empty();
//        var p = pOpt.get();
//
//        var groups = fgRepo.findByProfileId(profileId);
//        var groupIds = groups.stream().map(FunctionGroupDto::getId).toList();
//
//        var ents = groupIds.isEmpty() ? List.<EntitlementDto>of()
//                : entRepo.findByFunctionGroupIds(groupIds);
//
//        var entByGroupId = ents.stream()
//                .collect(Collectors.groupingBy(EntitlementDto::getFunctionGroupId));
//
//        var fgAgg = groups.stream().map(g -> {
//            var eList = entByGroupId.getOrDefault(g.getId(), List.of()).stream()
//                    .map(e -> new EntitlementAgg(e.getId(), e.getTittName(), e.getDe()))
//                    .toList();
//            return new FunctionGroupAgg(g.getId(), g.getFunctionName(), g.getDescription(), eList);
//        }).toList();
//
//        return Optional.of(new ProfileAgg(p.getId(), p.getName(), p.getAge(), p.isContain(), fgAgg));
//    }

}
