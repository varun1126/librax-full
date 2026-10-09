import React, { useState } from 'react';
import { ShieldCheck, GraduationCap, Lock, User, ArrowRight, AlertCircle, UserPlus, Mail, Phone, Building } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { ThemeSwitcher } from '../../components/ui/ThemeSwitcher';
import { clsx } from 'clsx';

import { LibraXLogo } from '../../components/ui/LibraXLogo';

export const LoginPage: React.FC = () => {
  const { login, studentLogin, register } = useAuth();
  const [tab, setTab] = useState<'admin' | 'student'>('admin');
  const [isRegisterMode, setIsRegisterMode] = useState(false);

  // Admin Form State
  const [adminUsername, setAdminUsername] = useState('admin');
  const [adminPassword, setAdminPassword] = useState('Admin@123');

  // Student Form State
  const [studentId, setStudentId] = useState('');
  const [studentPassword, setStudentPassword] = useState('');

  // Student Register State
  const [regFullName, setRegFullName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regStudentId, setRegStudentId] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regDepartment, setRegDepartment] = useState('');
  const [regPhone, setRegPhone] = useState('');

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleAdminSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);
    try {
      await login({ username: adminUsername, password: adminPassword });
    } catch (err: any) {
      setError(err.message || 'Invalid credentials. Please verify your username and password.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStudentSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);
    try {
      await studentLogin({ identifier: studentId, password: studentPassword });
    } catch (err: any) {
      setError(err.message || 'Student login failed. Check Member ID and password.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStudentRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);
    try {
      await register({
        fullName: regFullName,
        email: regEmail,
        password: regPassword,
        studentId: regStudentId.trim() || undefined,
        department: regDepartment.trim() || undefined,
        phone: regPhone.trim() || undefined,
      });
    } catch (err: any) {
      setError(err.message || 'Registration failed. Please verify your details.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center p-4 relative bg-zinc-50 dark:bg-zinc-950 text-zinc-900 dark:text-zinc-100">
      {/* Top Bar Floating Switcher */}
      <div className="absolute top-5 right-5 z-10 flex items-center gap-2">
        <ThemeSwitcher />
      </div>

      <div className={clsx('w-full relative z-10 transition-all duration-300', isRegisterMode ? 'max-w-md' : 'max-w-sm')}>
        <div className="rounded-xl p-6 sm:p-8 border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 shadow-sm">
          {/* Logo & Headline */}
          <div className="text-center space-y-1.5 mb-6">
            <div className="flex justify-center mb-1">
              <LibraXLogo size={44} />
            </div>
            <h1 className="text-lg font-semibold tracking-tight text-zinc-900 dark:text-zinc-100">
              LibraX Enterprise
            </h1>
            <p className="text-xs text-zinc-500 dark:text-zinc-400">
              Academic Library Management Platform
            </p>
          </div>

          {/* Portal Switcher Tabs */}
          <div className="grid grid-cols-2 p-1 bg-zinc-100 dark:bg-zinc-800 rounded-lg mb-5 text-xs font-medium">
            <button
              type="button"
              onClick={() => {
                setTab('admin');
                setIsRegisterMode(false);
                setError(null);
              }}
              className={clsx(
                'flex items-center justify-center gap-1.5 py-1.5 rounded-md transition-colors cursor-pointer',
                tab === 'admin'
                  ? 'bg-white dark:bg-zinc-900 text-zinc-900 dark:text-zinc-100 shadow-xs font-semibold'
                  : 'text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100'
              )}
            >
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>Staff / Admin</span>
            </button>
            <button
              type="button"
              onClick={() => {
                setTab('student');
                setError(null);
              }}
              className={clsx(
                'flex items-center justify-center gap-1.5 py-1.5 rounded-md transition-colors cursor-pointer',
                tab === 'student'
                  ? 'bg-white dark:bg-zinc-900 text-zinc-900 dark:text-zinc-100 shadow-xs font-semibold'
                  : 'text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100'
              )}
            >
              <GraduationCap className="w-3.5 h-3.5" />
              <span>Student Portal</span>
            </button>
          </div>

          {/* Error Banner */}
          {error && (
            <div className="p-2.5 mb-4 rounded-lg bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800/40 text-red-700 dark:text-red-400 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Tab Content Forms */}
          {tab === 'admin' ? (
            <form onSubmit={handleAdminSubmit} className="space-y-3.5">
              <Input
                label="Staff Username or Email"
                placeholder="admin"
                leftIcon={<User className="w-4 h-4" />}
                value={adminUsername}
                onChange={(e) => setAdminUsername(e.target.value)}
                required
              />
              <Input
                label="Password"
                type="password"
                placeholder="••••••••"
                leftIcon={<Lock className="w-4 h-4" />}
                value={adminPassword}
                onChange={(e) => setAdminPassword(e.target.value)}
                required
              />
              <Button
                type="submit"
                className="w-full mt-1"
                size="md"
                isLoading={isLoading}
                rightIcon={<ArrowRight className="w-4 h-4" />}
              >
                Sign In
              </Button>
            </form>
          ) : isRegisterMode ? (
            <form onSubmit={handleStudentRegister} className="space-y-3">
              <Input
                label="Full Legal Name"
                placeholder="e.g. Alex Johnson"
                leftIcon={<User className="w-4 h-4" />}
                value={regFullName}
                onChange={(e) => setRegFullName(e.target.value)}
                required
              />
              <Input
                label="College Email Address"
                type="email"
                placeholder="alex@college.edu"
                leftIcon={<Mail className="w-4 h-4" />}
                value={regEmail}
                onChange={(e) => setRegEmail(e.target.value)}
                required
              />
              <div className="grid grid-cols-2 gap-2">
                <Input
                  label="Student ID (Optional)"
                  placeholder="e.g. STU-2026"
                  leftIcon={<GraduationCap className="w-4 h-4" />}
                  value={regStudentId}
                  onChange={(e) => setRegStudentId(e.target.value)}
                />
                <Input
                  label="Department"
                  placeholder="Computer Sci"
                  leftIcon={<Building className="w-4 h-4" />}
                  value={regDepartment}
                  onChange={(e) => setRegDepartment(e.target.value)}
                />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <Input
                  label="Phone Number"
                  placeholder="+91 98765..."
                  leftIcon={<Phone className="w-4 h-4" />}
                  value={regPhone}
                  onChange={(e) => setRegPhone(e.target.value)}
                />
                <Input
                  label="Password (min 6)"
                  type="password"
                  placeholder="••••••••"
                  leftIcon={<Lock className="w-4 h-4" />}
                  value={regPassword}
                  onChange={(e) => setRegPassword(e.target.value)}
                  required
                />
              </div>
              <Button
                type="submit"
                className="w-full mt-2"
                size="md"
                isLoading={isLoading}
                rightIcon={<UserPlus className="w-4 h-4" />}
              >
                Register & Enter Portal
              </Button>
              <div className="text-center pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setIsRegisterMode(false);
                    setError(null);
                  }}
                  className="text-xs text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-200 cursor-pointer transition-colors underline"
                >
                  Already registered? Sign in here
                </button>
              </div>
            </form>
          ) : (
            <form onSubmit={handleStudentSubmit} className="space-y-3.5">
              <Input
                label="Student ID, Email or Username"
                placeholder="student01 or harshit@librax.edu"
                leftIcon={<GraduationCap className="w-4 h-4" />}
                value={studentId}
                onChange={(e) => setStudentId(e.target.value)}
                required
              />
              <Input
                label="Password"
                type="password"
                placeholder="••••••••"
                leftIcon={<Lock className="w-4 h-4" />}
                value={studentPassword}
                onChange={(e) => setStudentPassword(e.target.value)}
                required
              />
              <Button
                type="submit"
                className="w-full mt-1"
                size="md"
                isLoading={isLoading}
                rightIcon={<ArrowRight className="w-4 h-4" />}
              >
                Access Student Portal
              </Button>

              <div className="text-center pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setIsRegisterMode(true);
                    setError(null);
                  }}
                  className="text-xs text-blue-600 dark:text-blue-400 hover:underline cursor-pointer font-medium inline-flex items-center gap-1"
                >
                  <UserPlus className="w-3.5 h-3.5" />
                  New student? Register for library card
                </button>
              </div>
            </form>
          )}

          {/* Quick Credential Hints */}
          {!isRegisterMode && (
            <div className="mt-6 pt-4 border-t border-zinc-100 dark:border-zinc-800">
              <p className="text-[10px] font-medium text-zinc-400 dark:text-zinc-500 uppercase tracking-wider mb-2 text-center">
                Quick Test Credentials
              </p>
              <div className="text-xs">
                <button
                  type="button"
                  onClick={() => {
                    setTab('admin');
                    setAdminUsername('admin');
                    setAdminPassword('Admin@123');
                  }}
                  className="w-full p-2 rounded-lg bg-zinc-50 dark:bg-zinc-800/50 hover:bg-zinc-100 dark:hover:bg-zinc-800 border border-zinc-200 dark:border-zinc-700/60 text-left cursor-pointer transition-colors"
                >
                  <div className="font-medium text-zinc-900 dark:text-zinc-100 text-[11px]">Admin</div>
                  <div className="text-[10px] text-zinc-400 dark:text-zinc-500 font-mono">admin / Admin@123</div>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
