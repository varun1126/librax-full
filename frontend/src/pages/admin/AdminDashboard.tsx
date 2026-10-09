import React, { useState, useEffect } from 'react';
import {
  BookOpen,
  Repeat,
  AlertTriangle,
  Users,
  Clock,
  ShieldCheck,
  Plus,
  RefreshCw,
  ArrowRight,
  CheckCircle2,
  AlertCircle,
} from 'lucide-react';
import { api } from '../../services/api';
import type { DashboardStats } from '../../types';
import { StatsCard } from '../../components/ui/KpiCard';
import { Card } from '../../components/ui/GlassCard';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';

export interface AdminDashboardProps {
  onNavigate: (view: string) => void;
}

export const AdminDashboard: React.FC<AdminDashboardProps> = ({ onNavigate }) => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Quick Issue Modal State
  const [issueModalOpen, setIssueModalOpen] = useState(false);
  const [issueMemberId, setIssueMemberId] = useState('');
  const [issueBookId, setIssueBookId] = useState('');
  const [issueSubmitting, setIssueSubmitting] = useState(false);
  const [issueMessage, setIssueMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const fetchStats = React.useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await api.adminDashboard.getStats();
      setStats(data);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch dashboard metrics');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  const handleQuickIssue = async (e: React.FormEvent) => {
    e.preventDefault();
    setIssueSubmitting(true);
    setIssueMessage(null);
    try {
      await api.loans.issue({
        memberId: Number(issueMemberId),
        bookId: Number(issueBookId),
      });
      setIssueMessage({ type: 'success', text: 'Circulation loan issued successfully.' });
      setTimeout(() => {
        setIssueModalOpen(false);
        setIssueMessage(null);
        setIssueBookId('');
        setIssueMemberId('');
        fetchStats();
      }, 1000);
    } catch (err: any) {
      setIssueMessage({ type: 'error', text: err.message || 'Failed to issue loan.' });
    } finally {
      setIssueSubmitting(false);
    }
  };

  const categoriesList =
    stats?.categoryStats ||
    (stats?.booksByCategory
      ? Object.entries(stats.booksByCategory).map(([category, count]) => ({
          category,
          count: Number(count),
        }))
      : []);

  const totalBooksCount = stats?.totalBooks || 1;

  return (
    <div className="space-y-6">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-100">
            Executive Overview
          </h2>
          <p className="text-xs text-zinc-500 dark:text-zinc-400 mt-0.5">
            Real-time library telemetry across collections, active loans, and patrons.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={fetchStats}
            isLoading={isLoading}
            leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => setIssueModalOpen(true)}
            leftIcon={<Plus className="w-3.5 h-3.5" />}
          >
            Quick Issue Book
          </Button>
        </div>
      </div>

      {/* Error state with retry button */}
      {error && (
        <div className="p-4 rounded-xl border border-red-200 dark:border-red-800/40 bg-red-50 dark:bg-red-950/20 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
          <div className="flex items-center gap-2.5 text-red-700 dark:text-red-300">
            <AlertCircle className="w-4 h-4 shrink-0 text-red-500" />
            <span>Unable to connect to LibraX server: {error}</span>
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={fetchStats}
            isLoading={isLoading}
            className="border-red-300 dark:border-red-700 text-red-700 dark:text-red-300 h-8 text-xs shrink-0"
          >
            Retry Connection
          </Button>
        </div>
      )}

      {/* Urgent Attention Alert (if overdue loans exist) */}
      {stats && stats.overdueLoans > 0 && (
        <div className="p-3.5 rounded-xl border border-amber-200 dark:border-amber-800/50 bg-amber-50/60 dark:bg-amber-950/20 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
          <div className="flex items-center gap-2.5">
            <AlertTriangle className="w-4 h-4 text-amber-600 dark:text-amber-400 shrink-0" />
            <div>
              <span className="font-semibold text-amber-900 dark:text-amber-200">
                {stats.overdueLoans} {stats.overdueLoans === 1 ? 'item requires' : 'items require'} immediate attention:
              </span>{' '}
              <span className="text-amber-700 dark:text-amber-300">
                Loan due dates have elapsed. Automatic penalty accrual is active.
              </span>
            </div>
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={() => onNavigate('loans')}
            className="border-amber-300 dark:border-amber-700 text-amber-800 dark:text-amber-200 hover:bg-amber-100 dark:hover:bg-amber-900/40 shrink-0 h-7 text-xs"
          >
            View Overdue Loans
          </Button>
        </div>
      )}

      {/* Primary KPI Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatsCard
          title="Cataloged Titles"
          value={stats ? stats.totalBooks : '—'}
          subtitle="Physical & digital titles"
          icon={<BookOpen className="w-4 h-4" />}
          onClick={() => onNavigate('books')}
        />
        <StatsCard
          title="Active Circulation Loans"
          value={stats ? stats.activeLoans : '—'}
          subtitle="Currently checked out"
          icon={<Repeat className="w-4 h-4" />}
          onClick={() => onNavigate('loans')}
        />
        <StatsCard
          title="Overdue Items"
          value={stats ? stats.overdueLoans : '—'}
          subtitle="Exceeded return date"
          icon={<AlertTriangle className="w-4 h-4" />}
          trend={
            stats && stats.overdueLoans > 0
              ? { value: `${stats.overdueLoans}`, isPositive: false, label: 'action required' }
              : undefined
          }
          onClick={() => onNavigate('loans')}
        />
        <StatsCard
          title="Registered Students"
          value={stats ? stats.activeStudents : '—'}
          subtitle="Active library patrons"
          icon={<Users className="w-4 h-4" />}
          onClick={() => onNavigate('students')}
        />
      </div>

      {/* Bento Grid: Collection Distribution & Financial/Operations Ledger */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
        {/* Left Column (2 Cols): Collection Breakdown & Inventory Alert */}
        <div className="lg:col-span-2 space-y-5">
          {/* Collection Distribution Card */}
          <Card className="p-5">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
                  Collection by Subject Category
                </h3>
                <p className="text-xs text-zinc-500 dark:text-zinc-400">
                  Distribution of titles across academic disciplines
                </p>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => onNavigate('books')}
                className="text-xs text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100"
              >
                Browse All Books
              </Button>
            </div>

            <div className="space-y-3.5">
              {categoriesList.length === 0 ? (
                <div className="py-8 text-center text-xs text-zinc-400 dark:text-zinc-500">
                  No category records found in catalog.
                </div>
              ) : (
                categoriesList.map((cat, idx) => {
                  const percentage = Math.round((cat.count / totalBooksCount) * 100);
                  return (
                    <div key={idx} className="space-y-1.5">
                      <div className="flex items-center justify-between text-xs">
                        <span className="font-medium text-zinc-800 dark:text-zinc-200">
                          {cat.category}
                        </span>
                        <span className="font-mono text-zinc-500 dark:text-zinc-400">
                          {cat.count} {cat.count === 1 ? 'title' : 'titles'} ({percentage}%)
                        </span>
                      </div>
                      <div className="h-1.5 w-full rounded-full bg-zinc-100 dark:bg-zinc-800 overflow-hidden">
                        <div
                          className="h-full rounded-full bg-zinc-900 dark:bg-zinc-100 transition-all duration-300"
                          style={{ width: `${Math.max(percentage, 4)}%` }}
                        />
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </Card>

          {/* Recent Audit Ledger */}
          <Card className="p-5">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
                  Audit & Security Trail
                </h3>
                <p className="text-xs text-zinc-500 dark:text-zinc-400">
                  Immutable operations log for recent library actions
                </p>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => onNavigate('audit')}
                className="text-xs text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100"
              >
                Full Audit Log
              </Button>
            </div>

            <div className="divide-y divide-zinc-100 dark:divide-zinc-800/80">
              {stats?.recentActivity && stats.recentActivity.length > 0 ? (
                stats.recentActivity.slice(0, 5).map((log, idx) => (
                  <div key={idx} className="py-2.5 flex items-start gap-3 text-xs">
                    <div className="p-1 rounded-md bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 shrink-0 mt-0.5">
                      <ShieldCheck className="w-3.5 h-3.5" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between">
                        <span className="font-medium text-zinc-900 dark:text-zinc-100 truncate">
                          {log.action}
                        </span>
                        <span className="font-mono text-[10px] text-zinc-400 dark:text-zinc-500 shrink-0">
                          {new Date(log.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </span>
                      </div>
                      <p className="text-[11px] text-zinc-500 dark:text-zinc-400 truncate mt-0.5">
                        {log.details || `Entity: ${log.entityType || 'General'} (ID: ${log.entityId || 'N/A'})`}
                      </p>
                    </div>
                  </div>
                ))
              ) : (
                <div className="py-6 text-center text-xs text-zinc-400 dark:text-zinc-500">
                  No recent audit entries logged.
                </div>
              )}
            </div>
          </Card>
        </div>

        {/* Right Column (1 Col): Operations, Finance & Low Stock */}
        <div className="space-y-5">
          {/* Financial Summary Card */}
          <Card className="p-5">
            <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100 mb-1">
              Financial Summary
            </h3>
            <p className="text-xs text-zinc-500 dark:text-zinc-400 mb-4">
              Circulation dues & fee recovery
            </p>

            <div className="space-y-3">
              <div className="p-3 rounded-lg bg-zinc-50 dark:bg-zinc-800/50 border border-zinc-200/60 dark:border-zinc-800">
                <div className="text-[11px] font-medium text-zinc-500 dark:text-zinc-400">
                  Unpaid Fines Outstanding
                </div>
                <div className="text-xl font-bold font-mono text-zinc-900 dark:text-zinc-100 mt-0.5">
                  ₹{stats ? stats.unpaidFinesAmount.toFixed(2) : '0.00'}
                </div>
              </div>

              <div className="p-3 rounded-lg bg-zinc-50 dark:bg-zinc-800/50 border border-zinc-200/60 dark:border-zinc-800">
                <div className="text-[11px] font-medium text-zinc-500 dark:text-zinc-400">
                  Total Fees Collected
                </div>
                <div className="text-xl font-bold font-mono text-emerald-600 dark:text-emerald-400 mt-0.5">
                  ₹{stats ? stats.totalFinesCollected.toFixed(2) : '0.00'}
                </div>
              </div>
            </div>

            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigate('fines')}
              className="w-full mt-4 text-xs"
              rightIcon={<ArrowRight className="w-3.5 h-3.5" />}
            >
              Open Cashier & Fines
            </Button>
          </Card>

          {/* Operational Quick Actions & Holds */}
          <Card className="p-5">
            <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100 mb-1">
              Circulation Queue
            </h3>
            <p className="text-xs text-zinc-500 dark:text-zinc-400 mb-4">
              Pending reservations & stock
            </p>

            <div className="space-y-2.5">
              <div
                onClick={() => onNavigate('reservations')}
                className="flex items-center justify-between p-2.5 rounded-lg border border-zinc-200/60 dark:border-zinc-800 hover:bg-zinc-50 dark:hover:bg-zinc-800/60 transition-colors cursor-pointer"
              >
                <div className="flex items-center gap-2">
                  <Clock className="w-4 h-4 text-zinc-500" />
                  <span className="text-xs font-medium text-zinc-800 dark:text-zinc-200">
                    Pending Holds
                  </span>
                </div>
                <Badge variant={stats && stats.pendingReservations > 0 ? 'warning' : 'neutral'} size="sm">
                  {stats ? stats.pendingReservations : 0} waiting
                </Badge>
              </div>

              <div
                onClick={() => onNavigate('books')}
                className="flex items-center justify-between p-2.5 rounded-lg border border-zinc-200/60 dark:border-zinc-800 hover:bg-zinc-50 dark:hover:bg-zinc-800/60 transition-colors cursor-pointer"
              >
                <div className="flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 text-zinc-500" />
                  <span className="text-xs font-medium text-zinc-800 dark:text-zinc-200">
                    Zero-Stock Titles
                  </span>
                </div>
                <Badge variant={stats && stats.lowStockCount > 0 ? 'danger' : 'neutral'} size="sm">
                  {stats ? stats.lowStockCount : 0} titles
                </Badge>
              </div>
            </div>

            <div className="pt-4 mt-4 border-t border-zinc-100 dark:border-zinc-800 space-y-2">
              <Button
                variant="secondary"
                size="sm"
                className="w-full text-xs"
                onClick={() => onNavigate('students')}
              >
                Manage Student Registry
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="w-full text-xs"
                onClick={() => onNavigate('reports')}
              >
                Generate Monthly Export
              </Button>
            </div>
          </Card>
        </div>
      </div>

      {/* Quick Issue Circulation Loan Modal */}
      <Modal
        isOpen={issueModalOpen}
        onClose={() => setIssueModalOpen(false)}
        title="Quick Issue Circulation Loan"
        description="Issue an available copy directly to a student ID"
      >
        <form onSubmit={handleQuickIssue} className="space-y-4">
          <Input
            label="Student / Member Database ID"
            placeholder="e.g. 1"
            value={issueMemberId}
            onChange={(e) => setIssueMemberId(e.target.value)}
            required
            helperText="Enter the member database ID (e.g., 1 for Harshit Sharma)"
          />
          <Input
            label="Book Catalog ID"
            placeholder="e.g. 1"
            value={issueBookId}
            onChange={(e) => setIssueBookId(e.target.value)}
            required
            helperText="Enter the catalog book ID"
          />

          {issueMessage && (
            <div
              className={`p-2.5 rounded-lg text-xs flex items-center gap-2 ${
                issueMessage.type === 'error'
                  ? 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400 border border-red-200 dark:border-red-800/40'
                  : 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800/40'
              }`}
            >
              {issueMessage.type === 'error' ? (
                <AlertCircle className="w-4 h-4 shrink-0" />
              ) : (
                <CheckCircle2 className="w-4 h-4 shrink-0" />
              )}
              <span>{issueMessage.text}</span>
            </div>
          )}

          <div className="flex justify-end gap-2 pt-2 border-t border-zinc-100 dark:border-zinc-800">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => setIssueModalOpen(false)}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              isLoading={issueSubmitting}
            >
              Confirm Loan
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
