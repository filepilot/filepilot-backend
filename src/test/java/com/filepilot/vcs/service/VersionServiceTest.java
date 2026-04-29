package com.filepilot.vcs.service;

import com.filepilot.vcs.dto.request.CreateVersionRequest;
import com.filepilot.vcs.dto.response.VersionResponse;
import com.filepilot.vcs.mapper.DocumentMapper;
import com.filepilot.vcs.model.Document;
import com.filepilot.vcs.model.DocumentVersion;
import com.filepilot.vcs.model.Role;
import com.filepilot.vcs.model.User;
import com.filepilot.vcs.repository.DocumentRepository;
import com.filepilot.vcs.repository.DocumentVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VersionServiceTest {

    @Mock private DocumentVersionRepository versionRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private DocumentMapper mapper;
    @Mock private AuditService auditService;

    @InjectMocks private VersionService versionService;

    private User testUser;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("tester");
        testUser.setRole(Role.ADMIN);
    }

    @Test
    void getVersion_by_id_returns_response() {
        DocumentVersion version = new DocumentVersion();
        VersionResponse resp = new VersionResponse();
        when(versionRepository.findById(1L)).thenReturn(Optional.of(version));
        when(mapper.toVersionResponse(version)).thenReturn(resp);

        VersionResponse result = versionService.getVersionById(1L, testUser);
        assertNotNull(result);
        assertEquals(resp, result);
    }

    @Test
    void getVersionsByDocument_returns_list() {
        Document doc = mock(Document.class);
        when(documentRepository.findById(2L)).thenReturn(Optional.of(doc));
        DocumentVersion v1 = new DocumentVersion();
        java.util.List<DocumentVersion> list = java.util.Arrays.asList(v1);
        when(versionRepository.findByDocumentOrderByVersionNumberDesc(doc)).thenReturn(list);
        VersionResponse vr = new VersionResponse();
        when(mapper.toVersionResponse(any(DocumentVersion.class))).thenReturn(vr);

        java.util.List<VersionResponse> res = versionService.getVersionsByDocument(2L, testUser);
        assertEquals(1, res.size());
        assertEquals(vr, res.get(0));
    }
}
