package com.sthouts.backend.service;

import com.sthouts.backend.config.TenantContext;
import com.sthouts.backend.dto.StaffDto;
import com.sthouts.backend.model.Staff;
import com.sthouts.backend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;

    public List<StaffDto> getAllActiveStaff() {
        String tenantEmail = TenantContext.getTenantEmail();
        if (tenantEmail == null || tenantEmail.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<Staff> staffList = staffRepository.findByIsActiveTrueAndTenantEmail(tenantEmail);
        return staffList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public StaffDto addStaff(StaffDto dto) {
        Staff staff = Staff.builder()
                .name(dto.getName())
                .role(dto.getRole())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .joinDate(dto.getJoinDate())
                .salary(dto.getSalary())
                .isActive(true)
                .tenantEmail(TenantContext.getTenantEmail())
                .build();
        
        return mapToDto(staffRepository.save(staff));
    }

    @Transactional
    public StaffDto updateStaff(Long id, StaffDto dto) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found"));
        
        staff.setName(dto.getName());
        staff.setRole(dto.getRole());
        staff.setPhone(dto.getPhone());
        staff.setEmail(dto.getEmail());
        staff.setJoinDate(dto.getJoinDate());
        staff.setSalary(dto.getSalary());
        
        return mapToDto(staffRepository.save(staff));
    }

    @Transactional
    public void deleteStaff(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found"));
        
        staff.setIsActive(false);
        staffRepository.save(staff);
    }

    private StaffDto mapToDto(Staff staff) {
        return StaffDto.builder()
                .id(staff.getId().toString())
                .name(staff.getName())
                .role(staff.getRole())
                .phone(staff.getPhone())
                .email(staff.getEmail())
                .joinDate(staff.getJoinDate())
                .salary(staff.getSalary())
                .isActive(staff.getIsActive())
                .build();
    }
}
