'use client';

import { useEffect, useRef, useMemo } from 'react';

interface VoidLogoProps {
  showText?: boolean;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
  eyeState?: 'normal' | 'closed' | 'squint';
}

const SIZES = {
  sm: { eye: 14, gap: 6, text: 'text-lg' },
  md: { eye: 18, gap: 8, text: 'text-xl' },
  lg: { eye: 28, gap: 12, text: 'text-3xl' },
  xl: { eye: 44, gap: 18, text: 'text-5xl' },
} as const;

function Eye({ pupilRef, eyeSize, pupilSize, state }: {
  pupilRef: React.RefObject<HTMLDivElement | null>;
  eyeSize: number;
  pupilSize: number;
  state: 'normal' | 'closed' | 'squint';
}) {
  let heightMultiplier = 0.55;
  if (state === 'closed') heightMultiplier = 0.1; // Almost closed (a line)
  if (state === 'squint') heightMultiplier = 0.25; // Squinting

  return (
    <div
      className="relative flex items-center justify-center rounded-[50%] overflow-hidden transition-all duration-300 ease-in-out"
      style={{
        width: eyeSize,
        height: eyeSize * heightMultiplier,
        background: state === 'closed' ? '#5a5575' : 'radial-gradient(ellipse, #F5F3FF 0%, #d4d0e8 100%)',
      }}
    >
      <div
        ref={pupilRef}
        className="rounded-full bg-void-black shadow-[0_0_4px_rgba(124,58,237,0.5)] transition-opacity duration-300"
        style={{ width: pupilSize, height: pupilSize, opacity: state === 'closed' ? 0 : 1 }}
      />
    </div>
  );
}

export default function VoidLogo({ showText = true, size = 'md', className = '', eyeState = 'normal' }: VoidLogoProps) {
  const config = SIZES[size];
  const leftPupilRef = useRef<HTMLDivElement>(null);
  const rightPupilRef = useRef<HTMLDivElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const pupilSize = useMemo(() => Math.round(config.eye * 0.38), [config.eye]);

  const maxOffset = config.eye * 0.15;

  useEffect(() => {
    let rafId: number;
    let targetX = 0;
    let targetY = 0;
    let currentX = 0;
    let currentY = 0;

    const handleMouseMove = (e: MouseEvent) => {
      const container = containerRef.current;
      if (!container) return;
      const rect = container.getBoundingClientRect();
      const dx = e.clientX - (rect.left + rect.width / 2);
      const dy = e.clientY - (rect.top + rect.height / 2);
      const dist = Math.hypot(dx, dy) || 1;
      const scale = Math.min(dist / 400, 1);
      targetX = (dx / dist) * maxOffset * scale;
      targetY = (dy / dist) * maxOffset * scale;
    };

    const animate = () => {
      currentX += (targetX - currentX) * 0.06;
      currentY += (targetY - currentY) * 0.06;
      const t = `translate(${currentX}px, ${currentY}px)`;
      if (leftPupilRef.current) leftPupilRef.current.style.transform = t;
      if (rightPupilRef.current) rightPupilRef.current.style.transform = t;
      rafId = requestAnimationFrame(animate);
    };

    window.addEventListener('mousemove', handleMouseMove, { passive: true });
    rafId = requestAnimationFrame(animate);
    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      cancelAnimationFrame(rafId);
    };
  }, [maxOffset]);

  return (
    <div ref={containerRef} className={`flex items-center gap-2.5 ${className}`}>
      <div className="flex items-center" style={{ gap: config.gap }}>
        <Eye pupilRef={leftPupilRef} eyeSize={config.eye} pupilSize={pupilSize} state={eyeState} />
        <Eye pupilRef={rightPupilRef} eyeSize={config.eye} pupilSize={pupilSize} state={eyeState} />
      </div>
      {showText && (
        <span className={`${config.text} font-bold tracking-[0.18em] text-void-white`}>
          VO<span className="text-void-purple">ID</span>
        </span>
      )}
    </div>
  );
}
