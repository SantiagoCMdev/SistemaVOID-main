'use client';

import { useState } from 'react';
import Navbar from '@/components/Navbar';
import HeroCarousel from '@/components/HeroCarousel';
import ContinueWatchingRow from '@/components/ContinueWatchingRow';
import ContentRow from '@/components/ContentRow';
import Top10Row from '@/components/Top10Row';
import ContentModal from '@/components/ContentModal';
import Footer from '@/components/Footer';
import BottomNavigation from '@/components/BottomNavigation';
import { Movie } from '@/types';

import {
  FEATURED_CONTENT,
  CONTINUE_WATCHING,
  TOP_10,
  VOID_ORIGINALS,
  RECOMMENDED,
  NEW_RELEASES,
} from '@/data/mock';

export default function HomePage() {
  const [selectedMovie, setSelectedMovie] = useState<Movie | null>(null);

  return (
    <>
      <Navbar />

      <main className="min-h-screen">
        {/* Hero */}
        <HeroCarousel items={FEATURED_CONTENT} />

        {/* Content Sections */}
        <div className="-mt-16 relative z-10 space-y-2">
          {/* Continue Watching */}
          <ContinueWatchingRow items={CONTINUE_WATCHING} />

          {/* Top 10 */}
          <Top10Row
            items={TOP_10}
            region="Colombia"
            onSelect={setSelectedMovie}
          />

          {/* VOID Originals */}
          <ContentRow
            title="VOID Originals"
            subtitle="Contenido exclusivo de VOID"
            items={VOID_ORIGINALS}
            onSelect={setSelectedMovie}
          />

          {/* Recommended */}
          <ContentRow
            title="Recomendado para ti"
            items={RECOMMENDED}
            onSelect={setSelectedMovie}
          />

          {/* New Releases */}
          <ContentRow
            title="Recién llegados"
            items={NEW_RELEASES}
            onSelect={setSelectedMovie}
          />

          {/* Trending */}
          <ContentRow
            title="Tendencias"
            items={[...TOP_10].reverse().slice(0, 8)}
            onSelect={setSelectedMovie}
          />

          {/* Suspense & Thriller */}
          <ContentRow
            title="Suspenso que atrapa"
            items={[...VOID_ORIGINALS, ...TOP_10].filter(m => m.genres.some(g => ['Thriller', 'Suspenso', 'Misterio'].includes(g))).slice(0, 8)}
            onSelect={setSelectedMovie}
          />

          {/* Sci-Fi */}
          <ContentRow
            title="Ciencia ficción"
            items={[...FEATURED_CONTENT, ...TOP_10, ...VOID_ORIGINALS].filter(m => m.genres.includes('Ciencia ficción')).slice(0, 8)}
            onSelect={setSelectedMovie}
          />
        </div>

        <Footer />
      </main>

      {/* Bottom Nav (mobile) */}
      <BottomNavigation />

      {/* Content Detail Modal */}
      <ContentModal movie={selectedMovie} onClose={() => setSelectedMovie(null)} />
    </>
  );
}
