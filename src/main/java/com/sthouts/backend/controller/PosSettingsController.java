package com.sthouts.backend.controller;

import com.sthouts.backend.model.PosSettings;
import com.sthouts.backend.service.PosSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class PosSettingsController {

    private final PosSettingsService service;

    @GetMapping
    public ResponseEntity<PosSettings> getSettings() {
        return ResponseEntity.ok(service.getSettings());
    }

    @PutMapping
    public ResponseEntity<PosSettings> updateSettings(@RequestBody PosSettings settings) {
        return ResponseEntity.ok(service.updateSettings(settings));
    }
}
