import React from 'react';

interface LibraXLogoProps {
  className?: string;
  size?: number;
  showText?: boolean;
}

export const LibraXLogo: React.FC<LibraXLogoProps> = ({
  className = '',
  size = 32,
  showText = false,
}) => {
  return (
    <div className={`inline-flex items-center gap-2.5 select-none ${className}`}>
      {/* Cool Geometric Academic Book Logo */}
      <div
        className="relative shrink-0 flex items-center justify-center rounded-xl bg-gradient-to-br from-zinc-900 via-zinc-800 to-zinc-950 dark:from-zinc-100 dark:via-zinc-200 dark:to-zinc-50 text-white dark:text-zinc-900 shadow-md border border-zinc-700/40 dark:border-zinc-300/60 transition-transform duration-200 hover:scale-105"
        style={{ width: size, height: size }}
      >
        <svg
          viewBox="0 0 32 32"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          className="w-[62%] h-[62%]"
        >
          {/* Left Book Page */}
          <path
            d="M6 8.5C9.5 7.5 13 8.2 15 10V24.5C13 23 9.5 22.5 6 23.5V8.5Z"
            fill="currentColor"
            fillOpacity="0.9"
          />
          {/* Right Book Page */}
          <path
            d="M26 8.5C22.5 7.5 19 8.2 17 10V24.5C19 23 22.5 22.5 26 23.5V8.5Z"
            fill="currentColor"
            fillOpacity="0.75"
          />
          {/* Center Spine Beam */}
          <path
            d="M16 9.8V25"
            stroke="currentColor"
            strokeWidth="1.2"
            strokeLinecap="round"
          />
          {/* Intellect Star / Spark */}
          <path
            d="M16 3.5L16.8 5.6L19 6.2L17.2 7.7L17.8 9.8L16 8.6L14.2 9.8L14.8 7.7L13 6.2L15.2 5.6L16 3.5Z"
            fill="#F59E0B"
          />
        </svg>
      </div>

      {showText && (
        <div className="flex flex-col">
          <span className="font-bold text-sm tracking-tight text-zinc-900 dark:text-zinc-100">
            LibraX
          </span>
          <span className="text-[10px] text-zinc-500 dark:text-zinc-400 font-medium tracking-wider uppercase">
            Library System
          </span>
        </div>
      )}
    </div>
  );
};
