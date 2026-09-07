import React from 'react';
import { SearchX, RefreshCw } from 'lucide-react';

export default function EmptyState({ searchQuery, onReset }) {
  return (
    <div className="min-h-[400px] flex flex-col items-center justify-center p-8 rounded-2xl bg-white border border-dashed border-slate-300 text-center animate-in fade-in duration-300 shadow-sm">
      <div className="w-16 h-16 rounded-2xl bg-indigo-50 border border-indigo-100 flex items-center justify-center text-indigo-600 mb-4 shadow-sm">
        <SearchX className="w-8 h-8" />
      </div>

      <h3 className="text-lg font-bold text-slate-900 tracking-tight">No Matching Results Found</h3>
      <p className="text-xs text-slate-600 max-w-sm mt-1 leading-relaxed">
        We couldn't find any projects, tasks, or tags matching <span className="text-indigo-600 font-mono font-semibold">"{searchQuery || 'your query'}"</span>.
      </p>

      <button
        onClick={onReset}
        className="mt-6 flex items-center space-x-2 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold shadow-md shadow-indigo-600/20 transition-all"
      >
        <RefreshCw className="w-3.5 h-3.5" />
        <span>Reset Search & Filters</span>
      </button>
    </div>
  );
}
