import React, { useState } from 'react';
import { Pencil, SlidersHorizontal, Bell, Command, CheckCircle2 } from 'lucide-react';

export default function UserProfile({ profile }) {
  const [fullName, setFullName] = useState(profile?.name || 'Maya Chen');
  const [email, setEmail] = useState(profile?.email || 'maya@northstar.dev');
  
  const [compactRows, setCompactRows] = useState(false);
  const [weeklyNote, setWeeklyNote] = useState(true);
  const [saved, setSaved] = useState(false);

  const initials = fullName.split(' ').map(n => n[0]).join('') || 'MC';

  const handleSave = (e) => {
    e.preventDefault();
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      
      {/* Left Card: User Profile Information */}
      <div className="fieldnote-card p-6 rounded-2xl space-y-5">
        <div className="flex items-center justify-between pb-4 border-b border-slate-100">
          <div className="flex items-center space-x-3">
            <div className="w-12 h-12 rounded-full bg-[#E5F4C7] text-[#171E2D] font-extrabold text-base flex items-center justify-center border border-[#D5E8B1]">
              {initials}
            </div>
            <div>
              <h3 className="font-extrabold text-[#171E2D] text-base">{fullName}</h3>
              <p className="text-xs text-slate-500 font-medium">Product engineer</p>
            </div>
          </div>

          <button className="p-2 text-slate-400 hover:text-slate-700 rounded-xl hover:bg-slate-100">
            <Pencil className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSave} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-600 mb-1">
              Full name
            </label>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs text-[#171E2D] font-semibold focus:outline-none focus:bg-white focus:border-[#171E2D]"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-600 mb-1">
              Email address
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs text-[#171E2D] font-semibold focus:outline-none focus:bg-white focus:border-[#171E2D]"
            />
          </div>
        </form>
      </div>

      {/* Right Card: Workspace Preferences */}
      <div className="fieldnote-card p-6 rounded-2xl space-y-5">
        <div>
          <h3 className="font-extrabold text-[#171E2D] text-base">Workspace preferences</h3>
          <p className="text-xs text-slate-500 font-medium mt-0.5">
            Small changes, a more personal command center.
          </p>
        </div>

        {/* Toggle Items */}
        <div className="space-y-4 pt-1">
          
          {/* Toggle 1: Compact task rows */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#F3F5F2]/50 border border-slate-100">
            <div className="flex items-center space-x-3">
              <div className="w-8 h-8 rounded-lg bg-slate-200/60 text-slate-600 flex items-center justify-center shrink-0">
                <SlidersHorizontal className="w-4 h-4" />
              </div>
              <div>
                <div className="text-xs font-bold text-[#171E2D]">Compact task rows</div>
                <div className="text-[11px] text-slate-500">Fit more work on screen at once.</div>
              </div>
            </div>

            <button
              type="button"
              onClick={() => setCompactRows(!compactRows)}
              className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors ${
                compactRows ? 'bg-emerald-500' : 'bg-slate-300'
              }`}
            >
              <div className={`w-4 h-4 rounded-full bg-white transition-transform ${
                compactRows ? 'translate-x-5' : 'translate-x-0'
              }`} />
            </button>
          </div>

          {/* Toggle 2: Weekly momentum note */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#F3F5F2]/50 border border-slate-100">
            <div className="flex items-center space-x-3">
              <div className="w-8 h-8 rounded-lg bg-slate-200/60 text-slate-600 flex items-center justify-center shrink-0">
                <Bell className="w-4 h-4" />
              </div>
              <div>
                <div className="text-xs font-bold text-[#171E2D]">Weekly momentum note</div>
                <div className="text-[11px] text-slate-500">A short Friday summary of your progress.</div>
              </div>
            </div>

            <button
              type="button"
              onClick={() => setWeeklyNote(!weeklyNote)}
              className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors ${
                weeklyNote ? 'bg-emerald-500' : 'bg-slate-300'
              }`}
            >
              <div className={`w-4 h-4 rounded-full bg-white transition-transform ${
                weeklyNote ? 'translate-x-5' : 'translate-x-0'
              }`} />
            </button>
          </div>

          {/* Toggle 3: Keyboard shortcuts */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#F3F5F2]/50 border border-slate-100">
            <div className="flex items-center space-x-3">
              <div className="w-8 h-8 rounded-lg bg-slate-200/60 text-slate-600 flex items-center justify-center shrink-0">
                <Command className="w-4 h-4" />
              </div>
              <div>
                <div className="text-xs font-bold text-[#171E2D]">Keyboard shortcuts</div>
                <div className="text-[11px] text-slate-500">Quick actions are always one key away.</div>
              </div>
            </div>

            <span className="text-[11px] font-mono text-slate-500 bg-slate-200 px-2 py-0.5 rounded-md font-bold">
              ⌘ K
            </span>
          </div>

        </div>

        {/* Save Preferences Button */}
        <div className="pt-2">
          <button
            onClick={handleSave}
            className="fieldnote-btn-primary px-4 py-2.5 rounded-xl text-xs font-bold shadow-xs flex items-center space-x-2"
          >
            {saved ? (
              <>
                <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                <span>Preferences saved</span>
              </>
            ) : (
              <span>Save preferences</span>
            )}
          </button>
        </div>

      </div>

    </div>
  );
}
