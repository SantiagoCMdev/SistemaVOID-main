// ============================================================
// VOID Streaming Platform — Type Definitions
// ============================================================

export interface Movie {
  id: string;
  title: string;
  slug: string;
  description: string;
  year: number;
  rating: string; // "16+", "18+", "PG-13", etc.
  duration: string; // "2h 08m"
  durationSeconds: number;
  genres: string[];
  director: string;
  cast: string[];
  posterUrl: string;
  backdropUrl: string;
  videoUrl?: string;
  type: 'movie' | 'series';
  badge?: 'nuevo' | 'estreno' | 'exclusivo';
  match?: number; // percentage match for recommendations
}

export interface Series extends Movie {
  seasons: Season[];
}

export interface Season {
  number: number;
  episodes: Episode[];
}

export interface Episode {
  id: string;
  number: number;
  title: string;
  description: string;
  duration: string;
  durationSeconds: number;
  thumbnailUrl: string;
  progress?: number; // 0-100
}

export interface ContinueWatchingItem {
  content: Movie;
  progress: number; // 0-100
  remainingTime: string;
}

export interface ContentSection {
  id: string;
  title: string;
  subtitle?: string;
  items: Movie[];
}
