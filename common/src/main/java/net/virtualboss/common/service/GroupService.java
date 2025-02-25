package net.virtualboss.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.AlreadyExistsException;
import net.virtualboss.common.exception.EntityNotFoundException;
import net.virtualboss.common.mapper.v1.GroupMapperV1;
import net.virtualboss.common.model.entity.Group;
import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.common.repository.GroupRepository;
import net.virtualboss.common.web.dto.GroupDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMapperV1 mapper;

    @Cacheable(value = "group", key = "#id")
    public List<GroupDto> findById(Short id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Group with id: {0} not found", id)));
        return new ArrayList<>(Collections.singleton(mapper.mapToDto(group)));
    }

    public List<GroupDto> findAll(EntityType entityType, String groupIds) {
        return findGroups(entityType, groupIds)
                .stream().map(mapper::mapToDto).toList();
    }

    public Set<Group> findGroups(EntityType entityType, String groupIds) {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        if (groupIds != null) {
            List<Short> ids = Arrays.stream(groupIds.split(",")).map(Short::parseShort).toList();
            return groupRepository.findGroupsByIdInAndType(
                    ids, entityType, sort);
        } else {
            return groupRepository.findAllByType(
                    entityType, sort);
        }
    }

    public Set<Group> getGroups(EntityType entityType, String groupIds) {
        if (groupIds == null || groupIds.isBlank()) return Collections.emptySet();
        return findGroups(entityType, groupIds);
    }

    public Group findByName(String name, EntityType entityType) {
        if (name == null || name.isBlank()) return null;
        return groupRepository.findByNameIgnoreCaseAndType(name, entityType).orElseThrow(
                () -> new EntityNotFoundException(MessageFormat.format("Group with name: {0} not found!", name)));
    }

    @Transactional
    public GroupDto create(EntityType entityType, String name) {
        checkIfGroupAlreadyExist(entityType, name);
        Group group = Group.builder()
                .name(name)
                .type(entityType)
                .build();
        return mapper.mapToDto(groupRepository.save(group));
    }

    private void checkIfGroupAlreadyExist(EntityType entityType, String name) {
        Optional<Group> optionalGroup = groupRepository.findByNameIgnoreCaseAndType(name, entityType);
        if (optionalGroup.isPresent())
            throw new AlreadyExistsException(MessageFormat.format("Group with name <b>{0}</b> already exists!", name));
    }
}
