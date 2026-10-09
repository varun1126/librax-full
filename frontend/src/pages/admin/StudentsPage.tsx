import React, { useState, useEffect } from 'react';
import { Search, Plus, Edit2, CheckCircle, XCircle, Trash2 } from 'lucide-react';
import { api } from '../../services/api';
import type { Member } from '../../types';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { DataTable, Column } from '../../components/ui/DataTable';

export const StudentsPage: React.FC = () => {
  const [students, setStudents] = useState<Member[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  // Add / Edit Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingStudent, setEditingStudent] = useState<(Partial<Member> & { initialPassword?: string }) | null>(null);
  const [saving, setSaving] = useState(false);

  const fetchStudents = React.useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await api.students.getAll({
        search: search.trim() || undefined,
        page,
        size: 10,
      });
      setStudents(res.students || []);
      setTotal(res.total || 0);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  }, [page, search]);

  useEffect(() => {
    fetchStudents();
  }, [fetchStudents]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    fetchStudents();
  };

  const handleSaveStudent = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingStudent) return;
    setSaving(true);
    try {
      if (editingStudent.id) {
        await api.students.update(editingStudent.id, editingStudent);
      } else {
        await api.students.create(editingStudent);
      }
      setIsModalOpen(false);
      setEditingStudent(null);
      fetchStudents();
    } catch (e: any) {
      alert(e.message || 'Failed to save student record');
    } finally {
      setSaving(false);
    }
  };

  const handleToggleStatus = async (student: Member) => {
    const newStatus = student.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    if (!confirm(`Are you sure you want to change status of ${student.name} to ${newStatus}?`)) return;
    try {
      await api.students.toggleStatus(student.id, newStatus);
      fetchStudents();
    } catch (e: any) {
      alert(e.message || 'Failed to update student status');
    }
  };

  const handleDeleteStudent = async (student: Member) => {
    if (!confirm(`Are you sure you want to PERMANENTLY delete student "${student.name}" (${student.memberId})?\n\nThis will remove their membership and login account. This action cannot be undone.`)) {
      return;
    }
    try {
      await api.students.delete(student.id);
      alert(`Student "${student.name}" has been permanently removed.`);
      fetchStudents();
    } catch (e: any) {
      alert(e.message || 'Failed to delete student');
    }
  };

  const columns: Column<Member>[] = [
    {
      header: 'Student Name & ID',
      cell: (s) => (
        <div className="space-y-0.5">
          <div className="font-bold text-[var(--text-primary)] text-sm">{s.name}</div>
          <div className="text-xs text-[var(--text-secondary)] font-mono">{s.memberId}</div>
          <div className="text-[11px] text-[var(--text-muted)]">{s.email}</div>
        </div>
      ),
    },
    {
      header: 'Department / Year',
      cell: (s) => (
        <div className="space-y-0.5">
          <div className="text-xs font-semibold text-[var(--text-primary)]">{s.department || 'General'}</div>
          <div className="text-[11px] text-[var(--text-secondary)]">{s.course || 'B.Tech / CS'}</div>
        </div>
      ),
    },
    {
      header: 'Circulation Status',
      cell: (s) => (
        <div className="space-y-1">
          <div className="text-xs text-[var(--text-primary)]">
            <span className="font-bold">{s.currentBorrowedCount}</span> / {s.maxBooksAllowed} books loaned
          </div>
          <div className="h-1.5 w-24 rounded-full bg-[var(--bg-card)] overflow-hidden">
            <div
              className="h-full bg-cyan-400 rounded-full"
              style={{ width: `${Math.min((s.currentBorrowedCount / s.maxBooksAllowed) * 100, 100)}%` }}
            />
          </div>
        </div>
      ),
    },
    {
      header: 'Outstanding Fines',
      cell: (s) => (
        <span
          className={`text-xs font-mono font-bold ${
            s.outstandingFines > 0 ? 'text-rose-400' : 'text-emerald-400'
          }`}
        >
          ₹{s.outstandingFines.toFixed(2)}
        </span>
      ),
    },
    {
      header: 'Status',
      cell: (s) => (
        <Badge
          variant={s.status === 'ACTIVE' ? 'success' : 'danger'}
          size="sm"
          dot
        >
          {s.status}
        </Badge>
      ),
    },
    {
      header: 'Actions',
      className: 'text-right',
      cell: (s) => (
        <div className="flex items-center justify-end gap-1.5">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setEditingStudent(s);
              setIsModalOpen(true);
            }}
            title="Edit Student"
          >
            <Edit2 className="w-4 h-4 text-amber-400" />
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => handleToggleStatus(s)}
            title={s.status === 'ACTIVE' ? 'Suspend Student' : 'Activate Student'}
          >
            {s.status === 'ACTIVE' ? (
              <XCircle className="w-4 h-4 text-rose-400" />
            ) : (
              <CheckCircle className="w-4 h-4 text-emerald-400" />
            )}
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => handleDeleteStudent(s)}
            title="Delete Student Permanently"
          >
            <Trash2 className="w-4 h-4 text-rose-500 hover:text-rose-700" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-black text-[var(--text-primary)]">Student & Member Directory</h2>
          <p className="text-xs text-[var(--text-secondary)]">
            Manage patrons, loan quotas, contact data and student portal credentials
          </p>
        </div>
        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={() => {
            setEditingStudent({
              name: '',
              email: '',
              phone: '',
              memberType: 'STUDENT',
              department: 'Computer Science',
              maxBooksAllowed: 5,
              initialPassword: 'Student@123',
            });
            setIsModalOpen(true);
          }}
        >
          Register New Student
        </Button>
      </div>

      <form onSubmit={handleSearch} className="flex gap-3">
        <div className="flex-1">
          <Input
            placeholder="Search by student name, ID or email..."
            leftIcon={<Search className="w-4 h-4" />}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <Button type="submit" variant="secondary">
          Search
        </Button>
      </form>

      <DataTable
        columns={columns}
        data={students}
        isLoading={isLoading}
        page={page}
        pageSize={10}
        total={total}
        onPageChange={setPage}
        emptyMessage="No students found matching search."
      />

      {/* Add / Edit Student Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingStudent?.id ? 'Edit Student Details' : 'Register New Student'}
        description="Creates student member record and linked portal login account"
      >
        <form onSubmit={handleSaveStudent} className="space-y-4">
          <Input
            label="Full Name"
            value={editingStudent?.name || ''}
            onChange={(e) => setEditingStudent({ ...editingStudent, name: e.target.value })}
            required
            placeholder="e.g. John Doe"
          />
          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Email Address"
              type="email"
              value={editingStudent?.email || ''}
              onChange={(e) => setEditingStudent({ ...editingStudent, email: e.target.value })}
              required
              placeholder="e.g. john@university.edu"
            />
            <Input
              label="Phone Number"
              value={editingStudent?.phone || ''}
              onChange={(e) => setEditingStudent({ ...editingStudent, phone: e.target.value })}
              required
              placeholder="+1-555-0199"
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Department"
              value={editingStudent?.department || ''}
              onChange={(e) => setEditingStudent({ ...editingStudent, department: e.target.value })}
              placeholder="Computer Science"
            />
            <Input
              label="Max Borrow Quota"
              type="number"
              min="1"
              max="20"
              value={editingStudent?.maxBooksAllowed || 5}
              onChange={(e) => setEditingStudent({ ...editingStudent, maxBooksAllowed: Number(e.target.value) })}
              required
            />
          </div>

          {!editingStudent?.id && (
            <Input
              label="Initial Portal Password"
              value={editingStudent?.initialPassword || 'Student@123'}
              onChange={(e) => setEditingStudent({ ...editingStudent, initialPassword: e.target.value })}
              required
              helperText="Student can use this password to log in to the Student Portal"
            />
          )}

          <div className="flex justify-end gap-2 pt-3">
            <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" isLoading={saving}>
              Save Student
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
