package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class UploadDataBaseService {

    private final MigrationService service;

    public String convertData(MultipartFile file) throws IOException {

        File zip = File.createTempFile(UUID.randomUUID().toString(), "temp");
        FileOutputStream o = new FileOutputStream(zip);
        IOUtils.copy(file.getInputStream(), o);
        o.close();

        String destination = "src/main/resources/db/temp";
        try (ZipFile zipFile = new ZipFile(zip)) {
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            log.info("Error occurred while extracting from file {}", file);
        } finally {
            Files.delete(zip.toPath());
        }

        service.migrate(destination);

        return "Data added";
    }
}
