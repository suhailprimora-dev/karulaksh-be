"use client";

import React, { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Eye, EyeOff, ArrowLeft, Sparkles, ShieldCheck } from "lucide-react";

export default function RegisterPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    password: "",
  });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg("");

    if (typeof window !== "undefined") {
      localStorage.clear();
      localStorage.setItem("user_email", formData.email);
    }

    try {
      const { authService } = await import("@/services/auth.service");
      await authService.signup({
        fullName: formData.fullName,
        email: formData.email,
        password: formData.password,
        businessName: "Karulaksh Cafe", // Hardcoded as it's a single cafe
        sector: "Fine Dining Restaurant"
      });
      setLoading(false);
      router.push("/auth/login");
    } catch (err: any) {
      console.error("Signup error:", err);
      setLoading(false);
      if (err.response && err.response.data && err.response.data.error) {
        setErrorMsg(err.response.data.error);
      } else {
        setErrorMsg(err.response?.data?.message || "Failed to sign up. Please try again.");
      }
    }
  };

  return (
    <div className="min-h-screen w-full flex flex-col lg:flex-row font-sans bg-[#05070a] text-white selection:bg-gold-500 selection:text-black">
      
      {/* LEFT HALF: Custom Image Background Showcase */}
      <div className="w-full lg:w-1/2 min-h-[420px] lg:min-h-screen relative overflow-hidden bg-[#070a0e] text-white p-8 sm:p-12 lg:p-16 flex flex-col justify-between border-b lg:border-b-0 lg:border-r border-gold-500/30">
        
        {/* User's Custom Image Background */}
        <div className="absolute inset-0 z-0">
          <img
            src="/assets/images/2b5a4927d53e78992073b3d7b7cc9c6b.jpg"
            alt="Karulaksh Cafe"
            className="w-full h-full object-cover transform hover:scale-105 transition-transform duration-1000"
          />
          {/* Enhanced Dark Overlay & Shadows for Perfect Text Legibility */}
          <div className="absolute inset-0 bg-black/55"></div>
          <div className="absolute inset-0 bg-gradient-to-t from-[#05070a] via-black/50 to-black/40"></div>
        </div>

        {/* Top Header: Circular Back Icon Button & Badge */}
        <div className="relative z-10 flex items-center justify-between">
          <Link
            href="/welcome"
            title="Back to Home"
            className="w-11 h-11 rounded-full bg-black/60 hover:bg-gold-500/20 text-gold-400 hover:text-white backdrop-blur-xl border border-gold-500/40 flex items-center justify-center transition-all shadow-lg hover:scale-105 cursor-pointer"
          >
            <ArrowLeft className="w-5 h-5" />
          </Link>

          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-black/60 border border-gold-400/50 backdrop-blur-md text-xs font-bold text-gold-300 shadow-md">
            <Sparkles className="w-3.5 h-3.5 text-gold-400 animate-pulse" />
            <span>Karulaksh Cafe Admin</span>
          </div>
        </div>

        {/* Main Hero Content (Just Texts Only, No Container Box) */}
        <div className="relative z-10 my-auto py-12 max-w-lg space-y-4">
          <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-xl bg-gradient-to-r from-gold-400 via-gold-500 to-gold-600 text-black font-black text-xl shadow-lg shadow-gold-500/30">
            <span>Karulaksh Cafe</span>
          </div>
          <h1 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold tracking-tight text-white leading-tight drop-shadow-[0_5px_15px_rgba(0,0,0,1)]">
            Create an Account.
          </h1>
          <p className="text-white font-medium text-sm sm:text-base leading-relaxed drop-shadow-[0_3px_10px_rgba(0,0,0,1)]">
            Register to access the Karulaksh Cafe admin dashboard. Manage orders, billing, and staff seamlessly.
          </p>
        </div>

        {/* Bottom Footer Credit */}
        <div className="relative z-10 flex items-center justify-between text-xs text-gold-400/80 font-medium pt-4 border-t border-gold-500/25">
          <span>KARULAKSH CAFE ADMIN</span>
          <div className="flex items-center gap-1.5 text-gray-400">
            <ShieldCheck className="w-4 h-4 text-gold-400" />
            <span>Secure Access</span>
          </div>
        </div>
      </div>

      {/* RIGHT HALF: Luxury Obsidian Register Panel */}
      <div className="w-full lg:w-1/2 bg-[#0a0e16] flex flex-col justify-center p-6 sm:p-12 lg:p-16 relative overflow-y-auto">
        
        {/* Top Navigation Notice */}
        <div className="absolute top-8 right-8 flex justify-end items-center text-xs sm:text-sm text-gray-400">
          <span>Already have an account? </span>
          <Link
            href="/auth/login"
            className="font-bold text-gold-400 underline underline-offset-4 ml-1.5 hover:text-gold-300 transition-colors"
          >
            Log in
          </Link>
        </div>

        {/* Centered Register Form Box */}
        <div className="max-w-[480px] w-full mx-auto py-4 mt-8">
          <div className="mb-8">
            <h2 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
              Create Account
            </h2>
            <p className="text-xs text-gray-400 mt-1">Enter your details to register as an administrator.</p>
          </div>

          {errorMsg && (
            <div className="mb-6 p-3 bg-red-500/20 border border-red-500/50 rounded-xl text-red-200 text-sm font-medium text-center animate-in fade-in">
              {errorMsg}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-gold-300 mb-1.5">
                Full Name
              </label>
              <input
                type="text"
                required
                value={formData.fullName}
                onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                placeholder="Enter your full name"
                className="w-full px-4 py-3.5 rounded-2xl bg-[#040609] border border-gold-500/30 focus:border-gold-400 focus:ring-1 focus:ring-gold-400 outline-none transition-all text-sm text-white placeholder-gray-600"
              />
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-gold-300 mb-1.5">
                Email Address
              </label>
              <input
                type="email"
                required
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                placeholder="name@example.com"
                className="w-full px-4 py-3.5 rounded-2xl bg-[#040609] border border-gold-500/30 focus:border-gold-400 focus:ring-1 focus:ring-gold-400 outline-none transition-all text-sm text-white placeholder-gray-600"
              />
            </div>

            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="text-xs font-bold uppercase tracking-wider text-gold-300">
                  Password
                </label>
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="text-xs font-semibold text-gray-400 hover:text-gold-300 flex items-center gap-1 cursor-pointer transition-colors"
                >
                  {showPassword ? <Eye className="w-3.5 h-3.5 text-gold-400" /> : <EyeOff className="w-3.5 h-3.5 text-gold-400" />}
                  <span>{showPassword ? "Show" : "Hide"}</span>
                </button>
              </div>
              <input
                type={showPassword ? "text" : "password"}
                required
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                placeholder="Choose a secure password"
                className="w-full px-4 py-3.5 rounded-2xl bg-[#040609] border border-gold-500/30 focus:border-gold-400 focus:ring-1 focus:ring-gold-400 outline-none transition-all text-sm text-white placeholder-gray-600"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className={`w-full py-4 mt-6 rounded-2xl font-black text-sm transition-all cursor-pointer disabled:opacity-75 flex items-center justify-center gap-2 ${
                formData.email && formData.password && formData.fullName
                  ? "bg-gradient-to-r from-gold-400 via-gold-500 to-gold-600 hover:opacity-95 text-black shadow-xl shadow-gold-500/30 scale-[1.01]"
                  : "bg-white/10 border border-gold-500/20 text-gray-300 hover:bg-white/15 hover:text-white"
              }`}
            >
              {loading ? (
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 border-2 border-black border-t-transparent rounded-full animate-spin" />
                  <span>Creating Account...</span>
                </div>
              ) : (
                <span>Register</span>
              )}
            </button>
          </form>
        </div>

        {/* Bottom spacer / copyright */}
        <div className="absolute bottom-8 left-0 w-full text-center text-[11px] text-gray-400 font-medium pt-3 px-8">
          Protected by 256-bit SSL encryption.
        </div>
      </div>

    </div>
  );
}
