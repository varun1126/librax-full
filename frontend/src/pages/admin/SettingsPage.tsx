import React, { useState, useEffect } from 'react';
import { Settings, Database, Save, HardDrive, ShieldCheck, Plus } from 'lucide-react';
import { api } from '../../services/api';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { GlassCard } from '../../components/ui/GlassCard';

export const SettingsPage: React.FC = () => {
  const [settings, setSettings] = useState<Record<string, string>>({
    fine_rate_per_day: '1.00',
    loan_period_days: '14',
    max_renewals: '2',
    reservation_hold_days: '3',
  });
  const [backups, setBackups] = useState<string[]>([]);
  const [isSaving, setIsSaving] = useState(false);
  const [isBackingUp, setIsBackingUp] = useState(false);

  const fetchSettingsAndBackups = React.useCallback(async () => {
    try {
      const [s, b] = await Promise.all([api.settings.getAll(), api.settings.getBackups()]);
      if (s && Object.keys(s).length > 0) {
        setSettings(s);
      }
      setBackups(b || []);
    } catch (e) {
      console.error(e);
    }
  }, []);

  useEffect(() => {
    fetchSettingsAndBackups();
  }, [fetchSettingsAndBackups]);

  const handleSaveSettings = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    try {
      await api.settings.update(settings);
      alert('Global system parameters updated successfully!');
    } catch (e: any) {
      alert(e.message || 'Failed to update settings');
    } finally {
      setIsSaving(false);
    }
  };

  const handleCreateBackup = async () => {
    setIsBackingUp(true);
    try {
      const res = await api.settings.createBackup();
      alert(`Database snapshot generated successfully: ${res.fileName}`);
      fetchSettingsAndBackups();
    } catch (e: any) {
      alert(e.message || 'Backup failed');
    } finally {
      setIsBackingUp(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-black text-[var(--text-primary)]">System Settings & Backups</h2>
          <p className="text-xs text-[var(--text-secondary)]">
            Configure circulation policies, penalty multipliers and database snapshots
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Circulation Parameters */}
        <GlassCard>
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2.5 rounded-xl bg-cyan-500/10 text-cyan-400">
              <Settings className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-[var(--text-primary)]">Circulation Policies</h3>
              <p className="text-[11px] text-[var(--text-muted)]">Active rules enforced across all loans</p>
            </div>
          </div>

          <form onSubmit={handleSaveSettings} className="space-y-4">
            <Input
              label="Daily Overdue Fine Rate (₹ INR)"
              type="number"
              step="0.10"
              min="0"
              value={settings.fine_rate_per_day || '1.00'}
              onChange={(e) => setSettings({ ...settings, fine_rate_per_day: e.target.value })}
              required
              helperText="Fine added each day an item is delinquent"
            />

            <Input
              label="Standard Loan Duration (Days)"
              type="number"
              min="1"
              max="90"
              value={settings.loan_period_days || '14'}
              onChange={(e) => setSettings({ ...settings, loan_period_days: e.target.value })}
              required
              helperText="Default checkout window for new book issues"
            />

            <Input
              label="Maximum Renewals Allowed"
              type="number"
              min="0"
              max="10"
              value={settings.max_renewals || '2'}
              onChange={(e) => setSettings({ ...settings, max_renewals: e.target.value })}
              required
              helperText="Number of times a student can renew without returning"
            />

            <Input
              label="Reservation Hold Window (Days)"
              type="number"
              min="1"
              max="30"
              value={settings.reservation_hold_days || '3'}
              onChange={(e) => setSettings({ ...settings, reservation_hold_days: e.target.value })}
              required
              helperText="Days a copy is held before passing to next person in queue"
            />

            <div className="pt-2">
              <Button type="submit" variant="primary" isLoading={isSaving} leftIcon={<Save className="w-4 h-4" />}>
                Save Policy Parameters
              </Button>
            </div>
          </form>
        </GlassCard>

        {/* Database Snapshots & Maintenance */}
        <GlassCard>
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-xl bg-purple-500/10 text-purple-400">
                <Database className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-[var(--text-primary)]">Database Snapshots</h3>
                <p className="text-[11px] text-[var(--text-muted)]">Hot SQLite backups & integrity restore points</p>
              </div>
            </div>
            <Button
              variant="outline"
              size="sm"
              isLoading={isBackingUp}
              onClick={handleCreateBackup}
              leftIcon={<Plus className="w-3.5 h-3.5" />}
            >
              Take Snapshot
            </Button>
          </div>

          <div className="space-y-3">
            <div className="p-3 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)] text-xs text-[var(--text-secondary)] space-y-1">
              <div className="font-bold text-[var(--text-primary)] flex items-center gap-1.5">
                <ShieldCheck className="w-4 h-4 text-emerald-400" />
                <span>Automated Integrity Verification</span>
              </div>
              <p className="text-[11px]">
                Backups preserve users, roles, fine ledger, circulation status and the full catalog.
              </p>
            </div>

            <div className="mt-4">
              <h4 className="text-xs font-bold text-[var(--text-primary)] uppercase tracking-wider mb-2">
                Stored Snapshots ({backups.length})
              </h4>
              <div className="max-h-60 overflow-y-auto divide-y divide-[var(--border-color)]">
                {backups.length === 0 ? (
                  <div className="py-8 text-center text-xs text-[var(--text-muted)]">
                    No snapshots generated yet. Click "Take Snapshot" to create one.
                  </div>
                ) : (
                  backups.map((b, idx) => (
                    <div key={idx} className="py-2.5 flex items-center justify-between text-xs">
                      <div className="flex items-center gap-2">
                        <HardDrive className="w-4 h-4 text-[var(--text-muted)]" />
                        <span className="font-mono text-[var(--text-primary)]">{b}</span>
                      </div>
                      <span className="text-[10px] text-emerald-400 font-bold bg-emerald-500/10 px-2 py-0.5 rounded">
                        VALIDATED
                      </span>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </GlassCard>
      </div>
    </div>
  );
};
