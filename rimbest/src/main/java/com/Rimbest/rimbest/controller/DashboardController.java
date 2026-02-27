package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.model.dto.DashboardResponse;
import com.Rimbest.rimbest.service.DashboardService;
import com.Rimbest.rimbest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    @GetMapping("/admin")
    public ResponseEntity<DashboardResponse> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminStats());
    }

    @GetMapping("/partenaire")
    public ResponseEntity<DashboardResponse> getPartenaireDashboard(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(dashboardService.getPartenaireStats(user));
    }

    @GetMapping("/client")
    public ResponseEntity<DashboardResponse> getClientDashboard(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(dashboardService.getClientStats(user));
    }
}