import type {
  ApiResponse,
  User,
  Member,
  Book,
  BookCopy,
  Loan,
  Reservation,
  Fine,
  Payment,
  Notification,
  LoginHistory,
  DashboardStats,
  StudentDashboardSummary,
  RbacMatrix,
} from '../types';

const BASE_URL = '/api';

export class ApiError extends Error {
  errorCode?: string;
  status: number;

  constructor(message: string, status: number, errorCode?: string) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
  }
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('librax_token');
  const headers = new Headers(options.headers || {});
  
  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }
  
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    localStorage.removeItem('librax_token');
    localStorage.removeItem('librax_user');
    window.dispatchEvent(new Event('librax_auth_change'));
  }

  if (response.status === 204) {
    return {} as T;
  }

  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/octet-stream') || contentType?.includes('text/csv')) {
    return (await response.blob()) as unknown as T;
  }

  const json: ApiResponse<T> = await response.json();

  if (!response.ok || !json.success) {
    throw new ApiError(json.message || 'Request failed', response.status, json.errorCode);
  }

  return json.data;
}

export const api = {
  // Auth
  auth: {
    login: async (credentials: { username: string; password: string }): Promise<{ token: string; user: User; member?: Member }> => {
      const res = await request<any>('/auth/login', {
        method: 'POST',
        body: JSON.stringify({
          identifier: credentials.username,
          username: credentials.username,
          password: credentials.password,
        }),
      });
      const user: User = {
        id: res.userId,
        username: res.username,
        email: res.email,
        fullName: res.fullName,
        role: res.role,
        status: 'ACTIVE',
        studentId: res.member?.memberId,
        memberId: res.member?.id,
        lastLogin: res.lastLogin,
        permissions: res.permissions || [],
      };
      return { token: res.token, user, member: res.member || undefined };
    },
    studentLogin: async (credentials: { identifier: string; password: string }): Promise<{ token: string; user: User; member: Member }> => {
      const res = await request<any>('/auth/student-login', {
        method: 'POST',
        body: JSON.stringify({
          identifier: credentials.identifier,
          username: credentials.identifier,
          password: credentials.password,
        }),
      });
      const user: User = {
        id: res.userId,
        username: res.username,
        email: res.email,
        fullName: res.fullName,
        role: res.role,
        status: 'ACTIVE',
        studentId: res.member?.memberId,
        memberId: res.member?.id,
        lastLogin: res.lastLogin,
        permissions: res.permissions || [],
      };
      return { token: res.token, user, member: res.member };
    },
    register: async (data: {
      fullName: string;
      email: string;
      password: string;
      studentId?: string;
      username?: string;
      department?: string;
      phone?: string;
    }): Promise<{ token: string; user: User; member?: Member }> => {
      const res = await request<any>('/auth/register', {
        method: 'POST',
        body: JSON.stringify(data),
      });
      const user: User = {
        id: res.userId,
        username: res.username,
        email: res.email,
        fullName: res.fullName,
        role: res.role,
        status: 'ACTIVE',
        studentId: res.member?.memberId,
        memberId: res.member?.id,
        lastLogin: res.lastLogin,
        permissions: res.permissions || [],
      };
      return { token: res.token, user, member: res.member };
    },
    me: async (): Promise<{ user: User; member?: Member }> => {
      const res = await request<any>('/auth/me');
      const user: User = {
        id: res.userId,
        username: res.username,
        email: res.email,
        fullName: res.fullName,
        role: res.role,
        status: 'ACTIVE',
        studentId: res.member?.memberId,
        memberId: res.member?.id,
        lastLogin: res.lastLogin,
        permissions: res.permissions || [],
      };
      return { user, member: res.member || undefined };
    },
    changePassword: (data: { currentPassword: string; newPassword: string }) =>
      request<void>('/auth/change-password', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    logout: () =>
      request<void>('/auth/logout', {
        method: 'POST',
      }),
  },

  // Books
  books: {
    getAll: async (params?: { search?: string; category?: string; page?: number; size?: number }) => {
      const q = new URLSearchParams();
      if (params?.search) q.append('search', params.search);
      if (params?.category) q.append('category', params.category);
      if (params?.page) q.append('page', params.page.toString());
      if (params?.size) q.append('size', params.size.toString());
      const res = await request<any>(`/books?${q.toString()}`);
      const list = (res?.books || res?.items || []) as Book[];
      return {
        books: list,
        items: list,
        total: Number(res?.total ?? list.length),
        page: Number(res?.page ?? 1),
        totalPages: Number(res?.totalPages ?? 1),
      };
    },
    getById: (id: number) => request<Book>(`/books/${id}`),
    create: (book: Partial<Book>) =>
      request<Book>('/books', {
        method: 'POST',
        body: JSON.stringify(book),
      }),
    update: (id: number, book: Partial<Book>) =>
      request<Book>(`/books/${id}`, {
        method: 'PUT',
        body: JSON.stringify(book),
      }),
    delete: (id: number) =>
      request<void>(`/books/${id}`, {
        method: 'DELETE',
      }),
    getCopies: (bookId: number) => request<BookCopy[]>(`/books/${bookId}/copies`),
    addCopy: (bookId: number, copyData: Partial<BookCopy>) =>
      request<BookCopy>(`/books/${bookId}/copies`, {
        method: 'POST',
        body: JSON.stringify(copyData),
      }),
    updateCopyStatus: (bookId: number, copyId: number, status: string, condition?: string) =>
      request<BookCopy>(`/books/${bookId}/copies/${copyId}`, {
        method: 'PATCH',
        body: JSON.stringify({ status, condition }),
      }),
    searchGoogle: (query: string) =>
      request<any[]>(`/books/google/search?query=${encodeURIComponent(query)}`),
    importGoogle: (bookOrId: any, copies: number = 1) => {
      const body = typeof bookOrId === 'string' ? { volumeId: bookOrId } : bookOrId;
      return request<Book>(`/books/google/import?copies=${copies}`, {
        method: 'POST',
        body: JSON.stringify(body),
      });
    },
  },

  // Students
  students: {
    getAll: async (params?: { search?: string; department?: string; status?: string; page?: number; size?: number }) => {
      const q = new URLSearchParams();
      if (params?.search) q.append('search', params.search);
      if (params?.department) q.append('department', params.department);
      if (params?.status) q.append('status', params.status);
      if (params?.page) q.append('page', params.page.toString());
      if (params?.size) q.append('size', params.size.toString());
      const res = await request<any>(`/students?${q.toString()}`);
      const list = (res?.students || res?.items || []) as Member[];
      return {
        students: list,
        items: list,
        total: Number(res?.total ?? list.length),
        page: Number(res?.page ?? 1),
        totalPages: Number(res?.totalPages ?? 1),
      };
    },
    getById: (id: number) => request<Member>(`/students/${id}`),
    create: (data: Partial<Member> & { initialPassword?: string }) =>
      request<Member>('/students', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    update: (id: number, data: Partial<Member>) =>
      request<Member>(`/students/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data),
      }),
    toggleStatus: (id: number, status: string) =>
      request<Member>(`/students/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      }),
    delete: (id: number) =>
      request<void>(`/students/${id}`, {
        method: 'DELETE',
      }),
  },

  // Loans
  loans: {
    getAll: async (params?: { search?: string; status?: string; page?: number; size?: number }) => {
      const q = new URLSearchParams();
      if (params?.search) q.append('search', params.search);
      if (params?.status) q.append('status', params.status);
      if (params?.page) q.append('page', params.page.toString());
      if (params?.size) q.append('size', params.size.toString());
      const res = await request<any>(`/loans?${q.toString()}`);
      const list = (res?.loans || res?.items || []) as Loan[];
      return {
        loans: list,
        items: list,
        total: Number(res?.total ?? list.length),
        page: Number(res?.page ?? 1),
        totalPages: Number(res?.totalPages ?? 1),
      };
    },
    issue: (data: { copyId?: number; bookId?: number; memberId: number; dueDate?: string; notes?: string }) =>
      request<Loan>('/loans/issue', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    returnBook: (loanId: number, notes?: string) =>
      request<Loan>(`/loans/${loanId}/return`, {
        method: 'POST',
        body: JSON.stringify({ notes }),
      }),
    renew: (loanId: number) =>
      request<Loan>(`/loans/${loanId}/renew`, {
        method: 'POST',
      }),
    getMyLoans: () => request<Loan[]>('/loans/my-loans'),
  },

  // Reservations
  reservations: {
    getAll: (params?: { search?: string; status?: string }) => {
      const q = new URLSearchParams();
      if (params?.search) q.append('search', params.search);
      if (params?.status) q.append('status', params.status);
      return request<Reservation[]>(`/reservations?${q.toString()}`);
    },
    reserve: (bookId: number, memberId?: number) =>
      request<Reservation>('/reservations', {
        method: 'POST',
        body: JSON.stringify({ bookId, memberId }),
      }),
    cancel: (reservationId: number) =>
      request<void>(`/reservations/${reservationId}/cancel`, {
        method: 'POST',
      }),
    fulfill: (reservationId: number, copyId: number) =>
      request<Loan>(`/reservations/${reservationId}/fulfill`, {
        method: 'POST',
        body: JSON.stringify({ copyId }),
      }),
    getMyReservations: () => request<Reservation[]>('/reservations/my-reservations'),
  },

  // Finance / Fines
  fines: {
    getAll: (params?: { search?: string; status?: string }) => {
      const q = new URLSearchParams();
      if (params?.search) q.append('search', params.search);
      if (params?.status) q.append('status', params.status);
      return request<Fine[]>(`/fines?${q.toString()}`);
    },
    pay: (fineId: number, paymentMethod: string, amount: number, notes?: string) =>
      request<Payment>(`/fines/${fineId}/pay`, {
        method: 'POST',
        body: JSON.stringify({ paymentMethod, amount, notes }),
      }),
    waive: (fineId: number, reason: string) =>
      request<Fine>(`/fines/${fineId}/waive`, {
        method: 'POST',
        body: JSON.stringify({ reason }),
      }),
    getMyFines: () => request<Fine[]>('/fines/my-fines'),
  },

  // Admin Dashboard
  adminDashboard: {
    getStats: () => request<DashboardStats>('/admin/dashboard/stats'),
  },

  // Student Dashboard
  studentDashboard: {
    getSummary: () => request<StudentDashboardSummary>('/student/dashboard/summary'),
  },

  // RBAC
  rbac: {
    getRoles: () => request<any[]>('/rbac/roles'),
    getPermissions: () => request<any[]>('/rbac/permissions'),
    getMatrix: () => request<RbacMatrix>('/rbac/matrix'),
    updateRolePermissions: (roleName: string, permissions: string[]) =>
      request<void>(`/rbac/roles/${roleName}/permissions`, {
        method: 'PUT',
        body: JSON.stringify({ permissions }),
      }),
  },

  // Users
  users: {
    getAll: () => request<User[]>('/users'),
    create: (data: Partial<User> & { password: string }) =>
      request<User>('/users', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    updateStatus: (userId: number, status: string) =>
      request<User>(`/users/${userId}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      }),
    resetPassword: (userId: number, newPassword?: string) =>
      request<{ temporaryPassword?: string }>(`/users/${userId}/reset-password`, {
        method: 'POST',
        body: JSON.stringify({ newPassword }),
      }),
  },

  // Audit
  audit: {
    getLogs: async (params?: { search?: string; entityType?: string; page?: number; size?: number }) => {
      const q = new URLSearchParams();
      if (params?.search) q.append('search', params.search);
      if (params?.entityType) q.append('entity', params.entityType);
      const res = await request<any>(`/audit-logs?${q.toString()}`);
      if (Array.isArray(res)) {
        return { logs: res, total: res.length };
      }
      return { logs: res?.logs || [], total: res?.total || 0 };
    },
    getLoginHistory: () => request<LoginHistory[]>('/audit-logs/login-history'),
  },

  // Reports
  reports: {
    getInventory: () => request<any>('/reports/inventory'),
    getLoans: () => request<any>('/reports/loans'),
    getOverdue: () => request<any>('/reports/overdue'),
    getFines: () => request<any>('/reports/fines'),
    exportCsv: async (type: string) => {
      const blob = await request<Blob>(`/reports/export?type=${type}`);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `librax_${type}_report_${new Date().toISOString().slice(0, 10)}.csv`;
      document.body.appendChild(a);
      a.click();
      a.remove();
    },
  },

  // Settings
  settings: {
    getAll: () => request<Record<string, string>>('/settings'),
    update: (settings: Record<string, string>) =>
      request<void>('/settings', {
        method: 'PUT',
        body: JSON.stringify(settings),
      }),
    getBackups: () => request<string[]>('/settings/backups'),
    createBackup: () => request<{ fileName: string; path: string }>('/settings/backups', { method: 'POST' }),
  },

  // Notifications
  notifications: {
    getMyNotifications: async (): Promise<Notification[]> => {
      try {
        const res = await request<any>('/notifications');
        if (Array.isArray(res)) return res;
        if (res && Array.isArray(res.notifications)) return res.notifications;
        return [];
      } catch {
        return [];
      }
    },
    markRead: (id: number) => request<void>(`/notifications/${id}/read`, { method: 'POST' }),
    markAllRead: () => request<void>('/notifications/read-all', { method: 'POST' }),
  },
};
