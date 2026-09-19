import type { Metadata } from "next";
import { Inter, Nunito, Poppins } from "next/font/google";
import "./globals.css";
import Sidebar from "@/components/layout/Sidebar";

const inter = Inter({ 
  subsets: ["latin"],
  variable: "--font-sans",
});

const nunito = Nunito({ 
  subsets: ['latin'],
  variable: '--font-nunito' 
});

const poppins = Poppins({ 
  weight: ['300', '400', '500', '600', '700', '800', '900'],
  subsets: ['latin'],
  variable: '--font-poppins'
});

export const metadata: Metadata = {
  title: "Karulaksh Cafe - Multi-Tenant Enterprise Cloud POS",
  description: "Custom branded multi-tenant cloud POS management: billing, orders, reports, staff and menu management powered by Karulaksh Cafe.",
};

import { MenuProvider } from "@/context/MenuContext";

import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className={`${inter.variable} ${nunito.variable} ${poppins.variable} scroll-smooth`} suppressHydrationWarning>
      <body className="font-sans bg-slate-50 antialiased" suppressHydrationWarning>
        <MenuProvider>
          <div className="flex h-screen overflow-hidden">
            <Sidebar />
            <div className="flex-1 flex flex-col overflow-hidden bg-[#070a0f]">
              <main className="flex-1 overflow-y-auto">
                {children}
              </main>
            </div>
          </div>
        </MenuProvider>
        <ToastContainer position="top-right" autoClose={3000} hideProgressBar={false} />
      </body>
    </html>
  );
}
