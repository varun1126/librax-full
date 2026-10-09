import React, { useState, useEffect } from 'react';
import { Plus, CheckCircle, RotateCcw, Search } from 'lucide-react';
import { api } from '../../services/api';
import type { Loan } from '../../types';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { DataTable, Column } from '../../components/ui/DataTable';

export const LoansPage: React.FC = () => {
  const [loans, setLoans] = useState<Loan[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [status, setStatus] = useState<string>('ACTIVE');
  const [search, setSearch] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  // Issue Modal
  const [isIssueModalOpen, setIsIssueModalOpen] = useState(false);
  const [issueMemberId, setIssueMemberId] = useState('');
  const [issueBookId, setIssueBookId] = useState('');
  const [issueDueDate, setIssueDueDate] = useState('');
  const [issueLoading, setIssueLoading] = useState(false);

  const fetchLoans = React.useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await api.loans.getAll({
        search: search.trim() || undefined,
        status: status || undefined,
        page,
        size: 10,
      });
      setLoans(res.loans || []);
      setTotal(res.total || 0);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  }, [search, status, page]);

  useEffect(() => {
    fetchLoans();
  }, [fetchLoans]);

  const handleReturn = async (loanId: number) => {
    if (!confirm('Confirm return of this book?')) return;
    try {
      await api.loans.returnBook(loanId);
      alert('Book marked as returned successfully!');
      fetchLoans();
    } catch (e: any) {
      alert(e.message || 'Failed to return book');
    }
  };

  const handleRenew = async (loanId: number) => {
    try {
      await api.loans.renew(loanId);
      alert('Loan renewed successfully! Due date extended.');
      fetchLoans();
    } catch (e: any) {
      alert(e.message || 'Failed to renew loan');
    }
  };

  const handleIssueSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIssueLoading(true);
    try {
      await api.loans.issue({
        memberId: Number(issueMemberId),
        bookId: Number(issueBookId),
        dueDate: issueDueDate || undefined,
      });
      alert('Book checked out and loaned successfully!');
      setIsIssueModalOpen(false);
      setIssueMemberId('');
      setIssueBookId('');
      fetchLoans();
    } catch (e: any) {
      alert(e.message || 'Failed to issue book');
    } finally {
      setIssueLoading(false);
    }
  };

  const columns: Column<Loan>[] = [
    {
      header: 'Loan Details',
      cell: (l) => (
        <div className="space-y-0.5">
          <div className="font-bold text-[var(--text-primary)] text-sm">{l.bookTitle || `Book #${l.bookId}`}</div>
          <div className="text-xs text-[var(--text-secondary)]">Copy Barcode: #{l.copyId}</div>
          <div className="text-[10px] text-[var(--text-muted)] font-mono">Loan ID: #{l.id}</div>
        </div>
      ),
    },
    {
      header: 'Borrower',
      cell: (l) => (
        <div className="space-y-0.5">
          <div className="font-semibold text-xs text-[var(--text-primary)]">{l.memberName || `Member #${l.memberId}`}</div>
          <div className="text-[11px] text-[var(--text-secondary)] font-mono">{l.memberCode}</div>
        </div>
      ),
    },
    {
      header: 'Issue / Due Dates',
      cell: (l) => {
        const isOverdue = l.status === 'OVERDUE' || (l.status === 'ACTIVE' && new Date(l.dueDate) < new Date());
        return (
          <div className="space-y-0.5 text-xs">
            <div className="text-[var(--text-muted)]">Issued: {l.issueDate}</div>
            <div className={isOverdue ? 'text-rose-400 font-bold' : 'text-[var(--text-primary)]'}>
              Due: {l.dueDate}
            </div>
            {l.returnDate && <div className="text-emerald-400 text-[11px]">Returned: {l.returnDate}</div>}
          </div>
        );
      },
    },
    {
      header: 'Renewals',
      cell: (l) => (
        <span className="text-xs font-mono font-medium text-[var(--text-secondary)]">
          {l.renewalCount}x
        </span>
      ),
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
        <div className="flex items-center justify-end gap-1.5">
          {l.status !== 'RETURNED' && (
            <>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => handleRenew(l.id)}
                title="Renew Loan (+14 Days)"
              >
                <RotateCcw className="w-4 h-4 text-cyan-400" />
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleReturn(l.id)}
                leftIcon={<CheckCircle className="w-3.5 h-3.5 text-emerald-400" />}
              >
                Return
              </Button>
            </>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-black text-[var(--text-primary)]">Circulation & Book Loans</h2>
          <p className="text-xs text-[var(--text-secondary)]">
            Issue books, process returns, track due dates and calculate overdue status
          </p>
        </div>
        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={() => setIsIssueModalOpen(true)}
        >
          Issue New Book
        </Button>
      </div>

      {/* Tabs & Search */}
      <div className="flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="flex gap-1.5 p-1 bg-[var(--bg-secondary)] border border-[var(--border-color)] rounded-xl">
          {['ACTIVE', 'OVERDUE', 'RETURNED', ''].map((tab) => (
            <button
              key={tab}
              onClick={() => {
                setStatus(tab);
                setPage(1);
              }}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
                status === tab
                  ? 'bg-[var(--accent-primary)] text-[var(--accent-text)] shadow'
                  : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
              }`}
            >
              {tab === '' ? 'All Loans' : tab}
            </button>
          ))}
        </div>

        <div className="w-full sm:w-72">
          <Input
            placeholder="Search book or student name..."
            leftIcon={<Search className="w-4 h-4" />}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      <DataTable
        columns={columns}
        data={loans}
        isLoading={isLoading}
        page={page}
        pageSize={10}
        total={total}
        onPageChange={setPage}
        emptyMessage="No loan records found."
      />

      {/* Issue Modal */}
      <Modal
        isOpen={isIssueModalOpen}
        onClose={() => setIsIssueModalOpen(false)}
        title="Issue Book Loan"
        description="Check out a copy to a student"
      >
        <form onSubmit={handleIssueSubmit} className="space-y-4">
          <Input
            label="Student / Member ID (Numeric)"
            placeholder="e.g. 1"
            value={issueMemberId}
            onChange={(e) => setIssueMemberId(e.target.value)}
            required
            helperText="Enter member's database ID"
          />
          <Input
            label="Book ID (Catalog ID)"
            placeholder="e.g. 1"
            value={issueBookId}
            onChange={(e) => setIssueBookId(e.target.value)}
            required
            helperText="Enter catalog book ID"
          />
          <Input
            label="Custom Due Date (optional)"
            type="date"
            value={issueDueDate}
            onChange={(e) => setIssueDueDate(e.target.value)}
            helperText="Leave empty to use system default (14 days)"
          />

          <div className="flex justify-end gap-2 pt-3">
            <Button type="button" variant="outline" onClick={() => setIsIssueModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" isLoading={issueLoading}>
              Confirm Checkout
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
