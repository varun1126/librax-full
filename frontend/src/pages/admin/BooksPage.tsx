import React, { useState, useEffect } from 'react';
import {
  BookOpen,
  Search,
  Plus,
  Globe,
  Trash2,
  Edit2,
  Layers,
} from 'lucide-react';
import { api } from '../../services/api';
import type { Book, BookCopy } from '../../types';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Modal } from '../../components/ui/Modal';
import { DataTable, Column } from '../../components/ui/DataTable';

export const BooksPage: React.FC = () => {
  const [books, setBooks] = useState<Book[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  // Manual Add / Edit Modal
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingBook, setEditingBook] = useState<Partial<Book> | null>(null);
  const [editLoading, setEditLoading] = useState(false);

  // Google Import Modal
  const [isGoogleModalOpen, setIsGoogleModalOpen] = useState(false);
  const [googleQuery, setGoogleQuery] = useState('');
  const [googleResults, setGoogleResults] = useState<any[]>([]);
  const [googleLoading, setGoogleLoading] = useState(false);
  const [importingId, setImportingId] = useState<string | null>(null);

  // Copies Modal
  const [isCopiesModalOpen, setIsCopiesModalOpen] = useState(false);
  const [selectedBookForCopies, setSelectedBookForCopies] = useState<Book | null>(null);
  const [copies, setCopies] = useState<BookCopy[]>([]);
  const [newCopyShelf, setNewCopyShelf] = useState('');
  const [newCopyCondition, setNewCopyCondition] = useState<'NEW' | 'GOOD' | 'FAIR' | 'POOR'>('NEW');
  const [copiesLoading, setCopiesLoading] = useState(false);

  const fetchBooks = React.useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await api.books.getAll({
        search: search.trim() || undefined,
        category: category || undefined,
        page: page,
        size: 10,
      });
      setBooks(res.books || []);
      setTotal(res.total || 0);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  }, [search, category, page]);

  useEffect(() => {
    fetchBooks();
  }, [fetchBooks]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    fetchBooks();
  };

  const handleSaveBook = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingBook) return;
    setEditLoading(true);
    try {
      if (editingBook.id) {
        await api.books.update(editingBook.id, editingBook);
      } else {
        await api.books.create(editingBook);
      }
      setIsEditModalOpen(false);
      setEditingBook(null);
      fetchBooks();
    } catch (e: any) {
      alert(e.message || 'Failed to save book');
    } finally {
      setEditLoading(false);
    }
  };

  const handleDeleteBook = async (id: number) => {
    if (!confirm('Are you sure you want to delete this book?')) return;
    try {
      await api.books.delete(id);
      fetchBooks();
    } catch (e: any) {
      alert(e.message || 'Failed to delete book');
    }
  };

  // Google Search
  const handleGoogleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!googleQuery.trim()) return;
    setGoogleLoading(true);
    try {
      const results = await api.books.searchGoogle(googleQuery);
      setGoogleResults(results || []);
    } catch (e: any) {
      alert(e.message || 'Google Books search failed');
    } finally {
      setGoogleLoading(false);
    }
  };

  const handleImportGoogleBook = async (bookItem: any) => {
    const key = bookItem.id || bookItem.isbn || bookItem.title;
    setImportingId(key);
    try {
      await api.books.importGoogle(bookItem, 1);
      alert(`"${bookItem.title || 'Book'}" imported into catalog successfully!`);
      fetchBooks();
    } catch (e: any) {
      alert(e.message || 'Failed to import book');
    } finally {
      setImportingId(null);
    }
  };

  // Physical Copies
  const openCopiesModal = async (book: Book) => {
    setSelectedBookForCopies(book);
    setIsCopiesModalOpen(true);
    setCopiesLoading(true);
    try {
      const list = await api.books.getCopies(book.id);
      setCopies(list || []);
    } catch (e) {
      console.error(e);
    } finally {
      setCopiesLoading(false);
    }
  };

  const handleAddCopy = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedBookForCopies) return;
    try {
      await api.books.addCopy(selectedBookForCopies.id, {
        shelfLocation: newCopyShelf,
        condition: newCopyCondition,
        status: 'AVAILABLE',
      });
      const list = await api.books.getCopies(selectedBookForCopies.id);
      setCopies(list || []);
      setNewCopyShelf('');
      fetchBooks();
    } catch (e: any) {
      alert(e.message || 'Failed to add copy');
    }
  };

  const columns: Column<Book>[] = [
    {
      header: 'Title & Author',
      cell: (b) => (
        <div className="flex items-center gap-3">
          {b.coverImageUrl ? (
            <img
              src={b.coverImageUrl}
              alt={b.title}
              className="w-9 h-12 object-cover rounded-lg border border-[var(--border-color)] shrink-0"
            />
          ) : (
            <div className="w-9 h-12 rounded-lg bg-[var(--bg-card)] border border-[var(--border-color)] flex items-center justify-center shrink-0 text-[var(--text-muted)]">
              <BookOpen className="w-4 h-4" />
            </div>
          )}
          <div className="space-y-0.5">
            <div className="font-bold text-[var(--text-primary)] text-sm">{b.title}</div>
            <div className="text-xs text-[var(--text-secondary)]">by {b.author}</div>
            <div className="text-[10px] text-[var(--text-muted)] font-mono">ISBN: {b.isbn}</div>
          </div>
        </div>
      ),
    },
    {
      header: 'Category',
      cell: (b) => (
        <Badge variant="neutral" size="sm">
          {b.category || 'General'}
        </Badge>
      ),
    },
    {
      header: 'Shelf / Location',
      cell: (b) => (
        <span className="font-mono text-xs text-[var(--text-secondary)]">
          {b.shelf || b.location || 'Section A'}
        </span>
      ),
    },
    {
      header: 'Stock Availability',
      cell: (b) => {
        const available = b.availableCopies;
        const total = b.totalCopies;
        return (
          <div className="space-y-1">
            <div className="flex items-center gap-1.5">
              <span className="font-bold text-xs">{available}</span>
              <span className="text-[10px] text-[var(--text-muted)]">/ {total} available</span>
            </div>
            <Badge
              variant={available > 0 ? 'success' : 'danger'}
              size="sm"
              dot
            >
              {available > 0 ? 'In Stock' : 'Out of Stock'}
            </Badge>
          </div>
        );
      },
    },
    {
      header: 'Actions',
      className: 'text-right',
      cell: (b) => (
        <div className="flex items-center justify-end gap-1.5">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => openCopiesModal(b)}
            title="Manage Copies"
          >
            <Layers className="w-4 h-4 text-cyan-400" />
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setEditingBook(b);
              setIsEditModalOpen(true);
            }}
            title="Edit Book"
          >
            <Edit2 className="w-4 h-4 text-amber-400" />
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => handleDeleteBook(b.id)}
            title="Delete Book"
          >
            <Trash2 className="w-4 h-4 text-rose-400" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-black text-[var(--text-primary)]">Books & Inventory Catalog</h2>
          <p className="text-xs text-[var(--text-secondary)]">
            Manage titles, physical barcodes, editions and live Google Books integration
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="secondary"
            size="sm"
            leftIcon={<Globe className="w-4 h-4 text-cyan-400" />}
            onClick={() => setIsGoogleModalOpen(true)}
          >
            Google Books Import
          </Button>
          <Button
            variant="primary"
            size="sm"
            leftIcon={<Plus className="w-4 h-4" />}
            onClick={() => {
              setEditingBook({
                isbn: '',
                title: '',
                author: '',
                category: 'Computer Science',
                totalCopies: 1,
                availableCopies: 1,
                shelf: 'Floor 1 - Row A',
              });
              setIsEditModalOpen(true);
            }}
          >
            New Book Title
          </Button>
        </div>
      </div>

      {/* Filter Bar */}
      <form onSubmit={handleSearchSubmit} className="flex flex-col sm:flex-row gap-3">
        <div className="flex-1">
          <Input
            placeholder="Search by title, author, ISBN..."
            leftIcon={<Search className="w-4 h-4" />}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="flex gap-2">
          <Button type="submit" variant="secondary" size="md">
            Filter
          </Button>
          {(search || category) && (
            <Button
              type="button"
              variant="outline"
              size="md"
              onClick={() => {
                setSearch('');
                setCategory('');
                setPage(1);
                fetchBooks();
              }}
            >
              Reset
            </Button>
          )}
        </div>
      </form>

      {/* Table */}
      <DataTable
        columns={columns}
        data={books}
        isLoading={isLoading}
        page={page}
        pageSize={10}
        total={total}
        onPageChange={setPage}
        emptyMessage="No catalog titles found matching query."
      />

      {/* Edit / Add Modal */}
      <Modal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        title={editingBook?.id ? 'Edit Book Record' : 'Add New Book'}
      >
        <form onSubmit={handleSaveBook} className="space-y-4">
          <Input
            label="ISBN Number"
            value={editingBook?.isbn || ''}
            onChange={(e) => setEditingBook({ ...editingBook, isbn: e.target.value })}
            required
            placeholder="e.g. 978-0134685991"
          />
          <Input
            label="Book Title"
            value={editingBook?.title || ''}
            onChange={(e) => setEditingBook({ ...editingBook, title: e.target.value })}
            required
            placeholder="e.g. Effective Java"
          />
          <Input
            label="Author"
            value={editingBook?.author || ''}
            onChange={(e) => setEditingBook({ ...editingBook, author: e.target.value })}
            required
            placeholder="e.g. Joshua Bloch"
          />
          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Category"
              value={editingBook?.category || ''}
              onChange={(e) => setEditingBook({ ...editingBook, category: e.target.value })}
              placeholder="Computer Science"
            />
            <Input
              label="Shelf Location"
              value={editingBook?.shelf || ''}
              onChange={(e) => setEditingBook({ ...editingBook, shelf: e.target.value })}
              placeholder="Floor 2 - Shelf B4"
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Initial Total Copies"
              type="number"
              min="1"
              value={editingBook?.totalCopies || 1}
              onChange={(e) =>
                setEditingBook({
                  ...editingBook,
                  totalCopies: Number(e.target.value),
                  availableCopies: Number(e.target.value),
                })
              }
              required
            />
            <Input
              label="Publish Year"
              type="number"
              value={editingBook?.publishYear || new Date().getFullYear()}
              onChange={(e) => setEditingBook({ ...editingBook, publishYear: Number(e.target.value) })}
            />
          </div>
          <Input
            label="Cover Image URL (optional)"
            value={editingBook?.coverImageUrl || ''}
            onChange={(e) => setEditingBook({ ...editingBook, coverImageUrl: e.target.value })}
            placeholder="https://..."
          />

          <div className="flex justify-end gap-2 pt-3">
            <Button type="button" variant="outline" onClick={() => setIsEditModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" isLoading={editLoading}>
              Save Book
            </Button>
          </div>
        </form>
      </Modal>

      {/* Google Books Search & Import Modal */}
      <Modal
        isOpen={isGoogleModalOpen}
        onClose={() => setIsGoogleModalOpen(false)}
        title="Google Books Search & Instant Import"
        description="Search Google's library catalog and instantly clone metadata into LibraX"
        size="xl"
      >
        <div className="space-y-4">
          <form onSubmit={handleGoogleSearch} className="flex gap-2">
            <Input
              placeholder="Search by keywords, title or author (e.g. 'Clean Architecture')..."
              value={googleQuery}
              onChange={(e) => setGoogleQuery(e.target.value)}
            />
            <Button type="submit" variant="primary" isLoading={googleLoading}>
              Search
            </Button>
          </form>

          <div className="max-h-96 overflow-y-auto divide-y divide-[var(--border-color)]">
            {googleLoading ? (
              <div className="py-12 text-center text-xs text-[var(--text-muted)] animate-pulse">
                Fetching Google Books catalog...
              </div>
            ) : googleResults.length === 0 ? (
              <div className="py-12 text-center text-xs text-[var(--text-muted)]">
                Type a search query to preview live Google Books
              </div>
            ) : (
              googleResults.map((item, idx) => {
                const thumb = item.thumbnailUrl || item.thumbnail;
                const authors = item.authorNames || item.authors || 'Unknown';
                const idKey = item.id || item.isbn || item.title || String(idx);
                return (
                  <div key={idKey} className="py-3 flex items-start justify-between gap-4">
                    <div className="flex items-start gap-3">
                      {thumb ? (
                        <img
                          src={thumb}
                          alt={item.title}
                          className="w-12 h-16 object-cover rounded border border-[var(--border-color)] shrink-0"
                        />
                      ) : (
                        <div className="w-12 h-16 bg-zinc-100 dark:bg-zinc-800 rounded flex items-center justify-center text-[var(--text-muted)] shrink-0">
                          <BookOpen className="w-5 h-5" />
                        </div>
                      )}
                      <div className="space-y-1">
                        <h4 className="text-sm font-bold text-[var(--text-primary)]">{item.title}</h4>
                        <p className="text-xs text-[var(--text-secondary)]">by {authors}</p>
                        <p className="text-[11px] text-[var(--text-muted)] line-clamp-2">
                          {item.description || 'No description available'}
                        </p>
                        <div className="text-[10px] text-[var(--text-muted)] font-mono">
                          ISBN: {item.isbn || 'Auto-generated'} • {item.publisher || 'Unknown Publisher'}
                        </div>
                      </div>
                    </div>

                    <Button
                      variant="outline"
                      size="sm"
                      className="shrink-0"
                      isLoading={importingId === idKey}
                      onClick={() => handleImportGoogleBook(item)}
                      leftIcon={<Plus className="w-3.5 h-3.5" />}
                    >
                      Import
                    </Button>
                  </div>
                );
              })
            )}
          </div>
        </div>
      </Modal>

      {/* Copies Modal */}
      <Modal
        isOpen={isCopiesModalOpen}
        onClose={() => setIsCopiesModalOpen(false)}
        title={`Physical Copies: ${selectedBookForCopies?.title}`}
        size="lg"
      >
        <div className="space-y-4">
          <form onSubmit={handleAddCopy} className="p-3 rounded-xl bg-[var(--bg-card)] border border-[var(--border-color)] flex items-end gap-3">
            <div className="flex-1">
              <Input
                label="New Copy Shelf Location"
                placeholder="e.g. Shelf A-3"
                value={newCopyShelf}
                onChange={(e) => setNewCopyShelf(e.target.value)}
                required
              />
            </div>
            <div className="w-36">
              <label className="block text-xs font-medium text-[var(--text-secondary)] mb-1.5">
                Condition
              </label>
              <select
                className="w-full bg-[var(--bg-secondary)] text-[var(--text-primary)] rounded-xl border border-[var(--border-color)] px-3 py-2 text-xs outline-none focus:border-[var(--accent-primary)]"
                value={newCopyCondition}
                onChange={(e) => setNewCopyCondition(e.target.value as any)}
              >
                <option value="NEW">New</option>
                <option value="GOOD">Good</option>
                <option value="FAIR">Fair</option>
                <option value="POOR">Poor</option>
              </select>
            </div>
            <Button type="submit" variant="primary" size="md">
              Add Copy
            </Button>
          </form>

          <div className="max-h-64 overflow-y-auto divide-y divide-[var(--border-color)]">
            {copiesLoading ? (
              <div className="py-6 text-center text-xs text-[var(--text-muted)] animate-pulse">
                Loading physical copies...
              </div>
            ) : copies.length === 0 ? (
              <div className="py-6 text-center text-xs text-[var(--text-muted)]">
                No physical copy records found for this title.
              </div>
            ) : (
              copies.map((copy) => (
                <div key={copy.id} className="py-2.5 flex items-center justify-between text-xs">
                  <div className="space-y-0.5">
                    <div className="font-mono font-bold text-[var(--text-primary)]">
                      Barcode: {copy.barcode}
                    </div>
                    <div className="text-[11px] text-[var(--text-secondary)]">
                      Copy #{copy.copyNumber} • Location: {copy.shelfLocation || 'Main Stacks'}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge variant="neutral" size="sm">
                      {copy.condition}
                    </Badge>
                    <Badge
                      variant={copy.status === 'AVAILABLE' ? 'success' : 'warning'}
                      size="sm"
                      dot
                    >
                      {copy.status}
                    </Badge>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </Modal>
    </div>
  );
};
