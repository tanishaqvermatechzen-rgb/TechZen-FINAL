import React from 'react';
import { BarChart3, TrendingUp, Zap } from 'lucide-react';

export default function WeeklyProductivityChart({ weeklyHours = 0, weeklyCommits = 0 }) {
  const weeklyData = [
    { day: 'Mon', hours: 0.0, commits: 0, targetMet: false },
    { day: 'Tue', hours: 0.0, commits: 0, targetMet: false },
    { day: 'Wed', hours: 0.0, commits: 0, targetMet: false },
    { day: 'Thu', hours: 0.0, commits: 0, targetMet: false },
    { day: 'Fri', hours: 0.0, commits: 0, targetMet: false },
    { day: 'Sat', hours: 0.0, commits: 0, targetMet: false },
    { day: 'Sun', hours: 0.0, commits: 0, targetMet: false },
  ];

  const maxHours = 10;

  return (
    <div className="apple-card p-6 rounded-3xl border border-black/[0.06] bg-white space-y-4 shadow-sm">
      {/* Widget Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-black/[0.04]">
        <div>
          <h3 className="text-base font-bold text-[#1D1D1F] flex items-center gap-2">
            <BarChart3 className="w-5 h-5 text-[#0071E3]" />
            Weekly Velocity & Activity Breakdown
          </h3>
          <p className="text-xs text-slate-400">7-day telemetry recording coding hours and commit density</p>
        </div>

        <div className="flex items-center space-x-2 text-xs font-semibold px-3 py-1 rounded-full bg-[#0071E3]/10 text-[#0071E3] border border-[#0071E3]/20 w-fit">
          <TrendingUp className="w-3.5 h-3.5" />
          <span>Sprint Goal: Clean Slate</span>
        </div>
      </div>

      {/* Clean Bar Chart */}
      <div className="pt-2 pb-2">
        <div className="h-48 flex items-end justify-around gap-2 px-2 relative border-b border-slate-100 pb-2">
          
          {/* Subtle Background Grid Lines */}
          <div className="absolute inset-x-0 top-0 border-t border-dashed border-slate-100" />
          <div className="absolute inset-x-0 top-1/4 border-t border-dashed border-slate-100" />
          <div className="absolute inset-x-0 top-2/4 border-t border-dashed border-slate-100" />
          <div className="absolute inset-x-0 top-3/4 border-t border-dashed border-slate-100" />

          {weeklyData.map((d) => {
            const heightPercent = d.hours > 0 ? Math.max(6, (d.hours / maxHours) * 100) : 0;
            return (
              <div key={d.day} className="flex-1 flex flex-col items-center justify-end h-full z-10 group relative">
                
                {/* Tooltip */}
                <div className="absolute -top-10 opacity-0 group-hover:opacity-100 transition-all bg-[#1D1D1F] text-white text-[11px] font-semibold py-1.5 px-3 rounded-xl shadow-2xl pointer-events-none whitespace-nowrap z-30">
                  <div className="font-bold">{d.hours} hrs coded</div>
                  <div className="text-[#60A5FA] text-[10px]">{d.commits} commits</div>
                </div>

                {/* Hours Label */}
                <span className="text-[11px] font-bold text-slate-400 group-hover:text-[#0071E3] transition-colors mb-1.5">
                  {d.hours > 0 ? `${d.hours}h` : '-'}
                </span>

                {/* Slender Vertical Pillar Bar */}
                <div className="w-8 sm:w-11 bg-slate-100 rounded-t-xl overflow-hidden h-36 flex items-end p-0.5 border border-slate-200/50 shadow-inner">
                  {d.hours > 0 && (
                    <div 
                      className={`
                        w-full rounded-t-lg transition-all duration-700 group-hover:brightness-110
                        ${d.targetMet 
                          ? 'bg-gradient-to-t from-[#0071E3] via-[#3B82F6] to-[#60A5FA] shadow-sm shadow-[#0071E3]/20' 
                          : 'bg-gradient-to-t from-slate-400 to-slate-300'
                        }
                      `}
                      style={{ height: `${heightPercent}%` }}
                    />
                  )}
                </div>

                {/* Day Name */}
                <span className="text-xs font-extrabold text-slate-600 group-hover:text-[#1D1D1F] mt-2">
                  {d.day}
                </span>
              </div>
            );
          })}
        </div>
      </div>

      {/* Footer Legend */}
      <div className="flex flex-wrap items-center justify-between pt-3 border-t border-black/[0.04] text-xs text-slate-400 font-medium">
        <div className="flex items-center space-x-4">
          <span className="flex items-center gap-1.5 font-medium">
            <span className="w-2.5 h-2.5 rounded-sm bg-[#0071E3]" /> Target Met (8h+)
          </span>
          <span className="flex items-center gap-1.5 font-medium">
            <span className="w-2.5 h-2.5 rounded-sm bg-slate-300" /> Partial Day
          </span>
        </div>
        <span className="font-bold text-[#1D1D1F] flex items-center gap-1">
          <Zap className="w-3.5 h-3.5 text-[#FF9500]" /> Total Week: {weeklyHours} hrs ({weeklyCommits} Commits)
        </span>
      </div>
    </div>
  );
}
