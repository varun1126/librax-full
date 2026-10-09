import React, { useState, useEffect, useCallback } from 'react';
import { api } from '../../services/api';
import type { Fine } from '../../types';
import { Badge } from '../../components/ui/Badge';
import { DataTable, Column } from '../../components/ui/DataTable';

export const StudentFinesPage: React.FC = () => {
  const [fines, setFines] = useState<Fine[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const fetchMyFines = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await api.fines.getMyFines();
      setFines(data || []);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchMyFines();
  }, [fetchMyFines]);

  const totalOutstanding = (Array.isArray(fines) ? fines : [])
    .filter((f) => f.status === 'PENDING')
    .reduce((acc, f) => acc + f.amount, 0);

  const columns: Column<Fine>[] = [
    {
      header: 'Fee Assessment',
      cell: (f) => (
        <div className="space-y-0.5">
          <div className="font-bold text-sm text-[var(--text-primary)]">
            {f.bookTitle ? `Overdue: ${f.bookTitle}` : `Penalty Fee`}
          </div>
          <div className="text-xs text-[var(--text-secondary)]">{f.reason || 'Delinquent return penalty'}</div>
          <div className="text-[10px] text-[var(--text-muted)] font-mono">Fine ID: #{f.id}</div>
        </div>
      ),
    },
    {
      header: 'Amount',
      cell: (f) => (
        <span className="font-mono text-sm font-bold text-rose-400">
          ₹{f.amount.toFixed(2)}
        </span>
      ),
    },
    {
      header: 'Assessment Date',
      cell: (f) => <span className="text-xs font-mono text-[var(--text-secondary)]">{f.issueDate}</span>,
    },
    {
      header: 'Settlement Date',
      cell: (f) => (
        <span className="text-xs font-mono text-emerald-400">
          {f.paidDate || '—'}
        </span>
      ),
    },
    {
      header: 'Status',
      cell: (f) => (
        <Badge
          variant={f.status === 'PAID' ? 'success' : f.status === 'WAIVED' ? 'purple' : 'danger'}
          size="sm"
          dot
        >
          {f.status}
        </Badge>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-black text-[var(--text-primary)]">My Fines & Dues</h2>
          <p className="text-xs text-[var(--text-secondary)]">
            Outstanding balances for overdue materials and payment receipts
          </p>
        </div>

        <div className="p-3 px-5 rounded-2xl bg-[var(--bg-secondary)] border border-[var(--border-color)] flex items-center gap-3">
          <div className="text-xs text-[var(--text-secondary)] font-medium">Total Balance Due:</div>
          <div className="text-xl font-black font-mono text-rose-400">
            ₹{totalOutstanding.toFixed(2)}
          </div>
        </div>
      </div>

      <DataTable
        columns={columns}
        data={fines}
        isLoading={isLoading}
        emptyMessage="You have no outstanding or past fines on record."
      />
    </div>
  );
};
