package com.sthouts.backend.controller;

import com.sthouts.backend.model.PosNotification;
import com.sthouts.backend.service.PosNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class PosNotificationController {

    private final PosNotificationService service;

    @GetMapping
    public ResponseEntity<List<PosNotification>> getNotifications() {
        return ResponseEntity.ok(service.getNotifications());
    }

    @PostMapping
    public ResponseEntity<PosNotification> createNotification(@RequestBody PosNotification notification) {
        return new ResponseEntity<>(service.createNotification(notification), HttpStatus.CREATED);
    }

    @PutMapping("/mark-read")
    public ResponseEntity<Void> markAllRead() {
        service.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}
