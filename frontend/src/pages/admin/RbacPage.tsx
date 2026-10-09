import React, { useState, useEffect } from 'react';
import { Save, CheckSquare, Square, Info } from 'lucide-react';
import { api } from '../../services/api';
import type { RbacMatrix } from '../../types';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { GlassCard } from '../../components/ui/GlassCard';

export const RbacPage: React.FC = () => {
  const [matrixData, setMatrixData] = useState<RbacMatrix | null>(null);
  const [selectedRole, setSelectedRole] = useState<string>('ADMIN');
  const [rolePermissions, setRolePermissions] = useState<string[]>([]);
  const [isSaving, setIsSaving] = useState(false);

  const fetchMatrix = React.useCallback(async () => {
    try {
      const data = await api.rbac.getMatrix();
      setMatrixData(data);
      if (data?.matrix && data.matrix[selectedRole]) {
        setRolePermissions(data.matrix[selectedRole]);
      }
    } catch (e) {
      console.error(e);
    }
  }, [selectedRole]);

  useEffect(() => {
    fetchMatrix();
  }, [fetchMatrix]);

  useEffect(() => {
    if (matrixData?.matrix && matrixData.matrix[selectedRole]) {
      setRolePermissions(matrixData.matrix[selectedRole]);
    }
  }, [selectedRole, matrixData]);

  const togglePermission = (permName: string) => {
    if (selectedRole === 'SUPER_ADMIN') {
      alert('SUPER_ADMIN possesses unrestrictable root permissions.');
      return;
    }
    setRolePermissions((prev) =>
      prev.includes(permName) ? prev.filter((p) => p !== permName) : [...prev, permName]
    );
  };

  const handleSave = async () => {
    setIsSaving(true);
    try {
      await api.rbac.updateRolePermissions(selectedRole, rolePermissions);
      alert(`Permissions for ${selectedRole} updated successfully!`);
      fetchMatrix();
    } catch (e: any) {
      alert(e.message || 'Failed to update role permissions');
    } finally {
      setIsSaving(false);
    }
  };

  // Group permissions by category
  const groupedPermissions: Record<string, { id: number; name: string; description: string }[]> = {};
  matrixData?.permissions.forEach((p) => {
    const cat = p.category || 'GENERAL';
    if (!groupedPermissions[cat]) {
      groupedPermissions[cat] = [];
    }
    groupedPermissions[cat].push(p);
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-black text-[var(--text-primary)]">Role-Based Access Control (RBAC)</h2>
          <p className="text-xs text-[var(--text-secondary)]">
            Configure fine-grained privilege matrices and security guardrails
          </p>
        </div>
        <Button
          variant="primary"
          size="sm"
          isLoading={isSaving}
          disabled={selectedRole === 'SUPER_ADMIN'}
          onClick={handleSave}
          leftIcon={<Save className="w-4 h-4" />}
        >
          Save Permissions
        </Button>
      </div>

      {/* Role Picker Tabs */}
      <div className="flex gap-2 p-1.5 bg-[var(--bg-secondary)] border border-[var(--border-color)] rounded-2xl">
        {matrixData?.roles.map((r) => {
          const isSelected = selectedRole === r.name;
          return (
            <button
              key={r.id}
              onClick={() => setSelectedRole(r.name)}
              className={`flex-1 py-2.5 px-3 rounded-xl text-xs font-bold transition-all cursor-pointer text-center ${
                isSelected
                  ? 'bg-[var(--accent-primary)] text-[var(--accent-text)] shadow-md'
                  : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
              }`}
            >
              <div>{r.name}</div>
              <div className="text-[10px] font-normal opacity-80 truncate">{r.description}</div>
            </button>
          );
        })}
      </div>

      {selectedRole === 'SUPER_ADMIN' && (
        <div className="p-3 rounded-xl bg-purple-500/10 border border-purple-500/20 text-purple-300 text-xs flex items-center gap-2">
          <Info className="w-4 h-4 shrink-0" />
          <span>SUPER_ADMIN role bypasses all authorization checks and holds complete root privileges.</span>
        </div>
      )}

      {/* Permission Categories Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Object.entries(groupedPermissions).map(([category, perms]) => (
          <GlassCard key={category} className="space-y-3">
            <div className="flex items-center justify-between border-b border-[var(--border-color)] pb-2">
              <h4 className="text-xs font-extrabold uppercase tracking-wider text-[var(--accent-primary)]">
                {category}
              </h4>
              <Badge variant="neutral" size="sm">
                {perms.filter((p) => rolePermissions.includes(p.name)).length} / {perms.length}
              </Badge>
            </div>

            <div className="space-y-2">
              {perms.map((p) => {
                const isChecked = selectedRole === 'SUPER_ADMIN' || rolePermissions.includes(p.name);
                return (
                  <div
                    key={p.id}
                    onClick={() => togglePermission(p.name)}
                    className="flex items-start gap-2.5 p-2 rounded-xl hover:bg-[var(--bg-card-hover)] cursor-pointer transition-colors"
                  >
                    <div className="mt-0.5 text-[var(--accent-primary)]">
                      {isChecked ? (
                        <CheckSquare className="w-4 h-4 text-[var(--accent-primary)]" />
                      ) : (
                        <Square className="w-4 h-4 text-[var(--text-muted)]" />
                      )}
                    </div>
                    <div>
                      <div className="text-xs font-bold text-[var(--text-primary)]">{p.name}</div>
                      <div className="text-[10px] text-[var(--text-secondary)] leading-tight">
                        {p.description}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </GlassCard>
        ))}
      </div>
    </div>
  );
};
