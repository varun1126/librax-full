import React, { useState, useEffect, useRef } from 'react';
import {
  Search,
  BookOpen,
  Users,
  Repeat,
  BookmarkCheck,
  Receipt,
  FileBarChart,
  ShieldAlert,
  Settings,
  PlusCircle,
  Sun,
  Moon,
  LogOut,
  ArrowRight,
  Database,
  Library,
  ShieldCheck,
  UserCheck,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { clsx } from 'clsx';

interface CommandItem {
  id: string;
  title: string;
  subtitle?: string;
  category: 'Navigation' | 'Actions' | 'Preferences' | 'Account';
  icon: React.ReactNode;
  onSelect: () => void;
  roles?: string[];
}

export interface CommandPaletteProps {
  isOpen: boolean;
  onClose: () => void;
  onNavigate: (page: string) => void;
}

export const CommandPalette: React.FC<CommandPaletteProps> = ({ isOpen, onClose, onNavigate }) => {
  const { user, logout } = useAuth();
  const { setTheme } = useTheme();
  const [query, setQuery] = useState('');
  const [selectedIndex, setSelectedIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (isOpen) {
      setTimeout(() => inputRef.current?.focus(), 50);
      setSelectedIndex(0);
      setQuery('');
    }
  }, [isOpen]);

  const items: CommandItem[] = [
    // Navigation (Admin/Staff)
    {
      id: 'nav-admin-dash',
      title: 'Admin Dashboard',
      subtitle: 'Overview, KPIs & circulation telemetry',
      category: 'Navigation',
      icon: <FileBarChart className="w-4 h-4 text-zinc-500" />,
      roles: ['SUPER_ADMIN', 'ADMIN', 'LIBRARIAN'],
      onSelect: () => onNavigate('admin-dashboard'),
    },
    {
      id: 'nav-books',
      title: 'Books & Inventory',
      subtitle: 'Catalog, physical copies & Google Books import',
      category: 'Navigation',
      icon: <BookOpen className="w-4 h-4 text-zinc-500" />,
      roles: ['SUPER_ADMIN', 'ADMIN', 'LIBRARIAN'],
      onSelect: () => onNavigate('books'),
    },
    {
      id: 'nav-loans',
      title: 'Circulation & Loans',
      subtitle: 'Issue, return, renew and overdue alerts',
      category: 'Navigation',
      icon: <Repeat className="w-4 h-4 text-zinc-500" />,
      roles: ['SUPER_ADMIN', 'ADMIN', 'LIBRARIAN'],
      onSelect: () => onNavigate('loans'),
    },
    {
      id: 'nav-reservations',
      title: 'Reservations Queue',
      subtitle: 'Pending book holds and fulfillment',
      category: 'Navigation',
      icon: <BookmarkCheck className="w-4 h-4 text-zinc-500" />,
      roles: ['SUPER_ADMIN', 'ADMIN', 'LIBRARIAN'],
      onSelect: () => onNavigate('reservations'),
    },
    {
      id: 'nav-students',
      title: 'Student Directory',
      subtitle: 'Registered patron accounts & borrowing limits',
      category: 'Navigation',
      icon: <Users className="w-4 h-4 text-zinc-500" />,
      roles: ['SUPER_ADMIN', 'ADMIN', 'LIBRARIAN'],
      onSelect: () => onNavigate('students'),
    },
    {
      id: 'nav-fines',
      title: 'Fines & Cashier',
      subtitle: 'Outstanding fees, cash collection & waivers',
      category: 'Navigation',
      icon: <Receipt className="w-4 h-4 text-zinc-500" />,
      roles: ['SUPER_ADMIN', 'ADMIN', 'LIBRARIAN'],
      onSelect: () => onNavigate('fines'),
    },
    {
      id: 'nav-reports',
      title: 'Analytics & Reports',
      subtitle: 'CSV exports and inventory breakdown',
      category: 'Navigation',
      icon: <FileBarChart className="w-4 h-4 text-zinc-500" />,
      roles: ['SUPER_ADMIN', 'ADMIN'],
      onSelect: () => onNavigate('reports'),
    },
    {
      id: 'nav-audit',
      title: 'Security Audit Trail',
      subtitle: 'Immutable system operations log',
      category: 'Navigation',
      icon: <ShieldCheck className="w-4 h-4 text-zinc-500" />,
      roles: ['SUPER_ADMIN', 'ADMIN'],
      onSelect: () => onNavigate('audit'),
    },
    {
      id: 'nav-users',
      title: 'Staff Accounts',
      subtitle: 'Manage administrative users and permissions',
      category: 'Navigation',
      icon: <UserCheck className="w-4 h-4 text-zinc-500" />,
      roles: ['SUPER_ADMIN'],
      onSelect: () => onNavigate('users'),
    },
    {
      id: 'nav-rbac',
      title: 'RBAC Permission Matrix',
      subtitle: 'Configure role permissions',
      category: 'Navigation',
      icon: <ShieldAlert className="w-4 h-4 text-zinc-500" />,
      roles: ['SUPER_ADMIN'],
      onSelect: () => onNavigate('rbac'),
    },
    {
      id: 'nav-settings',
      title: 'System Settings',
      subtitle: 'Fine rates, loan duration & database backups',
      category: 'Navigation',
      icon: <Settings className="w-4 h-4 text-zinc-500" />,
      roles: ['SUPER_ADMIN', 'ADMIN'],
      onSelect: () => onNavigate('settings'),
    },

    // Student Navigation
    {
      id: 'nav-stu-dash',
      title: 'Student Dashboard',
      subtitle: 'Personal reading stats and due notices',
      category: 'Navigation',
      icon: <BookOpen className="w-4 h-4 text-zinc-500" />,
      roles: ['STUDENT'],
      onSelect: () => onNavigate('student-dashboard'),
    },
    {
      id: 'nav-stu-catalog',
      title: 'Browse Catalog',
      subtitle: 'Search available titles and place holds',
      category: 'Navigation',
      icon: <Search className="w-4 h-4 text-zinc-500" />,
      roles: ['STUDENT'],
      onSelect: () => onNavigate('student-catalog'),
    },
    {
      id: 'nav-stu-loans',
      title: 'My Borrowed Books',
      subtitle: 'Active loans and return due dates',
      category: 'Navigation',
      icon: <Repeat className="w-4 h-4 text-zinc-500" />,
      roles: ['STUDENT'],
      onSelect: () => onNavigate('student-loans'),
    },
    {
      id: 'nav-stu-fines',
      title: 'My Fines & Dues',
      subtitle: 'Pending library dues balance',
      category: 'Navigation',
      icon: <Receipt className="w-4 h-4 text-zinc-500" />,
      roles: ['STUDENT'],
      onSelect: () => onNavigate('student-fines'),
    },
    {
      id: 'nav-stu-profile',
      title: 'Library Member Card',
      subtitle: 'View barcode and account limits',
      category: 'Navigation',
      icon: <Library className="w-4 h-4 text-zinc-500" />,
      roles: ['STUDENT'],
      onSelect: () => onNavigate('student-profile'),
    },

    // Quick Actions
    {
      id: 'act-issue',
      title: 'Issue Book to Student',
      subtitle: 'Open circulation checkout modal',
      category: 'Actions',
      icon: <PlusCircle className="w-4 h-4 text-zinc-500" />,
      roles: ['SUPER_ADMIN', 'ADMIN', 'LIBRARIAN'],
      onSelect: () => onNavigate('loans'),
    },
    {
      id: 'act-backup',
      title: 'Generate Immediate Database Backup',
      subtitle: 'Create a SQL snapshot of SQLite database',
      category: 'Actions',
      icon: <Database className="w-4 h-4 text-zinc-500" />,
      roles: ['SUPER_ADMIN'],
      onSelect: () => onNavigate('settings'),
    },

    // Preferences / Themes
    {
      id: 'pref-light',
      title: 'Switch to Light Mode',
      subtitle: 'Clean alabaster & neutral gray palette',
      category: 'Preferences',
      icon: <Sun className="w-4 h-4 text-amber-500" />,
      onSelect: () => setTheme('light'),
    },
    {
      id: 'pref-dark',
      title: 'Switch to Dark Mode',
      subtitle: 'Subtle charcoal ink & zinc palette',
      category: 'Preferences',
      icon: <Moon className="w-4 h-4 text-zinc-400" />,
      onSelect: () => setTheme('dark'),
    },

    // Account
    {
      id: 'acc-logout',
      title: 'Log Out',
      subtitle: `Sign out (${user?.username || 'Current User'})`,
      category: 'Account',
      icon: <LogOut className="w-4 h-4 text-red-500" />,
      onSelect: () => {
        logout();
        onClose();
      },
    },
  ];

  const userRole = user?.role || '';
  const filtered = items.filter((item) => {
    if (item.roles && !item.roles.includes(userRole)) return false;
    if (!query.trim()) return true;
    const q = query.toLowerCase();
    return (
      item.title.toLowerCase().includes(q) ||
      (item.subtitle && item.subtitle.toLowerCase().includes(q)) ||
      item.category.toLowerCase().includes(q)
    );
  });

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (!isOpen) return;

      if (e.key === 'ArrowDown') {
        e.preventDefault();
        setSelectedIndex((prev) => (prev < filtered.length - 1 ? prev + 1 : 0));
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        setSelectedIndex((prev) => (prev > 0 ? prev - 1 : filtered.length - 1));
      } else if (e.key === 'Enter') {
        e.preventDefault();
        if (filtered[selectedIndex]) {
          filtered[selectedIndex].onSelect();
          onClose();
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, filtered, selectedIndex, onClose]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center pt-20 px-4">
      {/* Backdrop */}
      <div
        onClick={onClose}
        className="fixed inset-0 bg-zinc-950/60 backdrop-blur-xs transition-opacity duration-150"
      />

      {/* Dialog */}
      <div
        className="relative w-full max-w-xl rounded-xl bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 shadow-2xl z-10 overflow-hidden transition-all duration-150"
      >
            {/* Search Input Bar */}
            <div className="flex items-center px-3.5 py-3 border-b border-zinc-200 dark:border-zinc-800 gap-2.5">
              <Search className="w-4 h-4 text-zinc-400 dark:text-zinc-500 shrink-0" />
              <input
                ref={inputRef}
                value={query}
                onChange={(e) => {
                  setQuery(e.target.value);
                  setSelectedIndex(0);
                }}
                placeholder="Type a command or search..."
                className="w-full bg-transparent text-zinc-900 dark:text-zinc-100 placeholder-zinc-400 dark:placeholder-zinc-500 text-sm outline-none"
              />
              <span className="text-[10px] font-mono bg-zinc-100 dark:bg-zinc-800 border border-zinc-200 dark:border-zinc-700 px-1.5 py-0.5 rounded text-zinc-500 dark:text-zinc-400 shrink-0">
                ESC
              </span>
            </div>

            {/* Results List */}
            <div className="max-h-80 overflow-y-auto p-1.5">
              {filtered.length === 0 ? (
                <div className="py-8 text-center text-xs text-zinc-400 dark:text-zinc-500">
                  No matching commands found.
                </div>
              ) : (
                filtered.map((item, index) => {
                  const isSelected = index === selectedIndex;
                  return (
                    <div
                      key={item.id}
                      onMouseEnter={() => setSelectedIndex(index)}
                      onClick={() => {
                        item.onSelect();
                        onClose();
                      }}
                      className={clsx(
                        'flex items-center justify-between px-3 py-2 rounded-lg cursor-pointer transition-colors',
                        isSelected
                          ? 'bg-zinc-100 dark:bg-zinc-800/80 text-zinc-900 dark:text-zinc-100'
                          : 'text-zinc-700 dark:text-zinc-300 hover:bg-zinc-50 dark:hover:bg-zinc-800/40'
                      )}
                    >
                      <div className="flex items-center gap-2.5 min-w-0">
                        <span className="shrink-0">{item.icon}</span>
                        <div className="min-w-0">
                          <div className="text-xs font-medium truncate">
                            {item.title}
                          </div>
                          {item.subtitle && (
                            <div className="text-[11px] text-zinc-400 dark:text-zinc-500 truncate">
                              {item.subtitle}
                            </div>
                          )}
                        </div>
                      </div>

                      <div className="flex items-center gap-2 shrink-0">
                        <span className="text-[10px] font-medium text-zinc-400 dark:text-zinc-500 px-1.5 py-0.5 rounded bg-zinc-100 dark:bg-zinc-800">
                          {item.category}
                        </span>
                        {isSelected && <ArrowRight className="w-3.5 h-3.5 text-zinc-500" />}
                      </div>
                    </div>
                  );
                })
              )}
            </div>

            {/* Footer */}
            <div className="px-3.5 py-2 border-t border-zinc-100 dark:border-zinc-800/80 bg-zinc-50/50 dark:bg-zinc-900/60 flex items-center justify-between text-[11px] text-zinc-400 dark:text-zinc-500">
              <span className="flex items-center gap-2">
                <span>↑↓ Navigate</span>
                <span>↵ Select</span>
              </span>
              <span>LibraX Command</span>
            </div>
          </div>
        </div>
  );
};
