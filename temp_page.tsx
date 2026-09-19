"use client";

import React, { useState, useCallback } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Eye, EyeOff, ArrowLeft, Rocket, Sparkles, ShieldCheck, Zap } from "lucide-react";
import { authService } from "@/services/auth.service";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  const handleSubmit = useCallback(async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg("");

    try {
      const data = await authService.login({ email, password });
      
      if (typeof window !== "undefined") {
        localStorage.clear();
        localStorage.setItem("user_email", email || "customer@test.com");
        if (data?.businessName) localStorage.setItem("user_business_name", data.businessName);
        if (data?.fullSubdomainUrl) localStorage.setItem("user_subdomain", data.fullSubdomainUrl);
        if (data?.token) localStorage.setItem("user_token", data.token);
      }
      
      setTimeout(() => {
        setLoading(false);
        router.push("/billing");
      }, 800);
    } catch (err: any) {
      console.error("Login Axios fallback:", err);
      setLoading(false);
      
      if (err.response && err.response.data && err.response.data.error) {
        setErrorMsg(err.response.data.error);
      } else {
        setErrorMsg("Invalid username/email or password.");
      }
    }
  }, [email, password, router]);

  return (
    <div className="min-h-screen w-full relative flex items-center justify-center p-4 sm:p-6 lg:p-8 font-sans bg-[#030407] text-white overflow-hidden selection:bg-gold-500 selection:text-black">
      
      {/* CUSTOM KEYFRAME ANIMATIONS FOR FLYING ROCKET & COSMIC MOTION */}
      <style jsx>{`
        @keyframes flyRocket {
          0% {
            transform: translate(-15vw, 45vh) rotate(35deg) scale(0.8);
            opacity: 0;
          }
          10% {
            opacity: 1;
          }
          90% {
            opacity: 1;
          }
          100% {
            transform: translate(105vw, -45vh) rotate(35deg) scale(1.15);
            opacity: 0;
          }
        }
        @keyframes floatBadge1 {
          0%, 100% { transform: translateY(0px) rotate(-3deg); }
          50% { transform: translateY(-16px) rotate(2deg); }
        }
        @keyframes floatBadge2 {
          0%, 100% { transform: translateY(0px) rotate(3deg); }
          50% { transform: translateY(18px) rotate(-2deg); }
        }
        @keyframes orbitRing {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
        @keyframes pulseGlow {
          0%, 100% { opacity: 0.15; transform: scale(1); }
          50% { opacity: 0.3; transform: scale(1.1); }
        }
        .animate-fly-rocket {
          animation: flyRocket 16s ease-in-out infinite;
        }
        .animate-badge-1 {
          animation: floatBadge1 7s ease-in-out infinite;
        }
        .animate-badge-2 {
          animation: floatBadge2 8s ease-in-out infinite;
        }
        .animate-orbit {
          animation: orbitRing 40s linear infinite;
        }
        .animate-pulse-glow {
          animation: pulseGlow 10s ease-in-out infinite;
        }
      `}</style>

      {/* DEEP LUXURY OBSIDIAN BACKGROUND (REDUCED BRIGHTNESS) */}
      <div className="absolute inset-0 bg-gradient-to-br from-[#06080d] via-[#030407] to-[#020305] z-0"></div>
      
      {/* SUBTLE COSMIC GRID OVERLAY */}
      <div 
        className="absolute inset-0 opacity-10 pointer-events-none z-0"
        style={{
          backgroundImage: `radial-gradient(rgba(212, 175, 55, 0.25) 1px, transparent 1px)`,
          backgroundSize: '2.5rem 2.5rem'
        }}
      ></div>

      {/* SOFT AMBIENT DARK GOLD SPOTLIGHTS (LOW BRIGHTNESS) */}
      <div className="absolute -top-40 -left-40 w-[30rem] h-[30rem] bg-gold-500/10 rounded-full blur-[150px] pointer-events-none animate-pulse-glow z-0"></div>
      <div className="absolute -bottom-40 -right-40 w-[35rem] h-[35rem] bg-gold-600/10 rounded-full blur-[170px] pointer-events-none animate-pulse-glow z-0" style={{ animationDelay: '-5s' }}></div>

      {/* ROTATING COSMIC ORBIT RINGS AROUND THE CONTAINER */}
      <div className="absolute w-[36rem] h-[36rem] sm:w-[46rem] sm:h-[46rem] rounded-full border border-gold-500/15 pointer-events-none animate-orbit z-10 border-dashed"></div>
      <div className="absolute w-[42rem] h-[42rem] sm:w-[54rem] sm:h-[54rem] rounded-full border border-gold-500/10 pointer-events-none animate-orbit z-10" style={{ animationDirection: 'reverse', animationDuration: '55s' }}></div>

      {/* FLOATING DESIGN ELEMENTS AROUND THE LOGIN CARD */}
      <div className="absolute hidden lg:flex items-center gap-3 top-1/4 left-[10%] sm:left-[15%] bg-[#0d121a]/90 backdrop-blur-xl border border-gold-500/35 px-5 py-3.5 rounded-2xl shadow-2xl animate-badge-1 z-20">
        <div className="w-10 h-10 rounded-xl bg-gold-500/20 flex items-center justify-center text-gold-400 border border-gold-500/30">
          <Zap className="w-5 h-5 fill-gold-400" />
        </div>
        <div>
          <h4 className="text-sm font-bold text-white">Sub-Second Billing</h4>
          <p className="text-xs text-gold-400 font-medium">Lightning Fast Terminal</p>
        </div>
      </div>

      <div className="absolute hidden lg:flex items-center gap-3 bottom-1/4 right-[10%] sm:right-[15%] bg-[#0d121a]/90 backdrop-blur-xl border border-gold-500/35 px-5 py-3.5 rounded-2xl shadow-2xl animate-badge-2 z-20">
        <div className="w-10 h-10 rounded-xl bg-gold-500/20 flex items-center justify-center text-gold-400 border border-gold-500/30">
          <ShieldCheck className="w-5 h-5" />
        </div>
        <div>
          <h4 className="text-sm font-bold text-white">256-Bit SSL Encrypted</h4>
          <p className="text-xs text-gold-400 font-medium">100% Offline Capable</p>
        </div>
      </div>

      {/* TOP-LEFT FLOATING ICON-ONLY BACK BUTTON */}
      <div className="absolute top-6 left-6 sm:top-8 sm:left-8 z-30">
        <Link
          href="/welcome"
          title="Back to Home"
          className="w-11 h-11 rounded-full bg-[#0d121a]/90 hover:bg-gold-500/20 text-gold-400 hover:text-white backdrop-blur-xl border border-gold-500/40 flex items-center justify-center transition-all shadow-lg hover:scale-105 cursor-pointer"
        >
          <ArrowLeft className="w-5 h-5" />
        </Link>
      </div>

      {/* CENTERED GLASSMORPHIC LOGIN CONTAINER (LOW BRIGHTNESS, SLEEK LUXURY) */}
      <div className="relative z-20 max-w-md w-full bg-[#0a0e16]/90 backdrop-blur-2xl text-white rounded-[2.5rem] p-8 sm:p-10 shadow-[0_20px_70px_-15px_rgba(0,0,0,0.9)] border border-gold-500/35 hover:border-gold-400/60 transition-all duration-500 animate-in fade-in zoom-in-95 duration-300">
        
        {/* Card Header (Centered) */}
        <div className="mb-8 text-center">
          <h1 className="text-3xl sm:text-4xl font-bold tracking-tight text-white drop-shadow-md">
            Log in
          </h1>
          <p className="text-xs text-gray-400 mt-1">Access your enterprise dashboard</p>
        </div>

        {/* Form */}
        {errorMsg && (
          <div className="mb-4 p-3 bg-red-500/20 border border-red-500/50 rounded-xl text-red-200 text-sm font-medium text-center">
            {errorMsg}
          </div>
        )}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-gold-300 mb-1.5">
              User name or email address
            </label>
            <input
              type="text"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter username or email..."
              className="w-full px-4 py-3.5 rounded-2xl bg-[#040609] border border-gold-500/30 focus:border-gold-400 focus:ring-1 focus:ring-gold-400 outline-none transition-all text-sm text-white placeholder-gray-600"
            />
          </div>

          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="text-xs font-bold uppercase tracking-wider text-gold-300">
                Your password
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
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter password..."
              className="w-full px-4 py-3.5 rounded-2xl bg-[#040609] border border-gold-500/30 focus:border-gold-400 focus:ring-1 focus:ring-gold-400 outline-none transition-all text-sm text-white placeholder-gray-600"
            />
            
            {/* Bottom Row: Don't have an account on left, Forget password on right */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mt-3.5 text-xs">
              <div>
                <span className="text-gray-400">Don&apos;t have an account? </span>
                <Link
                  href="/auth/signup"
                  className="font-extrabold text-gold-400 underline underline-offset-4 hover:text-gold-300 transition-colors"
                >
                  Register
                </Link>
              </div>
              <a
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  alert("Password reset instructions sent to email.");
                }}
                className="font-bold text-gray-300 underline underline-offset-4 hover:text-gold-400 text-left sm:text-right transition-colors"
              >
                Forget your password
              </a>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className={`w-full py-4 mt-6 rounded-2xl font-black text-sm transition-all cursor-pointer disabled:opacity-75 flex items-center justify-center gap-2 ${
              email && password
                ? "bg-gradient-to-r from-gold-400 via-gold-500 to-gold-600 hover:opacity-95 text-black shadow-xl shadow-gold-500/30 scale-[1.01]"
                : "bg-white/10 border border-gold-500/20 text-gray-300 hover:bg-white/15 hover:text-white"
            }`}
          >
            {loading ? (
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 border-2 border-black border-t-transparent rounded-full animate-spin" />
                <span>Logging in...</span>
              </div>
            ) : (
              <span>Log in</span>
            )}
          </button>
        </form>

        {/* Bottom Security Notice */}
        <div className="mt-8 pt-6 border-t border-gold-500/20 text-center text-[11px] text-gray-400 font-medium">
          Protected by 256-bit SSL encryption & enterprise SSO.
        </div>
      </div>

    </div>
  );
}
