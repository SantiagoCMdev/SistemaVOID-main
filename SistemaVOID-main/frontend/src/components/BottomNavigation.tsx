'use client';

import { Home, Search, Plus, User } from 'lucide-react';
import Link from 'next/link';

export default function BottomNavigation() {
  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 lg:hidden bg-void-black/95 backdrop-blur-xl border-t border-white/[0.05]" aria-label="Navegación móvil">
      <div className="flex items-center justify-around h-16 max-w-lg mx-auto">
        <Link href="/" className="flex flex-col items-center gap-1 text-void-purple-glow">
          <Home size={20} strokeWidth={1.5} />
          <span className="text-[10px] font-medium">Inicio</span>
        </Link>
        <Link href="/buscar" className="flex flex-col items-center gap-1 text-void-gray-muted hover:text-void-gray-light transition-colors">
          <Search size={20} strokeWidth={1.5} />
          <span className="text-[10px] font-medium">Buscar</span>
        </Link>
        <Link href="/mi-lista" className="flex flex-col items-center gap-1 text-void-gray-muted hover:text-void-gray-light transition-colors">
          <Plus size={20} strokeWidth={1.5} />
          <span className="text-[10px] font-medium">Mi lista</span>
        </Link>
        <Link href="/perfil" className="flex flex-col items-center gap-1 text-void-gray-muted hover:text-void-gray-light transition-colors">
          <User size={20} strokeWidth={1.5} />
          <span className="text-[10px] font-medium">Perfil</span>
        </Link>
      </div>
    </nav>
  );
}
