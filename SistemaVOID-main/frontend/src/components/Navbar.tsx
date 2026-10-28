'use client';

import Link from 'next/link';
import { Search, Plus, Bell, User } from 'lucide-react';
import { useScrolled } from '@/hooks/useScrolled';
import VoidLogo from '@/components/ui/VoidLogo';
import { useState, useEffect } from 'react';
import { authService } from '@/services/auth.service';
import UserDropdown from './UserDropdown';
import AuthModal from './AuthModal';
import SettingsModal from './SettingsModal';

const NAV_LINKS = [
  { label: 'Inicio', href: '/' },
  { label: 'Películas', href: '/peliculas' },
  { label: 'Series', href: '/series' },
  { label: 'Categorías', href: '/categorias' },
];

export default function Navbar() {
  const scrolled = useScrolled(40);
  const [searchOpen, setSearchOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  
  // Simulated user state
  const [user, setUser] = useState<{name: string; email: string; plan: string; role?: string} | null>(null);

  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    if (currentUser) {
      setUser({
        name: currentUser.fullName,
        email: currentUser.email,
        plan: 'Plan Premium', // Hardcoded as requested
        role: currentUser.role
      });
    }
  }, []);

  const handleLogout = () => {
    setUser(null);
    authService.logout();
  };

  return (
    <>
      <header
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-500 ease-out border-b ${
        scrolled
          ? 'bg-void-black/90 backdrop-blur-xl border-white/[0.04]'
          : 'bg-gradient-to-b from-void-black/70 to-transparent border-transparent'
      }`}
    >
      <nav className="flex items-center justify-between px-6 lg:px-12 h-16 lg:h-[72px] max-w-[1920px] mx-auto" aria-label="Navegación principal">
        {/* Left: Logo + Navigation */}
        <div className="flex items-center gap-10">
          <Link href="/" className="flex-shrink-0" aria-label="VOID - Inicio">
            <VoidLogo size="md" />
          </Link>

          {/* Desktop Nav Links */}
          <ul className="hidden lg:flex items-center gap-7">
            {NAV_LINKS.map((link) => (
              <li key={link.href}>
                <Link
                  href={link.href}
                  className="text-sm text-void-gray-light hover:text-void-white transition-colors duration-200 font-medium"
                >
                  {link.label}
                </Link>
              </li>
            ))}
          </ul>
        </div>

        {/* Right: Actions */}
        <div className="flex items-center gap-5">
          {/* Search */}
          <button
            onClick={() => setSearchOpen(!searchOpen)}
            className="text-void-gray-light hover:text-void-white transition-colors duration-200"
            aria-label="Buscar"
          >
            <Search size={20} strokeWidth={1.5} />
          </button>

          {/* User Dropdown */}
          <UserDropdown 
            user={user}
            onOpenAuth={() => window.location.href = '/login'}
            onOpenSettings={() => setIsSettingsOpen(true)}
            onLogout={handleLogout}
          />
        </div>
      </nav>

      {/* Search Overlay */}
      {searchOpen && (
        <div className="absolute top-full left-0 right-0 bg-void-black/95 backdrop-blur-xl border-b border-void-gray-border px-6 lg:px-12 py-5 animate-fade-in-up">
          <div className="max-w-3xl mx-auto">
            <div className="flex items-center gap-3 bg-void-gray-dark rounded-lg px-4 py-3">
              <Search size={20} className="text-void-gray-muted flex-shrink-0" strokeWidth={1.5} />
              <input
                type="text"
                placeholder="Buscar películas, series, actores..."
                className="bg-transparent w-full text-void-white placeholder:text-void-gray-muted text-sm outline-none"
                autoFocus
              />
            </div>
          </div>
        </div>
      )}
    </header>

      
      <SettingsModal 
        isOpen={isSettingsOpen} 
        onClose={() => setIsSettingsOpen(false)} 
        user={user}
      />
    </>
  );
}
