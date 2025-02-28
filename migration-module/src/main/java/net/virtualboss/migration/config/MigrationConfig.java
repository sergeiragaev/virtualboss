package net.virtualboss.migration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "migration")
@Getter
@Setter
public class MigrationConfig {
    private Map<String, EntityConfig> entities;
    private Integer batchSize;

    @Getter
    @Setter
    public static class EntityConfig {
        private String name;
        private String table;
        private String dbfFile;
        private String memoFile;
        private List<ColumnMapping> columns;
        private List<CustomFieldMapping> customFields;
    }

    @Getter
    @Setter
    public static class ColumnMapping {
        private String name;
        private String source;
        private String type;
        private String processor;
        private boolean generated;
        private String reference;
        private boolean unique;
    }

    @Getter
    @Setter
    public static class CustomFieldMapping {
        private String source;
        private String target;
    }
}