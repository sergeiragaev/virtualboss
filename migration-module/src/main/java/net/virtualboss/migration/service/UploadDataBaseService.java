package net.virtualboss.migration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class UploadDataBaseService {

    private final MigrationService service;

    @Value("${file.upload.path}")
    private String uploadPath;

    public String convertData(MultipartFile file) throws IOException {

        Path tempDir = service.asDirectoryOnDisk(uploadPath + UUID.randomUUID()).toPath();

        File zip = File.createTempFile(UUID.randomUUID().toString(), "temp", tempDir.toFile());

        FileOutputStream o = new FileOutputStream(zip);
        IOUtils.copy(file.getInputStream(), o);
        o.close();

        try (ZipFile zipFile = new ZipFile(zip)) {
            List<FileHeader> fileHeaders = zipFile.getFileHeaders();

            for (FileHeader fileHeader : fileHeaders) {
                String entryName = fileHeader.getFileName();
                String lowerEntryName = entryName.toLowerCase();

                Path entryPath = tempDir.resolve(lowerEntryName);
                Files.createDirectories(entryPath.getParent());

                zipFile.extractFile(fileHeader, tempDir.toString(), lowerEntryName);
            }
        } catch (ZipException e) {
            service.deleteDirectory(tempDir);
            log.info("Error extracting file {}: {}", file, e.getMessage());
        } finally {
            Files.delete(zip.toPath());
        }

        service.migrate(tempDir.toString());

        return "Data added";
    }

}
