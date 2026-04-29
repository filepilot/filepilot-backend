package com.filepilot.vcs.service;

import com.filepilot.vcs.dto.response.DiffResponse;
import com.filepilot.vcs.exception.InvalidOperationException;
import com.filepilot.vcs.model.DocumentVersion;
import com.filepilot.vcs.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DiffService {

    // LCS is O(m*n) memory and CPU. Cap both per-side and product so a worst-case
    // 5000x5000 diff can't allocate a ~100MB int[][] and pin a request thread.
    static final int MAX_DIFF_LINES = 2000;
    static final int MAX_DIFF_CELLS = 2_000_000;

    private final VersionService versionService;

    public DiffResponse compareVersions(Long versionId1, Long versionId2, User user) {
        DocumentVersion v1 = versionService.findVersionForRead(versionId1, user);
        DocumentVersion v2 = versionService.findVersionForRead(versionId2, user);

        if (!v1.getDocument().getId().equals(v2.getDocument().getId())) {
            throw new InvalidOperationException("Cannot compare versions from different documents");
        }

        String content1 = v1.getContent() != null ? v1.getContent() : "";
        String content2 = v2.getContent() != null ? v2.getContent() : "";
        String[] lines1 = content1.split("\n", -1);
        String[] lines2 = content2.split("\n", -1);

        if (lines1.length > MAX_DIFF_LINES || lines2.length > MAX_DIFF_LINES) {
            throw new InvalidOperationException(
                    "Versions too large to diff (limit " + MAX_DIFF_LINES + " lines per side)");
        }
        if ((long) lines1.length * lines2.length > MAX_DIFF_CELLS) {
            throw new InvalidOperationException(
                    "Versions too large to diff (combined size exceeds " + MAX_DIFF_CELLS + " cells)");
        }

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