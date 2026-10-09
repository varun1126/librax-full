export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errorCode?: string;
  timestamp: string;
  requestId?: string;
}

export type UserRole = 'SUPER_ADMIN' | 'ADMIN' | 'LIBRARIAN' | 'STUDENT';
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'LOCKED';

export interface User {
  id: number;
  username: string;
  fullName: string;
  email: string;
  role: UserRole;
  status: UserStatus;
  studentId?: string;
  memberId?: number;
  lastLogin?: string;
  createdAt?: string;
  permissions?: string[];
}

export interface Member {
  id: number;
  memberId: string;
  name: string;
  email: string;
  phone: string;
  address?: string;
  memberType: 'STUDENT' | 'FACULTY' | 'STAFF' | 'GUEST';
  status: 'ACTIVE' | 'EXPIRED' | 'SUSPENDED';
  maxBooksAllowed: number;
  currentBorrowedCount: number;
  outstandingFines: number;
  registeredDate: string;
  expiryDate?: string;
  department?: string;
  course?: string;
  yearOfStudy?: number;
}

export interface Book {
  id: number;
  isbn: string;
  title: string;
  author: string;
  publisher?: string;
  publishYear?: number;
  category?: string;
  edition?: string;
  pages?: number;
  language?: string;
  description?: string;
  coverImageUrl?: string;
  totalCopies: number;
  availableCopies: number;
  tags?: string;
  shelf?: string;
  location?: string;
  status?: string;
}

export interface BookCopy {
  id: number;
  bookId: number;
  copyNumber: number;
  barcode: string;
  shelfLocation?: string;
  condition: 'NEW' | 'GOOD' | 'FAIR' | 'POOR' | 'DAMAGED';
  status: 'AVAILABLE' | 'LOANED' | 'RESERVED' | 'MAINTENANCE' | 'LOST';
  acquisitionDate?: string;
  price?: number;
}

export interface Loan {
  id: number;
  copyId: number;
  memberId: number;
  bookId: number;
  bookTitle?: string;
  memberName?: string;
  memberCode?: string;
  issueDate: string;
  dueDate: string;
  returnDate?: string;
  status: 'ACTIVE' | 'RETURNED' | 'OVERDUE' | 'LOST';
  renewalCount: number;
  notes?: string;
  fineAmount?: number;
}

export interface Reservation {
  id: number;
  bookId: number;
  memberId: number;
  bookTitle?: string;
  memberName?: string;
  memberCode?: string;
  reservationDate: string;
  expiryDate?: string;
  status: 'PENDING' | 'FULFILLED' | 'CANCELLED' | 'EXPIRED';
  queuePosition?: number;
  priorityScore?: number;
}

export interface Fine {
  id: number;
  loanId: number;
  memberId: number;
  memberName?: string;
  memberCode?: string;
  bookTitle?: string;
  amount: number;
  reason?: string;
  issueDate: string;
  paidDate?: string;
  status: 'PENDING' | 'PAID' | 'WAIVED';
}

export interface Payment {
  id: number;
  fineId?: number;
  memberId: number;
  memberName?: string;
  amount: number;
  paymentMethod: 'CASH' | 'CARD' | 'UPI' | 'ONLINE';
  paymentReference: string;
  paymentDate: string;
  notes?: string;
  cashierUsername?: string;
}

export interface Notification {
  id: number;
  userId: number;
  title: string;
  message: string;
  type: 'INFO' | 'SUCCESS' | 'WARNING' | 'DUE_REMINDER' | 'OVERDUE_ALERT' | 'RESERVATION_READY' | 'FINE_NOTICE';
  isRead: boolean;
  linkUrl?: string;
  createdAt: string;
}

export interface AuditLog {
  id: number;
  userId?: number;
  username?: string;
  action: string;
  entityType?: string;
  entityId?: string;
  details?: string;
  ipAddress?: string;
  timestamp: string;
}

export interface LoginHistory {
  id: number;
  username: string;
  success: boolean;
  ipAddress?: string;
  userAgent?: string;
  failureReason?: string;
  loginTime: string;
}

export interface DashboardStats {
  totalBooks: number;
  activeLoans: number;
  issuedCopies?: number;
  availableCopies?: number;
  overdueLoans: number;
  overdueBooks?: number;
  activeStudents: number;
  pendingReservations: number;
  reservationsCount?: number;
  unpaidFinesAmount: number;
  outstandingFines?: number;
  totalFinesCollected: number;
  lowStockCount: number;
  booksByCategory?: Record<string, number>;
  categoryStats?: { category: string; count: number }[];
  monthlyTrends?: any;
  recentActivity?: AuditLog[];
}

export interface StudentDashboardSummary {
  member: Member;
  activeLoans: Loan[];
  dueSoonCount: number;
  overdueCount: number;
  unpaidFines: Fine[];
  reservations: Reservation[];
  recentNotifications: Notification[];
  recommendations: Book[];
}

export interface RbacMatrix {
  roles: { id: number; name: string; description: string }[];
  permissions: { id: number; name: string; category: string; description: string }[];
  matrix: Record<string, string[]>;
}
