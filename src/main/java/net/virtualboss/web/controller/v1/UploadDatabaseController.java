package net.virtualboss.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.service.migration.UploadDataBaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
public class UploadDatabaseController {
    private final UploadDataBaseService service;

    @PostMapping(value = "/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(service.convertData(file));
    }
}
