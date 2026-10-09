import React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  glow?: boolean;
  interactive?: boolean;
}

export const Card: React.FC<CardProps> = ({
  children,
  className,
  glow: _glow,
  interactive = false,
  ...props
}) => {
  return (
    <div
      className={twMerge(
        clsx(
          'surface-card rounded-xl p-5 border border-zinc-200 dark:border-zinc-800/80 bg-white dark:bg-zinc-900/90 text-zinc-900 dark:text-zinc-100 shadow-sm transition-all duration-200',
          interactive &&
            'hover:border-zinc-300 dark:hover:border-zinc-700 hover:shadow-md cursor-pointer',
          className
        )
      )}
      {...props}
    >
      {children}
    </div>
  );
};

export const GlassCard = Card;
