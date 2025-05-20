package net.virtualboss.contact.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.contact.service.ContactService;
import net.virtualboss.contact.web.dto.ContactReferencesRequest;
import net.virtualboss.contact.web.dto.UpsertContactRequest;
import net.virtualboss.common.web.dto.filter.CommonFilter;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@CacheConfig(cacheNames = "contact")
public class ContactController {
    private final ContactService service;

    @GetMapping("/contact")
    public ResponseEntity<Page<Map<String, Object>>> getContacts(
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
            CustomFieldsAndLists customFieldsAndLists,
            ContactReferencesRequest referencesRequest) {
        return ResponseEntity.ok(service.saveContact(id, request, customFieldsAndLists, referencesRequest));
    }

    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> createContact(
            UpsertContactRequest request,
            CustomFieldsAndLists customFieldsAndLists,
            ContactReferencesRequest referencesRequest) {
        return new ResponseEntity<>(
                service.createContact(request, customFieldsAndLists, referencesRequest), HttpStatus.CREATED);
    }

}
