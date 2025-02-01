package net.virtualboss.repository;

import net.virtualboss.model.entity.Contact;
import net.virtualboss.model.entity.Job;
import net.virtualboss.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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

    @Query(value = """
            WITH RECURSIVE task_hierarchy AS (
                SELECT task_id 
                FROM tasks_follows tf
                WHERE follows_id = :parentId
                UNION ALL
                SELECT tf.task_id 
                FROM task_hierarchy th
                JOIN tasks_follows tf ON th.task_id = tf.follows_id
            )
            SELECT task_id FROM task_hierarchy
            """, nativeQuery = true)
    List<UUID> findAllPendingIdsRecursive(UUID parentId);

    @Query(value = """
                SELECT setval(
                    'tasks_number_seq', 
                    COALESCE((SELECT MAX(number) FROM tasks), 1)
                )
            """, nativeQuery = true)
    void resetTasksNumberSequenceToMaxNumber();
}
