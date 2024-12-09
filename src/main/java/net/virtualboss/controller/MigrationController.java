package net.virtualboss.controller;

import lombok.RequiredArgsConstructor;
import net.virtualboss.service.MigrationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequiredArgsConstructor
public class MigrationController {
    private final MigrationService service;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/migrate")
    public void migrate() {
        service.migrate(null);
    }

}
