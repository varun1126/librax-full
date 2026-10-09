import React, { useState, useEffect } from 'react';
import { Search } from 'lucide-react';
import { api } from '../../services/api';
import type { AuditLog, LoginHistory } from '../../types';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { DataTable, Column } from '../../components/ui/DataTable';

export const AuditPage: React.FC = () => {
  const [tab, setTab] = useState<'audit' | 'login'>('audit');
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loginHistory, setLoginHistory] = useState<LoginHistory[]>([]);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  const fetchAuditData = React.useCallback(async () => {
    setIsLoading(true);
    try {
      if (tab === 'audit') {
        const res = await api.audit.getLogs({
          search: search.trim() || undefined,
          page,
          size: 15,
        });
        setLogs(res.logs || []);
        setTotal(res.total || 0);
      } else {
        const data = await api.audit.getLoginHistory();
        setLoginHistory(data || []);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  }, [tab, page, search]);

  useEffect(() => {
    fetchAuditData();
  }, [fetchAuditData]);

  const auditColumns: Column<AuditLog>[] = [
    {
      header: 'Action & Entity',
      cell: (l) => (
        <div className="space-y-0.5">
          <div className="font-bold text-[var(--text-primary)] text-sm">{l.action}</div>
          <div className="text-xs text-[var(--text-secondary)]">
            Type: {l.entityType || 'SYSTEM'} {l.entityId ? `(#${l.entityId})` : ''}
          </div>
        </div>
      ),
    },
    {
      header: 'Details',
      cell: (l) => (
        <div className="text-xs text-[var(--text-secondary)] max-w-md truncate">
          {l.details || '—'}
        </div>
      ),
    },
    {
      header: 'Executed By',
      cell: (l) => (
        <span className="text-xs font-mono font-bold text-[var(--accent-primary)]">
          {l.username || 'System'}
        </span>
      ),
    },
    {
      header: 'IP Address',
      cell: (l) => (
        <span className="text-xs font-mono text-[var(--text-muted)]">
          {l.ipAddress || '127.0.0.1'}
        </span>
      ),
    },
    {
      header: 'Timestamp',
      cell: (l) => (
        <span className="text-xs font-mono text-[var(--text-secondary)]">
          {new Date(l.timestamp).toLocaleString()}
        </span>
      ),
    },
  ];

  const loginColumns: Column<LoginHistory>[] = [
    {
      header: 'Username',
      cell: (h) => (
        <span className="font-bold text-xs text-[var(--text-primary)] font-mono">
          @{h.username}
        </span>
      ),
    },
    {
      header: 'Login Result',
      cell: (h) => (
        <Badge variant={h.success ? 'success' : 'danger'} size="sm" dot>
          {h.success ? 'SUCCESS' : 'FAILED'}
        </Badge>
      ),
    },
    {
      header: 'Failure Reason',
      cell: (h) => (
        <span className="text-xs text-rose-400">
          {h.failureReason || '—'}
        </span>
      ),
    },
    {
      header: 'IP & Client Agent',
      cell: (h) => (
        <div className="text-xs text-[var(--text-secondary)] font-mono truncate max-w-xs">
          {h.ipAddress || '127.0.0.1'}
        </div>
      ),
    },
    {
      header: 'Login Timestamp',
      cell: (h) => (
        <span className="text-xs font-mono text-[var(--text-secondary)]">
          {new Date(h.loginTime).toLocaleString()}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-black text-[var(--text-primary)]">Audit Trail & Security Telemetry</h2>
          <p className="text-xs text-[var(--text-secondary)]">
            Cryptographically logged operations trail and authentication attempts
          </p>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="flex gap-1.5 p-1 bg-[var(--bg-secondary)] border border-[var(--border-color)] rounded-xl">
          <button
            onClick={() => {
              setTab('audit');
              setPage(1);
            }}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
              tab === 'audit'
                ? 'bg-[var(--accent-primary)] text-[var(--accent-text)] shadow'
                : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
            }`}
          >
            System Operations
          </button>
          <button
            onClick={() => {
              setTab('login');
              setPage(1);
            }}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
              tab === 'login'
                ? 'bg-[var(--accent-primary)] text-[var(--accent-text)] shadow'
                : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
            }`}
          >
            Authentication History
          </button>
        </div>

        {tab === 'audit' && (
          <div className="w-full sm:w-72">
            <Input
              placeholder="Search action or user..."
              leftIcon={<Search className="w-4 h-4" />}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchAuditData()}
            />
          </div>
        )}
      </div>

      {tab === 'audit' ? (
        <DataTable
          columns={auditColumns}
          data={logs}
          isLoading={isLoading}
          page={page}
          pageSize={15}
          total={total}
          onPageChange={setPage}
          emptyMessage="No audit log entries recorded."
        />
      ) : (
        <DataTable
          columns={loginColumns}
          data={loginHistory}
          isLoading={isLoading}
          emptyMessage="No login records found."
        />
      )}
    </div>
  );
};
