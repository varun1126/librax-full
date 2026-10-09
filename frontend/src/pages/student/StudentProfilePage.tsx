import React, { useState } from 'react';
import { Key, CheckCircle2, AlertCircle } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { api } from '../../services/api';
import { Card } from '../../components/ui/GlassCard';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';

export const StudentProfilePage: React.FC = () => {
  const { user, member } = useAuth();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      setMessage({ type: 'error', text: 'New passwords do not match' });
      return;
    }
    if (newPassword.length < 6) {
      setMessage({ type: 'error', text: 'Password must be at least 6 characters' });
      return;
    }

    setIsLoading(true);
    setMessage(null);
    try {
      await api.auth.changePassword({
        currentPassword,
        newPassword,
      });
      setMessage({ type: 'success', text: 'Password changed successfully' });
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err: any) {
      setMessage({ type: 'error', text: err.message || 'Failed to update password' });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-100">
          Student Profile & Library Pass
        </h2>
        <p className="text-xs text-zinc-500 dark:text-zinc-400 mt-0.5">
          Institutional membership credentials, borrowing quotas, and security.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Digital Library Card */}
        <div className="rounded-xl p-6 sm:p-7 bg-zinc-900 text-zinc-100 border border-zinc-800 shadow-sm flex flex-col justify-between min-h-[280px] relative overflow-hidden">
          {/* Top Bar */}
          <div className="flex items-center justify-between z-10">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-zinc-100 text-zinc-900 flex items-center justify-center font-bold text-xs tracking-tight shadow-xs">
                LX
              </div>
              <div>
                <div className="font-semibold text-xs text-zinc-100 tracking-wide uppercase">
                  LibraX Library Pass
                </div>
                <div className="text-[10px] text-zinc-400 font-mono">
                  Academic Patron Pass
                </div>
              </div>
            </div>
            <Badge variant="success" size="sm" dot>
              {member?.status || 'ACTIVE'}
            </Badge>
          </div>

          {/* Card Middle: Patron Info */}
          <div className="my-6 z-10 space-y-1">
            <div className="text-xl font-semibold text-zinc-50 tracking-tight">
              {member?.name || user?.fullName}
            </div>
            <div className="text-xs text-zinc-400 font-mono">
              Card ID: {member?.memberId || user?.username || 'LIB-STU-1001'}
            </div>
            <div className="text-xs text-zinc-400">
              {member?.department || 'Computer Science'} • {member?.course || 'Undergraduate'}
            </div>
          </div>

          {/* Card Footer: Barcode Representation */}
          <div className="pt-4 border-t border-zinc-800 flex items-center justify-between z-10 text-xs">
            <div>
              <div className="text-[10px] uppercase text-zinc-400">Borrow Quota</div>
              <div className="font-mono font-medium text-zinc-100">
                {member?.currentBorrowedCount || 0} / {member?.maxBooksAllowed || 5} Books
              </div>
            </div>
            <div className="text-right">
              <div className="text-[10px] uppercase text-zinc-400">Registered</div>
              <div className="font-mono text-zinc-200 text-xs">
                {member?.registeredDate ? new Date(member.registeredDate).toLocaleDateString() : '2026-01-15'}
              </div>
            </div>
          </div>
        </div>

        {/* Change Password Card */}
        <Card className="p-6">
          <div className="flex items-center gap-2.5 mb-4">
            <div className="p-2 rounded-lg bg-zinc-100 dark:bg-zinc-800 text-zinc-700 dark:text-zinc-300">
              <Key className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
                Security & Password
              </h3>
              <p className="text-xs text-zinc-500 dark:text-zinc-400">
                Update your account password
              </p>
            </div>
          </div>

          <form onSubmit={handleChangePassword} className="space-y-3.5">
            <Input
              label="Current Password"
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              required
              placeholder="••••••••"
            />
            <Input
              label="New Password"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              placeholder="••••••••"
            />
            <Input
              label="Confirm New Password"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              placeholder="••••••••"
            />

            {message && (
              <div
                className={`p-2.5 rounded-lg text-xs flex items-center gap-2 ${
                  message.type === 'error'
                    ? 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400 border border-red-200 dark:border-red-800/40'
                    : 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800/40'
                }`}
              >
                {message.type === 'error' ? (
                  <AlertCircle className="w-4 h-4 shrink-0" />
                ) : (
                  <CheckCircle2 className="w-4 h-4 shrink-0" />
                )}
                <span>{message.text}</span>
              </div>
            )}

            <Button
              type="submit"
              variant="primary"
              size="md"
              className="w-full mt-2"
              isLoading={isLoading}
            >
              Update Password
            </Button>
          </form>
        </Card>
      </div>
    </div>
  );
};
