import React, { useState, useRef, useEffect } from 'react';
import {
  Send,
  Sparkles,
  BookOpen,
  RotateCcw,
  Minimize2,
  ChevronRight,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { api } from '../../services/api';
import type { Book } from '../../types';
import { clsx } from 'clsx';

interface Message {
  id: string;
  sender: 'user' | 'assistant';
  text: string;
  books?: Book[];
  actions?: { label: string; view: string }[];
  timestamp: string;
}

interface AiLibrarianChatProps {
  onNavigate: (view: string) => void;
}

export const AiLibrarianChat: React.FC<AiLibrarianChatProps> = ({ onNavigate }) => {
  const { user, member } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const [input, setInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const studentName = user?.fullName || 'Student';
  const memberId = member?.memberId || user?.studentId || 'LIB-STU-XXXX';

  const initialMessages: Message[] = [
    {
      id: 'welcome-1',
      sender: 'assistant',
      text: `Hello ${studentName}! I am your AI Library Assistant. Ask me how to issue books, check catalog availability, understand loan rules, or navigate anywhere in LibraX.`,
      actions: [
        { label: 'Browse Catalog', view: user?.role === 'STUDENT' ? 'student-catalog' : 'books' },
        { label: 'Active Loans', view: user?.role === 'STUDENT' ? 'student-loans' : 'loans' },
      ],
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ];

  const [messages, setMessages] = useState<Message[]>(initialMessages);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, isOpen]);

  const quickPrompts = [
    'How do I issue a book?',
    'Search for Java & Python books',
    'What is the fine policy?',
    'How do I renew my borrowed books?',
  ];

  const processUserQuery = async (queryText: string) => {
    const q = queryText.toLowerCase().trim();
    const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    setIsTyping(true);

    // Simulate AI thinking micro-delay
    await new Promise((r) => setTimeout(r, 600));

    // 1. Issue Book query
    if (q.includes('issue') || q.includes('borrow') || q.includes('check out') || q.includes('get a book')) {
      setIsTyping(false);
      setMessages((prev) => [
        ...prev,
        {
          id: String(Date.now()),
          sender: 'assistant',
          text: `Here is the step-by-step procedure to issue a book:
1. **Find Book**: Look up your title in the Catalog and note its shelf location.
2. **Present Card**: Your Student Card ID is **${memberId}**.
3. **Desk Checkout**: The librarian or admin will scan the book barcode to your account.
4. **Loan Limit**: You can borrow up to 5 books for a standard period of 14 days.`,
          actions: [
            { label: 'Open Book Catalog', view: user?.role === 'STUDENT' ? 'student-catalog' : 'books' },
            { label: 'View Active Loans', view: user?.role === 'STUDENT' ? 'student-loans' : 'loans' },
          ],
          timestamp: time,
        },
      ]);
      return;
    }

    // 2. Renew / Return query
    if (q.includes('renew') || q.includes('return') || q.includes('extend')) {
      setIsTyping(false);
      setMessages((prev) => [
        ...prev,
        {
          id: String(Date.now()),
          sender: 'assistant',
          text: `**Renewal & Return Guidelines**:
• You can renew active loans up to **2 times** directly from your Student Portal under **Loans**.
• If another student has placed a reservation on the title, renewals are restricted.
• To return physical books, simply hand them to the circulation desk.`,
          actions: [
            { label: 'Manage Loans', view: user?.role === 'STUDENT' ? 'student-loans' : 'loans' },
          ],
          timestamp: time,
        },
      ]);
      return;
    }

    // 3. Fines / Policy query
    if (q.includes('fine') || q.includes('penalty') || q.includes('fee') || q.includes('late')) {
      setIsTyping(false);
      setMessages((prev) => [
        ...prev,
        {
          id: String(Date.now()),
          sender: 'assistant',
          text: `**LibraX Fines Policy**:
• Standard borrowing duration is **14 days**.
• Overdue books accumulate a rate of **₹1.00 per day**.
• You can settle dues in the **Fines & Penalties** tab using UPI, Cash, or Cards at the desk.`,
          actions: [
            { label: 'Check Fines', view: user?.role === 'STUDENT' ? 'student-fines' : 'fines' },
          ],
          timestamp: time,
        },
      ]);
      return;
    }

    // 4. Search query (e.g. search books, python, java, algorithms, clean code, etc.)
    let searchTerm = queryText.replace(/search|find|show me|look for|books?|about|for/gi, '').trim();
    if (!searchTerm) searchTerm = queryText.trim();

    try {
      const res = await api.books.getAll({ search: searchTerm, size: 4 });
      const booksFound = res.books || [];

      setIsTyping(false);

      if (booksFound.length > 0) {
        setMessages((prev) => [
          ...prev,
          {
            id: String(Date.now()),
            sender: 'assistant',
            text: `I located ${booksFound.length} matching title(s) in our library catalog for "${searchTerm}":`,
            books: booksFound,
            actions: [
              { label: 'View Full Catalog', view: user?.role === 'STUDENT' ? 'student-catalog' : 'books' },
            ],
            timestamp: time,
          },
        ]);
      } else {
        setMessages((prev) => [
          ...prev,
          {
            id: String(Date.now()),
            sender: 'assistant',
            text: `I could not find titles matching "${searchTerm}" currently in our physical catalog. You can ask an administrator to import it from Google Books or check spelling.`,
            actions: [
              { label: 'Explore All Titles', view: user?.role === 'STUDENT' ? 'student-catalog' : 'books' },
            ],
            timestamp: time,
          },
        ]);
      }
    } catch {
      setIsTyping(false);
      setMessages((prev) => [
        ...prev,
        {
          id: String(Date.now()),
          sender: 'assistant',
          text: `I am here to help you navigate LibraX. You can explore books in the catalog, track borrowed items, or view your library profile anytime.`,
          actions: [
            { label: 'Browse Catalog', view: user?.role === 'STUDENT' ? 'student-catalog' : 'books' },
          ],
          timestamp: time,
        },
      ]);
    }
  };

  const handleSend = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!input.trim() || isTyping) return;

    const userText = input.trim();
    setInput('');

    const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    setMessages((prev) => [
      ...prev,
      {
        id: String(Date.now()),
        sender: 'user',
        text: userText,
        timestamp: time,
      },
    ]);

    await processUserQuery(userText);
  };

  const handleChipClick = async (prompt: string) => {
    const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    setMessages((prev) => [
      ...prev,
      {
        id: String(Date.now()),
        sender: 'user',
        text: prompt,
        timestamp: time,
      },
    ]);
    await processUserQuery(prompt);
  };

  const handleResetChat = () => {
    setMessages(initialMessages);
  };

  return (
    <>
      {/* Floating Launcher Trigger */}
      {!isOpen && (
        <button
          type="button"
          onClick={() => setIsOpen(true)}
          className="fixed bottom-6 right-6 z-40 flex items-center gap-2.5 px-4 py-3 rounded-full bg-zinc-900 text-zinc-50 dark:bg-zinc-100 dark:text-zinc-900 shadow-lg hover:shadow-xl hover:scale-105 active:scale-95 transition-all duration-200 cursor-pointer border border-zinc-700/30 dark:border-zinc-300/30 group"
          aria-label="Open AI Library Assistant"
        >
          <div className="relative">
            <Sparkles className="w-4 h-4 text-amber-400 animate-subtle-pulse" />
          </div>
          <span className="text-xs font-semibold tracking-tight">AI Assistant</span>
          <span className="w-2 h-2 rounded-full bg-emerald-500 ring-2 ring-emerald-400/40 animate-pulse" />
        </button>
      )}

      {/* Floating Chat Drawer / Popout */}
      {isOpen && (
        <div className="fixed bottom-6 right-6 z-50 w-96 max-w-[calc(100vw-2rem)] h-[560px] max-h-[85vh] flex flex-col surface-card bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 shadow-2xl rounded-2xl overflow-hidden animate-slide-up-fade">
          {/* Header */}
          <div className="p-3.5 border-b border-zinc-200 dark:border-zinc-800 flex items-center justify-between bg-zinc-50 dark:bg-zinc-950/60">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-zinc-900 text-zinc-100 dark:bg-zinc-100 dark:text-zinc-900 flex items-center justify-center font-bold text-xs shadow-xs">
                <Sparkles className="w-4 h-4 text-amber-400" />
              </div>
              <div>
                <h3 className="text-xs font-bold text-zinc-900 dark:text-zinc-100 flex items-center gap-1.5">
                  LibraX Assistant
                  <span className="text-[10px] font-normal text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                    Online
                  </span>
                </h3>
                <p className="text-[10px] text-zinc-500 dark:text-zinc-400">
                  Student Navigation & Issue Helper
                </p>
              </div>
            </div>

            <div className="flex items-center gap-1 text-zinc-500 dark:text-zinc-400">
              <button
                type="button"
                onClick={handleResetChat}
                title="Reset conversation"
                className="p-1.5 rounded-md hover:bg-zinc-200 dark:hover:bg-zinc-800 cursor-pointer transition-colors"
              >
                <RotateCcw className="w-3.5 h-3.5" />
              </button>
              <button
                type="button"
                onClick={() => setIsOpen(false)}
                title="Minimize assistant"
                className="p-1.5 rounded-md hover:bg-zinc-200 dark:hover:bg-zinc-800 cursor-pointer transition-colors"
              >
                <Minimize2 className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>

          {/* Conversation Stream */}
          <div className="flex-1 p-3.5 overflow-y-auto space-y-3.5 text-xs">
            {messages.map((m) => (
              <div
                key={m.id}
                className={clsx(
                  'flex flex-col',
                  m.sender === 'user' ? 'items-end' : 'items-start'
                )}
              >
                <div
                  className={clsx(
                    'max-w-[85%] rounded-2xl p-3 leading-relaxed whitespace-pre-line shadow-xs',
                    m.sender === 'user'
                      ? 'bg-zinc-900 text-zinc-100 dark:bg-zinc-100 dark:text-zinc-900 rounded-br-xs'
                      : 'bg-zinc-100 dark:bg-zinc-800/80 text-zinc-800 dark:text-zinc-200 rounded-bl-xs border border-zinc-200 dark:border-zinc-700/60'
                  )}
                >
                  {m.text}
                </div>

                {/* Embedded Books Cards */}
                {m.books && m.books.length > 0 && (
                  <div className="w-full mt-2 space-y-1.5">
                    {m.books.map((b) => (
                      <div
                        key={b.id}
                        className="p-2 rounded-xl bg-zinc-50 dark:bg-zinc-800/50 border border-zinc-200 dark:border-zinc-700 flex items-center justify-between gap-2"
                      >
                        <div className="flex items-center gap-2 min-w-0">
                          <div className="w-7 h-9 rounded bg-zinc-200 dark:bg-zinc-700 flex items-center justify-center shrink-0 text-zinc-500">
                            <BookOpen className="w-3.5 h-3.5" />
                          </div>
                          <div className="min-w-0">
                            <h4 className="font-semibold text-[11px] text-zinc-900 dark:text-zinc-100 truncate">
                              {b.title}
                            </h4>
                            <p className="text-[10px] text-zinc-500 truncate">by {b.author}</p>
                          </div>
                        </div>

                        <span className={clsx(
                          'text-[9px] px-1.5 py-0.5 rounded-full font-mono shrink-0',
                          b.availableCopies > 0
                            ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400'
                            : 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400'
                        )}>
                          {b.availableCopies > 0 ? `${b.availableCopies} avail` : 'Out of stock'}
                        </span>
                      </div>
                    ))}
                  </div>
                )}

                {/* Action Navigation Buttons */}
                {m.actions && m.actions.length > 0 && (
                  <div className="flex flex-wrap gap-1.5 mt-2">
                    {m.actions.map((act, i) => (
                      <button
                        key={i}
                        type="button"
                        onClick={() => {
                          onNavigate(act.view);
                          setIsOpen(false);
                        }}
                        className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-[11px] font-medium bg-zinc-200 dark:bg-zinc-800 hover:bg-zinc-300 dark:hover:bg-zinc-700 text-zinc-800 dark:text-zinc-200 transition-colors cursor-pointer"
                      >
                        <span>{act.label}</span>
                        <ChevronRight className="w-3 h-3 opacity-70" />
                      </button>
                    ))}
                  </div>
                )}

                <span className="text-[9px] text-zinc-400 dark:text-zinc-500 mt-1 px-1">
                  {m.timestamp}
                </span>
              </div>
            ))}

            {/* Typing Indicator */}
            {isTyping && (
              <div className="flex items-center gap-1.5 p-2.5 rounded-2xl bg-zinc-100 dark:bg-zinc-800/80 w-16 border border-zinc-200 dark:border-zinc-700/60">
                <span className="w-1.5 h-1.5 rounded-full bg-zinc-500 dot-1" />
                <span className="w-1.5 h-1.5 rounded-full bg-zinc-500 dot-2" />
                <span className="w-1.5 h-1.5 rounded-full bg-zinc-500 dot-3" />
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Quick Suggestions Chips */}
          <div className="px-3 py-2 border-t border-zinc-100 dark:border-zinc-800/80 bg-zinc-50/50 dark:bg-zinc-950/40 flex items-center gap-1.5 overflow-x-auto text-[10px] no-scrollbar">
            {quickPrompts.map((p, i) => (
              <button
                key={i}
                type="button"
                onClick={() => handleChipClick(p)}
                className="shrink-0 px-2.5 py-1 rounded-full bg-white dark:bg-zinc-800 border border-zinc-200 dark:border-zinc-700 text-zinc-600 dark:text-zinc-300 hover:border-zinc-400 dark:hover:border-zinc-500 cursor-pointer transition-colors"
              >
                {p}
              </button>
            ))}
          </div>

          {/* Input Form */}
          <form
            onSubmit={handleSend}
            className="p-2.5 border-t border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 flex items-center gap-2"
          >
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Ask anything (e.g. 'issue a book', 'search Python')..."
              className="flex-1 bg-zinc-100 dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 placeholder-zinc-400 text-xs px-3 py-2 rounded-lg border-none focus:outline-none focus:ring-1 focus:ring-zinc-400 dark:focus:ring-zinc-600"
            />
            <button
              type="submit"
              disabled={!input.trim() || isTyping}
              className="p-2 rounded-lg bg-zinc-900 text-zinc-100 dark:bg-zinc-100 dark:text-zinc-900 disabled:opacity-40 disabled:cursor-not-allowed hover:opacity-90 cursor-pointer transition-opacity"
            >
              <Send className="w-3.5 h-3.5" />
            </button>
          </form>
        </div>
      )}
    </>
  );
};
