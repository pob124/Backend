package com.AutoSales_Agent.Identity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class AppIdentityController {
    private final AppIdentityService svc;

    @GetMapping("/identity")
    public ResponseEntity<AppIdentityDto> getIdentity(){
        return ResponseEntity.ok(svc.getIdentity());
    }
}
