package net.virtualboss.repository;

import net.virtualboss.model.entity.Contact;
import net.virtualboss.model.entity.Job;
import net.virtualboss.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {
    List<Task> findAllByJob(Job job);
    List<Task> findAllByContact(Contact contact);
    List<Task> findAllByNumberIn(Collection<Long> numbers);
    Optional<Task> findByNumber(long number);
}
