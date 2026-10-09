import React, { useState, useEffect, useCallback } from 'react';
import { CheckCircle, XCircle, Search } from 'lucide-react';
import { api } from '../../services/api';
import type { Reservation } from '../../types';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { DataTable, Column } from '../../components/ui/DataTable';

export const ReservationsPage: React.FC = () => {
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [status, setStatus] = useState<string>('PENDING');
  const [search, setSearch] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  const fetchReservations = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await api.reservations.getAll({
        search: search.trim() || undefined,
        status: status || undefined,
      });
      setReservations(data || []);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  }, [search, status]);

  useEffect(() => {
    fetchReservations();
  }, [fetchReservations]);

  const handleCancel = async (id: number) => {
    if (!confirm('Are you sure you want to cancel this reservation hold?')) return;
    try {
      await api.reservations.cancel(id);
      fetchReservations();
    } catch (e: any) {
      alert(e.message || 'Failed to cancel reservation');
    }
  };

  const handleFulfill = async (r: Reservation) => {
    const copyIdStr = prompt(`Enter available copy ID to fulfill hold for "${r.bookTitle}":`);
    if (!copyIdStr) return;
    try {
      await api.reservations.fulfill(r.id, Number(copyIdStr));
      alert('Reservation fulfilled! Book checked out to student.');
      fetchReservations();
    } catch (e: any) {
      alert(e.message || 'Failed to fulfill reservation');
    }
  };

  const columns: Column<Reservation>[] = [
    {
      header: 'Book & Queue',
      cell: (r) => (
        <div className="space-y-0.5">
          <div className="font-bold text-[var(--text-primary)] text-sm">{r.bookTitle || `Book #${r.bookId}`}</div>
          <div className="text-xs text-[var(--text-secondary)] font-mono">
            Queue Position: #{r.queuePosition ?? 1} • Hold ID: #{r.id}
          </div>
        </div>
      ),
    },
    {
      header: 'Requested By',
      cell: (r) => (
        <div className="space-y-0.5">
          <div className="font-semibold text-xs text-[var(--text-primary)]">{r.memberName || `Member #${r.memberId}`}</div>
          <div className="text-[11px] text-[var(--text-secondary)] font-mono">{r.memberCode}</div>
        </div>
      ),
    },
    {
      header: 'Reserved Date',
      cell: (r) => (
        <div className="text-xs text-[var(--text-secondary)] font-mono">
          {r.reservationDate}
        </div>
      ),
    },
    {
      header: 'Status',
      cell: (r) => (
        <Badge
          variant={r.status === 'PENDING' ? 'warning' : r.status === 'FULFILLED' ? 'success' : 'neutral'}
          size="sm"
          dot
        >
          {r.status}
        </Badge>
      ),
    },
    {
      header: 'Actions',
      className: 'text-right',
      cell: (r) => (
        <div className="flex items-center justify-end gap-1.5">
          {r.status === 'PENDING' && (
            <>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleFulfill(r)}
                leftIcon={<CheckCircle className="w-3.5 h-3.5 text-emerald-400" />}
              >
                Fulfill
              </Button>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => handleCancel(r.id)}
                title="Cancel Reservation"
              >
                <XCircle className="w-4 h-4 text-rose-400" />
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
          <h2 className="text-xl font-black text-[var(--text-primary)]">Hold Queues & Reservations</h2>
          <p className="text-xs text-[var(--text-secondary)]">
            Prioritized waiting list for in-demand and loaned library materials
          </p>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="flex gap-1.5 p-1 bg-[var(--bg-secondary)] border border-[var(--border-color)] rounded-xl">
          {['PENDING', 'FULFILLED', 'CANCELLED', ''].map((tab) => (
            <button
              key={tab}
              onClick={() => setStatus(tab)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
                status === tab
                  ? 'bg-[var(--accent-primary)] text-[var(--accent-text)] shadow'
                  : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
              }`}
            >
              {tab === '' ? 'All Holds' : tab}
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
        data={reservations}
        isLoading={isLoading}
        emptyMessage="No reservations currently on file."
      />
    </div>
  );
};
