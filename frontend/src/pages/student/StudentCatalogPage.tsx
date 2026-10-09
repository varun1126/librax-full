import React, { useState, useEffect } from 'react';
import { Search, BookOpen, BookmarkCheck, BookmarkX } from 'lucide-react';
import { api } from '../../services/api';
import type { Book } from '../../types';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { GlassCard } from '../../components/ui/GlassCard';

export const StudentCatalogPage: React.FC = () => {
  const [books, setBooks] = useState<Book[]>([]);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [reservingId, setReservingId] = useState<number | null>(null);
  const [unholdingId, setUnholdingId] = useState<number | null>(null);
  const [borrowingId, setBorrowingId] = useState<number | null>(null);
  const [myHolds, setMyHolds] = useState<Record<number, number>>({});

  const fetchHolds = React.useCallback(async () => {
    try {
      const list = await api.reservations.getMyReservations();
      const holdMap: Record<number, number> = {};
      (list || []).forEach((r) => {
        if (r.status === 'WAITING' || r.status === 'READY') {
          holdMap[r.bookId] = r.id;
        }
      });
      setMyHolds(holdMap);
    } catch (e) {
      console.error(e);
    }
  }, []);

  const fetchBooks = React.useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await api.books.getAll({
        search: search.trim() || undefined,
        page,
        size: 12,
      });
      setBooks(res.books || []);
      setTotal(res.total || 0);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  }, [search, page]);

  useEffect(() => {
    fetchBooks();
    fetchHolds();
  }, [fetchBooks, fetchHolds]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    fetchBooks();
  };

  const handleBorrow = async (bookId: number) => {
    setBorrowingId(bookId);
    try {
      await api.loans.issue({ bookId });
      alert('Book successfully issued! It has been added to your Active Loans.');
      fetchBooks();
      fetchHolds();
    } catch (e: any) {
      alert(e.message || 'Failed to issue book');
    } finally {
      setBorrowingId(null);
    }
  };

  const handleReserve = async (bookId: number) => {
    setReservingId(bookId);
    try {
      await api.reservations.reserve(bookId);
      alert('Hold placed successfully! You will be notified when this copy is ready for checkout.');
      fetchBooks();
      fetchHolds();
    } catch (e: any) {
      alert(e.message || 'Failed to place reservation hold');
    } finally {
      setReservingId(null);
    }
  };

  const handleUnhold = async (bookId: number) => {
    const resId = myHolds[bookId];
    if (!resId) return;
    if (!confirm('Are you sure you want to remove your hold on this book?')) return;
    setUnholdingId(bookId);
    try {
      await api.reservations.cancel(resId);
      alert('Hold cancelled successfully.');
      fetchBooks();
      fetchHolds();
    } catch (e: any) {
      alert(e.message || 'Failed to cancel hold');
    } finally {
      setUnholdingId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-black text-[var(--text-primary)]">Library Catalog Search</h2>
          <p className="text-xs text-[var(--text-secondary)]">
            Explore academic resources, reference works and place hold requests
          </p>
        </div>
        {total > 0 && (
          <div className="text-xs font-mono text-[var(--text-secondary)] px-3 py-1.5 rounded-lg bg-[var(--bg-secondary)] border border-[var(--border-color)]">
            Catalog: {total} titles
          </div>
        )}
      </div>

      {/* Search Bar */}
      <form onSubmit={handleSearch} className="flex gap-3">
        <div className="flex-1">
          <Input
            placeholder="Search by book title, author, category or ISBN..."
            leftIcon={<Search className="w-4 h-4" />}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <Button type="submit" variant="primary">
          Search Catalog
        </Button>
      </form>

      {/* Books Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 animate-pulse">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="h-64 rounded-2xl bg-[var(--bg-secondary)]" />
          ))}
        </div>
      ) : books.length === 0 ? (
        <div className="py-16 text-center text-xs text-[var(--text-muted)]">
          No catalog titles found matching your search.
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {books.map((book) => {
            const hasAvailable = book.availableCopies > 0;
            return (
              <GlassCard key={book.id} className="flex flex-col justify-between group">
                <div className="space-y-3">
                  <div className="flex items-start gap-3">
                    {book.coverImageUrl ? (
                      <img
                        src={book.coverImageUrl}
                        alt={book.title}
                        className="w-14 h-20 object-cover rounded-xl border border-[var(--border-color)] shrink-0"
                      />
                    ) : (
                      <div className="w-14 h-20 rounded-xl bg-[var(--bg-secondary)] border border-[var(--border-color)] flex items-center justify-center shrink-0 text-cyan-400">
                        <BookOpen className="w-6 h-6" />
                      </div>
                    )}
                    <div className="space-y-1 overflow-hidden">
                      <Badge variant="neutral" size="sm">
                        {book.category || 'General'}
                      </Badge>
                      <h4 className="font-bold text-sm text-[var(--text-primary)] line-clamp-2">
                        {book.title}
                      </h4>
                      <p className="text-xs text-[var(--text-secondary)] truncate">
                        by {book.author}
                      </p>
                    </div>
                  </div>

                  <p className="text-xs text-[var(--text-muted)] line-clamp-2">
                    {book.description || 'No summary provided.'}
                  </p>
                </div>

                <div className="mt-4 pt-3 border-t border-[var(--border-color)] flex items-center justify-between gap-2">
                  <div className="space-y-0.5 min-w-0">
                    <div className="text-[10px] text-[var(--text-muted)] font-mono truncate">
                      Location: {book.shelf || 'Section A'}
                    </div>
                    <Badge variant={hasAvailable ? 'success' : 'danger'} size="sm" dot>
                      {hasAvailable ? `${book.availableCopies} available` : 'On Loan'}
                    </Badge>
                  </div>

                  <div className="flex items-center gap-1.5 shrink-0">
                    {hasAvailable ? (
                      <Button
                        variant="primary"
                        size="sm"
                        isLoading={borrowingId === book.id}
                        onClick={() => handleBorrow(book.id)}
                        leftIcon={<BookOpen className="w-3.5 h-3.5" />}
                        className="h-8 text-xs font-semibold"
                      >
                        Issue Book
                      </Button>
                    ) : myHolds[book.id] ? (
                      <Button
                        variant="outline"
                        size="sm"
                        isLoading={unholdingId === book.id}
                        onClick={() => handleUnhold(book.id)}
                        leftIcon={<BookmarkX className="w-3.5 h-3.5 text-rose-400" />}
                        className="h-8 text-xs font-semibold text-rose-400 hover:text-rose-300 border-rose-500/30 hover:border-rose-500/60"
                      >
                        Unhold Book
                      </Button>
                    ) : (
                      <Button
                        variant="secondary"
                        size="sm"
                        isLoading={reservingId === book.id}
                        onClick={() => handleReserve(book.id)}
                        leftIcon={<BookmarkCheck className="w-3.5 h-3.5" />}
                        className="h-8 text-xs font-semibold"
                      >
                        Hold / Reserve
                      </Button>
                    )}
                  </div>
                </div>
              </GlassCard>
            );
          })}
        </div>
      )}
    </div>
  );
};
