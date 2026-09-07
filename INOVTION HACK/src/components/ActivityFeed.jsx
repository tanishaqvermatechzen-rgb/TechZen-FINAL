import React from 'react';
import { Activity, MessageSquare, GitCommit, Rocket } from 'lucide-react';

export default function ActivityFeed({ activities = [] }) {
  const defaultActivities = [
    { id: '1', dotColor: 'bg-emerald-500', text: "You completed 'Tighten keyboard focus rings'", time: '14 min ago' },
    { id: '2', dotColor: 'bg-purple-500', text: "Avery left a note on Atlas UI", time: '1 hr ago' },
    { id: '3', dotColor: 'bg-amber-500', text: "You created 'Document event payload conventions'", time: '3 hrs ago' },
    { id: '4', dotColor: 'bg-lime-500', text: "Signal crossed the 80% milestone", time: 'Yesterday' }
  ];

  const list = activities.length > 0 ? activities : defaultActivities;

  return (
    <div className="fieldnote-card p-6 rounded-2xl space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between pb-3 border-b border-slate-100">
        <div>
          <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">The Thread</div>
          <h3 className="text-base font-extrabold text-[#171E2D]">Recent activity</h3>
        </div>
        <Activity className="w-4 h-4 text-slate-400" />
      </div>

      {/* Activity Timeline */}
      <div className="space-y-4">
        {list.map((act) => (
          <div key={act.id} className="flex items-start space-x-3 text-xs">
            <span className={`w-2 h-2 rounded-full mt-1.5 shrink-0 ${act.dotColor || 'bg-emerald-500'}`} />
            <div className="space-y-0.5 min-w-0 flex-1">
              <p className="font-semibold text-[#171E2D] leading-snug">
                {act.text || act.message}
              </p>
              <p className="text-[10px] text-slate-400 font-medium">
                {act.time}
              </p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
