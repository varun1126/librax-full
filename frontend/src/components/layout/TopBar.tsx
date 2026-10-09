import React, { useState, useEffect, useRef } from 'react';
import { Search, Bell, CheckCheck, AlertCircle, Info, CheckCircle2 } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { ThemeSwitcher } from '../ui/ThemeSwitcher';
import { api } from '../../services/api';
import type { Notification } from '../../types';
import { clsx } from 'clsx';

export interface TopBarProps {
  currentView: string;
  onOpenCommandPalette: () => void;
}

export const TopBar: React.FC<TopBarProps> = ({ currentView, onOpenCommandPalette }) => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [showNotifications, setShowNotifications] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const fetchNotifications = React.useCallback(async () => {
    try {
      const data = await api.notifications.getMyNotifications();
      setNotifications(Array.isArray(data) ? data : []);
    } catch {
      setNotifications([]);
    }
  }, []);

  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, [fetchNotifications]);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setShowNotifications(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const unreadCount = Array.isArray(notifications)
    ? notifications.filter((n) => !n.isRead).length
    : 0;

  const handleMarkAllRead = async () => {
    try {
      await api.notifications.markAllRead();
      setNotifications((prev) => (Array.isArray(prev) ? prev.map((n) => ({ ...n, isRead: true })) : []));
    } catch (e) {
      console.error(e);
    }
  };

  const handleMarkOneRead = async (id: number) => {
    try {
      await api.notifications.markRead(id);
      setNotifications((prev) => (Array.isArray(prev) ? prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)) : []));
    } catch (e) {
      console.error(e);
    }
  };

  const viewTitles: Record<string, { section: string; title: string }> = {
    'admin-dashboard': { section: 'Overview', title: 'Dashboard' },
    books: { section: 'Collection', title: 'Books & Inventory' },
    students: { section: 'Patrons', title: 'Student Directory' },
    loans: { section: 'Circulation', title: 'Loans & Returns' },
    reservations: { section: 'Circulation', title: 'Reservations Queue' },
    fines: { section: 'Finance', title: 'Fines & Cashier' },
    reports: { section: 'Management', title: 'Analytics & Reports' },
    rbac: { section: 'Security', title: 'Role-Based Access Control' },
    users: { section: 'Security', title: 'Staff Accounts' },
    audit: { section: 'Security', title: 'Audit Trail' },
    settings: { section: 'System', title: 'Settings & Backups' },
    'student-dashboard': { section: 'Academic Hub', title: 'My Dashboard' },
    'student-catalog': { section: 'Catalog', title: 'Browse Books' },
    'student-loans': { section: 'Circulation', title: 'My Borrowed Books' },
    'student-reservations': { section: 'Circulation', title: 'My Book Holds' },
    'student-fines': { section: 'Finance', title: 'My Fines & Dues' },
    'student-profile': { section: 'Account', title: 'Library Card' },
  };

  const breadcrumb = viewTitles[currentView] || { section: 'Library', title: 'Management' };

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'DUE_REMINDER':
      case 'OVERDUE_ALERT':
        return <AlertCircle className="w-4 h-4 text-amber-500 shrink-0" />;
      case 'RESERVATION_READY':
      case 'SUCCESS':
        return <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />;
      default:
        return <Info className="w-4 h-4 text-zinc-500 shrink-0" />;
    }
  };

  return (
    <header className="h-14 border-b border-zinc-200 dark:border-zinc-800 bg-white/90 dark:bg-zinc-950/90 backdrop-blur-md px-4 sm:px-6 flex items-center justify-between sticky top-0 z-20">
      {/* Breadcrumb Title */}
      <div className="flex items-center gap-2 text-xs">
        <span className="text-zinc-400 dark:text-zinc-500 font-medium">
          {breadcrumb.section}
        </span>
        <span className="text-zinc-300 dark:text-zinc-700">/</span>
        <span className="font-semibold text-zinc-900 dark:text-zinc-100">
          {breadcrumb.title}
        </span>
      </div>

      {/* Actions */}
      <div className="flex items-center gap-2.5">
        {/* Command Palette Trigger */}
        <button
          onClick={onOpenCommandPalette}
          className="hidden sm:flex items-center gap-2.5 px-2.5 py-1.5 rounded-lg border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900 hover:bg-zinc-100 dark:hover:bg-zinc-800/80 text-xs text-zinc-500 dark:text-zinc-400 transition-colors cursor-pointer"
        >
          <Search className="w-3.5 h-3.5" />
          <span>Quick actions & search</span>
          <kbd className="font-mono text-[10px] bg-white dark:bg-zinc-800 border border-zinc-200 dark:border-zinc-700 px-1 py-0.5 rounded text-zinc-500 dark:text-zinc-400">
            Ctrl K
          </kbd>
        </button>

        {/* Theme Switcher */}
        <ThemeSwitcher />

        {/* Notifications Dropdown */}
        <div className="relative" ref={dropdownRef}>
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            className="p-2 rounded-lg border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-600 hover:text-zinc-900 dark:text-zinc-400 dark:hover:text-zinc-100 transition-colors cursor-pointer relative"
            title="Notifications"
            aria-label="Notifications"
          >
            <Bell className="w-4 h-4" />
            {unreadCount > 0 && (
              <span className="absolute top-1 right-1 w-2 h-2 rounded-full bg-red-500" />
            )}
          </button>

          {showNotifications && (
            <div className="absolute right-0 mt-2 w-80 sm:w-96 rounded-xl bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 shadow-xl z-50 overflow-hidden animate-in fade-in duration-100">
              <div className="p-3 border-b border-zinc-100 dark:border-zinc-800/80 flex items-center justify-between bg-zinc-50/50 dark:bg-zinc-900">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold text-zinc-900 dark:text-zinc-100">
                    Notifications
                  </span>
                  {unreadCount > 0 && (
                    <span className="text-[10px] bg-zinc-100 dark:bg-zinc-800 text-zinc-700 dark:text-zinc-300 px-1.5 py-0.2 rounded-full font-medium">
                      {unreadCount} unread
                    </span>
                  )}
                </div>
                {unreadCount > 0 && (
                  <button
                    onClick={handleMarkAllRead}
                    className="flex items-center gap-1 text-[11px] text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 cursor-pointer"
                  >
                    <CheckCheck className="w-3.5 h-3.5" />
                    <span>Mark all read</span>
                  </button>
                )}
              </div>

              <div className="max-h-80 overflow-y-auto divide-y divide-zinc-100 dark:divide-zinc-800/80">
                {!Array.isArray(notifications) || notifications.length === 0 ? (
                  <div className="py-8 text-center text-xs text-zinc-400 dark:text-zinc-500">
                    No notifications
                  </div>
                ) : (
                  notifications.map((n) => (
                    <div
                      key={n.id}
                      onClick={() => !n.isRead && handleMarkOneRead(n.id)}
                      className={clsx(
                        'p-3 flex items-start gap-2.5 transition-colors cursor-pointer text-left',
                        n.isRead
                          ? 'opacity-60 hover:opacity-100 hover:bg-zinc-50 dark:hover:bg-zinc-800/40'
                          : 'bg-zinc-50/80 dark:bg-zinc-800/30 hover:bg-zinc-100/80 dark:hover:bg-zinc-800/50'
                      )}
                    >
                      <div className="mt-0.5">{getNotificationIcon(n.type)}</div>
                      <div className="flex-1 space-y-0.5">
                        <div className="flex items-center justify-between">
                          <p className="text-xs font-medium text-zinc-900 dark:text-zinc-100">
                            {n.title}
                          </p>
                          <span className="text-[10px] text-zinc-400 dark:text-zinc-500 font-mono">
                            {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </span>
                        </div>
                        <p className="text-[11px] text-zinc-500 dark:text-zinc-400 leading-relaxed">
                          {n.message}
                        </p>
                      </div>
                      {!n.isRead && (
                        <span className="w-1.5 h-1.5 rounded-full bg-zinc-900 dark:bg-zinc-100 shrink-0 mt-1.5" />
                      )}
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        {/* User Mini Avatar */}
        <div className="flex items-center gap-2 pl-2 border-l border-zinc-200 dark:border-zinc-800">
          <div className="w-7 h-7 rounded-md bg-zinc-900 dark:bg-zinc-100 text-zinc-100 dark:text-zinc-900 flex items-center justify-center font-semibold text-xs">
            {user?.fullName?.charAt(0) || user?.username?.charAt(0) || 'U'}
          </div>
          <div className="hidden sm:block text-left">
            <div className="text-xs font-medium text-zinc-900 dark:text-zinc-100 leading-tight">
              {user?.fullName || user?.username}
            </div>
            <div className="text-[10px] text-zinc-400 dark:text-zinc-500 leading-tight">
              {user?.role}
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};
