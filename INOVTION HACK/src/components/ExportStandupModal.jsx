import React, { useState } from 'react';
import { X, Copy, Check, FileText, Sparkles } from 'lucide-react';

export default function ExportStandupModal({ isOpen, onClose, userProfile, tasks = [], projects = [], metrics = {} }) {
  const [copied, setCopied] = useState(false);

  if (!isOpen) return null;

  const todayStr = new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  const completedTasks = tasks.filter(t => t.status === 'Completed');
  const activeTasks = tasks.filter(t => t.status !== 'Completed');

  // Formatted Daily Standup Text
  const standupText = `🚀 *Daily Standup Report - ${userProfile?.name || 'Developer'}* (${todayStr})

✅ *Completed Tasks:*
${completedTasks.length > 0 
  ? completedTasks.map(t => `• ${t.title} [${t.project}]`).join('\n') 
  : '• No tasks completed yet today.'}

🚧 *Active Work Items (In Progress / Pending):*
${activeTasks.length > 0 
  ? activeTasks.map(t => `• ${t.title} [${t.project}] - Priority: ${t.priority}`).join('\n') 
  : '• Backlog empty.'}

📊 *Sprint Health & Telemetry:*
• Sprint Velocity: ${metrics?.sprintVelocity?.value || 0} pts
• Total Tasks Completed: ${metrics?.tasksCompleted?.value || 0}/${metrics?.tasksCompleted?.total || 0}
• Coding Streak: ${userProfile?.streakDays || 0} Days`;

  const handleCopy = () => {
    navigator.clipboard.writeText(standupText);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-in fade-in duration-150">
      <div className="w-full max-w-xl fieldnote-card rounded-2xl p-6 space-y-5 shadow-2xl">
        
        {/* Modal Header */}
        <div className="flex items-start justify-between pb-3 border-b border-slate-100">
          <div>
            <h3 className="text-lg font-extrabold text-[#171E2D] flex items-center gap-2">
              <FileText className="w-4 h-4 text-emerald-700" />
              Daily Standup Export
            </h3>
            <p className="text-xs text-slate-500 font-medium mt-0.5">
              Formatted markdown ready to copy into Slack, Teams, or GitHub.
            </p>
          </div>

          <button 
            onClick={onClose}
            className="p-1 text-slate-400 hover:text-slate-700 rounded-lg hover:bg-slate-100 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Text Area Container */}
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs text-slate-500 font-medium">
            <span className="flex items-center gap-1">
              <Sparkles className="w-3.5 h-3.5 text-emerald-700" /> Live Standup Preview
            </span>
            <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-[#E5F4C7] text-[#171E2D]">
              Markdown Format
            </span>
          </div>

          <textarea
            readOnly
            rows={10}
            value={standupText}
            className="w-full p-4 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs font-mono text-[#171E2D] focus:outline-none select-all font-medium leading-relaxed"
          />
        </div>

        {/* Actions Footer */}
        <div className="pt-2 border-t border-slate-100 flex items-center justify-end space-x-3">
          <button
            onClick={onClose}
            className="px-4 py-2 text-xs font-bold text-slate-500 hover:text-slate-900 rounded-xl"
          >
            Close
          </button>
          
          <button
            onClick={handleCopy}
            className="fieldnote-btn-primary px-5 py-2.5 rounded-xl text-xs font-bold shadow-xs flex items-center space-x-1.5"
          >
            {copied ? (
              <>
                <Check className="w-4 h-4 text-[#9BE838]" />
                <span>Copied to Clipboard!</span>
              </>
            ) : (
              <>
                <Copy className="w-4 h-4" />
                <span>Copy Summary</span>
              </>
            )}
          </button>
        </div>

      </div>
    </div>
  );
}
