import React from 'react';
import { clsx } from 'clsx';
import { Card } from './GlassCard';

export interface KpiCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: React.ReactNode;
  iconColor?: string;
  iconBg?: string;
  trend?: {
    value: string;
    isPositive?: boolean;
    label?: string;
  };
  onClick?: () => void;
  className?: string;
}

export const KpiCard: React.FC<KpiCardProps> = ({
  title,
  value,
  subtitle,
  icon,
  iconColor = 'text-zinc-700 dark:text-zinc-300',
  iconBg = 'bg-zinc-100 dark:bg-zinc-800',
  trend,
  onClick,
  className,
}) => {
  return (
    <Card
      interactive={!!onClick}
      onClick={onClick}
      className={clsx('flex flex-col justify-between group p-4 sm:p-5', className)}
    >
      <div className="flex items-start justify-between">
        <div className="space-y-1">
          <p className="text-xs font-medium text-zinc-500 dark:text-zinc-400">
            {title}
          </p>
          <div className="text-2xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-50 font-mono">
            {value}
          </div>
        </div>
        <div
          className={clsx(
            'w-9 h-9 rounded-lg flex items-center justify-center transition-colors shrink-0 border border-zinc-200/50 dark:border-zinc-700/50',
            iconBg,
            iconColor
          )}
        >
          {icon}
        </div>
      </div>

      {(subtitle || trend) && (
        <div className="mt-3 pt-3 border-t border-zinc-100 dark:border-zinc-800/80 flex items-center justify-between text-xs">
          {subtitle && (
            <span className="text-zinc-500 dark:text-zinc-400 text-[11px] truncate">
              {subtitle}
            </span>
          )}
          {trend && (
            <span
              className={clsx(
                'inline-flex items-center gap-1 font-medium rounded px-1.5 py-0.5 text-[11px]',
                trend.isPositive
                  ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400'
                  : 'bg-rose-50 text-rose-700 dark:bg-rose-950/40 dark:text-rose-400'
              )}
            >
              {trend.value} {trend.label && <span className="opacity-80 font-normal">{trend.label}</span>}
            </span>
          )}
        </div>
      )}
    </Card>
  );
};

export const StatsCard = KpiCard;
