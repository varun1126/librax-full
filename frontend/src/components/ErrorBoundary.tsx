import React, { Component, ErrorInfo, ReactNode } from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
    errorInfo: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error, errorInfo: null };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('LibraX Uncaught Component Error:', error, errorInfo);
    this.setState({ error, errorInfo });
  }

  private handleReset = () => {
    localStorage.removeItem('librax_token');
    localStorage.removeItem('librax_user');
    localStorage.removeItem('librax_member');
    window.location.reload();
  };

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen w-full flex items-center justify-center p-6 bg-zinc-900 text-zinc-100">
          <div className="max-w-md w-full p-6 rounded-xl border border-zinc-800 bg-zinc-950 text-center space-y-4 shadow-xl">
            <div className="inline-flex items-center justify-center w-12 h-12 rounded-lg bg-red-950/60 text-red-400 border border-red-800/50">
              <AlertTriangle className="w-6 h-6" />
            </div>

            <div className="space-y-1">
              <div className="text-xs font-mono tracking-widest text-zinc-500 uppercase">
                LIBRAX
              </div>
              <h1 className="text-lg font-semibold text-zinc-100">
                Something went wrong
              </h1>
              <p className="text-xs text-zinc-400 leading-relaxed">
                An unexpected interface error occurred. You can reload the application or reset your session.
              </p>
            </div>

            {this.state.error && (
              <div className="p-3 rounded-lg bg-zinc-900 border border-zinc-800 text-left overflow-x-auto text-[11px] font-mono text-red-400 max-h-32">
                {this.state.error.toString()}
              </div>
            )}

            <button
              onClick={this.handleReset}
              className="inline-flex items-center justify-center gap-2 w-full py-2 px-4 rounded-lg bg-zinc-100 text-zinc-900 hover:bg-zinc-200 text-xs font-semibold cursor-pointer transition-colors"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              <span>Try Again</span>
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
