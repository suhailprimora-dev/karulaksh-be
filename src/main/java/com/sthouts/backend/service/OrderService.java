package com.sthouts.backend.service;

import com.sthouts.backend.config.TenantContext;
import com.sthouts.backend.dto.OrderDto;
import com.sthouts.backend.dto.OrderItemDto;
import com.sthouts.backend.dto.PaginatedOrderHistoryDto;
import com.sthouts.backend.dto.SettleOrderRequest;
import com.sthouts.backend.model.Order;
import com.sthouts.backend.model.OrderItem;
import com.sthouts.backend.repository.OrderItemRepository;
import com.sthouts.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private final com.sthouts.backend.repository.PosSettingsRepository posSettingsRepository;
    private final com.sthouts.backend.repository.PosNotificationRepository posNotificationRepository;

    private Optional<Order> findActiveOrder() {
        String tenantEmail = TenantContext.getTenantEmail();
        if (tenantEmail == null || tenantEmail.trim().isEmpty()) {
            return Optional.empty();
        }
        return orderRepository.findByStatusAndTenantEmail("ACTIVE", tenantEmail);
    }

    private List<Order> getAllOrdersForTenant() {
        String tenantEmail = TenantContext.getTenantEmail();
        if (tenantEmail == null || tenantEmail.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return orderRepository.findByTenantEmail(tenantEmail);
    }

    @Transactional
    public OrderDto getActiveOrderOrCreate() {
        Optional<Order> activeOrderOpt = findActiveOrder();
        if (activeOrderOpt.isPresent()) {
            return mapToDto(activeOrderOpt.get());
        }

        String tenantEmail = TenantContext.getTenantEmail();
        Order newOrder = Order.builder()
                .billNo("B" + (int)(1000 + Math.random() * 9000))
                .status("ACTIVE")
                .gstRate(0.0)
                .discount(0.0)
                .serviceCharge(0.0)
                .subtotal(0.0)
                .totalAmount(0.0)
                .createdAt(LocalDateTime.now())
                .tenantEmail(tenantEmail)
                .build();
        
        return mapToDto(orderRepository.save(newOrder));
    }

    @Transactional
    public OrderDto addItemToOrder(Long orderId, OrderItemDto itemDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        Optional<OrderItem> existingItem = order.getItems().stream()
                .filter(i -> i.getMenuItemId().equals(itemDto.getMenuItemId()))
                .findFirst();

        if (existingItem.isPresent()) {
            OrderItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + itemDto.getQuantity());
        } else {
            OrderItem newItem = OrderItem.builder()
                    .order(order)
                    .menuItemId(itemDto.getMenuItemId())
                    .name(itemDto.getName())
                    .price(itemDto.getPrice())
                    .quantity(itemDto.getQuantity())
                    .build();
            order.getItems().add(newItem);
        }

        recalculateTotals(order);
        return mapToDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto updateItemQuantity(Long orderId, Long itemId, int delta) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        item.setQuantity(item.getQuantity() + delta);
        if (item.getQuantity() <= 0) {
            order.getItems().remove(item);
            orderItemRepository.delete(item);
        }

        recalculateTotals(order);
        return mapToDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto removeItemFromOrder(Long orderId, Long itemId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        order.getItems().remove(item);
        orderItemRepository.delete(item);

        recalculateTotals(order);
        return mapToDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto updateGstRate(Long orderId, Double gstRate) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        order.setGstRate(gstRate);
        recalculateTotals(order);
        return mapToDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto settleOrder(Long orderId, SettleOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        order.setCustomerName(request.getCustomerName() != null ? request.getCustomerName() : "Walk-in Customer");
        order.setTableNo(request.getTableNo());
        order.setDiscount(request.getDiscount() != null ? request.getDiscount() : 0.0);
        order.setServiceCharge(request.getServiceCharge() != null ? request.getServiceCharge() : 0.0);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus("SETTLED");
        
        recalculateTotals(order);
        return mapToDto(orderRepository.save(order));
    }

    public List<OrderDto> getSettledOrders() {
        return getAllOrdersForTenant().stream()
                .filter(o -> "SETTLED".equals(o.getStatus()))
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public PaginatedOrderHistoryDto getSettledOrdersPaginated(int page, int size, String search, String paymentMethod, String fromDate, String toDate) {
        List<Order> filtered = getAllOrdersForTenant().stream()
                .filter(o -> "SETTLED".equalsIgnoreCase(o.getStatus()) || "COMPLETED".equalsIgnoreCase(o.getStatus()) || "PAID".equalsIgnoreCase(o.getStatus()))
                .filter(o -> {
                    if (search != null && !search.trim().isEmpty()) {
                        String q = search.trim().toLowerCase();
                        boolean matchNo = o.getBillNo() != null && o.getBillNo().toLowerCase().contains(q);
                        boolean matchId = o.getId() != null && o.getId().toString().contains(q);
                        boolean matchCust = o.getCustomerName() != null && o.getCustomerName().toLowerCase().contains(q);
                        boolean matchTable = o.getTableNo() != null && o.getTableNo().toLowerCase().contains(q);
                        if (!matchNo && !matchId && !matchCust && !matchTable) return false;
                    }
                    if (paymentMethod != null && !paymentMethod.trim().isEmpty() && !"all".equalsIgnoreCase(paymentMethod.trim())) {
                        if (o.getPaymentMethod() == null || !o.getPaymentMethod().equalsIgnoreCase(paymentMethod.trim())) return false;
                    }
                    if (fromDate != null && !fromDate.trim().isEmpty() && o.getCreatedAt() != null) {
                        try {
                            if (o.getCreatedAt().toLocalDate().isBefore(LocalDate.parse(fromDate.trim()))) return false;
                        } catch (Exception ignored) {}
                    }
                    if (toDate != null && !toDate.trim().isEmpty() && o.getCreatedAt() != null) {
                        try {
                            if (o.getCreatedAt().toLocalDate().isAfter(LocalDate.parse(toDate.trim()))) return false;
                        } catch (Exception ignored) {}
                    }
                    return true;
                })
                .sorted((o1, o2) -> {
                    if (o1.getCreatedAt() == null || o2.getCreatedAt() == null) return 0;
                    return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                })
                .collect(Collectors.toList());

        long totalElements = filtered.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int fromIdx = page * size;
        int toIdx = Math.min(fromIdx + size, filtered.size());
        List<OrderDto> content = fromIdx < filtered.size() ? filtered.subList(fromIdx, toIdx).stream().map(this::mapToDto).collect(Collectors.toList()) : List.of();

        return PaginatedOrderHistoryDto.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    @Transactional
    public void cancelActiveOrder() {
        Optional<Order> activeOrderOpt = findActiveOrder();
        activeOrderOpt.ifPresent(orderRepository::delete);
    }

    @Transactional
    public OrderDto reopenOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        Optional<Order> currentActive = findActiveOrder();
        if (currentActive.isPresent() && !currentActive.get().getId().equals(orderId)) {
            orderRepository.delete(currentActive.get());
        }
        order.setStatus("ACTIVE");
        recalculateTotals(order);
        return mapToDto(orderRepository.save(order));
    }

    private void recalculateTotals(Order order) {
        double subtotal = order.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        order.setSubtotal(subtotal);
        double tax = subtotal * ((order.getGstRate() != null ? order.getGstRate() : 0.0) / 100.0);
        double discount = order.getDiscount() != null ? order.getDiscount() : 0.0;
        double serviceCharge = order.getServiceCharge() != null ? order.getServiceCharge() : 0.0;
        
        order.setTotalAmount(subtotal - discount + serviceCharge + tax);
    }

    private OrderDto mapToDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(i -> OrderItemDto.builder()
                        .id(i.getId())
                        .menuItemId(i.getMenuItemId())
                        .name(i.getName())
                        .price(i.getPrice())
                        .quantity(i.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .billNo(order.getBillNo())
                .customerName(order.getCustomerName())
                .tableNo(order.getTableNo())
                .discount(order.getDiscount())
                .serviceCharge(order.getServiceCharge())
                .paymentMethod(order.getPaymentMethod())
                .gstRate(order.getGstRate())
                .subtotal(order.getSubtotal())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(itemDtos)
                .build();
    }

    @Transactional
    public OrderDto createDirectSale(com.sthouts.backend.dto.CreateDirectSaleRequest request) {
        String tenantEmail = TenantContext.getTenantEmail();
        if (tenantEmail == null || tenantEmail.trim().isEmpty()) {
            throw new RuntimeException("Unauthorized");
        }

        Order order = Order.builder()
                .billNo(request.getBillNo() != null ? request.getBillNo() : "R-" + (int)(1000 + Math.random() * 9000))
                .customerName(request.getCustomerName() != null ? request.getCustomerName() : "Walk-in Customer")
                .discount(request.getDiscount() != null ? request.getDiscount() : 0.0)
                .serviceCharge(request.getServiceCharge() != null ? request.getServiceCharge() : 0.0)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "ONLINE")
                .gstRate(request.getGstRate() != null ? request.getGstRate() : 0.0)
                .status("SETTLED")
                .createdAt(LocalDateTime.now())
                .tenantEmail(tenantEmail)
                .items(new java.util.ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);

        if (request.getItems() != null) {
            for (com.sthouts.backend.dto.OrderItemDto itemDto : request.getItems()) {
                OrderItem item = OrderItem.builder()
                        .order(savedOrder)
                        .menuItemId(itemDto.getMenuItemId())
                        .name(itemDto.getName())
                        .price(itemDto.getPrice())
                        .quantity(itemDto.getQuantity())
                        .build();
                savedOrder.getItems().add(item);
            }
        }

        recalculateTotals(savedOrder);
        return mapToDto(orderRepository.save(savedOrder));
    }
}
