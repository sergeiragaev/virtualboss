package net.virtualboss.contact.service;

import com.querydsl.core.JoinType;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityManager;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.AccessDeniedException;
import net.virtualboss.common.model.entity.*;
import net.virtualboss.common.repository.FieldRepository;
import net.virtualboss.common.service.GenericService;
import net.virtualboss.common.service.MainService;
import net.virtualboss.contact.mapper.v1.ContactMapperV1;
import net.virtualboss.common.util.BeanUtils;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.contact.mapper.v1.ContactResponseMapper;
import net.virtualboss.contact.querydsl.ContactFilterCriteria;
import net.virtualboss.contact.web.dto.ContactResponse;
import net.virtualboss.common.repository.ContactRepository;
import net.virtualboss.contact.web.dto.UpsertContactRequest;
import net.virtualboss.common.web.dto.filter.CommonFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Log4j2
public class ContactService extends GenericService<Contact, UUID, ContactResponse, QContact> {
    private final ContactRepository repository;
    private final ContactMapperV1 mapper;
    private final ContactResponseMapper contactResponseMapper;
    private final Map<String, String> customMappings = Map.of("ContactCustom", "customFields.");

    public ContactService(EntityManager entityManager,
                          MainService mainService,
                          ContactRepository repository,
                          ContactMapperV1 mapper,
                          ContactResponseMapper contactResponseMapper,
                          FieldRepository fieldRepository) {
        super(entityManager, mainService, UUID::fromString, repository, fieldRepository);
        this.repository = repository;
        this.mapper = mapper;
        this.contactResponseMapper = contactResponseMapper;
    }

    @Override
    protected Map<String, String> getCustomMappings() {
        return customMappings;
    }

    @Override
    protected Map<String, String> getNestedMappings() {
        return Map.of();
    }

    @Cacheable(value = "contact", key = "#id")
    public Map<String, Object> getById(String id) {
        return contactResponseMapper.map(mapper.contactToResponse(mainService.getContactById(id)), null);
    }

    @Override
    protected QContact getQEntity() {
        return QContact.contact;
    }

    @Override
    protected Predicate buildFilterPredicate(CommonFilter filter) {
        return ContactFilterCriteria.builder()
                .findString(StringUtils.defaultIfBlank(filter.getFindString(), null))
                .showUnassigned(false)
                .isDeleted(filter.getIsDeleted())
                .build()
                .toPredicate();
    }

    @Override
    protected String getCustomFieldPrefix() {
        return "ContactCustom";
    }

    @Override
    protected String getCustomFieldsAndListsPrefix() {
        return "ContactCustomFieldsAndLists";
    }

    @Override
    protected String getDefaultSort() {
        return "firstName:asc,lastName:asc,company:asc";
    }

    @Override
    protected String getMustHaveFields() {
        return "ContactId,ContactCompany,ContactFirstName,ContactLastName";
    }

    @Override
    protected Class<Contact> getEntityClass() {
        return Contact.class;
    }

    @Override
    protected Class<ContactResponse> getResponseClass() {
        return ContactResponse.class;
    }

    @Override
    protected List<JoinExpression> getJoins() {
        QContact contact = QContact.contact;
        QFieldValue fieldValueContact = new QFieldValue("fieldValueContact");
        QField fieldContact = new QField("fieldContact");

        return List.of(
                new CollectionJoin<>(contact.customFieldsAndListsValues, fieldValueContact, JoinType.LEFTJOIN),
                new EntityJoin<>(fieldValueContact.field, fieldContact, JoinType.LEFTJOIN)
        );
    }

    @Override
    protected List<GroupByExpression> getGroupBy() {
        QContact contact = QContact.contact;
        return List.of(
                new GroupByExpression(contact.id)
        );
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
        return contactResponseMapper.map(mapper.contactToResponse(repository.save(contactFromDB)), null);
    }

    @Transactional
    public Map<String, Object> createContact(UpsertContactRequest request, CustomFieldsAndLists customFieldsAndLists) {
        Contact contact = mapper.requestToContact(request, customFieldsAndLists);
        return contactResponseMapper.map(mapper.contactToResponse(repository.save(contact)), null);
    }

}
