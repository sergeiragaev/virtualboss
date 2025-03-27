package net.virtualboss.contact.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.contact.service.ContactService;
import net.virtualboss.contact.web.dto.UpsertContactRequest;
import net.virtualboss.common.web.dto.filter.CommonFilter;
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
            CommonFilter commonFilter) {
        return ResponseEntity.ok(service.findAll(fields, commonFilter));
    }

    @GetMapping("/contact/{id}")
    public ResponseEntity<Map<String, Object>> getContactById(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/contact/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteContact(@PathVariable String id) {
        service.deleteContact(id);
    }

    @PutMapping("/contact/{id}")
    public ResponseEntity<Map<String, Object>> saveContact(
            @PathVariable String id,
            UpsertContactRequest request,
            CustomFieldsAndLists customFieldsAndLists) {
        return ResponseEntity.ok(service.saveContact(id, request, customFieldsAndLists));
    }

    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> createContact(
            UpsertContactRequest request,
            CustomFieldsAndLists customFieldsAndLists) {
        return new ResponseEntity<>(service.createContact(request, customFieldsAndLists), HttpStatus.CREATED);
    }

}
