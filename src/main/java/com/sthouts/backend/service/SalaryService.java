package com.sthouts.backend.service;

import com.sthouts.backend.config.TenantContext;
import com.sthouts.backend.dto.PayrollRecordDto;
import com.sthouts.backend.dto.SalaryStructureDto;
import com.sthouts.backend.model.PayrollRecord;
import com.sthouts.backend.model.SalaryStructure;
import com.sthouts.backend.repository.PayrollRepository;
import com.sthouts.backend.repository.SalaryStructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryService {

    private final SalaryStructureRepository salaryStructureRepository;
    private final PayrollRepository payrollRepository;

    public List<SalaryStructureDto> getAllSalaryStructures() {
        String tenantEmail = TenantContext.getTenantEmail();
        if (tenantEmail == null || tenantEmail.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<SalaryStructure> list = salaryStructureRepository.findByTenantEmail(tenantEmail);
        return list.stream()
                .map(this::mapStructureToDto)
                .collect(Collectors.toList());
    }

    public SalaryStructureDto getSalaryStructureByStaffId(Long staffId) {
        return salaryStructureRepository.findByStaffId(staffId)
                .map(this::mapStructureToDto)
                .orElse(null);
    }

    @Transactional
    public SalaryStructureDto saveOrUpdateSalaryStructure(SalaryStructureDto dto) {
        Long staffId = parseLongOrNull(dto.getStaffId());
        if (staffId == null) return dto;

        Optional<SalaryStructure> existingOpt = salaryStructureRepository.findByStaffId(staffId);
        SalaryStructure structure;
        if (existingOpt.isPresent()) {
            structure = existingOpt.get();
            if (dto.getBasic() != null) structure.setBasic(dto.getBasic());
            if (dto.getHra() != null) structure.setHra(dto.getHra());
            if (dto.getFoodAllowance() != null) structure.setFoodAllowance(dto.getFoodAllowance());
            if (dto.getTravelAllowance() != null) structure.setTravelAllowance(dto.getTravelAllowance());
            if (dto.getPfDeduction() != null) structure.setPfDeduction(dto.getPfDeduction());
            if (dto.getTaxDeduction() != null) structure.setTaxDeduction(dto.getTaxDeduction());
            if (structure.getTenantEmail() == null) structure.setTenantEmail(TenantContext.getTenantEmail());
        } else {
            structure = SalaryStructure.builder()
                    .staffId(staffId)
                    .basic(dto.getBasic() != null ? dto.getBasic() : 0.0)
                    .hra(dto.getHra() != null ? dto.getHra() : 0.0)
                    .foodAllowance(dto.getFoodAllowance() != null ? dto.getFoodAllowance() : 0.0)
                    .travelAllowance(dto.getTravelAllowance() != null ? dto.getTravelAllowance() : 0.0)
                    .pfDeduction(dto.getPfDeduction() != null ? dto.getPfDeduction() : 0.0)
                    .taxDeduction(dto.getTaxDeduction() != null ? dto.getTaxDeduction() : 0.0)
                    .tenantEmail(TenantContext.getTenantEmail())
                    .build();
        }
        structure = salaryStructureRepository.save(structure);
        return mapStructureToDto(structure);
    }

    public List<PayrollRecordDto> getPayrollRecords(String month, String staffIdStr) {
        String tenantEmail = TenantContext.getTenantEmail();
        if (tenantEmail == null || tenantEmail.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Long staffId = parseLongOrNull(staffIdStr);
        List<PayrollRecord> records;
        if (month != null && !month.trim().isEmpty() && staffId != null) {
            records = payrollRepository.findByStaffIdAndMonth(staffId, month.trim());
        } else if (month != null && !month.trim().isEmpty()) {
            records = payrollRepository.findByMonthAndTenantEmail(month.trim(), tenantEmail);
        } else if (staffId != null) {
            records = payrollRepository.findByStaffId(staffId);
        } else {
            records = payrollRepository.findAll().stream().filter(r -> tenantEmail.equals(r.getTenantEmail())).collect(Collectors.toList());
        }
        return records.stream()
                .map(this::mapPayrollToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PayrollRecordDto submitOrUpdatePayroll(PayrollRecordDto dto) {
        Long staffId = parseLongOrNull(dto.getStaffId());
        if (staffId == null) return dto;

        Optional<PayrollRecord> existingOpt = payrollRepository.findFirstByStaffIdAndMonth(staffId, dto.getMonth());
        PayrollRecord record;
        if (existingOpt.isPresent()) {
            record = existingOpt.get();
            if (dto.getNetSalary() != null) record.setNetSalary(dto.getNetSalary());
            if (dto.getStatus() != null) record.setStatus(dto.getStatus());
            if (dto.getPaidAt() != null) record.setPaidAt(dto.getPaidAt());
            if (record.getTenantEmail() == null) record.setTenantEmail(TenantContext.getTenantEmail());
        } else {
            record = PayrollRecord.builder()
                    .staffId(staffId)
                    .month(dto.getMonth())
                    .netSalary(dto.getNetSalary() != null ? dto.getNetSalary() : 0.0)
                    .status(dto.getStatus() != null ? dto.getStatus() : "paid")
                    .paidAt(dto.getPaidAt() != null ? dto.getPaidAt() : LocalDateTime.now().toString())
                    .tenantEmail(TenantContext.getTenantEmail())
                    .build();
        }
        record = payrollRepository.save(record);
        return mapPayrollToDto(record);
    }

    @Transactional
    public PayrollRecordDto updateStatus(Long id, String status) {
        Optional<PayrollRecord> opt = payrollRepository.findById(id);
        if (opt.isPresent()) {
            PayrollRecord rec = opt.get();
            rec.setStatus(status);
            if ("paid".equalsIgnoreCase(status) && (rec.getPaidAt() == null || rec.getPaidAt().isEmpty() || "-".equals(rec.getPaidAt()))) {
                rec.setPaidAt(LocalDateTime.now().toString());
            } else if ("unpaid".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status)) {
                rec.setPaidAt("-");
            }
            rec = payrollRepository.save(rec);
            return mapPayrollToDto(rec);
        }
        return null;
    }

    @Transactional
    public void deletePayrollRecord(Long id) {
        payrollRepository.deleteById(id);
    }

    private SalaryStructureDto mapStructureToDto(SalaryStructure s) {
        return SalaryStructureDto.builder()
                .staffId(String.valueOf(s.getStaffId()))
                .basic(s.getBasic())
                .hra(s.getHra())
                .foodAllowance(s.getFoodAllowance())
                .travelAllowance(s.getTravelAllowance())
                .pfDeduction(s.getPfDeduction())
                .taxDeduction(s.getTaxDeduction())
                .build();
    }

    private PayrollRecordDto mapPayrollToDto(PayrollRecord p) {
        return PayrollRecordDto.builder()
                .id(String.valueOf(p.getId()))
                .staffId(String.valueOf(p.getStaffId()))
                .month(p.getMonth())
                .netSalary(p.getNetSalary())
                .status(p.getStatus())
                .paidAt(p.getPaidAt())
                .build();
    }

    private Long parseLongOrNull(String str) {
        if (str == null) return null;
        try {
            return Long.parseLong(str.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
