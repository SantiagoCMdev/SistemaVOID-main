'use client';

import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, User, Bell, Shield, CreditCard } from 'lucide-react';

interface SettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  user: { name: string; email: string } | null;
}

export default function SettingsModal({ isOpen, onClose, user }: SettingsModalProps) {
  const [activeTab, setActiveTab] = useState('perfil');

  if (!isOpen) return null;

  const initial = user?.name ? user.name.charAt(0).toUpperCase() : 'U';
  const userName = user?.name || 'Usuario';
  const userEmail = user?.email || 'usuario@ejemplo.com';

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="absolute inset-0 bg-void-black/80 backdrop-blur-md"
          onClick={onClose}
        />
        
        <motion.div
          initial={{ opacity: 0, scale: 0.95, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.95, y: 20 }}
          className="relative w-full max-w-4xl h-[80vh] min-h-[500px] bg-void-black/95 border border-white/[0.08] rounded-2xl shadow-2xl overflow-hidden flex flex-col md:flex-row"
        >
          {/* Sidebar */}
          <div className="w-full md:w-64 bg-void-gray-dark border-b md:border-b-0 md:border-r border-white/[0.08] p-4 md:p-6 flex-shrink-0">
            <h2 className="text-xl font-bold text-void-white mb-4 md:mb-6 hidden md:block">Ajustes</h2>
            
            <nav className="flex md:flex-col gap-2 overflow-x-auto pb-2 md:pb-0 hide-scrollbar space-y-0 md:space-y-1">
              <button
                onClick={() => setActiveTab('perfil')}
                className={`w-full flex-shrink-0 md:flex-shrink flex items-center gap-2 md:gap-3 px-3 md:px-4 py-2 md:py-3 rounded-lg text-sm font-medium transition-colors whitespace-nowrap ${
                  activeTab === 'perfil' ? 'bg-void-purple/20 text-void-purple-glow' : 'text-void-gray-light hover:bg-white/[0.04] hover:text-void-white'
                }`}
              >
                <User size={18} />
                Mi Perfil
              </button>
              <button
                onClick={() => setActiveTab('preferencias')}
                className={`w-full flex-shrink-0 md:flex-shrink flex items-center gap-2 md:gap-3 px-3 md:px-4 py-2 md:py-3 rounded-lg text-sm font-medium transition-colors whitespace-nowrap ${
                  activeTab === 'preferencias' ? 'bg-void-purple/20 text-void-purple-glow' : 'text-void-gray-light hover:bg-white/[0.04] hover:text-void-white'
                }`}
              >
                <Bell size={18} />
                Preferencias
              </button>
              <button
                onClick={() => setActiveTab('facturacion')}
                className={`w-full flex-shrink-0 md:flex-shrink flex items-center gap-2 md:gap-3 px-3 md:px-4 py-2 md:py-3 rounded-lg text-sm font-medium transition-colors whitespace-nowrap ${
                  activeTab === 'facturacion' ? 'bg-void-purple/20 text-void-purple-glow' : 'text-void-gray-light hover:bg-white/[0.04] hover:text-void-white'
                }`}
              >
                <CreditCard size={18} />
                Facturación
              </button>
              <button
                onClick={() => setActiveTab('seguridad')}
                className={`w-full flex-shrink-0 md:flex-shrink flex items-center gap-2 md:gap-3 px-3 md:px-4 py-2 md:py-3 rounded-lg text-sm font-medium transition-colors whitespace-nowrap ${
                  activeTab === 'seguridad' ? 'bg-void-purple/20 text-void-purple-glow' : 'text-void-gray-light hover:bg-white/[0.04] hover:text-void-white'
                }`}
              >
                <Shield size={18} />
                Seguridad
              </button>
            </nav>
          </div>

          {/* Content */}
          <div className="flex-1 overflow-y-auto relative p-8">
            <button
              onClick={onClose}
              className="absolute top-6 right-6 z-50 w-8 h-8 rounded-full bg-white/[0.05] hover:bg-white/[0.1] flex items-center justify-center text-void-gray-light transition-colors"
            >
              <X size={18} />
            </button>

            {activeTab === 'perfil' && (
              <div className="max-w-2xl animate-fade-in-up">
                <h3 className="text-2xl font-bold text-void-white mb-6">Mi Perfil</h3>
                <div className="flex items-center gap-6 mb-8">
                  <div className="w-20 h-20 rounded-full bg-gradient-to-br from-void-purple to-[#d946ef] flex items-center justify-center text-2xl font-bold text-white shadow-[0_0_20px_rgba(139,92,246,0.3)]">
                    {initial}
                  </div>
                  <div>
                    <h4 className="text-lg font-semibold text-void-white">{userName}</h4>
                    <p className="text-void-gray-muted text-sm">{userEmail}</p>
                    <button className="mt-2 text-sm font-medium text-void-purple-glow hover:underline">
                      Cambiar avatar
                    </button>
                  </div>
                </div>

                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-void-gray-light mb-1.5">Nombre</label>
                    <input type="text" defaultValue={userName} className="w-full bg-white/[0.03] border border-white/[0.1] rounded-lg py-2.5 px-4 text-void-white focus:border-void-purple outline-none transition-colors max-w-md" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-void-gray-light mb-1.5">Idioma</label>
                    <select className="w-full bg-white/[0.03] border border-white/[0.1] rounded-lg py-2.5 px-4 text-void-white focus:border-void-purple outline-none transition-colors max-w-md appearance-none">
                      <option>Español (Latinoamérica)</option>
                      <option>English</option>
                    </select>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'preferencias' && (
              <div className="max-w-2xl animate-fade-in-up">
                <h3 className="text-2xl font-bold text-void-white mb-6">Preferencias de Reproducción</h3>
                
                <div className="space-y-6">
                  <div className="flex items-center justify-between p-4 bg-white/[0.02] border border-white/[0.05] rounded-lg">
                    <div>
                      <h4 className="font-medium text-void-white">Reproducción automática</h4>
                      <p className="text-sm text-void-gray-muted mt-1">Reproducir el siguiente episodio automáticamente.</p>
                    </div>
                    <label className="relative inline-flex items-center cursor-pointer">
                      <input type="checkbox" defaultChecked className="sr-only peer" />
                      <div className="w-11 h-6 bg-void-gray-dark peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-void-purple"></div>
                    </label>
                  </div>

                  <div className="flex items-center justify-between p-4 bg-white/[0.02] border border-white/[0.05] rounded-lg">
                    <div>
                      <h4 className="font-medium text-void-white">Calidad de video preferida</h4>
                      <p className="text-sm text-void-gray-muted mt-1">Usar 4K HDR siempre que esté disponible.</p>
                    </div>
                    <label className="relative inline-flex items-center cursor-pointer">
                      <input type="checkbox" defaultChecked className="sr-only peer" />
                      <div className="w-11 h-6 bg-void-gray-dark peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-void-purple"></div>
                    </label>
                  </div>
                </div>
              </div>
            )}

            {(activeTab === 'facturacion' || activeTab === 'seguridad') && (
              <div className="flex flex-col items-center justify-center h-full animate-fade-in-up text-center">
                <div className="w-16 h-16 rounded-full bg-void-purple/10 flex items-center justify-center mb-4">
                  {activeTab === 'facturacion' ? <CreditCard size={32} className="text-void-purple-glow" /> : <Shield size={32} className="text-void-purple-glow" />}
                </div>
                <h3 className="text-xl font-bold text-void-white mb-2">Próximamente</h3>
                <p className="text-void-gray-muted max-w-sm">Esta sección de {activeTab} estará disponible en la próxima actualización.</p>
              </div>
            )}
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
}
