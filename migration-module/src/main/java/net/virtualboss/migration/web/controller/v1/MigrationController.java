package net.virtualboss.migration.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.migration.service.MigrationService;
import net.virtualboss.migration.service.UploadDataBaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
public class MigrationController {
    private final MigrationService service;
    private final UploadDataBaseService uploadDataBaseService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/migrate")
    public void migrate() {
        service.migrate(null);
    }


    @PostMapping(value = "/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(uploadDataBaseService.convertData(file));
    }

}
