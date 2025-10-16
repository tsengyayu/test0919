package com.example.test0919.dao.Impl;

import com.example.test0919.dto.FunctionGroupDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Component
public class FunctionGroupDaoImpl {
    private final JdbcTemplate jdbc;

    public FunctionGroupDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long create(FunctionGroupDto fg) {
        String sql = "INSERT INTO function_groups (profile_id, function_name, description) VALUES (?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, fg.getProfileId());
            ps.setString(2, fg.getFunctionName());
            ps.setString(3, fg.getDescription());
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public Optional<FunctionGroupDto> findById(Long id) {
        String sql = "SELECT id, profile_id, function_name, description FROM function_groups WHERE id = ?";
        List<FunctionGroupDto> list = jdbc.query(sql, (rs, i) -> {
            FunctionGroupDto fg = new FunctionGroupDto();
            fg.setId(rs.getLong("id"));
            fg.setProfileId(rs.getLong("profile_id"));
            fg.setFunctionName(rs.getString("function_name"));
            fg.setDescription(rs.getString("description"));
            return fg;
        }, id);
        return list.stream().findFirst();
    }

    public List<FunctionGroupDto> findByProfileId(Long profileId) {
        String sql = "SELECT id, profile_id, function_name, description FROM function_groups WHERE profile_id = ?";
        return jdbc.query(sql, (rs, i) -> {
            FunctionGroupDto fg = new FunctionGroupDto();
            fg.setId(rs.getLong("id"));
            fg.setProfileId(rs.getLong("profile_id"));
            fg.setFunctionName(rs.getString("function_name"));
            fg.setDescription(rs.getString("description"));
            return fg;
        }, profileId);
    }

    public int update(FunctionGroupDto fg) {
        String sql = "UPDATE function_groups SET function_name=?, description=? WHERE id=?";
        return jdbc.update(sql, fg.getFunctionName(), fg.getDescription(), fg.getId());
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM function_groups WHERE id=?";
        return jdbc.update(sql, id);
    }

    public int deleteByProfileId(Long profileId) {
        String sql = "DELETE FROM function_groups WHERE profile_id=?";
        return jdbc.update(sql, profileId);
    }
}
