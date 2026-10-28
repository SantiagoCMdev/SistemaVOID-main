'use client';

import { useState } from 'react';
import { Movie } from '@/types';

interface Top10RowProps {
  items: Movie[];
  region?: string;
  onSelect?: (movie: Movie) => void;
}

const FILTERS = [
  { key: 'all', label: 'Todo' },
  { key: 'movie', label: 'Películas' },
  { key: 'series', label: 'Series' },
] as const;

type FilterKey = typeof FILTERS[number]['key'];

export default function Top10Row({ items, region = 'tu región', onSelect }: Top10RowProps) {
  const [filter, setFilter] = useState<FilterKey>('all');

  const filtered = (filter === 'all'
    ? items
    : items.filter(m => m.type === filter)
  ).slice(0, 10);

  return (
    <section className="py-6 lg:py-10">
      <div className="px-6 lg:px-12 mb-5">
        <div className="flex items-center justify-between flex-wrap gap-3">
          <h2 className="text-lg lg:text-xl font-semibold text-void-white">
            Top 10 en {region}
          </h2>
          <div className="flex items-center gap-1 bg-void-gray-dark rounded-lg p-1" role="tablist">
            {FILTERS.map(({ key, label }) => (
              <button
                key={key}
                role="tab"
                aria-selected={filter === key}
                onClick={() => setFilter(key)}
                className={`text-xs font-medium px-3 py-1.5 rounded-md transition-all duration-200 ${
                  filter === key
                    ? 'bg-void-purple text-white'
                    : 'text-void-gray-light hover:text-void-white'
                }`}
              >
                {label}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="flex gap-4 lg:gap-5 overflow-x-auto scrollbar-hide px-6 lg:px-12 pb-2">
        {filtered.map((movie, i) => (
          <div
            key={movie.id}
            className="relative flex-shrink-0 w-[160px] cursor-pointer group"
            onClick={() => onSelect?.(movie)}
          >
            {/* Ranking Number */}
            <div className="absolute -left-5 lg:-left-7 bottom-12 z-0 select-none pointer-events-none">
              <span
                className="font-bold leading-none text-[120px]"
                style={{
                  WebkitTextStroke: '1.5px rgba(124, 58, 237, 0.2)',
                  color: 'transparent',
                }}
              >
                {String(i + 1).padStart(2, '0')}
              </span>
            </div>

            <div className="relative z-10 aspect-[2/3] rounded-lg overflow-hidden transition-transform duration-300 ease-out group-hover:scale-[1.04]">
              <img
                src={movie.posterUrl}
                alt={`#${i + 1} ${movie.title}`}
                className="w-full h-full object-cover"
                loading="lazy"
              />
            </div>

            <div className="mt-2 px-0.5 relative z-10">
              <h3 className="text-sm font-medium text-void-white truncate">{movie.title}</h3>
              <p className="text-xs text-void-gray-muted mt-0.5">{movie.genres[0]}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
