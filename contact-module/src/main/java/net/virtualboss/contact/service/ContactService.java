package net.virtualboss.contact.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.AccessDeniedException;
import net.virtualboss.common.model.entity.*;
import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.common.service.CustomFieldService;
import net.virtualboss.common.service.MainService;
import net.virtualboss.common.util.DtoFlattener;
import net.virtualboss.common.util.QueryDslUtil;
import net.virtualboss.contact.mapper.v1.ContactMapperV1;
import net.virtualboss.common.util.BeanUtils;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.contact.repository.querydsl.ContactFilterCriteria;
import net.virtualboss.contact.web.dto.ContactResponse;
import net.virtualboss.common.repository.ContactRepository;
import net.virtualboss.contact.web.dto.UpsertContactRequest;
import net.virtualboss.common.web.dto.filter.CommonFilter;
import org.apache.commons.lang3.StringUtils;
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
    private final CustomFieldService customFieldService;
    @PersistenceContext
    private final EntityManager entityManager;

    @Cacheable(value = "contact", key = "#id")
    public Map<String, Object> findById(String id) {
        return ContactResponse.getFieldsMap(mapper.contactToResponse(mainService.getContactById(id)), null);
    }

    public List<Map<String, Object>> findAll(String fields, CommonFilter filter) {
        if (fields == null) fields = "ContactId,ContactPerson";
        Set<String> fieldsSet = parseFields(fields);
        List<String> fieldsList = List.copyOf(fieldsSet);

        QContact contact = QContact.contact;

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        initializeFilterDefaults(filter);
        PageRequest pageRequest = createPageRequest(filter);
        OrderSpecifier<?>[] orderSpecifiers =
                QueryDslUtil.getOrderSpecifiers(pageRequest.getSort(), Contact.class, "contact");

        QFieldValue fieldValueContact = new QFieldValue("fieldValueContact");
        QField fieldContact = new QField("fieldContact");

        List<ContactResponse> contacts = queryFactory.select(
                        QueryDslUtil.buildProjection(ContactResponse.class, contact, fieldsList)
                )
                .from(contact)
                .where(
                        buildContactFilterCriteriaQuery(filter)
                )
                .leftJoin(contact.customFieldsAndListsValues, fieldValueContact)
                .leftJoin(fieldValueContact.field, fieldContact)
                .groupBy(contact.id)
                .orderBy(orderSpecifiers)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        if (fields.contains("ContactCustom") || fields.contains("ContactPerson")) {
            for (ContactResponse contactResponse : contacts) {
                Contact contactFromDb = repository.getReferenceById(contactResponse.getId());
                contactResponse.setPerson(contactFromDb.getPerson());
                if (fields.contains("ContactCustom")) {
                    contactResponse.setCustomFieldsAndLists(
                            customFieldService.setCustomFieldsAndLists(
                                    contactFromDb.getCustomFieldsAndListsValues(), EntityType.CONTACT));
                }
            }
        }

        return contacts.stream().map(DtoFlattener::flatten).toList();
    }

    private BooleanBuilder buildContactFilterCriteriaQuery(CommonFilter filter) {
        return ContactFilterCriteria.builder()
                .findString(StringUtils.isBlank(filter.getFindString()) ? null : filter.getFindString())
                .showUnassigned(false)
                .isDeleted(filter.getIsDeleted())
                .build().toPredicate();
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

    private Set<String> parseFields(String fields) {
        Set<String> fieldsSet = Arrays.stream(fields.split(","))
                .collect(Collectors.toSet());
        fieldsSet.add("ContactId");
        return fieldsSet;
    }

    private void initializeFilterDefaults(CommonFilter filter) {
        if (filter.getSize() == null) filter.setSize(Integer.MAX_VALUE);
        if (filter.getPage() == null) filter.setPage(1);
        if (filter.getSort() == null) filter.setSort("firstName asc,lastName asc,company asc");
    }

    private PageRequest createPageRequest(CommonFilter filter) {
        return PageRequest.of(
                filter.getPage() - 1,
                filter.getSize(),
                Sort.by(parseSortOrders(filter.getSort()))
        );
    }

    private List<Sort.Order> parseSortOrders(String sortString) {
        return Arrays.stream(sortString.split(","))
                .map(this::createSortOrder)
                .toList();
    }

    private Sort.Order createSortOrder(String sort) {
        String[] parts = sort.trim().split(" ");
        Sort.Direction direction = Sort.Direction.valueOf(parts[1].toUpperCase());
        return new Sort.Order(direction, parts[0]);
    }
}
