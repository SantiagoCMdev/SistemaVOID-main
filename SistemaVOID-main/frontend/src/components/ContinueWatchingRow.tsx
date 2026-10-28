'use client';

import { Play } from 'lucide-react';
import { ContinueWatchingItem } from '@/types';

interface ContinueWatchingRowProps {
  items: ContinueWatchingItem[];
}

export default function ContinueWatchingRow({ items }: ContinueWatchingRowProps) {
  if (items.length === 0) return null;

  return (
    <section className="py-4 lg:py-6">
      <div className="px-6 lg:px-12 mb-4">
        <h2 className="text-lg lg:text-xl font-semibold text-void-white">
          Continuar viendo
        </h2>
      </div>

      <div className="flex gap-3 lg:gap-4 overflow-x-auto scrollbar-hide px-6 lg:px-12 pb-2">
        {items.map((item) => (
          <div
            key={item.content.id}
            className="relative flex-shrink-0 group cursor-pointer"
            style={{ width: '260px' }}
          >
            {/* Thumbnail (landscape) */}
            <div className="relative aspect-video rounded-lg overflow-hidden transition-all duration-300 ease-out group-hover:scale-[1.03] group-hover:shadow-lg group-hover:shadow-void-purple/10">
              <img
                src={item.content.backdropUrl}
                alt={item.content.title}
                className="w-full h-full object-cover"
                loading="lazy"
              />

              {/* Hover: play button */}
              <div className="absolute inset-0 bg-void-black/40 opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex items-center justify-center">
                <div className="w-12 h-12 rounded-full bg-void-purple/90 flex items-center justify-center shadow-lg shadow-void-purple/30">
                  <Play size={20} fill="white" className="ml-0.5" />
                </div>
              </div>

              {/* Progress bar */}
              <div className="absolute bottom-0 left-0 right-0 h-1 bg-void-gray-surface">
                <div
                  className="h-full bg-void-purple rounded-r-full transition-all duration-300"
                  style={{ width: `${item.progress}%` }}
                />
              </div>
            </div>

            {/* Info */}
            <div className="mt-2.5 px-0.5 flex items-center justify-between">
              <div className="min-w-0">
                <h3 className="text-sm font-medium text-void-white truncate">{item.content.title}</h3>
                <p className="text-xs text-void-gray-muted mt-0.5">
                  {item.remainingTime} restantes
                </p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
