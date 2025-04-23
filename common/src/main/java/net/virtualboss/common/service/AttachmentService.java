package net.virtualboss.common.service;

import lombok.RequiredArgsConstructor;
import net.virtualboss.common.model.entity.Resource;
import net.virtualboss.common.model.entity.Task;
import net.virtualboss.common.model.entity.TaskAttachment;
import net.virtualboss.common.repository.ResourceRepository;
import net.virtualboss.common.repository.TaskAttachmentRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final Map<String, Resource> pathCache = new ConcurrentHashMap<>();

    private final TaskAttachmentRepository taskAttachmentRepository;
    private final ResourceRepository resourceRepository;

    public Set<TaskAttachment> createAttachments(String files, Task task) {
        Set<TaskAttachment> attachments = new HashSet<>();
        if (files == null) return attachments;
        Arrays.stream(files.split("\n"))
                .map(String::trim)
                .forEach(attachmentString -> {
                    String[] urlParts = attachmentString.split(" ");
                    if (urlParts.length < 2) return;
                    String fullPath = urlParts[0];
                    String uncPath = urlParts[1];
                    boolean clip;
                    if (urlParts.length < 3) {
                        clip = true;
                    } else {
                        clip = urlParts[2].equals("T");
                    }

                    final Resource resource = Resource.builder()
                            .allFullPath(fullPath)
                            .uncFullPath(uncPath)
                            .build();

                    pathCache.computeIfAbsent(fullPath, path ->
                            resourceRepository.findByAllFullPathIgnoreCase(fullPath)
                                    .orElse(resourceRepository.save(resource))
                    );

                    TaskAttachment attachment = new TaskAttachment();
                    attachment.setResource(pathCache.get(fullPath));
                    attachment.setClip(clip);
                    attachment.setTask(task);
                    attachments.add(taskAttachmentRepository.save(attachment));
                });
        return attachments;
    }
}