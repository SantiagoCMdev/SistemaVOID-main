'use client';

import { useState, useEffect, useCallback } from 'react';
import { Play, Plus, ChevronLeft, ChevronRight } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { Movie } from '@/types';

interface HeroCarouselProps {
  items: Movie[];
}

const BADGE_LABELS: Record<string, string> = {
  exclusivo: 'VOID Original',
  estreno: 'Estreno',
  nuevo: 'Nuevo',
};

const bgVariants = {
  enter: { opacity: 0, scale: 1.05 },
  center: { opacity: 1, scale: 1 },
  exit: { opacity: 0 },
};

const contentVariants = {
  enter: { opacity: 0, y: 30 },
  center: { opacity: 1, y: 0 },
  exit: { opacity: 0, y: -20 },
};

export default function HeroCarousel({ items }: HeroCarouselProps) {
  const [current, setCurrent] = useState(0);
  const [isPaused, setIsPaused] = useState(false);

  const next = useCallback(() => {
    setCurrent((c) => (c + 1) % items.length);
  }, [items.length]);

  const prev = useCallback(() => {
    setCurrent((c) => (c - 1 + items.length) % items.length);
  }, [items.length]);

  useEffect(() => {
    if (isPaused) return;
    const timer = setInterval(next, 10000);
    return () => clearInterval(timer);
  }, [isPaused, next]);

  const movie = items[current];
  if (!movie) return null;

  return (
    <section
      className="relative w-full h-[75vh] lg:h-[85vh] overflow-hidden group"
      onMouseEnter={() => setIsPaused(true)}
      onMouseLeave={() => setIsPaused(false)}
      aria-label="Contenido destacado"
    >
      {/* Background */}
      <AnimatePresence mode="wait">
        <motion.div
          key={movie.id}
          variants={bgVariants}
          initial="enter"
          animate="center"
          exit="exit"
          transition={{ duration: 1.2, ease: 'easeOut' }}
          className="absolute inset-0"
        >
          <div
            className="absolute inset-0 bg-cover bg-center bg-no-repeat"
            style={{ backgroundImage: `url(${movie.backdropUrl})` }}
          />
          <div className="absolute inset-0 bg-gradient-to-r from-void-black via-void-black/60 to-transparent" />
          <div className="absolute inset-0 bg-gradient-to-t from-void-black via-transparent to-void-black/30" />
          <div className="absolute bottom-0 left-0 right-0 h-40 bg-gradient-to-t from-void-black to-transparent" />
        </motion.div>
      </AnimatePresence>

      {/* Content */}
      <div className="relative z-10 h-full flex items-end lg:items-center pb-24 lg:pb-0 px-6 lg:px-12 max-w-[1920px] mx-auto">
        <AnimatePresence mode="wait">
          <motion.div
            key={`${movie.id}-content`}
            variants={contentVariants}
            initial="enter"
            animate="center"
            exit="exit"
            transition={{ duration: 0.6, ease: 'easeOut', delay: 0.2 }}
            className="max-w-xl lg:max-w-2xl"
          >
            {movie.badge && (
              <span className="inline-block text-xs font-semibold tracking-wider uppercase text-void-purple-glow mb-4 border border-void-purple/30 rounded px-2.5 py-1 bg-void-purple/10">
                {BADGE_LABELS[movie.badge] ?? movie.badge}
              </span>
            )}

            <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold text-void-white leading-[1.1] tracking-tight mb-4">
              {movie.title}
            </h1>

            <p className="text-sm md:text-base text-void-gray-light leading-relaxed mb-5 line-clamp-3">
              {movie.description}
            </p>

            <div className="flex items-center gap-2.5 text-xs md:text-sm text-void-gray-muted mb-7 flex-wrap">
              <span className="text-void-white font-medium">{movie.year}</span>
              <span className="w-1 h-1 rounded-full bg-void-gray-muted" />
              <span className="border border-void-gray-muted/50 rounded px-1.5 py-0.5 text-xs">{movie.rating}</span>
              <span className="w-1 h-1 rounded-full bg-void-gray-muted" />
              <span>{movie.duration}</span>
              <span className="w-1 h-1 rounded-full bg-void-gray-muted" />
              <span>{movie.genres.join(' · ')}</span>
            </div>

            <div className="flex items-center gap-3">
              <button className="flex items-center gap-2.5 bg-void-purple hover:bg-void-purple-glow text-white font-semibold text-sm px-7 py-3 rounded-lg transition-all duration-200 hover:scale-[1.02] active:scale-[0.98]">
                <Play size={18} fill="currentColor" />
                Reproducir
              </button>
              <button className="flex items-center gap-2 bg-void-white/10 hover:bg-void-white/15 text-void-white font-medium text-sm px-5 py-3 rounded-lg transition-all duration-200 border border-white/10 hover:border-white/20">
                <Plus size={18} strokeWidth={1.5} />
                Mi lista
              </button>
            </div>
          </motion.div>
        </AnimatePresence>
      </div>

      {/* Navigation — visible on hover via CSS group */}
      {items.length > 1 && (
        <>
          <button
            onClick={prev}
            className="absolute left-4 lg:left-6 top-1/2 -translate-y-1/2 z-20 w-10 h-10 rounded-full bg-void-black/40 hover:bg-void-black/70 border border-white/10 flex items-center justify-center text-white/60 hover:text-white transition-all duration-200 opacity-0 group-hover:opacity-100"
            aria-label="Anterior"
          >
            <ChevronLeft size={20} />
          </button>
          <button
            onClick={next}
            className="absolute right-4 lg:right-6 top-1/2 -translate-y-1/2 z-20 w-10 h-10 rounded-full bg-void-black/40 hover:bg-void-black/70 border border-white/10 flex items-center justify-center text-white/60 hover:text-white transition-all duration-200 opacity-0 group-hover:opacity-100"
            aria-label="Siguiente"
          >
            <ChevronRight size={20} />
          </button>

          <div className="absolute bottom-8 left-1/2 -translate-x-1/2 z-20 flex items-center gap-2">
            {items.map((_, i) => (
              <button
                key={i}
                onClick={() => setCurrent(i)}
                className={`transition-all duration-300 rounded-full ${
                  i === current
                    ? 'w-6 h-1.5 bg-void-purple'
                    : 'w-1.5 h-1.5 bg-white/25 hover:bg-white/40'
                }`}
                aria-label={`Ir al slide ${i + 1}`}
              />
            ))}
          </div>
        </>
      )}
    </section>
  );
}
