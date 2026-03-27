package com.filepilot.vcs.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class DiffResponse {

    private Integer version1Number;
    private Integer version2Number;
    private List<DiffLine> lines;

    @Data
    public static class DiffLine {
        private String content;
        private String type; // "ADDED", "REMOVED", "UNCHANGED"

        public DiffLine(String content, String type) {
            this.content = content;
            this.type = type;
        }
    }
}