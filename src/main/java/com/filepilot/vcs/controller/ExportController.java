package com.filepilot.vcs.controller;

import com.filepilot.vcs.exception.InvalidOperationException;
import com.filepilot.vcs.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
public class ExportController {

    private static final Set<String> ALLOWED_FORMATS = Set.of("txt", "pdf");

    private final ExportService exportService;

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportVersion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "txt") String format) {

        if (!ALLOWED_FORMATS.contains(format.toLowerCase())) {
            throw new InvalidOperationException("Unsupported export format. Allowed: txt, pdf");
        }

        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdf = exportService.exportAsPdf(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=version_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        }

        byte[] txt = exportService.exportAsTxt(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=version_" + id + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(txt);
    }
}