package net.virtualboss.common.repository;

import net.virtualboss.common.model.entity.CommunicationType;
import net.virtualboss.common.model.enums.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunicationTypeRepository extends JpaRepository<CommunicationType, UUID> {
    Optional<CommunicationType> findByCaptionIgnoreCaseAndChannel(String caption, ChannelType channel);
}