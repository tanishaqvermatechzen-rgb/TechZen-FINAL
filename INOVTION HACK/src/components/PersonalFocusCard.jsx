import React from 'react';
import { Sparkles, ArrowUpRight, Circle, CheckCircle2 } from 'lucide-react';

export default function PersonalFocusCard({ onOpenTasks }) {
  return (
    <div className="fieldnote-accent-card p-6 rounded-2xl space-y-4 relative overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between">
        <span className="text-[10px] font-extrabold uppercase tracking-widest text-[#171E2D]/70">
          Personal Focus
        </span>
        <div className="w-7 h-7 rounded-full bg-[#171E2D]/10 flex items-center justify-center text-[#171E2D]">
          <Sparkles className="w-3.5 h-3.5" />
        </div>
      </div>

      {/* Headline & Body */}
      <div className="space-y-2">
        <h3 className="text-xl font-extrabold text-[#171E2D] tracking-tight leading-tight">
          Make the next move count.
        </h3>
        <p className="text-xs font-medium text-[#171E2D]/80 leading-relaxed">
          Your highest-leverage task is waiting in Atlas UI. It's small enough to finish before lunch.
        </p>
      </div>

      {/* Inner White Task Card */}
      <div className="p-3.5 bg-white rounded-xl border border-black/5 flex items-center space-x-3 shadow-2xs">
        <Circle className="w-4 h-4 text-slate-300 shrink-0" />
        <div className="min-w-0 flex-1">
          <div className="text-xs font-bold text-[#171E2D] truncate">Polish empty states</div>
          <div className="text-[10px] font-bold text-slate-400">ATLAS UI • HIGH PRIORITY</div>
        </div>
      </div>

      {/* Footer Link */}
      <button
        onClick={onOpenTasks}
        className="text-xs font-bold text-[#171E2D] hover:underline flex items-center gap-1 pt-1"
      >
        <span>Open task workspace</span>
        <ArrowUpRight className="w-3.5 h-3.5" />
      </button>
    </div>
  );
}
