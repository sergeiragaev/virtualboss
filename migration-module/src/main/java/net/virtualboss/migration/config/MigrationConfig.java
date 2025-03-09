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
    private List<RelationConfig> relations;

    @Getter
    @Setter
    public static class EntityConfig {
        private String name;
        private String table;
        private String dbfFile;
        private String memoFile;
        private String idField;
        private String idFieldSource;
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
        private boolean assigned;
        private boolean unique;
    }

    @Getter
    @Setter
    public static class CustomFieldMapping {
        private String source;
        private String target;
    }

    @Getter
    @Setter
    public static class RelationConfig {
        private String name;
        private String type;
        private String joinTable;
        private RelationMapping from;
        private RelationMapping to;
    }

    @Getter
    @Setter
    public static class RelationMapping {
        private String entity;
        private String column;
        private String sourceFile;
        private String sourceField;
        private String separator;
        private String type;

        public boolean hasSourceFile() {
            return sourceFile != null && !sourceFile.isBlank();
        }
    }
}