package com.filepilot.vcs.service;

import com.filepilot.vcs.dto.response.DiffResponse;
import com.filepilot.vcs.exception.InvalidOperationException;
import com.filepilot.vcs.exception.ResourceNotFoundException;
import com.filepilot.vcs.model.DocumentVersion;
import com.filepilot.vcs.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DiffService {

    private final DocumentVersionRepository versionRepository;

    public DiffResponse compareVersions(Long versionId1, Long versionId2) {
        DocumentVersion v1 = versionRepository.findById(versionId1)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found with id: " + versionId1));

        DocumentVersion v2 = versionRepository.findById(versionId2)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found with id: " + versionId2));

        if (!v1.getDocument().getId().equals(v2.getDocument().getId())) {
            throw new InvalidOperationException("Cannot compare versions from different documents");
        }

        String content1 = v1.getContent() != null ? v1.getContent() : "";
        String content2 = v2.getContent() != null ? v2.getContent() : "";
        String[] lines1 = content1.split("\n", -1);
        String[] lines2 = content2.split("\n", -1);

        List<DiffResponse.DiffLine> diffLines = computeLcsDiff(lines1, lines2);

        DiffResponse response = new DiffResponse();
        response.setVersion1Number(v1.getVersionNumber());
        response.setVersion2Number(v2.getVersionNumber());
        response.setLines(diffLines);

        return response;
    }

    private List<DiffResponse.DiffLine> computeLcsDiff(String[] lines1, String[] lines2) {
        int m = lines1.length;
        int n = lines2.length;

        // Build LCS table
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (lines1[i - 1].equals(lines2[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        // Backtrack to produce diff
        List<DiffResponse.DiffLine> result = new ArrayList<>();
        int i = m, j = n;
        List<DiffResponse.DiffLine> temp = new ArrayList<>();

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && lines1[i - 1].equals(lines2[j - 1])) {
                temp.add(new DiffResponse.DiffLine(lines1[i - 1], "UNCHANGED"));
                i--;
                j--;
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                temp.add(new DiffResponse.DiffLine(lines2[j - 1], "ADDED"));
                j--;
            } else {
                temp.add(new DiffResponse.DiffLine(lines1[i - 1], "REMOVED"));
                i--;
            }
        }

        Collections.reverse(temp);
        return temp;
    }
}