'use client';

import { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { User, Settings, Plus } from 'lucide-react';

interface UserProfile {
  name: string;
  email: string;
  plan: string;
  role?: string;
}

interface UserDropdownProps {
  user?: UserProfile | null;
  onOpenSettings: () => void;
}

export default function UserDropdown({ user, onOpenSettings }: UserDropdownProps) {
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

  const displayName = user?.name || 'Usuario';
  const displayPlan = user?.plan || 'Plan Premium';

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
            <div className="px-4 py-3 border-b border-white/[0.08] mb-2">
              <p className="text-sm font-medium text-void-white">{displayName}</p>
              <p className="text-xs text-void-gray-muted mt-0.5">{displayPlan}</p>
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
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
