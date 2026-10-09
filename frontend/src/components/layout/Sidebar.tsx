import React from 'react';
import {
  LayoutDashboard,
  BookOpen,
  Users,
  Repeat,
  BookmarkCheck,
  Receipt,
  FileBarChart,
  ShieldAlert,
  Settings,
  ShieldCheck,
  UserCheck,
  LogOut,
  ChevronLeft,
  ChevronRight,
  Library,
  CreditCard,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { clsx } from 'clsx';
import { LibraXLogo } from '../ui/LibraXLogo';

export interface SidebarProps {
  currentView: string;
  onNavigate: (view: string) => void;
  collapsed: boolean;
  setCollapsed: (collapsed: boolean) => void;
}

interface NavSection {
  title?: string;
  items: {
    id: string;
    label: string;
    icon: React.ReactNode;
    superAdminOnly?: boolean;
    badge?: string;
  }[];
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentView,
  onNavigate,
  collapsed,
  setCollapsed,
}) => {
  const { user, isAdmin, logout } = useAuth();

  const adminSections: NavSection[] = [
    {
      title: 'Overview',
      items: [
        {
          id: 'admin-dashboard',
          label: 'Dashboard',
          icon: <LayoutDashboard className="w-4 h-4" />,
        },
      ],
    },
    {
      title: 'Collection & Circulation',
      items: [
        {
          id: 'books',
          label: 'Books Catalog',
          icon: <BookOpen className="w-4 h-4" />,
        },
        {
          id: 'loans',
          label: 'Circulation & Loans',
          icon: <Repeat className="w-4 h-4" />,
        },
        {
          id: 'reservations',
          label: 'Reservations',
          icon: <BookmarkCheck className="w-4 h-4" />,
        },
      ],
    },
    {
      title: 'Patrons & Finance',
      items: [
        {
          id: 'students',
          label: 'Student Directory',
          icon: <Users className="w-4 h-4" />,
        },
        {
          id: 'fines',
          label: 'Fines & Cashier',
          icon: <Receipt className="w-4 h-4" />,
        },
      ],
    },
    {
      title: 'Management',
      items: [
        {
          id: 'reports',
          label: 'Analytics & Reports',
          icon: <FileBarChart className="w-4 h-4" />,
        },
        {
          id: 'audit',
          label: 'Security & Audit',
          icon: <ShieldCheck className="w-4 h-4" />,
        },
        {
          id: 'users',
          label: 'Staff Accounts',
          icon: <UserCheck className="w-4 h-4" />,
          superAdminOnly: true,
        },
        {
          id: 'rbac',
          label: 'Role Permissions',
          icon: <ShieldAlert className="w-4 h-4" />,
          superAdminOnly: true,
        },
        {
          id: 'settings',
          label: 'System Settings',
          icon: <Settings className="w-4 h-4" />,
        },
      ],
    },
  ];

  const studentSections: NavSection[] = [
    {
      title: 'Academic Hub',
      items: [
        {
          id: 'student-dashboard',
          label: 'Dashboard',
          icon: <LayoutDashboard className="w-4 h-4" />,
        },
        {
          id: 'student-catalog',
          label: 'Browse Catalog',
          icon: <BookOpen className="w-4 h-4" />,
        },
      ],
    },
    {
      title: 'Circulation',
      items: [
        {
          id: 'student-loans',
          label: 'My Borrowed Books',
          icon: <Repeat className="w-4 h-4" />,
        },
        {
          id: 'student-reservations',
          label: 'My Book Holds',
          icon: <BookmarkCheck className="w-4 h-4" />,
        },
        {
          id: 'student-fines',
          label: 'Fines & Dues',
          icon: <CreditCard className="w-4 h-4" />,
        },
      ],
    },
    {
      title: 'Member Account',
      items: [
        {
          id: 'student-profile',
          label: 'Library Card',
          icon: <Library className="w-4 h-4" />,
        },
      ],
    },
  ];

  const sections = isAdmin ? adminSections : studentSections;

  return (
    <aside
      className={clsx(
        'h-screen sticky top-0 flex flex-col justify-between border-r border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 transition-all duration-200 z-30 select-none shrink-0',
        collapsed ? 'w-16' : 'w-64'
      )}
    >
      {/* Top Branding */}
      <div>
        <div className="h-14 flex items-center justify-between px-3.5 border-b border-zinc-100 dark:border-zinc-800/80">
          <div
            className="flex items-center gap-2.5 overflow-hidden cursor-pointer"
            onClick={() => onNavigate(isAdmin ? 'admin-dashboard' : 'student-dashboard')}
          >
            <LibraXLogo size={32} />
            {!collapsed && (
              <div className="flex flex-col">
                <div className="flex items-center gap-1.5">
                  <span className="font-semibold text-sm tracking-tight text-zinc-900 dark:text-zinc-100">
                    LibraX
                  </span>
                  <span className="text-[10px] font-medium px-1.5 py-0.2 rounded bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400">
                    {isAdmin ? 'ADMIN' : 'STUDENT'}
                  </span>
                </div>
              </div>
            )}
          </div>
          <button
            onClick={() => setCollapsed(!collapsed)}
            className="p-1 rounded-md text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors cursor-pointer"
            title={collapsed ? 'Expand Sidebar' : 'Collapse Sidebar'}
            aria-label={collapsed ? 'Expand Sidebar' : 'Collapse Sidebar'}
          >
            {collapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
          </button>
        </div>

        {/* Navigation Categories */}
        <div className="py-3 px-2 overflow-y-auto max-h-[calc(100vh-8rem)] space-y-4">
          {sections.map((section, sIdx) => {
            const visibleItems = section.items.filter(
              (item) => !item.superAdminOnly || user?.role === 'SUPER_ADMIN'
            );

            if (visibleItems.length === 0) return null;

            return (
              <div key={sIdx} className="space-y-0.5">
                {!collapsed && section.title && (
                  <div className="px-2.5 py-1 text-[10px] font-semibold text-zinc-400 dark:text-zinc-500 uppercase tracking-wider">
                    {section.title}
                  </div>
                )}
                {visibleItems.map((item) => {
                  const isActive = currentView === item.id;
                  return (
                    <button
                      key={item.id}
                      onClick={() => onNavigate(item.id)}
                      title={collapsed ? item.label : undefined}
                      className={clsx(
                        'w-full flex items-center gap-2.5 px-2.5 py-1.5 rounded-lg text-xs font-medium transition-colors cursor-pointer text-left',
                        isActive
                          ? 'bg-zinc-900 text-zinc-50 dark:bg-zinc-100 dark:text-zinc-900 shadow-xs'
                          : 'text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 hover:bg-zinc-100 dark:hover:bg-zinc-800/60'
                      )}
                    >
                      <span className="shrink-0">{item.icon}</span>
                      {!collapsed && (
                        <span className="truncate flex-1">{item.label}</span>
                      )}
                      {!collapsed && item.badge && (
                        <span className="text-[10px] px-1.5 py-0.2 rounded-full bg-zinc-200 dark:bg-zinc-800 text-zinc-700 dark:text-zinc-300">
                          {item.badge}
                        </span>
                      )}
                    </button>
                  );
                })}
              </div>
            );
          })}
        </div>
      </div>

      {/* User Footer Card */}
      <div className="p-2 border-t border-zinc-100 dark:border-zinc-800/80">
        <div
          className={clsx(
            'flex items-center gap-2.5 p-2 rounded-lg bg-zinc-50 dark:bg-zinc-900/60 border border-zinc-200/60 dark:border-zinc-800/60',
            collapsed && 'justify-center p-1.5'
          )}
        >
          <div className="w-7 h-7 rounded-md bg-zinc-200 dark:bg-zinc-800 text-zinc-800 dark:text-zinc-200 flex items-center justify-center font-semibold text-xs shrink-0">
            {user?.fullName?.charAt(0) || user?.username?.charAt(0) || 'U'}
          </div>
          {!collapsed && (
            <div className="flex-1 min-w-0">
              <div className="text-xs font-medium text-zinc-900 dark:text-zinc-100 truncate">
                {user?.fullName || user?.username}
              </div>
              <div className="text-[10px] text-zinc-400 dark:text-zinc-500 truncate">
                {user?.role || 'STUDENT'}
              </div>
            </div>
          )}
          <button
            onClick={logout}
            className="p-1 rounded-md text-zinc-400 hover:text-red-600 dark:hover:text-red-400 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors cursor-pointer shrink-0"
            title="Sign Out"
            aria-label="Sign Out"
          >
            <LogOut className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </aside>
  );
};
