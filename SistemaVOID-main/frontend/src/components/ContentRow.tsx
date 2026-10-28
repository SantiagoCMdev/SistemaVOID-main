'use client';

import { useRef, useState } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import MovieCard from './MovieCard';
import { Movie } from '@/types';

interface ContentRowProps {
  title: string;
  subtitle?: string;
  items: Movie[];
  onSelect?: (movie: Movie) => void;
}

export default function ContentRow({ title, subtitle, items, onSelect }: ContentRowProps) {
  const scrollRef = useRef<HTMLDivElement>(null);
  const [showLeft, setShowLeft] = useState(false);
  const [showRight, setShowRight] = useState(true);

  const checkScroll = () => {
    if (!scrollRef.current) return;
    const { scrollLeft, scrollWidth, clientWidth } = scrollRef.current;
    setShowLeft(scrollLeft > 20);
    setShowRight(scrollLeft < scrollWidth - clientWidth - 20);
  };

  const scroll = (direction: 'left' | 'right') => {
    if (!scrollRef.current) return;
    const amount = scrollRef.current.clientWidth * 0.75;
    scrollRef.current.scrollBy({
      left: direction === 'left' ? -amount : amount,
      behavior: 'smooth',
    });
  };

  return (
    <section className="relative py-4 lg:py-6">
      {/* Title */}
      <div className="px-6 lg:px-12 mb-4">
        <h2 className="text-lg lg:text-xl font-semibold text-void-white">
          {title}
        </h2>
        {subtitle && (
          <p className="text-sm text-void-gray-muted mt-0.5">{subtitle}</p>
        )}
      </div>

      {/* Scrollable Row */}
      <div className="relative group/row">
        {/* Left Arrow */}
        {showLeft && (
          <button
            onClick={() => scroll('left')}
            className="absolute left-1 top-0 bottom-12 z-10 w-10 flex items-center justify-center bg-gradient-to-r from-void-black/80 to-transparent opacity-0 group-hover/row:opacity-100 transition-opacity duration-200"
            aria-label="Desplazar izquierda"
          >
            <ChevronLeft size={24} className="text-white/70" />
          </button>
        )}

        {/* Right Arrow */}
        {showRight && (
          <button
            onClick={() => scroll('right')}
            className="absolute right-1 top-0 bottom-12 z-10 w-10 flex items-center justify-center bg-gradient-to-l from-void-black/80 to-transparent opacity-0 group-hover/row:opacity-100 transition-opacity duration-200"
            aria-label="Desplazar derecha"
          >
            <ChevronRight size={24} className="text-white/70" />
          </button>
        )}

        {/* Cards */}
        <div
          ref={scrollRef}
          onScroll={checkScroll}
          className="flex gap-3 lg:gap-4 overflow-x-auto scrollbar-hide px-6 lg:px-12 pb-2"
        >
          {items.map((movie) => (
            <MovieCard key={movie.id} movie={movie} onSelect={onSelect} />
          ))}
        </div>
      </div>
    </section>
  );
}
