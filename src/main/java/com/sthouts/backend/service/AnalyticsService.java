package com.sthouts.backend.service;

import com.sthouts.backend.config.TenantContext;
import com.sthouts.backend.dto.*;
import com.sthouts.backend.model.Order;
import com.sthouts.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;

    private List<Order> getAllOrdersForTenant() {
        String tenantEmail = TenantContext.getTenantEmail();
        if (tenantEmail == null || tenantEmail.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return orderRepository.findByTenantEmail(tenantEmail);
    }

    private List<Order> getFilteredOrders(String range, String startDate, String endDate) {
        List<Order> orders = getAllOrdersForTenant().stream()
                .filter(o -> "SETTLED".equals(o.getStatus()))
                .collect(Collectors.toList());

        if (startDate != null && !startDate.trim().isEmpty() && endDate != null && !endDate.trim().isEmpty()) {
            try {
                LocalDate start = LocalDate.parse(startDate.trim());
                LocalDate end = LocalDate.parse(endDate.trim());
                return orders.stream()
                        .filter(o -> {
                            if (o.getCreatedAt() == null) return false;
                            LocalDate orderDate = o.getCreatedAt().toLocalDate();
                            return !orderDate.isBefore(start) && !orderDate.isAfter(end);
                        })
                        .collect(Collectors.toList());
            } catch (Exception e) {
            }
        } else if (startDate != null && !startDate.trim().isEmpty()) {
            try {
                LocalDate start = LocalDate.parse(startDate.trim());
                return orders.stream()
                        .filter(o -> {
                            if (o.getCreatedAt() == null) return false;
                            return !o.getCreatedAt().toLocalDate().isBefore(start);
                        })
                        .collect(Collectors.toList());
            } catch (Exception e) {
            }
        } else if (endDate != null && !endDate.trim().isEmpty()) {
            try {
                LocalDate end = LocalDate.parse(endDate.trim());
                return orders.stream()
                        .filter(o -> {
                            if (o.getCreatedAt() == null) return false;
                            return !o.getCreatedAt().toLocalDate().isAfter(end);
                        })
                        .collect(Collectors.toList());
            } catch (Exception e) {
            }
        }

        LocalDateTime cutoff = null;
        if ("1d".equalsIgnoreCase(range)) {
            cutoff = LocalDateTime.now().minusDays(1);
        } else if ("1w".equalsIgnoreCase(range) || "7d".equalsIgnoreCase(range)) {
            cutoff = LocalDateTime.now().minusDays(7);
        } else if ("1m".equalsIgnoreCase(range) || "30d".equalsIgnoreCase(range)) {
            cutoff = LocalDateTime.now().minusDays(30);
        } else if ("1y".equalsIgnoreCase(range) || "365d".equalsIgnoreCase(range)) {
            cutoff = LocalDateTime.now().minusDays(365);
        }

        if (cutoff != null) {
            LocalDateTime finalCutoff = cutoff;
            orders = orders.stream()
                    .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(finalCutoff))
                    .collect(Collectors.toList());
        }
        return orders;
    }

    private List<Order> getFilteredOrders(String range) {
        return getFilteredOrders(range, null, null);
    }

    public AnalyticsOverviewDto getOverview(String range) {
        return getOverview(range, null, null);
    }

    public AnalyticsOverviewDto getOverview(String range, String startDate, String endDate) {
        List<Order> orders = getFilteredOrders(range, startDate, endDate);
        
        double totalRevenue = orders.stream().mapToDouble(Order::getTotalAmount).sum();
        double totalDiscount = orders.stream().mapToDouble(o -> o.getDiscount() != null ? o.getDiscount() : 0.0).sum();
        double totalTax = orders.stream().mapToDouble(o -> (o.getSubtotal() != null ? o.getSubtotal() : 0.0) * ((o.getGstRate() != null ? o.getGstRate() : 0.0) / 100.0)).sum();
        double totalServiceCharge = orders.stream().mapToDouble(o -> o.getServiceCharge() != null ? o.getServiceCharge() : 0.0).sum();
        double avgBillValue = orders.isEmpty() ? 0.0 : totalRevenue / orders.size();

        return AnalyticsOverviewDto.builder()
                .totalRevenue(totalRevenue)
                .totalDiscount(totalDiscount)
                .totalTax(totalTax)
                .totalServiceCharge(totalServiceCharge)
                .avgBillValue(avgBillValue)
                .totalBills(orders.size())
                .build();
    }

    public TodayYesterdayDto getTodayYesterday() {
        List<Order> orders = getAllOrdersForTenant().stream()
                .filter(o -> ("SETTLED".equalsIgnoreCase(o.getStatus()) || "COMPLETED".equalsIgnoreCase(o.getStatus()) || "PAID".equalsIgnoreCase(o.getStatus()) || o.getStatus() == null) && o.getCreatedAt() != null)
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        double todayRev = orders.stream()
                .filter(o -> o.getCreatedAt().toLocalDate().equals(today))
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                .sum();

        double yesterdayRev = orders.stream()
                .filter(o -> o.getCreatedAt().toLocalDate().equals(yesterday))
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                .sum();

        if (todayRev == 0.0 && yesterdayRev == 0.0 && !orders.isEmpty()) {
            LocalDate maxDate = orders.stream()
                    .map(o -> o.getCreatedAt().toLocalDate())
                    .max(LocalDate::compareTo)
                    .orElse(today);
            LocalDate prevDate = maxDate.minusDays(1);

            todayRev = orders.stream()
                    .filter(o -> o.getCreatedAt().toLocalDate().equals(maxDate))
                    .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                    .sum();

            yesterdayRev = orders.stream()
                    .filter(o -> o.getCreatedAt().toLocalDate().equals(prevDate))
                    .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                    .sum();
        }

        double change = 0.0;
        if (yesterdayRev > 0) {
            change = ((todayRev - yesterdayRev) / yesterdayRev) * 100;
        } else if (todayRev > 0) {
            change = 100.0;
        }

        return TodayYesterdayDto.builder()
                .todayRevenue(todayRev)
                .yesterdayRevenue(yesterdayRev)
                .changePercentage(change)
                .build();
    }

    public List<DailyRevenueDto> getDailyRevenue(String range) {
        return getDailyRevenue(range, null, null);
    }

    public List<DailyRevenueDto> getDailyRevenue(String range, String startDate, String endDate) {
        List<Order> orders = getFilteredOrders(range, startDate, endDate);

        if ((startDate == null || startDate.trim().isEmpty()) && (endDate == null || endDate.trim().isEmpty())) {
            if ("1d".equalsIgnoreCase(range)) {
                List<String> timeLabels = Arrays.asList("8 AM", "10 AM", "12 PM", "2 PM", "4 PM", "6 PM", "8 PM", "10 PM");
                Map<String, Double> hourlyMap = new LinkedHashMap<>();
                for (String label : timeLabels) hourlyMap.put(label, 0.0);
                LocalDate today = LocalDate.now();
                for (Order order : orders) {
                    if (order.getCreatedAt() != null && order.getCreatedAt().toLocalDate().equals(today)) {
                        int hour = order.getCreatedAt().getHour();
                        String bucket = "8 AM";
                        if (hour >= 20) bucket = "10 PM";
                        else if (hour >= 18) bucket = "8 PM";
                        else if (hour >= 16) bucket = "6 PM";
                        else if (hour >= 14) bucket = "4 PM";
                        else if (hour >= 12) bucket = "12 PM";
                        else if (hour >= 10) bucket = "10 AM";
                        hourlyMap.put(bucket, hourlyMap.get(bucket) + order.getTotalAmount());
                    }
                }
                List<DailyRevenueDto> result = new ArrayList<>();
                for (Map.Entry<String, Double> e : hourlyMap.entrySet()) result.add(new DailyRevenueDto(e.getKey(), e.getValue()));
                return result;
            } else if ("1w".equalsIgnoreCase(range)) {
                List<String> daysOfWeek = Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
                Map<String, Double> weekMap = new LinkedHashMap<>();
                for (String d : daysOfWeek) weekMap.put(d, 0.0);
                LocalDate cutoff = LocalDate.now().minusDays(7);
                for (Order order : orders) {
                    if (order.getCreatedAt() != null && !order.getCreatedAt().toLocalDate().isBefore(cutoff)) {
                        String dow = order.getCreatedAt().getDayOfWeek().name();
                        String label = dow.substring(0, 1) + dow.substring(1, 3).toLowerCase();
                        if (weekMap.containsKey(label)) weekMap.put(label, weekMap.get(label) + order.getTotalAmount());
                    }
                }
                List<DailyRevenueDto> result = new ArrayList<>();
                for (Map.Entry<String, Double> e : weekMap.entrySet()) result.add(new DailyRevenueDto(e.getKey(), e.getValue()));
                return result;
            } else if ("1m".equalsIgnoreCase(range)) {
                List<String> weeks = Arrays.asList("1st week", "2nd week", "3rd week", "4th week");
                Map<String, Double> monthMap = new LinkedHashMap<>();
                for (String w : weeks) monthMap.put(w, 0.0);
                LocalDate cutoff = LocalDate.now().minusDays(30);
                for (Order order : orders) {
                    if (order.getCreatedAt() != null && !order.getCreatedAt().toLocalDate().isBefore(cutoff)) {
                        int dom = order.getCreatedAt().getDayOfMonth();
                        String bucket = "1st week";
                        if (dom >= 22) bucket = "4th week";
                        else if (dom >= 15) bucket = "3rd week";
                        else if (dom >= 8) bucket = "2nd week";
                        monthMap.put(bucket, monthMap.get(bucket) + (order.getTotalAmount() != null ? order.getTotalAmount() : 0.0));
                    }
                }
                List<DailyRevenueDto> result = new ArrayList<>();
                for (Map.Entry<String, Double> e : monthMap.entrySet()) result.add(new DailyRevenueDto(e.getKey(), e.getValue()));
                return result;
            } else if ("1y".equalsIgnoreCase(range)) {
                List<String> months = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
                Map<String, Double> yearMap = new LinkedHashMap<>();
                for (String m : months) yearMap.put(m, 0.0);
                LocalDate cutoff = LocalDate.now().minusDays(365);
                for (Order order : orders) {
                    if (order.getCreatedAt() != null && !order.getCreatedAt().toLocalDate().isBefore(cutoff)) {
                        String mName = order.getCreatedAt().getMonth().name();
                        String label = mName.substring(0, 1) + mName.substring(1, 3).toLowerCase();
                        if (yearMap.containsKey(label)) yearMap.put(label, yearMap.get(label) + order.getTotalAmount());
                    }
                }
                List<DailyRevenueDto> result = new ArrayList<>();
                for (Map.Entry<String, Double> e : yearMap.entrySet()) result.add(new DailyRevenueDto(e.getKey(), e.getValue()));
                return result;
            }
        }

        LocalDate start;
        LocalDate end;
        if (startDate != null && !startDate.trim().isEmpty() && endDate != null && !endDate.trim().isEmpty()) {
            start = LocalDate.parse(startDate.trim());
            end = LocalDate.parse(endDate.trim());
            if (start.isAfter(end)) {
                LocalDate temp = start;
                start = end;
                end = temp;
            }
        } else if (startDate != null && !startDate.trim().isEmpty()) {
            start = LocalDate.parse(startDate.trim());
            end = LocalDate.now();
            if (start.isAfter(end)) end = start;
        } else if (endDate != null && !endDate.trim().isEmpty()) {
            end = LocalDate.parse(endDate.trim());
            start = end.minusDays(6);
        } else {
            int days = "1d".equalsIgnoreCase(range) ? 1 : ("1w".equalsIgnoreCase(range) || "7d".equalsIgnoreCase(range) ? 7 : ("1m".equalsIgnoreCase(range) || "30d".equalsIgnoreCase(range) ? 30 : ("1y".equalsIgnoreCase(range) ? 365 : 30)));
            end = LocalDate.now();
            start = end.minusDays(days - 1);
            if ("all".equalsIgnoreCase(range)) {
                Optional<LocalDateTime> minDate = orders.stream().map(Order::getCreatedAt).filter(Objects::nonNull).min(LocalDateTime::compareTo);
                if (minDate.isPresent()) {
                    start = minDate.get().toLocalDate();
                }
            }
        }

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        if (daysBetween > 366) {
            start = end.minusDays(365);
            daysBetween = 366;
        }

        Map<String, Double> dailyMap = new HashMap<>();
        for (int i = 0; i < daysBetween; i++) {
            dailyMap.put(start.plusDays(i).toString(), 0.0);
        }

        for (Order order : orders) {
            if (order.getCreatedAt() != null) {
                String dateKey = order.getCreatedAt().toLocalDate().toString();
                if (dailyMap.containsKey(dateKey)) {
                    dailyMap.put(dateKey, dailyMap.get(dateKey) + order.getTotalAmount());
                }
            }
        }

        return dailyMap.entrySet().stream()
                .map(e -> new DailyRevenueDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DailyRevenueDto::getDate))
                .collect(Collectors.toList());
    }

    public PaymentMethodBreakdownDto getPaymentBreakdown(String range) {
        return getPaymentBreakdown(range, null, null);
    }

    public PaymentMethodBreakdownDto getPaymentBreakdown(String range, String startDate, String endDate) {
        List<Order> orders = getFilteredOrders(range, startDate, endDate);

        double cash = 0, card = 0, upi = 0;
        for (Order order : orders) {
            String method = order.getPaymentMethod() != null ? order.getPaymentMethod().toLowerCase() : "";
            double amt = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
            if (method.contains("cash")) cash += amt;
            else if (method.contains("card")) card += amt;
            else if (method.contains("upi")) upi += amt;
        }

        AnalyticsOverviewDto overview = getOverview(range, startDate, endDate);

        return PaymentMethodBreakdownDto.builder()
                .cash(cash)
                .card(card)
                .upi(upi)
                .totalRevenue(overview.getTotalRevenue())
                .totalDiscount(overview.getTotalDiscount())
                .totalServiceCharge(overview.getTotalServiceCharge())
                .build();
    }

    public List<TopItemDto> getTopItems(String range) {
        return getTopItems(range, null, null);
    }

    public List<TopItemDto> getTopItems(String range, String date) {
        return getTopItems(range, date, date);
    }

    public List<TopItemDto> getTopItems(String range, String startDate, String endDate) {
        List<Order> orders = getFilteredOrders(range, startDate, endDate);

        Map<String, TopItemDto> itemMap = new HashMap<>();
        
        orders.forEach(order -> {
            order.getItems().forEach(item -> {
                String name = item.getName();
                int qty = item.getQuantity();
                double rev = item.getPrice() * qty;
                
                TopItemDto dto = itemMap.getOrDefault(name, new TopItemDto(name, 0, 0.0));
                dto.setQty(dto.getQty() + qty);
                dto.setRevenue(dto.getRevenue() + rev);
                itemMap.put(name, dto);
            });
        });

        return itemMap.values().stream()
                .sorted((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()))
                .limit(6)
                .collect(Collectors.toList());
    }
}


