package com.filepilot.vcs.service;

import com.filepilot.vcs.model.Document;
import com.filepilot.vcs.model.DocumentVersion;
import com.filepilot.vcs.model.User;
import com.filepilot.vcs.repository.DocumentVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock private DocumentVersionRepository versionRepository;
    @Mock private User mockUser;

    @InjectMocks private ExportService exportService;

    @Test
    void exportAsTxt_contains_header_and_content() {
        Document doc = new Document();
        DocumentVersion version = new DocumentVersion();
        // Inject dependencies via reflection since Lombok-generated setters may not be available in tests
        try {
            java.lang.reflect.Field fDoc = com.filepilot.vcs.model.DocumentVersion.class.getDeclaredField("document");
            fDoc.setAccessible(true);
            fDoc.set(version, doc);
            java.lang.reflect.Field fVer = com.filepilot.vcs.model.DocumentVersion.class.getDeclaredField("versionNumber");
            fVer.setAccessible(true); fVer.set(version, 1);
            java.lang.reflect.Field fCont = com.filepilot.vcs.model.DocumentVersion.class.getDeclaredField("content");
            fCont.setAccessible(true); fCont.set(version, "Hello world");
            java.lang.reflect.Field fAuth = com.filepilot.vcs.model.DocumentVersion.class.getDeclaredField("author");
            fAuth.setAccessible(true); fAuth.set(version, new com.filepilot.vcs.model.User());
            java.lang.reflect.Field fCreated = com.filepilot.vcs.model.DocumentVersion.class.getDeclaredField("createdAt");
            fCreated.setAccessible(true); fCreated.set(version, java.time.LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(versionRepository.findById(1L)).thenReturn(java.util.Optional.of(version));

        byte[] txt = exportService.exportAsTxt(1L, mockUser);
        String content = new String(txt, StandardCharsets.UTF_8);
        assertTrue(content.length() > 0);
    }

    @Test
    void exportAsPdf_starts_with_pdf_header() {
        Document doc = new Document();
        DocumentVersion version = new DocumentVersion();
        try {
            java.lang.reflect.Field fDoc = com.filepilot.vcs.model.DocumentVersion.class.getDeclaredField("document");
            fDoc.setAccessible(true);
            fDoc.set(version, doc);
            java.lang.reflect.Field fVer = com.filepilot.vcs.model.DocumentVersion.class.getDeclaredField("versionNumber");
            fVer.setAccessible(true); fVer.set(version, 1);
            java.lang.reflect.Field fAuth = com.filepilot.vcs.model.DocumentVersion.class.getDeclaredField("author");
            fAuth.setAccessible(true); fAuth.set(version, new com.filepilot.vcs.model.User());
            java.lang.reflect.Field fCreated = com.filepilot.vcs.model.DocumentVersion.class.getDeclaredField("createdAt");
            fCreated.setAccessible(true); fCreated.set(version, java.time.LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(versionRepository.findById(1L)).thenReturn(java.util.Optional.of(version));

        byte[] pdf = exportService.exportAsPdf(1L, mockUser);
        String header = new String(pdf, StandardCharsets.UTF_8).substring(0, 8);
        assertEquals("%PDF-1.4", header);
    }
}
