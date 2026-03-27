package com.filepilot.vcs.controller;

import com.filepilot.vcs.dto.response.DiffResponse;
import com.filepilot.vcs.service.DiffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
public class DiffController {

    private final DiffService diffService;

    @GetMapping("/{id1}/diff/{id2}")
    public ResponseEntity<DiffResponse> compareVersions(
            @PathVariable Long id1,
            @PathVariable Long id2) {
        return ResponseEntity.ok(diffService.compareVersions(id1, id2));
    }
}