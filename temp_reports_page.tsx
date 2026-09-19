"use client";

import React, { useState, useEffect } from "react";
import {
  TrendingUp,
  TrendingDown,
  IndianRupee,
  Receipt,
  BarChart3,
  ArrowUpRight,
  ArrowDownRight,
  Calendar,
  X,
  Loader2,
} from "lucide-react";
import { 
  analyticsService, 
  AnalyticsOverviewDto, 
  TodayYesterdayDto, 
  DailyRevenueDto, 
  PaymentMethodBreakdownDto, 
  TopItemDto 
} from "@/services/analytics.service";
import { toast } from "react-toastify";

function formatRs(n: number) {
  return "₹" + (n || 0).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

const MONTH_NAMES = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];

interface DateRangeControlProps {
  fromDate: string;
  toDate: string;
  onFromChange: (date: string) => void;
  onToChange: (date: string) => void;
  onClear: () => void;
  isDark?: boolean;
}

function DateRangeControl({ fromDate, toDate, onFromChange, onToChange, onClear, isDark }: DateRangeControlProps) {
  return (
    <div className={`flex flex-wrap items-center gap-1.5 p-1 rounded-xl border text-xs font-semibold shadow-2xs ${
      isDark ? "bg-white/10 border-white/20 text-white" : "bg-slate-50 border-slate-200 text-slate-700"
    }`}>
      <div className="flex items-center gap-1 px-2 py-0.5">
        <Calendar className={`w-3.5 h-3.5 shrink-0 ${isDark ? "text-primary-300" : "text-primary-600"}`} />
        <input
          type="date"
          value={fromDate}
          onChange={(e) => onFromChange(e.target.value)}
          max={toDate || new Date().toISOString().split("T")[0]}
          style={{ colorScheme: isDark ? "dark" : "light" }}
          className={`bg-transparent font-bold cursor-pointer outline-none text-xs ${isDark ? "text-white" : "text-slate-800"}`}
        />
      </div>

      <span className={isDark ? "text-white/40 font-black shrink-0 hidden sm:inline" : "text-slate-300 font-black shrink-0 hidden sm:inline"}>→</span>

      <div className="flex items-center gap-1 px-2 py-0.5">
        <input
          type="date"
          value={toDate}
          onChange={(e) => onToChange(e.target.value)}
          min={fromDate}
          max={new Date().toISOString().split("T")[0]}
          style={{ colorScheme: isDark ? "dark" : "light" }}
          className={`bg-transparent font-bold cursor-pointer outline-none text-xs ${isDark ? "text-white" : "text-slate-800"}`}
        />
      </div>

      {(fromDate || toDate) && (
        <button
          type="button"
          onClick={onClear}
          title="Clear Dates"
          className={`p-1 rounded-lg transition-colors cursor-pointer shrink-0 ${
            isDark ? "hover:bg-white/20 text-rose-300" : "hover:bg-rose-100 text-rose-600"
          }`}
        >
          <X className="w-3.5 h-3.5" />
        </button>
      )}
    </div>
  );
}

function DailyRevenueChart({ dailyRev, dailyRange, isDailyLoading }: { dailyRev: DailyRevenueDto[]; dailyRange: "1d" | "1w" | "1m" | "1y"; isDailyLoading: boolean }) {
  const formattedDailyRev = (() => {
    if (dailyRange === "1d") {
      const defaultLabels = ["8 AM", "10 AM", "12 PM", "2 PM", "4 PM", "6 PM", "8 PM", "10 PM"];
      const map = new Map(dailyRev.map(d => [d.date, d.revenue]));
      return defaultLabels.map(l => ({ date: l, revenue: map.get(l) || 0 }));
    }
    if (dailyRange === "1w") {
      const defaultLabels = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
      const map = new Map(dailyRev.map(d => [d.date, d.revenue]));
      return defaultLabels.map(l => ({ date: l, revenue: map.get(l) || 0 }));
    }
    if (dailyRange === "1m") {
      const defaultLabels = ["1st week", "2nd week", "3rd week", "4th week"];
      const map = new Map(dailyRev.map(d => [d.date, d.revenue]));
      return defaultLabels.map(l => ({ date: l, revenue: map.get(l) || 0 }));
    }
    if (dailyRange === "1y") {
      const defaultLabels = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
      const map = new Map(dailyRev.map(d => [d.date, d.revenue]));
      return defaultLabels.map(l => ({ date: l, revenue: map.get(l) || 0 }));
    }
    return dailyRev;
  })();

  const maxChartVal = Math.max(...formattedDailyRev.map(d => d.revenue), 1);

  if (isDailyLoading) {
    return (
      <div className="h-44 flex items-center justify-center gap-2 text-slate-400">
        <Loader2 className="w-5 h-5 text-primary-600 animate-spin" />
        <span className="text-xs font-medium">Loading daily revenue...</span>
      </div>
    );
  }

  if (formattedDailyRev.length === 0) {
    return (
      <div className="h-44 flex items-center justify-center text-slate-400 text-xs">
        No revenue recorded in this period.
      </div>
    );
  }

  if (dailyRange === "1d") {
    const pts = formattedDailyRev;
    const coords = pts.map((p, idx) => {
      const x = pts.length === 1 ? 200 : (idx / Math.max(pts.length - 1, 1)) * 400;
      const y = 92 - (p.revenue / maxChartVal) * 82;
      return { x, y, p };
    });
    const coordStrs = coords.map(c => `${c.x},${c.y}`);
    const areaPath = pts.length === 1
      ? `M 0,100 L 200,${coords[0].y} L 400,100 Z`
      : `M 0,100 L ${coordStrs.join(" L ")} L 400,100 Z`;
    const linePath = pts.length === 1
      ? `M 0,92 L 200,${coords[0].y} L 400,92`
      : `M ${coordStrs.join(" L ")}`;

    return (
      <div className="h-44 w-full flex flex-col justify-end pt-4 pb-1">
        <div className="relative h-32 w-full">
          <svg className="w-full h-full overflow-visible" viewBox="0 0 400 100" preserveAspectRatio="none">
            <defs>
              <linearGradient id="areaGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" stopColor="#2563eb" stopOpacity="0.3" />
                <stop offset="100%" stopColor="#2563eb" stopOpacity="0.0" />
              </linearGradient>
            </defs>
            <path d={areaPath} fill="url(#areaGradient)" />
            <path d={linePath} fill="none" stroke="#2563eb" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
            {coords.map((c, idx) => (
              <g key={idx} className="group cursor-pointer">
                <circle cx={c.x} cy={c.y} r="4.5" className="fill-white stroke-primary-600 stroke-2 transition-all group-hover:r-6" />
                <title>{c.p.date}: ₹{c.p.revenue.toFixed(0)}</title>
              </g>
            ))}
          </svg>
        </div>
        <div className="flex justify-between text-[9px] text-slate-400 font-semibold mt-2 px-1">
          {formattedDailyRev.map((p, i) => (
            <span key={i} className="truncate">{p.date}</span>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="w-full overflow-x-auto pb-2 pt-6 min-w-0 custom-scrollbar">
      <div className={`flex items-end gap-1.5 h-38 ${formattedDailyRev.length > 14 ? "min-w-[550px]" : "w-full"}`}>
        {formattedDailyRev.map((day) => {
          const pct = maxChartVal > 0 ? (day.revenue / maxChartVal) * 100 : 0;
          const d = new Date(day.date);
          return (
            <div key={day.date} className="flex-1 flex flex-col items-center gap-1.5 group relative min-w-[20px]">
              <div className="absolute -top-7 opacity-0 group-hover:opacity-100 transition-opacity bg-slate-900 text-white text-[10px] font-bold px-2 py-0.5 rounded shadow-md whitespace-nowrap z-20 pointer-events-none">
                ₹{day.revenue.toFixed(0)}
              </div>
              <div className="w-full bg-slate-100 rounded-t-md relative overflow-hidden h-28">
                <div
                  className="absolute bottom-0 w-full bg-gradient-to-t from-primary-600 to-primary-400 group-hover:from-primary-500 group-hover:to-amber-400 rounded-t-md transition-all duration-500"
                  style={{ height: `${Math.max(pct, 3)}%` }}
                />
              </div>
              <span className="text-[9px] text-slate-500 font-semibold truncate max-w-full">
                {isNaN(d.getTime()) ? day.date : `${d.getDate()}${MONTH_NAMES[d.getMonth()]?.substring(0,3)}`}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default function ReportsPage() {
  const [range, setRange] = useState<"7d" | "30d" | "all">("all");
  
  // Overall / Global Date Filter
  const [globalFromDate, setGlobalFromDate] = useState<string>("");
  const [globalToDate, setGlobalToDate] = useState<string>("");

  const [overview, setOverview] = useState<AnalyticsOverviewDto | null>(null);
  const [todayYest, setTodayYest] = useState<TodayYesterdayDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Daily Revenue Filter & State
  const [dailyRev, setDailyRev] = useState<DailyRevenueDto[]>([]);
  const [dailyRange, setDailyRange] = useState<"1d" | "1w" | "1m" | "1y">("1m");
  const [isDailyLoading, setIsDailyLoading] = useState<boolean>(false);

  // Payment Breakdown Filter & State
  const [paymentBreakdown, setPaymentBreakdown] = useState<PaymentMethodBreakdownDto | null>(null);
  const [paymentFromDate, setPaymentFromDate] = useState<string>("");
  const [paymentToDate, setPaymentToDate] = useState<string>("");
  const [isPaymentLoading, setIsPaymentLoading] = useState<boolean>(false);

  // Top Items Filter & State
  const [topItems, setTopItems] = useState<TopItemDto[]>([]);
  const [topFromDate, setTopFromDate] = useState<string>("");
  const [topToDate, setTopToDate] = useState<string>("");
  const [isTopItemsLoading, setIsTopItemsLoading] = useState<boolean>(false);

  // Fetch Overview & Today/Yesterday
  useEffect(() => {
    let isMounted = true;
    setIsLoading(true);

    Promise.all([
      analyticsService.getOverview(range, globalFromDate || undefined, globalToDate || undefined),
      analyticsService.getTodayYesterday(),
    ]).then(([overviewData, todayYestData]) => {
      if (isMounted) {
        setOverview(overviewData);
        setTodayYest(todayYestData);
        setIsLoading(false);
      }
    }).catch(err => {
      toast.error("Failed to load reports overview");
      if (isMounted) setIsLoading(false);
    });

    return () => { isMounted = false; };
  }, [range, globalFromDate, globalToDate]);

  // Fetch Daily Revenue
  useEffect(() => {
    let isMounted = true;
    setIsDailyLoading(true);
    analyticsService.getDailyRevenue(dailyRange)
      .then((data) => {
        if (isMounted) {
          setDailyRev(data);
          setIsDailyLoading(false);
        }
      })
      .catch(() => {
        if (isMounted) setIsDailyLoading(false);
      });
    return () => { isMounted = false; };
  }, [dailyRange]);

  // Fetch Payment Breakdown
  useEffect(() => {
    let isMounted = true;
    setIsPaymentLoading(true);
    const from = paymentFromDate || globalFromDate || undefined;
    const to = paymentToDate || globalToDate || undefined;
    analyticsService.getPaymentBreakdown(range, from, to)
      .then((data) => {
        if (isMounted) {
          setPaymentBreakdown(data);
          setIsPaymentLoading(false);
        }
      })
      .catch(() => {
        if (isMounted) setIsPaymentLoading(false);
      });
    return () => { isMounted = false; };
  }, [range, globalFromDate, globalToDate, paymentFromDate, paymentToDate]);

  // Fetch Top Items
  useEffect(() => {
    let isMounted = true;
    setIsTopItemsLoading(true);
    const from = topFromDate || globalFromDate || undefined;
    const to = topToDate || globalToDate || undefined;
    analyticsService.getTopItems(range, from, to)
      .then((data) => {
        if (isMounted) {
          setTopItems(data);
          setIsTopItemsLoading(false);
        }
      })
      .catch(() => {
        if (isMounted) setIsTopItemsLoading(false);
      });
    return () => { isMounted = false; };
  }, [range, globalFromDate, globalToDate, topFromDate, topToDate]);

  const maxDayRevenue = Math.max(...dailyRev.map((d) => d.revenue), 1);
  const maxItemRevenue = Math.max(...topItems.map((i) => i.revenue), 1);

  if (isLoading || !overview || !todayYest) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="text-slate-400 font-bold animate-pulse">Loading Analytics...</div>
      </div>
    );
  }

  const activeTodayYest = (() => {
    if (todayYest.todayRevenue > 0 || todayYest.yesterdayRevenue > 0) return todayYest;
    const activeDays = dailyRev.filter(d => d.revenue > 0);
    if (activeDays.length >= 2) {
      const todayRev = activeDays[activeDays.length - 1].revenue;
      const yestRev = activeDays[activeDays.length - 2].revenue;
      const change = yestRev > 0 ? ((todayRev - yestRev) / yestRev) * 100 : 100;
      return { todayRevenue: todayRev, yesterdayRevenue: yestRev, changePercentage: change };
    } else if (activeDays.length === 1) {
      return { todayRevenue: activeDays[0].revenue, yesterdayRevenue: 0, changePercentage: 100 };
    } else if (overview.totalRevenue > 0) {
      return { todayRevenue: overview.totalRevenue, yesterdayRevenue: 0, changePercentage: 100 };
    }
    return todayYest;
  })();

  const dayDelta = activeTodayYest.changePercentage;

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary-950 to-primary-800 px-6 py-6 shadow-sm">
        <div className="max-w-6xl mx-auto flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-black text-white tracking-tight">Reports & Analytics</h1>
            <p className="text-sm text-primary-300 mt-0.5">Revenue, profit & loss overview</p>
          </div>
          
          {/* Overall Date Range Selector */}
          <div className="flex items-center">
            <DateRangeControl
              fromDate={globalFromDate}
              toDate={globalToDate}
              onFromChange={(d) => setGlobalFromDate(d)}
              onToChange={(d) => setGlobalToDate(d)}
              onClear={() => {
                setGlobalFromDate("");
                setGlobalToDate("");
              }}
              isDark={true}
            />
          </div>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-6 py-6 space-y-6">

        {/* KPI Cards Row */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {[
            { label: "Total Revenue", value: formatRs(overview.totalRevenue), icon: IndianRupee, color: "emerald", sub: `${overview.totalBills} bills` },
            { label: "Total Discount", value: formatRs(overview.totalDiscount), icon: TrendingDown, color: "rose", sub: "Given to customers" },
            { label: "Total Tax (GST)", value: formatRs(overview.totalTax), icon: Receipt, color: "blue", sub: "CGST + SGST collected" },
            { label: "Avg. Bill Value", value: formatRs(overview.avgBillValue), icon: BarChart3, color: "purple", sub: "Per transaction" },
          ].map((kpi) => {
            const Icon = kpi.icon;
            const colorMap: Record<string, string> = {
              emerald: "bg-emerald-50 text-emerald-600",
              rose: "bg-rose-50 text-rose-600",
              blue: "bg-blue-50 text-blue-600",
              purple: "bg-purple-50 text-purple-600",
            };
            return (
              <div key={kpi.label} className="bg-white rounded-2xl border border-slate-200 p-5">
                <div className="flex items-center justify-between mb-3">
                  <p className="text-xs text-slate-400 font-semibold uppercase tracking-wide">{kpi.label}</p>
                  <div className={`p-2 rounded-lg ${colorMap[kpi.color]}`}>
                    <Icon className="w-4 h-4" />
                  </div>
                </div>
                <p className="text-xl font-black text-slate-800">{kpi.value}</p>
                <p className="text-[11px] text-slate-400 mt-1">{kpi.sub}</p>
              </div>
            );
          })}
        </div>

        {/* Today vs Yesterday */}
        <div className="bg-white rounded-2xl border border-slate-200 p-5">
          <h2 className="text-sm font-bold text-slate-700 mb-4">Today vs Yesterday</h2>
          <div className="grid grid-cols-3 gap-6">
            <div>
              <p className="text-xs text-slate-400 font-semibold uppercase mb-1">Today</p>
              <p className="text-2xl font-black text-slate-800">{formatRs(activeTodayYest.todayRevenue)}</p>
            </div>
            <div>
              <p className="text-xs text-slate-400 font-semibold uppercase mb-1">Yesterday</p>
              <p className="text-2xl font-black text-slate-500">{formatRs(activeTodayYest.yesterdayRevenue)}</p>
            </div>
            <div>
              <p className="text-xs text-slate-400 font-semibold uppercase mb-1">Change</p>
              <div className="flex items-center gap-1.5">
                {dayDelta >= 0 ? (
                  <ArrowUpRight className="w-5 h-5 text-emerald-500" />
                ) : (
                  <ArrowDownRight className="w-5 h-5 text-rose-500" />
                )}
                <p className={`text-2xl font-black ${dayDelta >= 0 ? "text-emerald-600" : "text-rose-600"}`}>
                  {dayDelta >= 0 ? "+" : ""}{dayDelta.toFixed(1)}%
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 min-w-0">
          {/* Daily Revenue Chart */}
          <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm flex flex-col justify-between overflow-hidden min-w-0">
            <div>
              <div className="flex flex-col xl:flex-row xl:items-center justify-between gap-2 mb-4">
                <div>
                  <h2 className="text-sm font-bold text-slate-700">Daily Revenue</h2>
                  <p className="text-[11px] text-slate-400 mt-0.5">
                    {dailyRange === "1d"
                      ? "Last 24 Hours"
                      : dailyRange === "1w"
                      ? "Last 7 Days"
                      : dailyRange === "1y"
                      ? "Last 1 Year (365 Days)"
                      : "Last 1 Month (30 Days)"}
                  </p>
                </div>
                <div className="flex bg-slate-100 p-1 rounded-xl gap-1">
                  {(["1d", "1w", "1m", "1y"] as const).map((r) => (
                    <button
                      key={r}
                      type="button"
                      onClick={() => setDailyRange(r)}
                      className={`px-2.5 py-1 rounded-lg text-[11px] font-bold transition-all cursor-pointer ${
                        dailyRange === r
                          ? "bg-white text-primary-600 shadow-2xs"
                          : "text-slate-500 hover:text-slate-900"
                      }`}
                    >
                      {r === "1d" ? "1 Day" : r === "1w" ? "1 Week" : r === "1m" ? "1 Month" : "1 Year"}
                    </button>
                  ))}
                </div>
              </div>

              <DailyRevenueChart dailyRev={dailyRev} dailyRange={dailyRange} isDailyLoading={isDailyLoading} />
            </div>
          </div>

          {/* Payment Breakdown */}
          <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm flex flex-col justify-between overflow-hidden min-w-0">
            <div>
              <div className="flex flex-col xl:flex-row xl:items-center justify-between gap-2 mb-4">
                <div>
                  <h2 className="text-sm font-bold text-slate-700">Revenue by Payment Method</h2>
                  <p className="text-[11px] text-slate-400 mt-0.5">
                    {paymentFromDate || paymentToDate
                      ? `Filtered: ${paymentFromDate || "Start"} → ${paymentToDate || "Today"}`
                      : globalFromDate || globalToDate
                      ? `Overall: ${globalFromDate || "Start"} → ${globalToDate || "Today"}`
                      : `${range === "7d" ? "Last 7 Days" : range === "30d" ? "Last 30 Days" : "All Time"}`}
                  </p>
                </div>
                <DateRangeControl
                  fromDate={paymentFromDate}
                  toDate={paymentToDate}
                  onFromChange={setPaymentFromDate}
                  onToChange={setPaymentToDate}
                  onClear={() => {
                    setPaymentFromDate("");
                    setPaymentToDate("");
                  }}
                />
              </div>

              {isPaymentLoading || !paymentBreakdown ? (
                <div className="py-12 flex items-center justify-center gap-2 text-slate-400">
                  <Loader2 className="w-5 h-5 text-primary-600 animate-spin" />
                  <span className="text-xs font-medium">Loading breakdown...</span>
                </div>
              ) : (
                <>
                  <div className="space-y-3">
                    {[
                      { key: "cash", label: "Cash", color: "bg-emerald-500", val: paymentBreakdown.cash },
                      { key: "upi", label: "UPI", color: "bg-purple-500", val: paymentBreakdown.upi },
                      { key: "card", label: "Card", color: "bg-blue-500", val: paymentBreakdown.card },
                    ].map((m) => {
                      const val = m.val;
                      const pct = overview.totalRevenue > 0 ? (val / overview.totalRevenue) * 100 : 0;
                      return (
                        <div key={m.key}>
                          <div className="flex justify-between text-xs mb-1">
                            <span className="font-semibold text-slate-600">{m.label}</span>
                            <span className="font-bold text-slate-800">{formatRs(val)} <span className="text-slate-400">({pct.toFixed(1)}%)</span></span>
                          </div>
                          <div className="h-2 bg-slate-100 rounded-full overflow-hidden">
                            <div
                              className={`h-full rounded-full transition-all duration-500 ${m.color}`}
                              style={{ width: `${pct}%` }}
                            />
                          </div>
                        </div>
                      );
                    })}
                  </div>

                  {/* Profit & Loss Summary */}
                  <div className="mt-5 pt-4 border-t border-slate-100">
                    <h3 className="text-xs font-bold text-slate-500 uppercase tracking-wide mb-3">P&L Summary</h3>
                    <div className="space-y-2 text-xs">
                      <div className="flex justify-between">
                        <span className="text-slate-500">Gross Revenue</span>
                        <span className="font-bold text-emerald-600">{formatRs(overview.totalRevenue)}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-500">Discount Given</span>
                        <span className="font-bold text-rose-500">-{formatRs(overview.totalDiscount)}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-500">Service Charge Collected</span>
                        <span className="font-bold text-blue-600">+{formatRs(overview.totalServiceCharge)}</span>
                      </div>
                      <div className="flex justify-between border-t border-dashed border-slate-200 pt-2 mt-1">
                        <span className="font-bold text-slate-700">Net Revenue</span>
                        <span className="font-black text-slate-900">{formatRs(overview.totalRevenue - overview.totalDiscount)}</span>
                      </div>
                    </div>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>

        {/* Top Selling Items */}
        <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm">
          <div className="flex flex-col xl:flex-row xl:items-center justify-between gap-2 mb-4">
            <div>
              <h2 className="text-sm font-bold text-slate-700">Top Selling Items</h2>
              <p className="text-[11px] text-slate-400 mt-0.5">
                {topFromDate || topToDate
                  ? `Filtered: ${topFromDate || "Start"} → ${topToDate || "Today"}`
                  : globalFromDate || globalToDate
                  ? `Overall: ${globalFromDate || "Start"} → ${globalToDate || "Today"}`
                  : `${range === "7d" ? "Last 7 Days" : range === "30d" ? "Last 30 Days" : "All Time"}`}
              </p>
            </div>

            <DateRangeControl
              fromDate={topFromDate}
              toDate={topToDate}
              onFromChange={setTopFromDate}
              onToChange={setTopToDate}
              onClear={() => {
                setTopFromDate("");
                setTopToDate("");
              }}
            />
          </div>

          {isTopItemsLoading ? (
            <div className="py-12 flex flex-col items-center justify-center gap-2 text-slate-400">
              <Loader2 className="w-6 h-6 text-primary-600 animate-spin" />
              <span className="text-xs font-medium">Loading top items...</span>
            </div>
          ) : topItems.length === 0 ? (
            <div className="text-center py-10 bg-slate-50/50 rounded-xl border border-dashed border-slate-200">
              <Calendar className="w-8 h-8 text-slate-300 mx-auto mb-2" />
              <p className="text-slate-600 font-bold text-sm">No sales data found</p>
              <p className="text-slate-400 text-xs mt-0.5">
                {topFromDate || topToDate ? "No items sold in selected date range" : "Start taking orders to see top selling items!"}
              </p>
            </div>
          ) : (
            <div className="space-y-3.5">
              {topItems.map((item, i) => {
                const pct = maxItemRevenue > 0 ? (item.revenue / maxItemRevenue) * 100 : 0;
                return (
                  <div key={item.name} className="flex items-center gap-3.5 group">
                    <span className="text-xs font-black text-slate-400 w-5 text-center bg-slate-100 py-1 rounded-md group-hover:bg-primary-50 group-hover:text-primary-600 transition-colors">
                      #{i + 1}
                    </span>
                    <div className="flex-1">
                      <div className="flex justify-between text-xs mb-1.5">
                        <span className="font-bold text-slate-800">{item.name}</span>
                        <span className="text-slate-600 font-medium">
                          <span className="font-black text-slate-900">{item.qty}</span> sold · <span className="font-bold text-emerald-600">{formatRs(item.revenue)}</span>
                        </span>
                      </div>
                      <div className="h-2.5 bg-slate-100 rounded-full overflow-hidden p-0.5 border border-slate-200/60">
                        <div
                          className="h-full rounded-full bg-gradient-to-r from-primary-600 to-amber-500 transition-all duration-700"
                          style={{ width: `${Math.max(pct, 4)}%` }}
                        />
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

      </div>
    </div>
  );
}

