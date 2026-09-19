package com.sthouts.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {
    private Long id;
    private String fullName;
    private String businessName;
    private String subdomain;
    private String fullSubdomainUrl; // e.g. www.scafe.Thoughtit.com
    private String email;
    private String sector;
    private String plan;
    private String status;
    private String paymentStatus;
    private String role;
    private String token;
}
