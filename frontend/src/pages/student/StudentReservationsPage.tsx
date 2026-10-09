import React, { useState, useEffect, useCallback } from 'react';
import { XCircle } from 'lucide-react';
import { api } from '../../services/api';
import type { Reservation } from '../../types';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { DataTable, Column } from '../../components/ui/DataTable';

export const StudentReservationsPage: React.FC = () => {
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const fetchMyReservations = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await api.reservations.getMyReservations();
      setReservations(data || []);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchMyReservations();
  }, [fetchMyReservations]);

  const handleCancel = async (id: number) => {
    if (!confirm('Are you sure you want to cancel this hold request?')) return;
    try {
      await api.reservations.cancel(id);
      alert('Hold cancelled.');
      fetchMyReservations();
    } catch (e: any) {
      alert(e.message || 'Failed to cancel hold');
    }
  };

  const columns: Column<Reservation>[] = [
    {
      header: 'Book Title',
      cell: (r) => (
        <div className="space-y-0.5">
          <div className="font-bold text-sm text-[var(--text-primary)]">{r.bookTitle || `Book #${r.bookId}`}</div>
          <div className="text-xs text-[var(--text-secondary)] font-mono">Hold ID: #{r.id}</div>
        </div>
      ),
    },
    {
      header: 'Queue Position',
      cell: (r) => (
        <span className="font-mono text-xs font-bold text-cyan-400">
          Position #{r.queuePosition ?? 1}
        </span>
      ),
    },
    {
      header: 'Request Date',
      cell: (r) => <span className="text-xs font-mono text-[var(--text-secondary)]">{r.reservationDate}</span>,
    },
    {
      header: 'Status',
      cell: (r) => (
        <Badge
          variant={r.status === 'WAITING' || r.status === 'READY' || r.status === 'PENDING' ? 'warning' : r.status === 'FULFILLED' ? 'success' : 'neutral'}
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
        <div className="flex items-center justify-end">
          {(r.status === 'WAITING' || r.status === 'READY' || r.status === 'PENDING') && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => handleCancel(r.id)}
              leftIcon={<XCircle className="w-3.5 h-3.5 text-rose-400" />}
              className="text-rose-400 hover:text-rose-300 border-rose-500/30 hover:border-rose-500/60"
            >
              Unhold Book
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-black text-[var(--text-primary)]">My Book Holds & Reservations</h2>
        <p className="text-xs text-[var(--text-secondary)]">
          Queue status for books currently loaned or awaiting collection
        </p>
      </div>

      <DataTable
        columns={columns}
        data={reservations}
        isLoading={isLoading}
        emptyMessage="You have no active hold requests."
      />
    </div>
  );
};
