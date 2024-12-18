package net.virtualboss.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.web.dto.contact.ContactResponse;
import net.virtualboss.service.ContactService;
import net.virtualboss.web.dto.contact.UpsertContactRequest;
import net.virtualboss.web.dto.filter.Filter;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@CacheConfig(cacheNames = "contact")
public class ContactController {
    private final ContactService service;

    @GetMapping("/contact")
    public ResponseEntity<List<Map<String, Object>>> getContacts(
            @RequestParam(required = false) String fields,
            Filter filter) {
        return ResponseEntity.ok(service.findAll(fields, filter));
    }

    @GetMapping("/contact/{id}")
    public ResponseEntity<ContactResponse> getContactById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/contact/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteContact(@PathVariable String id) {
        service.deleteContact(id);
    }

    @PutMapping("/contact/{id}")
    public ResponseEntity<ContactResponse> saveContact(
            @PathVariable String id,
            UpsertContactRequest request) {
        return ResponseEntity.ok(service.saveContact(id, request));
    }

    @PostMapping("/contact")
    public ResponseEntity<ContactResponse> createContact(UpsertContactRequest request) {
        return ResponseEntity.ok(service.createContact(request));
    }

}
