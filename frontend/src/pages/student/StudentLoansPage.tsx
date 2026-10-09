import React, { useState, useEffect, useCallback } from 'react';
import { RotateCcw, CheckCircle } from 'lucide-react';
import { api } from '../../services/api';
import type { Loan } from '../../types';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { DataTable, Column } from '../../components/ui/DataTable';

export const StudentLoansPage: React.FC = () => {
  const [loans, setLoans] = useState<Loan[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [renewingId, setRenewingId] = useState<number | null>(null);
  const [returningId, setReturningId] = useState<number | null>(null);

  const fetchMyLoans = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await api.loans.getMyLoans();
      setLoans(data || []);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchMyLoans();
  }, [fetchMyLoans]);

  const handleRenew = async (loanId: number) => {
    setRenewingId(loanId);
    try {
      await api.loans.renew(loanId);
      alert('Loan successfully renewed! Due date extended by 14 days.');
      fetchMyLoans();
    } catch (e: any) {
      alert(e.message || 'Renewal not permitted');
    } finally {
      setRenewingId(null);
    }
  };

  const handleReturn = async (loanId: number) => {
    if (!confirm('Are you sure you want to return this book to the library?')) {
      return;
    }
    setReturningId(loanId);
    try {
      await api.loans.returnBook(loanId);
      alert('Book successfully returned to the library!');
      fetchMyLoans();
    } catch (e: any) {
      alert(e.message || 'Failed to return book');
    } finally {
      setReturningId(null);
    }
  };

  const columns: Column<Loan>[] = [
    {
      header: 'Book Title',
      cell: (l) => (
        <div className="space-y-0.5">
          <div className="font-bold text-sm text-[var(--text-primary)]">{l.bookTitle || `Book #${l.bookId}`}</div>
          <div className="text-xs text-[var(--text-secondary)] font-mono">Copy Barcode: #{l.copyId}</div>
        </div>
      ),
    },
    {
      header: 'Checkout Date',
      cell: (l) => <span className="text-xs font-mono text-[var(--text-secondary)]">{l.issueDate}</span>,
    },
    {
      header: 'Due Date',
      cell: (l) => {
        const isOverdue = l.status === 'OVERDUE' || (l.status === 'ACTIVE' && new Date(l.dueDate) < new Date());
        return (
          <span className={`text-xs font-mono font-bold ${isOverdue ? 'text-rose-400' : 'text-[var(--text-primary)]'}`}>
            {l.dueDate}
          </span>
        );
      },
    },
    {
      header: 'Renewals Used',
      cell: (l) => <span className="text-xs font-mono text-[var(--text-secondary)]">{l.renewalCount}x</span>,
    },
    {
      header: 'Status',
      cell: (l) => {
        const isOverdue = l.status === 'OVERDUE' || (l.status === 'ACTIVE' && new Date(l.dueDate) < new Date());
        return (
          <Badge
            variant={l.status === 'RETURNED' ? 'neutral' : isOverdue ? 'danger' : 'success'}
            size="sm"
            dot
          >
            {isOverdue && l.status === 'ACTIVE' ? 'OVERDUE' : l.status}
          </Badge>
        );
      },
    },
    {
      header: 'Actions',
      className: 'text-right',
      cell: (l) => (
        <div className="flex items-center justify-end gap-2">
          {(l.status === 'ACTIVE' || l.status === 'OVERDUE') && (
            <>
              <Button
                variant="outline"
                size="sm"
                isLoading={returningId === l.id}
                onClick={() => handleReturn(l.id)}
                className="text-emerald-500 hover:text-emerald-400 border-emerald-500/30 hover:border-emerald-500/60"
                leftIcon={<CheckCircle className="w-3.5 h-3.5" />}
              >
                Return Book
              </Button>
              <Button
                variant="outline"
                size="sm"
                isLoading={renewingId === l.id}
                onClick={() => handleRenew(l.id)}
                leftIcon={<RotateCcw className="w-3.5 h-3.5 text-cyan-400" />}
              >
                Renew (+14 Days)
              </Button>
            </>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-black text-[var(--text-primary)]">My Borrowed Books</h2>
        <p className="text-xs text-[var(--text-secondary)]">
          Manage your active book checkouts, review loan history and renew before due date
        </p>
      </div>

      <DataTable
        columns={columns}
        data={loans}
        isLoading={isLoading}
        emptyMessage="You have no borrowed books on record."
      />
    </div>
  );
};
