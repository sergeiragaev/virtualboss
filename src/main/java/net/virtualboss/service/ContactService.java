package net.virtualboss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.mapper.v1.ContactMapperV1;
import net.virtualboss.model.dto.ContactDto;
import net.virtualboss.model.entity.Contact;
import net.virtualboss.repository.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class ContactService {
    private final ContactRepository repository;
    private final ContactMapperV1 mapper;

    public ContactDto[] findById(String id) {
        Contact contact = repository.findById(UUID.fromString(id)).orElse(null);
        return new ContactDto[]{mapper.mapToDto(contact)};
    }

    public List<ContactDto> findAll() {
        return mapper.map(repository.findAllNotUnassigned());
    }
}
