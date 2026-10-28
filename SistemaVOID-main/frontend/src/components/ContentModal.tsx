'use client';

import { X, Play, Plus, Heart, Share2 } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { useEffect } from 'react';
import { Movie } from '@/types';

interface ContentModalProps {
  movie: Movie | null;
  onClose: () => void;
}

const overlayVariants = {
  hidden: { opacity: 0 },
  visible: { opacity: 1 },
};

const modalVariants = {
  hidden: { opacity: 0, y: 40, scale: 0.97 },
  visible: { opacity: 1, y: 0, scale: 1 },
};

function IconButton({ label, children, onClick }: {
  label: string;
  children: React.ReactNode;
  onClick?: () => void;
}) {
  return (
    <button
      className="w-10 h-10 rounded-full bg-void-white/10 hover:bg-void-white/20 border border-white/15 flex items-center justify-center text-white transition-colors duration-150"
      aria-label={label}
      onClick={onClick}
    >
      {children}
    </button>
  );
}

export default function ContentModal({ movie, onClose }: ContentModalProps) {
  // Lock body scroll when modal is open
  useEffect(() => {
    if (movie) {
      document.body.style.overflow = 'hidden';
      return () => { document.body.style.overflow = ''; };
    }
  }, [movie]);

  // Close on Escape
  useEffect(() => {
    if (!movie) return;
    const handleKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handleKey);
    return () => window.removeEventListener('keydown', handleKey);
  }, [movie, onClose]);

  return (
    <AnimatePresence>
      {movie && (
        <motion.div
          variants={overlayVariants}
          initial="hidden"
          animate="visible"
          exit="hidden"
          transition={{ duration: 0.2 }}
          className="fixed inset-0 z-[60] flex items-start justify-center overflow-y-auto py-10 px-4"
          onClick={onClose}
        >
          <div className="fixed inset-0 bg-void-black/85 backdrop-blur-sm" />

          <motion.div
            variants={modalVariants}
            initial="hidden"
            animate="visible"
            exit="hidden"
            transition={{ duration: 0.3, ease: 'easeOut' }}
            className="relative z-10 w-full max-w-3xl rounded-xl overflow-hidden bg-void-gray-dark shadow-2xl shadow-black/60"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Backdrop Image */}
            <div className="relative aspect-video">
              <img
                src={movie.backdropUrl}
                alt=""
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-void-gray-dark via-void-gray-dark/40 to-transparent" />

              <button
                onClick={onClose}
                className="absolute top-4 right-4 w-9 h-9 rounded-full bg-void-black/60 hover:bg-void-black/80 border border-white/10 flex items-center justify-center text-white/70 hover:text-white transition-all duration-150"
                aria-label="Cerrar"
              >
                <X size={18} />
              </button>

              <div className="absolute bottom-6 left-6 right-6">
                <h2 className="text-3xl lg:text-4xl font-bold text-void-white mb-4">
                  {movie.title}
                </h2>
                <div className="flex items-center gap-3 flex-wrap">
                  <button className="flex items-center gap-2 bg-void-purple hover:bg-void-purple-glow text-white font-semibold text-sm px-6 py-2.5 rounded-lg transition-all duration-200 hover:scale-[1.02]">
                    <Play size={16} fill="currentColor" />
                    Reproducir
                  </button>
                  <IconButton label="Mi lista"><Plus size={18} /></IconButton>
                  <IconButton label="Me gusta"><Heart size={18} /></IconButton>
                  <IconButton label="Compartir"><Share2 size={18} /></IconButton>
                </div>
              </div>
            </div>

            {/* Info */}
            <div className="p-6 lg:p-8">
              <div className="flex items-center gap-2.5 text-sm text-void-gray-muted mb-4 flex-wrap">
                <span className="text-void-white font-medium">{movie.year}</span>
                <span className="w-1 h-1 rounded-full bg-void-gray-muted" />
                <span className="border border-void-gray-muted/50 rounded px-1.5 py-0.5 text-xs">{movie.rating}</span>
                <span className="w-1 h-1 rounded-full bg-void-gray-muted" />
                <span>{movie.duration}</span>
              </div>

              <p className="text-sm text-void-gray-light leading-relaxed mb-6 line-clamp-4">
                {movie.description}
              </p>

              <dl className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                <div>
                  <dt className="inline text-void-gray-muted">Géneros: </dt>
                  <dd className="inline text-void-gray-light">{movie.genres.join(', ')}</dd>
                </div>
                <div>
                  <dt className="inline text-void-gray-muted">Director: </dt>
                  <dd className="inline text-void-gray-light">{movie.director}</dd>
                </div>
                {movie.cast.length > 0 && (
                  <div className="md:col-span-2">
                    <dt className="inline text-void-gray-muted">Reparto: </dt>
                    <dd className="inline text-void-gray-light">{movie.cast.join(', ')}</dd>
                  </div>
                )}
              </dl>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
