import React, { useState, useEffect } from 'react';
import {
  BookOpen,
  Receipt,
  RotateCcw,
  BookmarkCheck,
  Search,
  Clock,
  ArrowRight,
  CheckCircle2,
  AlertCircle,
} from 'lucide-react';
import { api } from '../../services/api';
import type { StudentDashboardSummary } from '../../types';
import { useAuth } from '../../context/AuthContext';
import { StatsCard } from '../../components/ui/KpiCard';
import { Card } from '../../components/ui/GlassCard';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';

export interface StudentDashboardProps {
  onNavigate: (view: string) => void;
}

export const StudentDashboard: React.FC<StudentDashboardProps> = ({ onNavigate }) => {
  const { user, member } = useAuth();
  const [summary, setSummary] = useState<StudentDashboardSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [renewingId, setRenewingId] = useState<number | null>(null);
  const [returningId, setReturningId] = useState<number | null>(null);
  const [actionNotice, setActionNotice] = useState<string | null>(null);

  const [error, setError] = useState<string | null>(null);

  const fetchSummary = React.useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await api.studentDashboard.getSummary();
      setSummary(data);
    } catch (e: any) {
      setError(e.message || 'Unable to load academic dashboard');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchSummary();
  }, [fetchSummary]);

  const handleRenew = async (loanId: number) => {
    setRenewingId(loanId);
    setActionNotice(null);
    try {
      await api.loans.renew(loanId);
      setActionNotice('Book renewed successfully. Due date extended by 14 days.');
      fetchSummary();
      setTimeout(() => setActionNotice(null), 4000);
    } catch (e: any) {
      alert(e.message || 'Renewal limit reached or error');
    } finally {
      setRenewingId(null);
    }
  };

  const handleReturn = async (loanId: number) => {
    if (!confirm('Are you sure you want to return this book?')) return;
    setReturningId(loanId);
    setActionNotice(null);
    try {
      await api.loans.returnBook(loanId);
      setActionNotice('Book returned successfully! Thank you.');
      fetchSummary();
      setTimeout(() => setActionNotice(null), 4000);
    } catch (e: any) {
      alert(e.message || 'Failed to return book');
    } finally {
      setReturningId(null);
    }
  };

  const handleBorrow = async (bookId: number) => {
    setActionNotice(null);
    try {
      await api.loans.issue({ bookId });
      setActionNotice('Book successfully issued! It has been added to your Active Loans.');
      fetchSummary();
      setTimeout(() => setActionNotice(null), 4000);
    } catch (e: any) {
      alert(e.message || 'Failed to issue book');
    }
  };

  const handleReserve = async (bookId: number) => {
    setActionNotice(null);
    try {
      await api.reservations.reserve(bookId);
      setActionNotice('Hold placed successfully. You will receive an alert once ready.');
      fetchSummary();
      setTimeout(() => setActionNotice(null), 4000);
    } catch (e: any) {
      alert(e.message || 'Failed to place reservation');
    }
  };

  const activeLoans = summary?.activeLoans || [];
  const unpaidFines = summary?.unpaidFines || [];
  const totalFineAmount = unpaidFines.reduce((acc, f) => acc + f.amount, 0);

  return (
    <div className="space-y-6">
      {/* Top Welcome Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-xl border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 shadow-xs">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="text-[11px] font-medium px-2 py-0.5 rounded bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400">
              Academic Patron Portal
            </span>
          </div>
          <h2 className="text-xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-100">
            Welcome, {member?.name || user?.fullName || 'Student'}
          </h2>
          <p className="text-xs text-zinc-500 dark:text-zinc-400 mt-0.5">
            Card #{member?.memberId || 'LIB-STU-1001'} • Maximum limit:{' '}
            {member?.maxBooksAllowed || 5} concurrent books
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => onNavigate('student-profile')}
          >
            Digital Library Card
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => onNavigate('student-catalog')}
            leftIcon={<Search className="w-3.5 h-3.5" />}
          >
            Browse Catalog
          </Button>
        </div>
      </div>

      {/* Error state with retry */}
      {error && (
        <div className="p-4 rounded-xl border border-red-200 dark:border-red-800/40 bg-red-50 dark:bg-red-950/20 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
          <div className="flex items-center gap-2.5 text-red-700 dark:text-red-300">
            <AlertCircle className="w-4 h-4 shrink-0 text-red-500" />
            <span>Unable to connect to LibraX server: {error}</span>
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={fetchSummary}
            isLoading={isLoading}
            className="border-red-300 dark:border-red-700 text-red-700 dark:text-red-300 h-8 text-xs shrink-0"
          >
            Retry Connection
          </Button>
        </div>
      )}

      {actionNotice && (
        <div className="p-3 rounded-lg bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800/40 text-emerald-800 dark:text-emerald-300 text-xs flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{actionNotice}</span>
        </div>
      )}

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatsCard
          title="Active Borrowed Books"
          value={activeLoans.length}
          subtitle={`Max limit: ${member?.maxBooksAllowed || 5} books`}
          icon={<BookOpen className="w-4 h-4" />}
          onClick={() => onNavigate('student-loans')}
        />
        <StatsCard
          title="Due in 3 Days"
          value={summary?.dueSoonCount ?? 0}
          subtitle="Return or renew before deadline"
          icon={<Clock className="w-4 h-4" />}
          trend={
            (summary?.dueSoonCount ?? 0) > 0
              ? { value: `${summary?.dueSoonCount}`, isPositive: false, label: 'due soon' }
              : undefined
          }
          onClick={() => onNavigate('student-loans')}
        />
        <StatsCard
          title="Outstanding Fines"
          value={`₹${totalFineAmount.toFixed(2)}`}
          subtitle={unpaidFines.length > 0 ? `${unpaidFines.length} pending notices` : 'No outstanding dues'}
          icon={<Receipt className="w-4 h-4" />}
          onClick={() => onNavigate('student-fines')}
        />
        <StatsCard
          title="Active Book Holds"
          value={summary?.reservations?.length ?? 0}
          subtitle="Awaiting available copies"
          icon={<BookmarkCheck className="w-4 h-4" />}
          onClick={() => onNavigate('student-reservations')}
        />
      </div>

      {/* Currently Reading & Recommended Reading */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
        {/* Left Column (2 Cols): Active Loans */}
        <div className="lg:col-span-2 space-y-4">
          <Card className="p-5">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
                  Currently Checked Out
                </h3>
                <p className="text-xs text-zinc-500 dark:text-zinc-400">
                  Books currently in your possession requiring return or extension
                </p>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => onNavigate('student-loans')}
                className="text-xs text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100"
              >
                View History
              </Button>
            </div>

            <div className="space-y-3">
              {activeLoans.length === 0 ? (
                <div className="py-10 text-center text-xs text-zinc-400 dark:text-zinc-500">
                  You currently have no books checked out. Explore the catalog to start reading.
                </div>
              ) : (
                activeLoans.map((loan) => {
                  const dueDate = new Date(loan.dueDate);
                  const isOverdue = dueDate < new Date();
                  const diffDays = Math.ceil((dueDate.getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24));

                  return (
                    <div
                      key={loan.id}
                      className="p-3.5 rounded-lg border border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-800/40 flex flex-col sm:flex-row sm:items-center justify-between gap-3"
                    >
                      <div className="flex items-start gap-3">
                        <div className="w-8 h-11 rounded bg-zinc-200 dark:bg-zinc-700 flex items-center justify-center shrink-0 text-zinc-500 dark:text-zinc-400">
                          <BookOpen className="w-4 h-4" />
                        </div>
                        <div className="space-y-1">
                          <h4 className="text-xs font-semibold text-zinc-900 dark:text-zinc-100">
                            {loan.bookTitle || `Book #${loan.bookId}`}
                          </h4>
                          <div className="text-[11px] text-zinc-500 dark:text-zinc-400">
                            Due date:{' '}
                            <span className={isOverdue ? 'text-red-600 dark:text-red-400 font-medium' : 'font-mono'}>
                              {loan.dueDate}
                            </span>
                          </div>
                          <div className="flex items-center gap-2 pt-0.5">
                            <Badge variant={isOverdue ? 'danger' : diffDays <= 3 ? 'warning' : 'success'} size="sm" dot>
                              {isOverdue ? 'Overdue' : `${diffDays} days left`}
                            </Badge>
                            <span className="text-[10px] text-zinc-400 dark:text-zinc-500 font-mono">
                              Renewals: {loan.renewalCount}/2
                            </span>
                          </div>
                        </div>
                      </div>

                      <div className="self-end sm:self-center flex items-center gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          isLoading={returningId === loan.id}
                          onClick={() => handleReturn(loan.id)}
                          className="h-8 text-xs text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 dark:hover:text-emerald-300 border-emerald-500/30 hover:border-emerald-500/60"
                        >
                          Return
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          isLoading={renewingId === loan.id}
                          onClick={() => handleRenew(loan.id)}
                          leftIcon={<RotateCcw className="w-3.5 h-3.5" />}
                          className="h-8 text-xs"
                        >
                          Renew (+14d)
                        </Button>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </Card>
        </div>

        {/* Right Column (1 Col): Recommended Reading */}
        <div>
          <Card className="p-5 h-full flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
                    Recommended Titles
                  </h3>
                  <p className="text-xs text-zinc-500 dark:text-zinc-400">
                    Popular in library collection
                  </p>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => onNavigate('student-catalog')}
                  className="text-xs text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100"
                >
                  All Titles
                </Button>
              </div>

              <div className="space-y-3">
                {summary?.recommendations && summary.recommendations.length > 0 ? (
                  summary.recommendations.slice(0, 4).map((book) => (
                    <div
                      key={book.id}
                      className="p-3 rounded-lg border border-zinc-200/70 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-800/30 flex items-center justify-between gap-3"
                    >
                      <div className="space-y-1 min-w-0">
                        <div className="font-medium text-xs text-zinc-900 dark:text-zinc-100 truncate">
                          {book.title}
                        </div>
                        <div className="text-[11px] text-zinc-500 dark:text-zinc-400 truncate">
                          by {book.author}
                        </div>
                        <Badge variant="neutral" size="sm">
                          {book.category || 'General'}
                        </Badge>
                      </div>

                      <Button
                        variant="primary"
                        size="sm"
                        onClick={() => handleBorrow(book.id)}
                        className="h-7 text-xs shrink-0"
                      >
                        Issue Book
                      </Button>
                    </div>
                  ))
                ) : (
                  <div className="py-8 text-center text-xs text-zinc-400 dark:text-zinc-500">
                    No recommendations currently available.
                  </div>
                )}
              </div>
            </div>

            <div className="pt-4 mt-4 border-t border-zinc-100 dark:border-zinc-800">
              <Button
                variant="outline"
                size="sm"
                className="w-full text-xs"
                onClick={() => onNavigate('student-catalog')}
                rightIcon={<ArrowRight className="w-3.5 h-3.5" />}
              >
                Browse Full Catalog
              </Button>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};
