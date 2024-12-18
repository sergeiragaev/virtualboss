package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.mapper.v1.ContactMapperV1;
import net.virtualboss.util.BeanUtils;
import net.virtualboss.web.criteria.ContactFilterCriteria;
import net.virtualboss.web.dto.contact.ContactResponse;
import net.virtualboss.model.entity.Contact;
import net.virtualboss.repository.ContactRepository;
import net.virtualboss.web.dto.contact.UpsertContactRequest;
import net.virtualboss.web.dto.filter.Filter;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class ContactService {
    private final ContactRepository repository;
    private final ContactMapperV1 mapper;
    private final TaskService taskService;

    @Cacheable(value = "contact", key = "#id")
    public ContactResponse findById(String id) {
        return mapper.contactToResponse(taskService.getContactById(id));
    }

    public List<Map<String, Object>> findAll(String fields, Filter filter) {
        List<String> fieldList = Arrays.stream(fields.split(",")).toList();

        if (filter.getSize() == null) filter.setSize(Integer.MAX_VALUE);
        if (filter.getPage() == null) filter.setPage(1);
        if (filter.getSort() == null) filter.setSort("firstName asc,lastName asc,company asc");

        String[] sorts = filter.getSort().split(",");
        List<Sort.Order> orders = new ArrayList<>();
        for (String sort : sorts) {
            String[] order = sort.split(" ");
            orders.add(new Sort.Order(Sort.Direction.valueOf(order[1].toUpperCase()), order[0]));
        }


        return repository.findAll(
                ContactFilterCriteria.builder()
                        .findString(filter.getFindString() == null || filter.getFindString().isBlank() ? null : filter.getFindString())
//                        .showUnassigned(false)
                        .build().getSpecification(),
                PageRequest.of(filter.getPage() - 1, filter.getSize(),
                        Sort.by(orders)
                ))
                .map(mapper::contactToResponse).getContent().stream()
                .map(contactResponse -> ContactResponse.getFieldsMap(contactResponse, fieldList))
                .toList();
    }

    @Transactional
    @CacheEvict(value = "contact", key = "#id")
    public void deleteContact(String id) {
        Contact contact = taskService.getContactById(id);
        taskService.reassignTasksContact(contact);
        repository.delete(contact);
    }

    @Transactional
    @CachePut(value = "contact", key = "#id")
    public ContactResponse saveContact(String id, UpsertContactRequest request) {
        Contact contact = mapper.requestToContact(id, request);
        Contact contactFromDB = taskService.getContactById(id);
        BeanUtils.copyNonNullProperties(contact, contactFromDB);
        return mapper.contactToResponse(repository.save(contactFromDB));
    }

    @Transactional
    public ContactResponse createContact(UpsertContactRequest request) {
        Contact contact = mapper.requestToContact(request);
        return mapper.contactToResponse(repository.save(contact));
    }

}
