package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.mapper.v1.ContactMapperV1;
import net.virtualboss.util.BeanUtils;
import net.virtualboss.repository.criteria.ContactFilterCriteria;
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
    public ContactResponse findById(String id) {
        return mapper.contactToResponse(mainService.getContactById(id));
    }

    public List<Map<String, Object>> findAll(String fields, CommonFilter commonFilter) {
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
//                        .showUnassigned(false)
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
        repository.delete(contact);
    }

    @Transactional
    @CachePut(value = "contact", key = "#id")
    public ContactResponse saveContact(String id, UpsertContactRequest request) {
        Contact contact = mapper.requestToContact(id, request);
        Contact contactFromDB = mainService.getContactById(id);
        BeanUtils.copyNonNullProperties(contact, contactFromDB);
        return mapper.contactToResponse(repository.save(contactFromDB));
    }

    @Transactional
    public ContactResponse createContact(UpsertContactRequest request) {
        Contact contact = mapper.requestToContact(request);
        return mapper.contactToResponse(repository.save(contact));
    }

}
