package com.example.test0919.dao.Impl;

import com.example.test0919.dto.ProfileDto;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Component
public class ProfileDaoImpl {

    private final JdbcTemplate jdbc;

    public ProfileDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long create(ProfileDto p) {
        String sql = "INSERT INTO profiles (name, age, is_contain) VALUES (?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getName());
            ps.setInt(2, p.getAge());
            ps.setBoolean(3, p.isContain());
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public Optional<ProfileDto> findById(Long id) {
        String sql = "SELECT id, name, age, is_contain FROM profiles WHERE id = ?";
        List<ProfileDto> list = jdbc.query(sql, (rs, i) -> {
            ProfileDto p = new ProfileDto();
            p.setId(rs.getLong("id"));
            p.setName(rs.getString("name"));
            p.setAge(rs.getInt("age"));
            p.setContain(rs.getBoolean("is_contain"));
            return p;
        }, id);
        return list.stream().findFirst();
    }

    public List<ProfileDto> findAll() {
        String sql = "SELECT id, name, age, is_contain FROM profiles";
        return jdbc.query(sql, (rs, i) -> {
            ProfileDto p = new ProfileDto();
            p.setId(rs.getLong("id"));
            p.setName(rs.getString("name"));
            p.setAge(rs.getInt("age"));
            p.setContain(rs.getBoolean("is_contain"));
            return p;
        });
    }

    public int update(ProfileDto p) {
        String sql = "UPDATE profiles SET name=?, age=?, is_contain=? WHERE id=?";
        return jdbc.update(sql, p.getName(), p.getAge(), p.isContain(), p.getId());
    }

    public int deleteById(Long id) {
        // 有外鍵 ON DELETE CASCADE，刪除 profile 會連帶刪除 group/entitlement
        String sql = "DELETE FROM profiles WHERE id=?";
        return jdbc.update(sql, id);
    }
}
