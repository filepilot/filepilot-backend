package com.filepilot.vcs.service;

import com.filepilot.vcs.exception.InvalidOperationException;
import com.filepilot.vcs.model.DocumentVersion;
import com.filepilot.vcs.model.User;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final VersionService versionService;

    public byte[] exportAsTxt(Long versionId, User user) {
        DocumentVersion version = versionService.findVersionForRead(versionId, user);

        String header = "Document: " + version.getDocument().getTitle() + "\n"
                + "Version: " + version.getVersionNumber() + "\n"
                + "Status: " + version.getStatus() + "\n"
                + "Author: " + version.getAuthor().getUsername() + "\n"
                + "Created: " + version.getCreatedAt() + "\n"
                + "---\n\n";

        String fullContent = header + (version.getContent() != null ? version.getContent() : "");

        return fullContent.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportAsPdf(Long versionId, User user) {
        DocumentVersion version = versionService.findVersionForRead(versionId, user);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document pdf = new Document(PageSize.LETTER, 50, 50, 50, 50);
        try {
            PdfWriter.getInstance(pdf, out);
            pdf.open();

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            pdf.add(new Paragraph(version.getDocument().getTitle(), headerFont));
            pdf.add(new Paragraph("Version " + version.getVersionNumber(), metaFont));
            pdf.add(new Paragraph("Status: " + version.getStatus(), metaFont));
            pdf.add(new Paragraph("Author: " + version.getAuthor().getUsername(), metaFont));
            pdf.add(new Paragraph("Created: " + version.getCreatedAt(), metaFont));
            pdf.add(new Paragraph(" "));
            pdf.add(new Paragraph("---", metaFont));
            pdf.add(new Paragraph(" "));

            String content = version.getContent() != null ? version.getContent() : "";
            // Use one paragraph per source line so line breaks survive but long lines wrap.
            for (String line : content.split("\n", -1)) {
                pdf.add(new Paragraph(line.isEmpty() ? " " : line, bodyFont));
            }
        } catch (DocumentException e) {
            throw new InvalidOperationException("Failed to generate PDF: " + e.getMessage());
        } finally {
            if (pdf.isOpen()) pdf.close();
        }

        return out.toByteArray();
    }
}
