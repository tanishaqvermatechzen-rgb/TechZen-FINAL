import React, { useState } from 'react';
import { 
  LayoutGrid, 
  Folder, 
  CheckCircle2, 
  Settings, 
  Plus, 
  ChevronUp,
  X,
  User,
  Zap
} from 'lucide-react';

export default function Sidebar({ 
  activeTab, 
  setActiveTab, 
  mobileOpen, 
  setMobileOpen, 
  userProfile, 
  projectsCount = 0, 
  tasksCount = 0,
  onOpenAddTaskModal,
  onOpenAddProjectModal 
}) {
  const [showProfilePopover, setShowProfilePopover] = useState(false);
  const [focusMode, setFocusMode] = useState(false);

  const workspaceItems = [
    { id: 'dashboard', label: 'Overview', icon: LayoutGrid, badge: null },
    { id: 'projects', label: 'Projects', icon: Folder, badge: projectsCount > 0 ? String(projectsCount) : null },
    { id: 'tasks', label: 'Tasks', icon: CheckCircle2, badge: tasksCount > 0 ? String(tasksCount) : '4' },
  ];

  const teamAvatars = [
    { initials: 'MC', bg: 'bg-emerald-500' },
    { initials: 'AL', bg: 'bg-amber-500' },
    { initials: 'RK', bg: 'bg-purple-500' },
    { initials: 'TS', bg: 'bg-teal-500' },
  ];

  const initials = userProfile?.name?.split(' ').map(n => n[0]).join('') || 'MC';

  return (
    <>
      {/* Mobile Backdrop Overlay */}
      {mobileOpen && (
        <div 
          className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40 lg:hidden"
          onClick={() => setMobileOpen(false)}
        />
      )}

      {/* Fieldnote Dark Sidebar Container */}
      <aside className={`
        fixed top-0 bottom-0 left-0 z-50 w-64 bg-[#171E2D] text-slate-400
        flex flex-col justify-between transition-transform duration-300 ease-in-out
        lg:translate-x-0 ${mobileOpen ? 'translate-x-0' : '-translate-x-full'}
      `}>
        <div className="flex-1 overflow-y-auto p-5 space-y-6">
          
          {/* Brand Logo Header */}
          <div className="flex items-center justify-between pb-2">
            <div 
              className="flex items-center space-x-2.5 cursor-pointer group" 
              onClick={() => setActiveTab('dashboard')}
            >
              <div className="w-8 h-8 rounded-xl bg-[#9BE838] text-[#171E2D] flex items-center justify-center font-bold shadow-sm">
                <LayoutGrid className="w-4 h-4" />
              </div>
              <span className="font-extrabold text-xl text-white tracking-tight flex items-center">
                fieldnote<span className="text-[#9BE838] font-black text-2xl leading-none">.</span>
              </span>
            </div>

            {/* Mobile Close Button */}
            <button 
              onClick={() => setMobileOpen(false)}
              className="lg:hidden p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Navigation Group 1: WORKSPACE */}
          <div className="space-y-1">
            <div className="px-3 py-1 text-[10px] font-bold text-slate-500 uppercase tracking-widest">
              Workspace
            </div>
            {workspaceItems.map((item) => {
              const Icon = item.icon;
              const isActive = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => {
                    setActiveTab(item.id);
                    setMobileOpen(false);
                  }}
                  className={`
                    w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl text-xs font-semibold
                    transition-all duration-150 group
                    ${isActive 
                      ? 'bg-[#263147] text-white font-bold' 
                      : 'text-slate-400 hover:text-slate-200 hover:bg-[#1E283C]'
                    }
                  `}
                >
                  <div className="flex items-center space-x-3">
                    <Icon className={`w-4 h-4 ${isActive ? 'text-white' : 'text-slate-400 group-hover:text-slate-300'}`} />
                    <span>{item.label}</span>
                  </div>
                  {item.badge && (
                    <span className={`
                      text-[10px] px-2 py-0.5 rounded-full font-bold
                      ${isActive ? 'bg-[#9BE838] text-[#171E2D]' : 'bg-[#263147] text-slate-400'}
                    `}>
                      {item.badge}
                    </span>
                  )}
                </button>
              );
            })}
          </div>

          {/* Navigation Group 2: SHORTCUTS */}
          <div className="space-y-1 pt-2 border-t border-slate-800/80">
            <div className="px-3 py-1 text-[10px] font-bold text-slate-500 uppercase tracking-widest">
              Shortcuts
            </div>

            <button
              onClick={() => { onOpenAddTaskModal && onOpenAddTaskModal(); setMobileOpen(false); }}
              className="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl text-xs font-semibold text-slate-400 hover:text-slate-200 hover:bg-[#1E283C] transition-all"
            >
              <div className="flex items-center space-x-3">
                <Plus className="w-4 h-4" />
                <span>New task</span>
              </div>
              <span className="text-[10px] font-mono text-slate-500 bg-slate-800 px-1.5 py-0.5 rounded">N</span>
            </button>

            <button
              onClick={() => { onOpenAddProjectModal && onOpenAddProjectModal(); setMobileOpen(false); }}
              className="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl text-xs font-semibold text-slate-400 hover:text-slate-200 hover:bg-[#1E283C] transition-all"
            >
              <div className="flex items-center space-x-3">
                <Plus className="w-4 h-4" />
                <span>New project</span>
              </div>
            </button>
          </div>

          {/* Navigation Group 3: TEAM PULSE CARD */}
          <div className="pt-2">
            <div className="p-3.5 rounded-2xl bg-[#1E283C]/80 border border-slate-800 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Team Pulse</span>
                <span className="w-2 h-2 rounded-full bg-[#9BE838] animate-pulse" />
              </div>
              <p className="text-xs text-slate-300 leading-snug">
                Everyone is moving. Keep the thread warm.
              </p>
              <div className="flex items-center space-x-1">
                {teamAvatars.map((av, idx) => (
                  <div 
                    key={idx} 
                    className={`w-6 h-6 rounded-full ${av.bg} text-[#171E2D] font-extrabold text-[10px] flex items-center justify-center ring-2 ring-[#171E2D]`}
                  >
                    {av.initials}
                  </div>
                ))}
              </div>
            </div>
          </div>

        </div>

        {/* Footer User Info & Upward Popover */}
        <div className="p-4 border-t border-slate-800/80 space-y-2 relative">
          
          {/* Upward Profile Menu Popover */}
          {showProfilePopover && (
            <div className="absolute bottom-16 left-4 right-4 bg-[#1E283C] border border-slate-700/80 rounded-2xl p-2 z-50 shadow-2xl space-y-1 animate-in fade-in slide-in-from-bottom-2">
              <button
                onClick={() => { setActiveTab('settings'); setShowProfilePopover(false); setMobileOpen(false); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 transition-colors"
              >
                <User className="w-4 h-4 text-slate-400" />
                <span>View profile</span>
              </button>

              <button
                onClick={() => setFocusMode(!focusMode)}
                className="w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 transition-colors"
              >
                <div className="flex items-center space-x-3">
                  <Zap className="w-4 h-4 text-amber-400" />
                  <span>Focus mode</span>
                </div>
                {focusMode && <span className="text-[10px] font-bold text-[#9BE838]">ON</span>}
              </button>
            </div>
          )}

          <button
            onClick={() => setActiveTab('settings')}
            className={`w-full flex items-center space-x-3 px-3.5 py-2 rounded-xl text-xs font-semibold transition-all ${
              activeTab === 'settings' ? 'bg-[#263147] text-white' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Settings className="w-4 h-4" />
            <span>Settings</span>
          </button>

          <div 
            onClick={() => setShowProfilePopover(!showProfilePopover)}
            className="flex items-center justify-between p-2 rounded-xl bg-[#1E283C]/50 border border-slate-800/60 cursor-pointer hover:bg-[#1E283C] transition-colors"
          >
            <div className="flex items-center space-x-2.5 overflow-hidden">
              <div className="w-8 h-8 rounded-full bg-[#9BE838] text-[#171E2D] font-extrabold text-xs flex items-center justify-center shrink-0">
                {initials}
              </div>
              <div className="overflow-hidden">
                <div className="text-xs font-bold text-white truncate">{userProfile.name || 'Maya Chen'}</div>
                <div className="text-[10px] text-slate-400 truncate">Northstar team</div>
              </div>
            </div>
            <ChevronUp className="w-3.5 h-3.5 text-slate-400 shrink-0" />
          </div>
        </div>
      </aside>
    </>
  );
}
