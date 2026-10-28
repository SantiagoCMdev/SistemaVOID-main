'use client';

import VoidLogo from '@/components/ui/VoidLogo';

export default function Footer() {
  return (
    <footer className="border-t border-white/[0.04] mt-16 pb-20 lg:pb-8">
      <div className="max-w-[1920px] mx-auto px-6 lg:px-12 py-10">
        <div className="flex flex-col items-center gap-6">
          <VoidLogo size="sm" showText={false} />

          <nav className="flex items-center gap-6 text-xs text-void-gray-muted flex-wrap justify-center" aria-label="Enlaces del pie de página">
            <a href="#" className="hover:text-void-gray-light transition-colors duration-200">Ayuda</a>
            <a href="#" className="hover:text-void-gray-light transition-colors duration-200">Privacidad</a>
            <a href="#" className="hover:text-void-gray-light transition-colors duration-200">Términos</a>
            <a href="#" className="hover:text-void-gray-light transition-colors duration-200">Cookies</a>
            <a href="#" className="hover:text-void-gray-light transition-colors duration-200">Contacto</a>
          </nav>

          <p className="text-xs text-void-gray-muted/50">
            © {new Date().getFullYear()} VOID. Todos los derechos reservados.
          </p>
        </div>
      </div>
    </footer>
  );
}
