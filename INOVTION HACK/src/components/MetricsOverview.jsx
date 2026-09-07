import React from 'react';
import { Target, CheckCircle2, Clock, ArrowUpRight } from 'lucide-react';

export default function MetricsOverview({ metrics, onSelectTab }) {
  const openTasksVal = metrics?.tasksCompleted ? (metrics.tasksCompleted.total - metrics.tasksCompleted.value) : 6;
  const completedVal = metrics?.tasksCompleted?.value || 3;
  const totalVal = metrics?.tasksCompleted?.total || 9;
  
  // Format hours coded into "Xh Ym"
  const totalHours = parseFloat(metrics?.hoursCoded?.value || 4.33);
  const hrs = Math.floor(totalHours);
  const mins = Math.floor((totalHours % 1) * 60);
  const formattedFocusTime = `${hrs}h ${mins}m`;

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      
      {/* Metric 1: Open tasks */}
      <div 
        onClick={() => onSelectTab && onSelectTab('tasks')}
        className="fieldnote-card p-5 rounded-2xl flex flex-col justify-between space-y-4 hover:border-slate-300 transition-all cursor-pointer group"
      >
        <div className="flex items-center justify-between">
          <span className="text-xs font-semibold text-slate-500">Open tasks</span>
          <div className="w-8 h-8 rounded-full bg-[#E5F4C7] text-[#171E2D] flex items-center justify-center">
            <Target className="w-4 h-4 text-emerald-700" />
          </div>
        </div>

        <div>
          <div className="text-3xl font-extrabold text-[#171E2D] tracking-tight">{openTasksVal}</div>
          <div className="text-xs text-slate-500 font-medium mt-1">
            <span className="font-bold text-emerald-700">3 due soon</span> across 3 projects
          </div>
        </div>
      </div>

      {/* Metric 2: Completed this week */}
      <div 
        onClick={() => onSelectTab && onSelectTab('tasks')}
        className="fieldnote-card p-5 rounded-2xl flex flex-col justify-between space-y-4 hover:border-slate-300 transition-all cursor-pointer group"
      >
        <div className="flex items-center justify-between">
          <span className="text-xs font-semibold text-slate-500">Completed this week</span>
          <div className="w-8 h-8 rounded-full bg-emerald-50 text-emerald-700 flex items-center justify-center border border-emerald-100">
            <CheckCircle2 className="w-4 h-4" />
          </div>
        </div>

        <div>
          <div className="text-3xl font-extrabold text-[#171E2D] tracking-tight">
            {completedVal}<span className="text-xl text-slate-400 font-bold">/{totalVal}</span>
          </div>
          <div className="text-xs text-slate-500 font-medium mt-1">
            <span className="font-bold text-emerald-600">+18%</span> from last week
          </div>
        </div>
      </div>

      {/* Metric 3: Focus time */}
      <div 
        onClick={() => onSelectTab && onSelectTab('analytics')}
        className="fieldnote-card p-5 rounded-2xl flex flex-col justify-between space-y-4 hover:border-slate-300 transition-all cursor-pointer group"
      >
        <div className="flex items-center justify-between">
          <span className="text-xs font-semibold text-slate-500">Focus time</span>
          <div className="w-8 h-8 rounded-full bg-amber-50 text-amber-700 flex items-center justify-center border border-amber-100">
            <Clock className="w-4 h-4 text-amber-600" />
          </div>
        </div>

        <div>
          <div className="text-3xl font-extrabold text-[#171E2D] tracking-tight">{formattedFocusTime}</div>
          <div className="text-xs text-slate-500 font-medium mt-1">
            Your best focus window is <span className="font-bold text-[#171E2D]">now</span>
          </div>
        </div>
      </div>

    </div>
  );
}
