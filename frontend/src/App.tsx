import React, { useState, useEffect } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { LoginPage } from './pages/auth/LoginPage';
import { Sidebar } from './components/layout/Sidebar';
import { TopBar } from './components/layout/TopBar';
import { CommandPalette } from './components/CommandPalette';
import { AiLibrarianChat } from './components/chat/AiLibrarianChat';
import { LibraXLogo } from './components/ui/LibraXLogo';

// Admin Pages
import { AdminDashboard } from './pages/admin/AdminDashboard';
import { BooksPage } from './pages/admin/BooksPage';
import { StudentsPage } from './pages/admin/StudentsPage';
import { LoansPage } from './pages/admin/LoansPage';
import { ReservationsPage } from './pages/admin/ReservationsPage';
import { FinesPage } from './pages/admin/FinesPage';
import { ReportsPage } from './pages/admin/ReportsPage';
import { RbacPage } from './pages/admin/RbacPage';
import { UsersPage } from './pages/admin/UsersPage';
import { AuditPage } from './pages/admin/AuditPage';
import { SettingsPage } from './pages/admin/SettingsPage';

// Student Pages
import { StudentDashboard } from './pages/student/StudentDashboard';
import { StudentCatalogPage } from './pages/student/StudentCatalogPage';
import { StudentLoansPage } from './pages/student/StudentLoansPage';
import { StudentReservationsPage } from './pages/student/StudentReservationsPage';
import { StudentFinesPage } from './pages/student/StudentFinesPage';
import { StudentProfilePage } from './pages/student/StudentProfilePage';

const MainLayout: React.FC = () => {
  const { isAdmin, isStudent } = useAuth();
  const [currentView, setCurrentView] = useState<string>(() =>
    isAdmin ? 'admin-dashboard' : 'student-dashboard'
  );
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [commandPaletteOpen, setCommandPaletteOpen] = useState(false);

  useEffect(() => {
    if (isAdmin && currentView.startsWith('student-')) {
      setCurrentView('admin-dashboard');
    } else if (isStudent && !currentView.startsWith('student-')) {
      setCurrentView('student-dashboard');
    }
  }, [isAdmin, isStudent, currentView]);

  // Global Ctrl+K / Cmd+K shortcut
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        setCommandPaletteOpen((prev) => !prev);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const renderView = () => {
    switch (currentView) {
      // Admin Views
      case 'admin-dashboard':
        return <AdminDashboard onNavigate={setCurrentView} />;
      case 'books':
        return <BooksPage />;
      case 'students':
        return <StudentsPage />;
      case 'loans':
        return <LoansPage />;
      case 'reservations':
        return <ReservationsPage />;
      case 'fines':
        return <FinesPage />;
      case 'reports':
        return <ReportsPage />;
      case 'rbac':
        return <RbacPage />;
      case 'users':
        return <UsersPage />;
      case 'audit':
        return <AuditPage />;
      case 'settings':
        return <SettingsPage />;

      // Student Views
      case 'student-dashboard':
        return <StudentDashboard onNavigate={setCurrentView} />;
      case 'student-catalog':
        return <StudentCatalogPage />;
      case 'student-loans':
        return <StudentLoansPage />;
      case 'student-reservations':
        return <StudentReservationsPage />;
      case 'student-fines':
        return <StudentFinesPage />;
      case 'student-profile':
        return <StudentProfilePage />;

      default:
        return isAdmin ? (
          <AdminDashboard onNavigate={setCurrentView} />
        ) : (
          <StudentDashboard onNavigate={setCurrentView} />
        );
    }
  };

  return (
    <div className="min-h-screen flex bg-zinc-50 dark:bg-zinc-950 text-zinc-900 dark:text-zinc-100 relative">
      {/* Collapsible Sidebar */}
      <Sidebar
        currentView={currentView}
        onNavigate={setCurrentView}
        collapsed={sidebarCollapsed}
        setCollapsed={setSidebarCollapsed}
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 z-10">
        <TopBar
          currentView={currentView}
          onOpenCommandPalette={() => setCommandPaletteOpen(true)}
        />
        <main className="flex-1 p-4 sm:p-6 md:p-8 max-w-7xl w-full mx-auto overflow-y-auto">
          {renderView()}
        </main>
      </div>

      {/* AI Librarian Chat Widget */}
      <AiLibrarianChat onNavigate={setCurrentView} />

      {/* Global Command Palette */}
      <CommandPalette
        isOpen={commandPaletteOpen}
        onClose={() => setCommandPaletteOpen(false)}
        onNavigate={(view) => {
          setCurrentView(view);
          setCommandPaletteOpen(false);
        }}
      />
    </div>
  );
};

const Root: React.FC = () => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-zinc-50 dark:bg-zinc-950">
        <div className="flex flex-col items-center gap-3">
          <LibraXLogo size={42} />
          <span className="text-xs text-zinc-400 dark:text-zinc-500 font-medium">
            Loading LibraX...
          </span>
        </div>
      </div>
    );
  }

  return user ? <MainLayout /> : <LoginPage />;
};

import { ErrorBoundary } from './components/ErrorBoundary';

export default function App() {
  return (
    <ErrorBoundary>
      <ThemeProvider>
        <AuthProvider>
          <Root />
        </AuthProvider>
      </ThemeProvider>
    </ErrorBoundary>
  );
}
