import React, { useState, useEffect } from 'react';
import { Download, BookOpen, Repeat, AlertTriangle, IndianRupee } from 'lucide-react';
import { api } from '../../services/api';
import { GlassCard } from '../../components/ui/GlassCard';
import { Button } from '../../components/ui/Button';

export const ReportsPage: React.FC = () => {
  const [inventoryReport, setInventoryReport] = useState<any>(null);
  const [loanReport, setLoanReport] = useState<any>(null);
  const [overdueReport, setOverdueReport] = useState<any>(null);
  const [finesReport, setFinesReport] = useState<any>(null);
  const [exporting, setExporting] = useState<string | null>(null);

  const fetchReports = React.useCallback(async () => {
    try {
      const [inv, ln, od, fn] = await Promise.all([
        api.reports.getInventory(),
        api.reports.getLoans(),
        api.reports.getOverdue(),
        api.reports.getFines(),
      ]);
      setInventoryReport(inv);
      setLoanReport(ln);
      setOverdueReport(od);
      setFinesReport(fn);
    } catch (e) {
      console.error(e);
    }
  }, []);

  useEffect(() => {
    fetchReports();
  }, [fetchReports]);

  const handleExport = async (type: string) => {
    setExporting(type);
    try {
      await api.reports.exportCsv(type);
    } catch (e: any) {
      alert(e.message || 'Export failed');
    } finally {
      setExporting(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-black text-[var(--text-primary)]">System Analytics & Data Export</h2>
          <p className="text-xs text-[var(--text-secondary)]">
            Aggregate institutional reporting and instant CSV exports
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Inventory Report Card */}
        <GlassCard>
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-3">
              <div className="p-3 rounded-xl bg-cyan-500/10 text-cyan-400">
                <BookOpen className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-[var(--text-primary)]">Collection Inventory</h3>
                <p className="text-[11px] text-[var(--text-muted)]">Books, titles and copy statistics</p>
              </div>
            </div>
            <Button
              variant="outline"
              size="sm"
              isLoading={exporting === 'inventory'}
              onClick={() => handleExport('inventory')}
              leftIcon={<Download className="w-3.5 h-3.5" />}
            >
              CSV
            </Button>
          </div>
          <div className="grid grid-cols-3 gap-2 mt-4 pt-4 border-t border-[var(--border-color)] text-xs">
            <div>
              <span className="text-[var(--text-muted)]">Total Titles:</span>
              <div className="text-lg font-bold text-[var(--text-primary)]">
                {inventoryReport?.totalTitles ?? '—'}
              </div>
            </div>
            <div>
              <span className="text-[var(--text-muted)]">Total Copies:</span>
              <div className="text-lg font-bold text-[var(--text-primary)]">
                {inventoryReport?.totalCopies ?? '—'}
              </div>
            </div>
            <div>
              <span className="text-[var(--text-muted)]">Available:</span>
              <div className="text-lg font-bold text-emerald-400">
                {inventoryReport?.availableCopies ?? '—'}
              </div>
            </div>
          </div>
        </GlassCard>

        {/* Loans Report Card */}
        <GlassCard>
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-3">
              <div className="p-3 rounded-xl bg-indigo-500/10 text-indigo-400">
                <Repeat className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-[var(--text-primary)]">Circulation Activity</h3>
                <p className="text-[11px] text-[var(--text-muted)]">Borrowing velocity and loan totals</p>
              </div>
            </div>
            <Button
              variant="outline"
              size="sm"
              isLoading={exporting === 'loans'}
              onClick={() => handleExport('loans')}
              leftIcon={<Download className="w-3.5 h-3.5" />}
            >
              CSV
            </Button>
          </div>
          <div className="grid grid-cols-3 gap-2 mt-4 pt-4 border-t border-[var(--border-color)] text-xs">
            <div>
              <span className="text-[var(--text-muted)]">Active Loans:</span>
              <div className="text-lg font-bold text-[var(--text-primary)]">
                {loanReport?.activeLoans ?? '—'}
              </div>
            </div>
            <div>
              <span className="text-[var(--text-muted)]">Total Issued:</span>
              <div className="text-lg font-bold text-[var(--text-primary)]">
                {loanReport?.totalIssued ?? '—'}
              </div>
            </div>
            <div>
              <span className="text-[var(--text-muted)]">Returned:</span>
              <div className="text-lg font-bold text-cyan-400">
                {loanReport?.returnedLoans ?? '—'}
              </div>
            </div>
          </div>
        </GlassCard>

        {/* Overdue Report Card */}
        <GlassCard>
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-3">
              <div className="p-3 rounded-xl bg-rose-500/10 text-rose-400">
                <AlertTriangle className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-[var(--text-primary)]">Overdue Delinquencies</h3>
                <p className="text-[11px] text-[var(--text-muted)]">Patrons past expected return dates</p>
              </div>
            </div>
            <Button
              variant="outline"
              size="sm"
              isLoading={exporting === 'overdue'}
              onClick={() => handleExport('overdue')}
              leftIcon={<Download className="w-3.5 h-3.5" />}
            >
              CSV
            </Button>
          </div>
          <div className="grid grid-cols-2 gap-2 mt-4 pt-4 border-t border-[var(--border-color)] text-xs">
            <div>
              <span className="text-[var(--text-muted)]">Overdue Items:</span>
              <div className="text-lg font-bold text-rose-400">
                {overdueReport?.overdueCount ?? '—'}
              </div>
            </div>
            <div>
              <span className="text-[var(--text-muted)]">Estimated Fines:</span>
              <div className="text-lg font-bold text-amber-400">
                ₹{(overdueReport?.estimatedFines ?? 0).toFixed(2)}
              </div>
            </div>
          </div>
        </GlassCard>

        {/* Fines Collection Card */}
        <GlassCard>
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-3">
              <div className="p-3 rounded-xl bg-amber-500/10 text-amber-400">
                <IndianRupee className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-[var(--text-primary)]">Finance & Cashier</h3>
                <p className="text-[11px] text-[var(--text-muted)]">Penalties assessed, settled and waived</p>
              </div>
            </div>
            <Button
              variant="outline"
              size="sm"
              isLoading={exporting === 'fines'}
              onClick={() => handleExport('fines')}
              leftIcon={<Download className="w-3.5 h-3.5" />}
            >
              CSV
            </Button>
          </div>
          <div className="grid grid-cols-3 gap-2 mt-4 pt-4 border-t border-[var(--border-color)] text-xs">
            <div>
              <span className="text-[var(--text-muted)]">Collected:</span>
              <div className="text-lg font-bold text-emerald-400">
                ₹{(finesReport?.collectedAmount ?? 0).toFixed(2)}
              </div>
            </div>
            <div>
              <span className="text-[var(--text-muted)]">Pending:</span>
              <div className="text-lg font-bold text-rose-400">
                ₹{(finesReport?.pendingAmount ?? 0).toFixed(2)}
              </div>
            </div>
            <div>
              <span className="text-[var(--text-muted)]">Waived:</span>
              <div className="text-lg font-bold text-slate-400">
                ₹{(finesReport?.waivedAmount ?? 0).toFixed(2)}
              </div>
            </div>
          </div>
        </GlassCard>
      </div>
    </div>
  );
};
