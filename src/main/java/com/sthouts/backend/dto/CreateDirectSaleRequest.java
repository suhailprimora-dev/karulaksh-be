package com.sthouts.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDirectSaleRequest {
    private String billNo;
    private String customerName;
    private Double discount;
    private Double serviceCharge;
    private String paymentMethod;
    private Double gstRate;
    private List<OrderItemDto> items;
}
