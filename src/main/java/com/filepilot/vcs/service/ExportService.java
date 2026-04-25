package com.filepilot.vcs.service;

import com.filepilot.vcs.model.DocumentVersion;
import com.filepilot.vcs.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        String fullContent = header + version.getContent();

        return fullContent.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportAsPdf(Long versionId, User user) {
        DocumentVersion version = versionService.findVersionForRead(versionId, user);

        String content = "Document: " + version.getDocument().getTitle() + "\n"
                + "Version: " + version.getVersionNumber() + "\n"
                + "Status: " + version.getStatus() + "\n"
                + "Author: " + version.getAuthor().getUsername() + "\n"
                + "Created: " + version.getCreatedAt() + "\n"
                + "---\n\n"
                + version.getContent();

        // Simple PDF generation — creates a basic but valid PDF
        String pdfContent = "%PDF-1.4\n"
                + "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"
                + "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n"
                + "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n"
                + "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n";

        StringBuilder textStream = new StringBuilder();
        textStream.append("BT\n/F1 12 Tf\n");

        String[] lines = content.split("\n");
        int y = 750;
        for (String line : lines) {
            if (y < 50) break;
            String safeLine = line.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
            textStream.append(String.format("1 0 0 1 50 %d Tm\n(%s) Tj\n", y, safeLine));
            y -= 18;
        }
        textStream.append("ET\n");

        String stream = textStream.toString();
        String streamObj = "4 0 obj\n<< /Length " + stream.length() + " >>\nstream\n" + stream + "endstream\nendobj\n";

        String fullPdf = pdfContent + streamObj;

        int[] offsets = new int[5];
        int pos = 0;
        for (int i = 0; i < 5; i++) {
            String search = (i + 1) + " 0 obj";
            offsets[i] = fullPdf.indexOf(search);
        }

        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 6\n");
        xref.append("0000000000 65535 f \n");
        for (int offset : offsets) {
            xref.append(String.format("%010d 00000 n \n", offset));
        }

        int xrefPos = fullPdf.length();
        String trailer = xref.toString()
                + "trailer\n<< /Size 6 /Root 1 0 R >>\n"
                + "startxref\n" + xrefPos + "\n%%EOF";

        return (fullPdf + trailer).getBytes(StandardCharsets.UTF_8);
    }
}