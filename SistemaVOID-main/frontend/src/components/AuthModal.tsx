'use client';

import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Mail, Lock, User, Loader2 } from 'lucide-react';
import { authService } from '@/services/auth.service';

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  onLogin: (user: any) => void;
}

export default function AuthModal({ isOpen, onClose, onLogin }: AuthModalProps) {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      const authRes = isLogin
        ? await authService.login(email, password)
        : await authService.register(email, password, fullName);

      onLogin(authRes.data.user);
      onClose();
    } catch (err: any) {
      setError(err.message || 'Error de conexión');
    } finally {
      setIsLoading(false);
    }
  };

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
          className="relative w-full max-w-md bg-void-black/95 border border-white/[0.08] rounded-2xl shadow-2xl overflow-hidden"
        >
          {/* Header */}
          <div className="flex items-center justify-between px-6 py-4 border-b border-white/[0.08]">
            <h2 className="text-xl font-bold text-void-white">
              {isLogin ? 'Iniciar Sesión' : 'Crear Cuenta'}
            </h2>
            <button
              onClick={onClose}
              className="w-8 h-8 rounded-full bg-white/[0.05] hover:bg-white/[0.1] flex items-center justify-center text-void-gray-light transition-colors"
            >
              <X size={18} />
            </button>
          </div>

          {/* Form */}
          <div className="p-6">
            <div className="space-y-4">
              {error && (
                <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-3 rounded-lg text-sm">
                  {error}
                </div>
              )}
              {!isLogin && (
                <div>
                  <label className="block text-sm font-medium text-void-gray-light mb-1.5">Nombre completo</label>
                  <div className="relative">
                    <User size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-void-gray-muted" />
                    <input
                      type="text"
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      className="w-full bg-white/[0.03] border border-white/[0.1] rounded-lg py-2.5 pl-10 pr-4 text-void-white placeholder:text-void-gray-muted focus:outline-none focus:border-void-purple transition-colors"
                      placeholder="Tu nombre"
                    />
                  </div>
                </div>
              )}
              
              <div>
                <label className="block text-sm font-medium text-void-gray-light mb-1.5">Correo electrónico</label>
                <div className="relative">
                  <Mail size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-void-gray-muted" />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full bg-white/[0.03] border border-white/[0.1] rounded-lg py-2.5 pl-10 pr-4 text-void-white placeholder:text-void-gray-muted focus:outline-none focus:border-void-purple transition-colors"
                    placeholder="correo@ejemplo.com"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-void-gray-light mb-1.5">Contraseña</label>
                <div className="relative">
                  <Lock size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-void-gray-muted" />
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="w-full bg-white/[0.03] border border-white/[0.1] rounded-lg py-2.5 pl-10 pr-4 text-void-white placeholder:text-void-gray-muted focus:outline-none focus:border-void-purple transition-colors"
                    placeholder="••••••••"
                  />
                </div>
              </div>

              <button
                onClick={handleSubmit}
                disabled={isLoading}
                className="w-full bg-void-purple hover:bg-void-purple-glow text-white font-semibold py-3 rounded-lg mt-2 transition-colors flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading && <Loader2 size={18} className="animate-spin" />}
                {isLogin ? 'Entrar a VOID' : 'Comenzar ahora'}
              </button>
            </div>

            <div className="mt-6 text-center text-sm text-void-gray-muted">
              {isLogin ? '¿No tienes cuenta?' : '¿Ya tienes una cuenta?'}{' '}
              <button
                onClick={() => {
                  setIsLogin(!isLogin);
                  setError(null);
                }}
                className="text-void-purple-glow hover:underline font-medium"
              >
                {isLogin ? 'Regístrate aquí' : 'Inicia sesión'}
              </button>
            </div>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
}
