package net.virtualboss.controller;

import lombok.RequiredArgsConstructor;
import net.virtualboss.model.dto.ContactDto;
import net.virtualboss.service.ContactService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class ContactController {
    private final ContactService service;

    @GetMapping("/contactfeed")
    public ResponseEntity<List<ContactDto>> contactFeed() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/contactdata")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ContactDto[]> contactData(@RequestParam String ContactId) {
        return ResponseEntity.ok(service.findById(ContactId));
    }

}
