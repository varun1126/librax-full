import React, { useState, useEffect } from 'react';
import { IndianRupee, ShieldOff, Search } from 'lucide-react';
import { api } from '../../services/api';
import type { Fine } from '../../types';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { DataTable, Column } from '../../components/ui/DataTable';

export const FinesPage: React.FC = () => {
  const [fines, setFines] = useState<Fine[]>([]);
  const [status, setStatus] = useState<string>('PENDING');
  const [search, setSearch] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  // Pay Modal
  const [isPayModalOpen, setIsPayModalOpen] = useState(false);
  const [selectedFine, setSelectedFine] = useState<Fine | null>(null);
  const [payMethod, setPayMethod] = useState<'CASH' | 'CARD' | 'UPI' | 'ONLINE'>('CASH');
  const [payAmount, setPayAmount] = useState<number>(0);
  const [payLoading, setPayLoading] = useState(false);

  // Waive Modal
  const [isWaiveModalOpen, setIsWaiveModalOpen] = useState(false);
  const [waiveReason, setWaiveReason] = useState('');
  const [waiveLoading, setWaiveLoading] = useState(false);

  const fetchFines = React.useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await api.fines.getAll({
        search: search.trim() || undefined,
        status: status || undefined,
      });
      setFines(data || []);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  }, [search, status]);

  useEffect(() => {
    fetchFines();
  }, [fetchFines]);

  const handlePaySubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFine) return;
    setPayLoading(true);
    try {
      await api.fines.pay(selectedFine.id, payMethod, payAmount);
      alert('Payment collected successfully and fine cleared!');
      setIsPayModalOpen(false);
      fetchFines();
    } catch (e: any) {
      alert(e.message || 'Payment processing failed');
    } finally {
      setPayLoading(false);
    }
  };

  const handleWaiveSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFine) return;
    setWaiveLoading(true);
    try {
      await api.fines.waive(selectedFine.id, waiveReason);
      alert('Fine successfully waived!');
      setIsWaiveModalOpen(false);
      fetchFines();
    } catch (e: any) {
      alert(e.message || 'Failed to waive fine');
    } finally {
      setWaiveLoading(false);
    }
  };

  const columns: Column<Fine>[] = [
    {
      header: 'Fine Record',
      cell: (f) => (
        <div className="space-y-0.5">
          <div className="font-bold text-[var(--text-primary)] text-sm">
            {f.bookTitle ? `Overdue: ${f.bookTitle}` : `Penalty #${f.id}`}
          </div>
          <div className="text-xs text-[var(--text-secondary)]">{f.reason || 'Overdue item penalty'}</div>
          <div className="text-[10px] text-[var(--text-muted)] font-mono">Fine ID: #{f.id} • Loan: #{f.loanId}</div>
        </div>
      ),
    },
    {
      header: 'Student / Borrower',
      cell: (f) => (
        <div className="space-y-0.5">
          <div className="font-semibold text-xs text-[var(--text-primary)]">{f.memberName || `Member #${f.memberId}`}</div>
          <div className="text-[11px] text-[var(--text-secondary)] font-mono">{f.memberCode}</div>
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
      header: 'Issue / Settlement Date',
      cell: (f) => (
        <div className="text-xs space-y-0.5">
          <div className="text-[var(--text-muted)]">Assessed: {f.issueDate}</div>
          {f.paidDate && <div className="text-emerald-400 font-medium">Cleared: {f.paidDate}</div>}
        </div>
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
    {
      header: 'Actions',
      className: 'text-right',
      cell: (f) => (
        <div className="flex items-center justify-end gap-1.5">
          {f.status === 'PENDING' && (
            <>
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setSelectedFine(f);
                  setWaiveReason('');
                  setIsWaiveModalOpen(true);
                }}
                title="Waive Fine"
              >
                <ShieldOff className="w-3.5 h-3.5 text-amber-400" />
              </Button>
              <Button
                variant="primary"
                size="sm"
                onClick={() => {
                  setSelectedFine(f);
                  setPayAmount(f.amount);
                  setIsPayModalOpen(true);
                }}
                leftIcon={<IndianRupee className="w-3.5 h-3.5" />}
              >
                Collect
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
          <h2 className="text-xl font-black text-[var(--text-primary)]">Fines, Dues & Cashier</h2>
          <p className="text-xs text-[var(--text-secondary)]">
            Manage overdue library fees, record payments and process administrative waivers
          </p>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="flex gap-1.5 p-1 bg-[var(--bg-secondary)] border border-[var(--border-color)] rounded-xl">
          {['PENDING', 'PAID', 'WAIVED', ''].map((tab) => (
            <button
              key={tab}
              onClick={() => setStatus(tab)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
                status === tab
                  ? 'bg-[var(--accent-primary)] text-[var(--accent-text)] shadow'
                  : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
              }`}
            >
              {tab === '' ? 'All Fines' : tab}
            </button>
          ))}
        </div>

        <div className="w-full sm:w-72">
          <Input
            placeholder="Search student or book title..."
            leftIcon={<Search className="w-4 h-4" />}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      <DataTable
        columns={columns}
        data={fines}
        isLoading={isLoading}
        emptyMessage="No fine records on file."
      />

      {/* Pay Modal */}
      <Modal
        isOpen={isPayModalOpen}
        onClose={() => setIsPayModalOpen(false)}
        title="Collect Fine Payment"
        description={`Record settlement of ₹${selectedFine?.amount.toFixed(2)} from ${selectedFine?.memberName}`}
      >
        <form onSubmit={handlePaySubmit} className="space-y-4">
          <div className="p-3 rounded-xl bg-[var(--bg-card)] border border-[var(--border-color)] space-y-1">
            <div className="text-xs text-[var(--text-secondary)]">Fine Balance</div>
            <div className="text-2xl font-bold font-mono text-rose-400">
              ₹{selectedFine?.amount.toFixed(2)}
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="block text-xs font-medium text-[var(--text-secondary)]">
              Payment Method
            </label>
            <div className="grid grid-cols-4 gap-2">
              {(['CASH', 'CARD', 'UPI', 'ONLINE'] as const).map((method) => (
                <button
                  key={method}
                  type="button"
                  onClick={() => setPayMethod(method)}
                  className={`py-2 text-xs font-medium rounded-lg border transition-colors cursor-pointer ${
                    payMethod === method
                      ? 'border-zinc-900 bg-zinc-900 text-zinc-50 dark:border-zinc-100 dark:bg-zinc-100 dark:text-zinc-900'
                      : 'border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900 text-zinc-600 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-zinc-800'
                  }`}
                >
                  {method}
                </button>
              ))}
            </div>
          </div>

          <Input
            label="Amount Paid (₹)"
            type="number"
            step="0.01"
            min="0.01"
            value={payAmount}
            onChange={(e) => setPayAmount(Number(e.target.value))}
            required
          />

          <div className="flex justify-end gap-2 pt-3">
            <Button type="button" variant="outline" onClick={() => setIsPayModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" isLoading={payLoading}>
              Confirm Receipt & Clear Fine
            </Button>
          </div>
        </form>
      </Modal>

      {/* Waive Modal */}
      <Modal
        isOpen={isWaiveModalOpen}
        onClose={() => setIsWaiveModalOpen(false)}
        title="Waive Outstanding Fine"
        description={`Forgive balance of ₹${selectedFine?.amount.toFixed(2)} for ${selectedFine?.memberName}`}
      >
        <form onSubmit={handleWaiveSubmit} className="space-y-4">
          <Input
            label="Administrative Reason for Waiver"
            placeholder="e.g. Dean's special dispensation, medical leave"
            value={waiveReason}
            onChange={(e) => setWaiveReason(e.target.value)}
            required
            helperText="Recorded in immutable audit trail"
          />

          <div className="flex justify-end gap-2 pt-3">
            <Button type="button" variant="outline" onClick={() => setIsWaiveModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="danger" isLoading={waiveLoading}>
              Authorize Waiver
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
