package net.virtualboss.migration.service;

import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.MigrationException;
import net.virtualboss.migration.config.MigrationConfig;
import net.virtualboss.field.service.FieldService;
import net.virtualboss.field.web.dto.FieldDto;
import net.virtualboss.migration.processor.EntityProcessor;
import net.virtualboss.migration.processor.relation.RelationProcessor;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class MigrationService {
    private final FieldService fieldService;
    private final DBFReaderFactory dbfReaderFactory;
    private final DatabaseSaver databaseSaver;

    private final Map<String, EntityProcessor> processors;
    private final MigrationConfig migrationConfig;

    private final RelationProcessor relationProcessor;

    @Value("${migration.test-data-path}")
    private String testDataPath;

    private final ResourceLoader resourceLoader;

    public void migrate(String dataPath) {

        String pathToUse = Optional.ofNullable(dataPath)
                .filter(p -> !p.isBlank())
                .orElse(testDataPath);

        try {
            File baseDir = asDirectoryOnDisk(pathToUse);
            String finalDataPath = baseDir.getAbsolutePath();

            migrateFields(finalDataPath);
            databaseSaver.preloadCaches();

            migrationConfig.getEntities().forEach((entityName, config) -> {
                EntityProcessor processor = processors.get(entityName + "Processor");
                processDBF(finalDataPath, config, processor);
            });

            migrationConfig.getRelations().forEach(relation -> {
                if (relation.getFrom().hasSourceFile()) {
                    relationProcessor.processExternalFile(finalDataPath, relation);
                } else {
                    relationProcessor.processEmbeddedField(finalDataPath, relation);
                }
            });
        } catch (IOException ex) {
            throw new MigrationException(ex.getLocalizedMessage());
        }
    }

    private File asDirectoryOnDisk(String path) throws IOException {
        File maybe = new File(path);
        if (maybe.exists() && maybe.isDirectory()) {
            return maybe;
        }

        String base = path.replaceFirst("^classpath:", "").replaceFirst("^/+", "");
        String pattern = "classpath*:" + base + "/**/*";

        Path tempDir = createSecureTempDirectory("migration-data-");

        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver(resourceLoader);
        Resource[] resources = resolver.getResources(pattern);

        for (Resource res : resources) {
            String filename = res.getFilename();
            String uri = res.getURI().toString();
            int idx = uri.indexOf(base);

            if (filename == null || !res.isReadable() || idx < 0) {
                continue;
            }

            String rel = uri.substring(idx + base.length()).replaceFirst("^/+", "");
            Path target = tempDir.resolve(rel);
            Files.createDirectories(target.getParent());
            try (InputStream in = res.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return tempDir.toFile();
    }

    private Path createSecureTempDirectory(String prefix) throws IOException {
        File secureBaseDir = new File("vbSecureDirectory");
        if (!secureBaseDir.exists() && !secureBaseDir.mkdirs()) {
            throw new IOException("Cannot create secure base dir: " + secureBaseDir);
        }

        Path tempDir;
        if (SystemUtils.IS_OS_UNIX) {
            FileAttribute<Set<PosixFilePermission>> attr =
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
            tempDir = Files.createTempDirectory(secureBaseDir.toPath(), prefix, attr);
        } else {
            tempDir = Files.createTempDirectory(secureBaseDir.toPath(), prefix);
            File dir = tempDir.toFile();
            boolean ok = dir.setReadable(true, true)
                         && dir.setWritable(true, true)
                         && dir.setExecutable(true, true);
            if (!ok) {
                throw new IOException("Failed to set owner‑only perms on " + tempDir);
            }
        }

        return tempDir;
    }

    private void processDBF(String path, MigrationConfig.EntityConfig config, EntityProcessor processor) {
        try (DBFReader reader = dbfReaderFactory.createReader(
                path + "/" + config.getDbfFile(),
                config.getMemoFile() == null ? null : path + "/" + config.getMemoFile())) {
            DBFRow row;
            while ((row = reader.nextRow()) != null) {
                processor.process(row);
            }
            databaseSaver.flushAll();
        } catch (Exception e) {
            log.error("There is error occurred while migrate data: " + e.getLocalizedMessage(), e);
        }
    }

    private void migrateFields(String path) {
        try (DBFReader reader =
                     dbfReaderFactory.createReader(path + "/ctfields.dbf", null)) {
            updateFields(reader);
        } catch (Exception e) {
            log.error("There is error occurred while parsing ctfields.dbf: {}", e.getLocalizedMessage());
        }
    }

    private void updateFields(DBFReader reader) {
        DBFRow row;
        while ((row = reader.nextRow()) != null) {
            String name = row.getString("FI_VARNAME");
            try {
                FieldDto fieldDto = FieldDto.builder()
                        .name(name)
                        .defaultValue(row.getString("FI_NAME"))
                        .alias(row.getString("FI_ALIAS"))
                        .enabled(row.getBoolean("FI_RVBLIST"))
                        .fieldOrder((short) row.getInt("FI_RVBORDR"))
                        .build();
                fieldService.updateField(fieldDto);
            } catch (Exception e) {
                log.error("There is error occurred while parsing fields info for field {}: {}", name, e.getLocalizedMessage());
            }
        }
    }
}
