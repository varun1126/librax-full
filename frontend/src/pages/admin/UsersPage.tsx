import React, { useState, useEffect } from 'react';
import { Plus, Key, Lock, CheckCircle } from 'lucide-react';
import { api } from '../../services/api';
import type { User } from '../../types';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { DataTable, Column } from '../../components/ui/DataTable';

export const UsersPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Add User Modal
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [newUser, setNewUser] = useState({
    username: '',
    fullName: '',
    email: '',
    password: '',
    role: 'LIBRARIAN' as any,
  });
  const [addingUser, setAddingUser] = useState(false);

  // Reset Password Modal
  const [isResetModalOpen, setIsResetModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [newPassword, setNewPassword] = useState('');
  const [resetting, setResetting] = useState(false);

  const fetchUsers = React.useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await api.users.getAll();
      setUsers(data || []);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    setAddingUser(true);
    try {
      await api.users.create(newUser);
      alert('Staff user created successfully!');
      setIsAddModalOpen(false);
      setNewUser({ username: '', fullName: '', email: '', password: '', role: 'LIBRARIAN' });
      fetchUsers();
    } catch (e: any) {
      alert(e.message || 'Failed to create user');
    } finally {
      setAddingUser(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedUser) return;
    setResetting(true);
    try {
      await api.users.resetPassword(selectedUser.id, newPassword);
      alert(`Password for ${selectedUser.username} successfully updated!`);
      setIsResetModalOpen(false);
      setNewPassword('');
    } catch (e: any) {
      alert(e.message || 'Failed to reset password');
    } finally {
      setResetting(false);
    }
  };

  const handleToggleStatus = async (user: User) => {
    const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    if (!confirm(`Set status of ${user.username} to ${newStatus}?`)) return;
    try {
      await api.users.updateStatus(user.id, newStatus);
      fetchUsers();
    } catch (e: any) {
      alert(e.message || 'Failed to update user status');
    }
  };

  const columns: Column<User>[] = [
    {
      header: 'User Account',
      cell: (u) => (
        <div className="space-y-0.5">
          <div className="font-bold text-[var(--text-primary)] text-sm">{u.fullName || u.username}</div>
          <div className="text-xs text-[var(--text-secondary)] font-mono">@{u.username}</div>
          <div className="text-[11px] text-[var(--text-muted)]">{u.email}</div>
        </div>
      ),
    },
    {
      header: 'Role',
      cell: (u) => (
        <Badge
          variant={u.role === 'SUPER_ADMIN' ? 'purple' : u.role === 'ADMIN' ? 'primary' : 'neutral'}
          size="sm"
        >
          {u.role}
        </Badge>
      ),
    },
    {
      header: 'Last Login',
      cell: (u) => (
        <span className="text-xs font-mono text-[var(--text-secondary)]">
          {u.lastLogin ? new Date(u.lastLogin).toLocaleString() : 'Never'}
        </span>
      ),
    },
    {
      header: 'Status',
      cell: (u) => (
        <Badge variant={u.status === 'ACTIVE' ? 'success' : 'danger'} size="sm" dot>
          {u.status}
        </Badge>
      ),
    },
    {
      header: 'Actions',
      className: 'text-right',
      cell: (u) => (
        <div className="flex items-center justify-end gap-1.5">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setSelectedUser(u);
              setIsResetModalOpen(true);
            }}
            title="Reset Password"
          >
            <Key className="w-4 h-4 text-amber-400" />
          </Button>
          {u.role !== 'SUPER_ADMIN' && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => handleToggleStatus(u)}
              title="Toggle Status"
            >
              {u.status === 'ACTIVE' ? (
                <Lock className="w-4 h-4 text-rose-400" />
              ) : (
                <CheckCircle className="w-4 h-4 text-emerald-400" />
              )}
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-black text-[var(--text-primary)]">System Users & Staff</h2>
          <p className="text-xs text-[var(--text-secondary)]">
            Manage administrative credentials, librarian access and account security
          </p>
        </div>
        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={() => setIsAddModalOpen(true)}
        >
          Create Staff User
        </Button>
      </div>

      <DataTable
        columns={columns}
        data={users}
        isLoading={isLoading}
        emptyMessage="No staff accounts found."
      />

      {/* Create User Modal */}
      <Modal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        title="Create New Staff User"
      >
        <form onSubmit={handleCreateUser} className="space-y-4">
          <Input
            label="Username"
            value={newUser.username}
            onChange={(e) => setNewUser({ ...newUser, username: e.target.value })}
            required
            placeholder="e.g. librarian_mary"
          />
          <Input
            label="Full Name"
            value={newUser.fullName}
            onChange={(e) => setNewUser({ ...newUser, fullName: e.target.value })}
            required
            placeholder="e.g. Mary Jenkins"
          />
          <Input
            label="Email Address"
            type="email"
            value={newUser.email}
            onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
            required
            placeholder="mary@library.edu"
          />
          <div className="space-y-1.5">
            <label className="block text-xs font-medium text-[var(--text-secondary)]">
              Assigned Role
            </label>
            <select
              className="w-full bg-[var(--bg-secondary)] text-[var(--text-primary)] rounded-xl border border-[var(--border-color)] px-3 py-2 text-xs outline-none"
              value={newUser.role}
              onChange={(e) => setNewUser({ ...newUser, role: e.target.value as any })}
            >
              <option value="LIBRARIAN">Librarian</option>
              <option value="ADMIN">Administrator</option>
            </select>
          </div>
          <Input
            label="Password"
            type="password"
            value={newUser.password}
            onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
            required
            placeholder="••••••••"
          />

          <div className="flex justify-end gap-2 pt-3">
            <Button type="button" variant="outline" onClick={() => setIsAddModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" isLoading={addingUser}>
              Create Account
            </Button>
          </div>
        </form>
      </Modal>

      {/* Reset Password Modal */}
      <Modal
        isOpen={isResetModalOpen}
        onClose={() => setIsResetModalOpen(false)}
        title={`Reset Password: ${selectedUser?.username}`}
      >
        <form onSubmit={handleResetPassword} className="space-y-4">
          <Input
            label="New Password"
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            required
            placeholder="Enter new strong password"
          />

          <div className="flex justify-end gap-2 pt-3">
            <Button type="button" variant="outline" onClick={() => setIsResetModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" isLoading={resetting}>
              Save New Password
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
