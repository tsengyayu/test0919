package com.example.test0919.dao.Impl;

import com.example.test0919.dto.EntitlementDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Component
public class EntitlementDaoImpl {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public EntitlementDaoImpl(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public Long create(EntitlementDto e) {
        String sql = "INSERT INTO entitlements (function_group_id, titt_name, de) VALUES (?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        System.out.println();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, e.getFunctionGroupId());
            ps.setString(2, e.getTittName());
            try {
                ps.setString(3, mapper.writeValueAsString(e.getDe())); // JSON array
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public Optional<EntitlementDto> findById(Long id) {
        String sql = "SELECT id, function_group_id, titt_name, de FROM entitlements WHERE id = ?";
        List<EntitlementDto> list = jdbc.query(sql, (rs, i) -> {
            EntitlementDto e = new EntitlementDto();
            e.setId(rs.getLong("id"));
            e.setFunctionGroupId(rs.getLong("function_group_id"));
            e.setTittName(rs.getString("titt_name"));
            try {
                List<String> de = mapper.readValue(rs.getString("de"), new TypeReference<List<String>>() {});
                e.setDe(de);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return e;
        }, id);
        return list.stream().findFirst();
    }

    public List<EntitlementDto> findByFunctionGroupId(Long fgId) {
        String sql = "SELECT id, function_group_id, titt_name, de FROM entitlements WHERE function_group_id = ?";
        return jdbc.query(sql, (rs, i) -> {
            EntitlementDto e = new EntitlementDto();
            e.setId(rs.getLong("id"));
            e.setFunctionGroupId(rs.getLong("function_group_id"));
            e.setTittName(rs.getString("titt_name"));
            try {
                e.setDe(mapper.readValue(rs.getString("de"), new TypeReference<List<String>>() {}));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return e;
        }, fgId);
    }

    public int update(EntitlementDto e) {
        String sql = "UPDATE entitlements SET titt_name=?, de=? WHERE id=?";
        try {
            return jdbc.update(sql, e.getTittName(), mapper.writeValueAsString(e.getDe()), e.getId());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM entitlements WHERE id=?";
        return jdbc.update(sql, id);
    }

    public int deleteByFunctionGroupId(Long fgId) {
        String sql = "DELETE FROM entitlements WHERE function_group_id=?";
        return jdbc.update(sql, fgId);
    }
}
