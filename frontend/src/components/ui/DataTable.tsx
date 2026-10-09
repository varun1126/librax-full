import React from 'react';
import { ChevronLeft, ChevronRight, Inbox } from 'lucide-react';
import { clsx } from 'clsx';
import { Button } from './Button';

export interface Column<T> {
  header: string;
  accessorKey?: keyof T;
  cell?: (item: T) => React.ReactNode;
  className?: string;
}

export interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  isLoading?: boolean;
  emptyMessage?: string;
  page?: number;
  pageSize?: number;
  total?: number;
  onPageChange?: (newPage: number) => void;
  keyExtractor?: (item: T) => string | number;
}

export function DataTable<T>({
  columns,
  data,
  isLoading = false,
  emptyMessage = 'No records found',
  page = 1,
  pageSize = 10,
  total,
  onPageChange,
  keyExtractor,
}: DataTableProps<T>) {
  const totalPages = total ? Math.ceil(total / pageSize) : 1;

  return (
    <div className="w-full flex flex-col rounded-xl border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900/90 shadow-xs overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse text-xs sm:text-sm">
          <thead>
            <tr className="border-b border-zinc-200 dark:border-zinc-800 bg-zinc-50/70 dark:bg-zinc-900">
              {columns.map((col, idx) => (
                <th
                  key={idx}
                  className={clsx(
                    'py-3 px-4 font-semibold text-zinc-600 dark:text-zinc-400 uppercase tracking-wider text-[11px]',
                    col.className
                  )}
                >
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800/80">
            {isLoading ? (
              Array.from({ length: 5 }).map((_, rIdx) => (
                <tr key={rIdx} className="animate-pulse">
                  {columns.map((_, cIdx) => (
                    <td key={cIdx} className="py-3.5 px-4">
                      <div className="h-4 bg-zinc-200/60 dark:bg-zinc-800/60 rounded w-3/4" />
                    </td>
                  ))}
                </tr>
              ))
            ) : data.length === 0 ? (
              <tr>
                <td colSpan={columns.length} className="py-12 text-center">
                  <div className="flex flex-col items-center justify-center space-y-2 text-zinc-400 dark:text-zinc-500">
                    <Inbox className="w-8 h-8 stroke-1" />
                    <p className="text-sm font-medium">{emptyMessage}</p>
                  </div>
                </td>
              </tr>
            ) : (
              data.map((item, rowIdx) => {
                const rowKey = keyExtractor ? keyExtractor(item) : (item as any)?.id || rowIdx;
                return (
                  <tr
                    key={rowKey}
                    className="hover:bg-zinc-50/80 dark:hover:bg-zinc-800/40 transition-colors group"
                  >
                    {columns.map((col, colIdx) => (
                      <td
                        key={colIdx}
                        className={clsx(
                          'py-3 px-4 text-zinc-800 dark:text-zinc-200 align-middle',
                          col.className
                        )}
                      >
                        {col.cell
                          ? col.cell(item)
                          : col.accessorKey
                          ? String((item as any)[col.accessorKey] ?? '—')
                          : null}
                      </td>
                    ))}
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination Footer */}
      {total !== undefined && totalPages > 1 && onPageChange && (
        <div className="flex items-center justify-between px-4 py-3 border-t border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900">
          <span className="text-xs text-zinc-500 dark:text-zinc-400">
            Showing <span className="font-medium text-zinc-900 dark:text-zinc-100">{(page - 1) * pageSize + 1}</span> to{' '}
            <span className="font-medium text-zinc-900 dark:text-zinc-100">
              {Math.min(page * pageSize, total)}
            </span>{' '}
            of <span className="font-medium text-zinc-900 dark:text-zinc-100">{total}</span> items
          </span>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={page <= 1 || isLoading}
              onClick={() => onPageChange(page - 1)}
              leftIcon={<ChevronLeft className="w-3.5 h-3.5" />}
            >
              Prev
            </Button>
            <span className="text-xs font-medium px-2 text-zinc-700 dark:text-zinc-300">
              {page} / {totalPages}
            </span>
            <Button
              variant="outline"
              size="sm"
              disabled={page >= totalPages || isLoading}
              onClick={() => onPageChange(page + 1)}
              rightIcon={<ChevronRight className="w-3.5 h-3.5" />}
            >
              Next
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
