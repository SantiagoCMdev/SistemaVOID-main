'use client';

import { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { User, Settings, Plus, LogOut, LogIn } from 'lucide-react';

interface UserProfile {
  name: string;
  email: string;
  plan: string;
  role?: string;
}

interface UserDropdownProps {
  user: UserProfile | null;
  onOpenAuth: () => void;
  onOpenSettings: () => void;
  onLogout: () => void;
}

export default function UserDropdown({ user, onOpenAuth, onOpenSettings, onLogout }: UserDropdownProps) {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="w-8 h-8 rounded-full bg-void-purple/30 border border-void-purple/40 flex items-center justify-center hover:border-void-purple transition-colors duration-200"
        aria-label="Menú de Usuario"
      >
        <User size={16} strokeWidth={1.5} className="text-void-purple-glow" />
      </button>

      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, y: 10, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 10, scale: 0.95 }}
            transition={{ duration: 0.2, ease: 'easeOut' }}
            className="absolute right-0 mt-3 w-56 bg-void-black/95 backdrop-blur-xl border border-white/[0.08] rounded-xl shadow-2xl py-2 z-50 flex flex-col"
          >
            {user ? (
              <>
                <div className="px-4 py-3 border-b border-white/[0.08] mb-2">
                  <p className="text-sm font-medium text-void-white">{user.name}</p>
                  <p className="text-xs text-void-gray-muted mt-0.5">{user.plan} {user.role === 'ADMIN' && '· Admin'}</p>
                </div>
                <button className="flex items-center gap-3 px-4 py-2.5 text-sm text-void-gray-light hover:text-void-white hover:bg-white/[0.04] transition-colors w-full text-left">
                  <Plus size={16} />
                  Mi lista
                </button>
                <button
                  onClick={() => { setIsOpen(false); onOpenSettings(); }}
                  className="flex items-center gap-3 px-4 py-2.5 text-sm text-void-gray-light hover:text-void-white hover:bg-white/[0.04] transition-colors w-full text-left"
                >
                  <Settings size={16} />
                  Ajustes
                </button>
                <div className="h-px bg-white/[0.08] my-2" />
                <button
                  onClick={() => { setIsOpen(false); onLogout(); }}
                  className="flex items-center gap-3 px-4 py-2.5 text-sm text-red-400 hover:text-red-300 hover:bg-red-500/10 transition-colors w-full text-left"
                >
                  <LogOut size={16} />
                  Cerrar sesión
                </button>
              </>
            ) : (
              <>
                <div className="px-4 py-3 border-b border-white/[0.08] mb-2 text-center">
                  <p className="text-sm text-void-gray-muted">Bienvenido a VOID</p>
                </div>
                <button
                  onClick={() => { setIsOpen(false); onOpenAuth(); }}
                  className="flex items-center gap-3 px-4 py-2.5 text-sm text-void-white hover:bg-white/[0.04] transition-colors w-full text-left font-medium text-void-purple-glow"
                >
                  <LogIn size={16} />
                  Iniciar Sesión / Registrarse
                </button>
              </>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
