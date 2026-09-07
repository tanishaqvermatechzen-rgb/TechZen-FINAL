import React from 'react';
import { AlertOctagon, RotateCcw } from 'lucide-react';

export default function ErrorState({ onRetry }) {
  return (
    <div className="min-h-[400px] flex flex-col items-center justify-center p-8 rounded-2xl bg-rose-50/80 border border-rose-200 text-center animate-in fade-in duration-300 shadow-sm">
      <div className="w-16 h-16 rounded-2xl bg-rose-100 border border-rose-200 flex items-center justify-center text-rose-600 mb-4 shadow-sm">
        <AlertOctagon className="w-8 h-8" />
      </div>

      <h3 className="text-lg font-bold text-slate-900 tracking-tight">Failed to Fetch Productivity Stream</h3>
      <p className="text-xs text-rose-700 max-w-md mt-1 leading-relaxed">
        A temporary network connection error occurred while connecting to the telemetry gateway (503 Service Unavailable).
      </p>

      <button
        onClick={onRetry}
        className="mt-6 flex items-center space-x-2 px-4 py-2 rounded-xl bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold shadow-md shadow-rose-600/20 transition-all"
      >
        <RotateCcw className="w-3.5 h-3.5" />
        <span>Retry Stream Connection</span>
      </button>
    </div>
  );
}
