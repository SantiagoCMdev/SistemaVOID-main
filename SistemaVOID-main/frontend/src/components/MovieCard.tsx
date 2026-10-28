'use client';

import { Play, Plus, Info } from 'lucide-react';
import { Movie } from '@/types';

interface MovieCardProps {
  movie: Movie;
  onSelect?: (movie: Movie) => void;
}

const BADGE_LABELS: Record<string, string> = {
  exclusivo: 'Original',
  estreno: 'Estreno',
  nuevo: 'Nuevo',
};

export default function MovieCard({ movie, onSelect }: MovieCardProps) {
  return (
    <div
      className="relative flex-shrink-0 w-[180px] group cursor-pointer"
      onClick={() => onSelect?.(movie)}
    >
      {/* Poster */}
      <div className="relative aspect-[2/3] rounded-lg overflow-hidden transition-transform duration-300 ease-out group-hover:scale-[1.04]">
        <img
          src={movie.posterUrl}
          alt={movie.title}
          className="w-full h-full object-cover"
          loading="lazy"
        />

        {/* Badge */}
        {movie.badge && (
          <span className="absolute top-2 left-2 text-[10px] font-semibold tracking-wider uppercase text-void-white bg-void-purple/80 backdrop-blur-sm rounded px-1.5 py-0.5">
            {BADGE_LABELS[movie.badge] ?? movie.badge}
          </span>
        )}

        {/* Hover overlay */}
        <div className="absolute inset-0 bg-gradient-to-t from-void-black/90 via-void-black/30 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-200">
          <div className="absolute bottom-3 left-3 right-3 flex items-center gap-2">
            <button
              className="w-9 h-9 rounded-full bg-void-purple hover:bg-void-purple-glow flex items-center justify-center transition-colors duration-150"
              aria-label={`Reproducir ${movie.title}`}
              onClick={(e) => e.stopPropagation()}
            >
              <Play size={14} fill="white" className="ml-0.5" />
            </button>
            <button
              className="w-9 h-9 rounded-full bg-void-white/10 hover:bg-void-white/20 border border-white/15 flex items-center justify-center transition-colors duration-150"
              aria-label={`Agregar ${movie.title} a mi lista`}
              onClick={(e) => e.stopPropagation()}
            >
              <Plus size={14} className="text-white" />
            </button>
            <button
              className="w-9 h-9 rounded-full bg-void-white/10 hover:bg-void-white/20 border border-white/15 flex items-center justify-center transition-colors duration-150 ml-auto"
              aria-label={`Más información sobre ${movie.title}`}
              onClick={(e) => {
                e.stopPropagation();
                onSelect?.(movie);
              }}
            >
              <Info size={14} className="text-white" />
            </button>
          </div>
        </div>
      </div>

      {/* Info */}
      <div className="mt-2.5 px-0.5">
        <h3 className="text-sm font-medium text-void-white truncate">{movie.title}</h3>
        <div className="flex items-center gap-1.5 text-xs text-void-gray-muted mt-0.5">
          <span>{movie.year}</span>
          <span className="w-0.5 h-0.5 rounded-full bg-void-gray-muted" />
          <span>{movie.genres[0]}</span>
        </div>
      </div>
    </div>
  );
}
