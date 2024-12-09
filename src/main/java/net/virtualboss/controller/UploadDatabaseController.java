package net.virtualboss.controller;

import lombok.RequiredArgsConstructor;
import net.virtualboss.service.UploadDataBaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;


@RestController
@RequiredArgsConstructor
public class UploadDatabaseController {
    private final UploadDataBaseService service;

    @PostMapping(value ="/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException, SQLException {


        return ResponseEntity.ok(service.convertData(file));
    }
}
