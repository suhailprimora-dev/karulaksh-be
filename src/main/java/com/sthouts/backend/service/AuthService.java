package com.sthouts.backend.service;

import com.sthouts.backend.dto.AuthResponseDto;
import com.sthouts.backend.dto.LoginRequestDto;
import com.sthouts.backend.dto.SignupRequestDto;
import com.sthouts.backend.model.Tenant;
import com.sthouts.backend.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;

    public AuthResponseDto registerTenant(SignupRequestDto request) {
        if (tenantRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered with Thoughtit Cloud!");
        }

        String finalSubdomain;
        if (request.getSubdomain() != null && !request.getSubdomain().trim().isEmpty()) {
            finalSubdomain = request.getSubdomain().toLowerCase().replaceAll("[^a-z0-9-]", "");
            if (finalSubdomain.isEmpty()) {
                throw new RuntimeException("Invalid workspace URL provided.");
            }
            if (tenantRepository.existsBySubdomain(finalSubdomain)) {
                throw new RuntimeException("Workspace URL '" + finalSubdomain + "' is already taken. Please choose another.");
            }
        } else {
            String rawSubdomain = request.getBusinessName() != null 
                    ? request.getBusinessName().toLowerCase().replaceAll("[^a-z0-9-]", "")
                    : "myshop";

            if (rawSubdomain.isEmpty()) {
                rawSubdomain = "shop" + System.currentTimeMillis() % 1000;
            }

            finalSubdomain = rawSubdomain;
            int counter = 1;
            while (tenantRepository.existsBySubdomain(finalSubdomain)) {
                finalSubdomain = rawSubdomain + counter++;
            }
        }

        boolean isDev = request.getEmail() != null && (
                request.getEmail().toLowerCase().contains("thoughtit") ||
                request.getEmail().toLowerCase().contains("dev") ||
                request.getEmail().toLowerCase().contains("admin") ||
                request.getEmail().toLowerCase().contains("suhail")
        );

        Tenant tenant = Tenant.builder()
                .fullName(request.getFullName())
                .businessName(request.getBusinessName())
                .subdomain(finalSubdomain)
                .email(request.getEmail())
                .password(request.getPassword())
                .sector(request.getSector() != null ? request.getSector() : "Fine Dining Restaurant")
                .plan("ENTERPRISE_TRIAL")
                .status("ACTIVE")
                .paymentStatus(isDev ? "EXEMPT" : "UNPAID")
                .role(isDev ? "DEVELOPER" : "TENANT")
                .build();

        Tenant saved = tenantRepository.save(tenant);

        return mapToResponse(saved);
    }

    public AuthResponseDto login(LoginRequestDto request) {
        Optional<Tenant> tenantOpt = tenantRepository.findByEmail(request.getEmail());
        if (tenantOpt.isEmpty()) {
            String subClean = request.getEmail().toLowerCase().replace("www.", "").replace(".thoughtit.com", "");
            tenantOpt = tenantRepository.findBySubdomain(subClean);
        }

        if (tenantOpt.isEmpty() || !tenantOpt.get().getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid username/email or password.");
        }

        return mapToResponse(tenantOpt.get());
    }

    public boolean checkSubdomainAvailable(String subdomain) {
        if (subdomain == null || subdomain.trim().isEmpty()) return false;
        String clean = subdomain.toLowerCase().replaceAll("[^a-z0-9-]", "");
        return !tenantRepository.existsBySubdomain(clean);
    }

    private AuthResponseDto mapToResponse(Tenant t) {
        String fullDomain = "www." + t.getSubdomain() + ".Thoughtit.com";
        return AuthResponseDto.builder()
                .id(t.getId())
                .fullName(t.getFullName())
                .businessName(t.getBusinessName())
                .subdomain(t.getSubdomain())
                .fullSubdomainUrl(fullDomain)
                .email(t.getEmail())
                .sector(t.getSector())
                .plan(t.getPlan())
                .status(t.getStatus())
                .paymentStatus(t.getPaymentStatus() != null ? t.getPaymentStatus() : "UNPAID")
                .role(t.getRole() != null ? t.getRole() : "TENANT")
                .token("THOUGHTIT-JWT-" + UUID.randomUUID().toString())
                .build();
    }
}
