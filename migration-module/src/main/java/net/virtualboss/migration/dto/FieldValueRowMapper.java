package net.virtualboss.migration.dto;

import net.virtualboss.common.web.dto.CustomValueDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FieldValueRowMapper implements RowMapper<CustomValueDto> {


    @Override
    public CustomValueDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        CustomValueDto fieldValue = new CustomValueDto();
        fieldValue.setId(rs.getLong("id"));
        fieldValue.setValue(rs.getString("value"));
        fieldValue.setFieldId(rs.getInt("field_id"));
        return fieldValue;
    }
}