package net.virtualboss.migration.dto;

import net.virtualboss.common.model.entity.Field;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FieldRowMapper implements RowMapper<Field> {

    @Override
    public Field mapRow(ResultSet rs, int rowNum) throws SQLException {
        Field field = new Field();
        field.setId(rs.getInt("id"));
        field.setName(rs.getString("name"));
        field.setAlias(rs.getString("alias"));
        field.setOrder(rs.getShort("order"));
        field.setDefaultValue(rs.getString("default_value"));
        return field;
    }
}