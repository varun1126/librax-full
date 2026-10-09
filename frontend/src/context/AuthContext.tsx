import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import type { User, Member } from '../types';
import { api } from '../services/api';

interface AuthContextType {
  user: User | null;
  member: Member | null;
  token: string | null;
  isLoading: boolean;
  isAdmin: boolean;
  isStudent: boolean;
  hasPermission: (permission: string) => boolean;
  login: (credentials: { username: string; password: string }) => Promise<void>;
  studentLogin: (credentials: { identifier: string; password: string }) => Promise<void>;
  register: (data: {
    fullName: string;
    email: string;
    password: string;
    studentId?: string;
    username?: string;
    department?: string;
    phone?: string;
  }) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function getSavedUser(): User | null {
  try {
    const saved = localStorage.getItem('librax_user');
    if (!saved || saved === 'undefined' || saved === 'null') return null;
    return JSON.parse(saved);
  } catch {
    localStorage.removeItem('librax_user');
    return null;
  }
}

function getSavedMember(): Member | null {
  try {
    const saved = localStorage.getItem('librax_member');
    if (!saved || saved === 'undefined' || saved === 'null') return null;
    return JSON.parse(saved);
  } catch {
    localStorage.removeItem('librax_member');
    return null;
  }
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(getSavedUser);
  const [member, setMember] = useState<Member | null>(getSavedMember);
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('librax_token'));
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const logout = useCallback(() => {
    api.auth.logout().catch(() => {});
    localStorage.removeItem('librax_token');
    localStorage.removeItem('librax_user');
    localStorage.removeItem('librax_member');
    setToken(null);
    setUser(null);
    setMember(null);
  }, []);

  const refreshUser = useCallback(async () => {
    const currentToken = localStorage.getItem('librax_token');
    if (!currentToken) {
      setUser(null);
      setMember(null);
      setIsLoading(false);
      return;
    }
    try {
      const res = await api.auth.me();
      setUser(res.user);
      localStorage.setItem('librax_user', JSON.stringify(res.user));
      if (res.member) {
        setMember(res.member);
        localStorage.setItem('librax_member', JSON.stringify(res.member));
      }
    } catch {
      logout();
    } finally {
      setIsLoading(false);
    }
  }, [logout]);

  useEffect(() => {
    refreshUser();

    const handleAuthChange = () => {
      setToken(localStorage.getItem('librax_token'));
      setUser(getSavedUser());
      setMember(getSavedMember());
    };

    window.addEventListener('librax_auth_change', handleAuthChange);
    return () => window.removeEventListener('librax_auth_change', handleAuthChange);
  }, [refreshUser]);

  const login = async (credentials: { username: string; password: string }) => {
    setIsLoading(true);
    try {
      const res = await api.auth.login(credentials);
      localStorage.setItem('librax_token', res.token);
      localStorage.setItem('librax_user', JSON.stringify(res.user));
      if (res.member) {
        localStorage.setItem('librax_member', JSON.stringify(res.member));
        setMember(res.member);
      } else {
        localStorage.removeItem('librax_member');
        setMember(null);
      }
      setToken(res.token);
      setUser(res.user);
    } finally {
      setIsLoading(false);
    }
  };

  const studentLogin = async (credentials: { identifier: string; password: string }) => {
    setIsLoading(true);
    try {
      const res = await api.auth.studentLogin(credentials);
      localStorage.setItem('librax_token', res.token);
      localStorage.setItem('librax_user', JSON.stringify(res.user));
      if (res.member) {
        localStorage.setItem('librax_member', JSON.stringify(res.member));
        setMember(res.member);
      }
      setToken(res.token);
      setUser(res.user);
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (data: {
    fullName: string;
    email: string;
    password: string;
    studentId?: string;
    username?: string;
    department?: string;
    phone?: string;
  }) => {
    setIsLoading(true);
    try {
      const res = await api.auth.register(data);
      localStorage.setItem('librax_token', res.token);
      localStorage.setItem('librax_user', JSON.stringify(res.user));
      if (res.member) {
        localStorage.setItem('librax_member', JSON.stringify(res.member));
        setMember(res.member);
      }
      setToken(res.token);
      setUser(res.user);
    } finally {
      setIsLoading(false);
    }
  };

  const isAdmin = user?.role === 'SUPER_ADMIN' || user?.role === 'ADMIN' || user?.role === 'LIBRARIAN';
  const isStudent = user?.role === 'STUDENT';

  const hasPermission = (permission: string): boolean => {
    if (!user) return false;
    if (user.role === 'SUPER_ADMIN') return true;
    return !!user.permissions?.includes(permission);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        member,
        token,
        isLoading,
        isAdmin,
        isStudent,
        hasPermission,
        login,
        studentLogin,
        register,
        logout,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
