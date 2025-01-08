package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.exception.AccessDeniedException;
import net.virtualboss.mapper.v1.contact.ContactMapperV1;
import net.virtualboss.util.BeanUtils;
import net.virtualboss.repository.criteria.ContactFilterCriteria;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import net.virtualboss.web.dto.contact.ContactResponse;
import net.virtualboss.model.entity.Contact;
import net.virtualboss.repository.ContactRepository;
import net.virtualboss.web.dto.contact.UpsertContactRequest;
import net.virtualboss.web.dto.filter.CommonFilter;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ContactService {
    private final ContactRepository repository;
    private final ContactMapperV1 mapper;
    private final MainService mainService;

    @Cacheable(value = "contact", key = "#id")
    public Map<String, Object> findById(String id) {
        return ContactResponse.getFieldsMap(mapper.contactToResponse(mainService.getContactById(id)), null);
    }

    public List<Map<String, Object>> findAll(String fields, CommonFilter commonFilter) {
        if (fields == null) fields = "ContactId,ContactPerson";
        Set<String> fieldList = Arrays.stream(fields.split(",")).collect(Collectors.toSet());

        if (commonFilter.getSize() == null) commonFilter.setSize(Integer.MAX_VALUE);
        if (commonFilter.getPage() == null) commonFilter.setPage(1);
        if (commonFilter.getSort() == null) commonFilter.setSort("firstName asc,lastName asc,company asc");

        String[] sorts = commonFilter.getSort().split(",");
        List<Sort.Order> orders = new ArrayList<>();
        for (String sort : sorts) {
            String[] order = sort.split(" ");
            orders.add(new Sort.Order(Sort.Direction.valueOf(order[1].toUpperCase()), order[0]));
        }


        return repository.findAll(
                ContactFilterCriteria.builder()
                        .findString(commonFilter.getFindString() == null || commonFilter.getFindString().isBlank() ? null : commonFilter.getFindString())
                        .showUnassigned(false)
                        .isDeleted(commonFilter.getIsDeleted())
                        .build().getSpecification(),
                PageRequest.of(commonFilter.getPage() - 1, commonFilter.getSize(),
                        Sort.by(orders)
                ))
                .map(mapper::contactToResponse).getContent().stream()
                .map(contactResponse -> ContactResponse.getFieldsMap(contactResponse, fieldList))
                .toList();
    }

    @Transactional
    @CacheEvict(value = "contact", key = "#id")
    public void deleteContact(String id) {
        Contact contact = mainService.getContactById(id);
        mainService.reassignTasksContact(contact);
        contact.setIsDeleted(true);
        repository.save(contact);
    }

    @Transactional
    @CachePut(value = "contact", key = "#id")
    public Map<String, Object> saveContact(String id, UpsertContactRequest request, CustomFieldsAndLists customFieldsAndLists) {
        Contact unassigned = mainService.getContactById(null);
        if (unassigned.getId().toString().equals(id))
            throw new AccessDeniedException("Cannot update Unassigned contact");
        Contact contact = mapper.requestToContact(id, request, customFieldsAndLists);
        Contact contactFromDB = mainService.getContactById(id);
        contact.getCustomFieldsAndListsValues().addAll(contactFromDB.getCustomFieldsAndListsValues());
        BeanUtils.copyNonNullProperties(contact, contactFromDB);
        return ContactResponse.getFieldsMap(mapper.contactToResponse(repository.save(contactFromDB)), null);
    }

    @Transactional
    public Map<String, Object> createContact(UpsertContactRequest request, CustomFieldsAndLists customFieldsAndLists) {
        Contact contact = mapper.requestToContact(request, customFieldsAndLists);
        return ContactResponse.getFieldsMap(mapper.contactToResponse(repository.save(contact)), null);
    }
}
