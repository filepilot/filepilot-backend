package com.filepilot.vcs.config;

import com.filepilot.vcs.model.Document;
import com.filepilot.vcs.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlugMigration implements ApplicationRunner {

    private final DocumentRepository documentRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Document> docs = documentRepository.findAll();
        int migrated = 0;
        for (Document doc : docs) {
            if (doc.getSlug() == null || doc.getSlug().isBlank()) {
                String baseSlug = Document.generateSlug(doc.getTitle());
                String slug = baseSlug;
                int suffix = 1;
                while (documentRepository.findBySlug(slug).isPresent()) {
                    slug = baseSlug + "-" + suffix++;
                }
                doc.setSlug(slug);
                documentRepository.save(doc);
                migrated++;
                log.info("Slug migration: '{}' -> '{}'", doc.getTitle(), slug);
            }
        }
        log.info("Slug migration complete: {}/{} documents updated", migrated, docs.size());
    }
}
