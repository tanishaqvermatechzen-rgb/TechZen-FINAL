import React, { useState } from 'react';
import { 
  Search, 
  Bell, 
  Menu, 
  Plus, 
  ChevronDown,
  Sparkles,
  X,
  FileText,
  LogOut
} from 'lucide-react';

export default function Header({ 
  searchQuery, 
  setSearchQuery, 
  setMobileOpen, 
  onOpenAddTaskModal,
  onOpenAIModal,
  onOpenExportModal,
  userProfile,
  notifications = [],
  onLogout 
}) {
  const [showNotifications, setShowNotifications] = useState(false);
  const [showProfileMenu, setShowProfileMenu] = useState(false);

  const unreadCount = notifications.filter(n => n.unread).length;
  const initials = userProfile?.name?.split(' ').map(n => n[0]).join('') || 'MC';

  return (
    <header className="sticky top-0 z-30 h-16 bg-[#F3F5F2]/90 backdrop-blur-md px-4 lg:px-8 flex items-center justify-between gap-4 border-b border-[#E4E8DF]/60">
      
      {/* Left: Mobile Toggle & Search Bar */}
      <div className="flex items-center space-x-3 flex-1 max-w-md">
        <button
          onClick={() => setMobileOpen(true)}
          className="lg:hidden p-2 text-slate-600 hover:text-slate-900 rounded-xl hover:bg-slate-200/60"
        >
          <Menu className="w-5 h-5" />
        </button>

        {/* Fieldnote Search Bar */}
        <div className="relative w-full">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            placeholder="Search anything..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-12 py-2 bg-white border border-[#E4E8DF] rounded-xl text-xs text-[#171E2D] placeholder-slate-400 focus:outline-none focus:border-[#171E2D] transition-all font-medium shadow-2xs"
          />
          <div className="absolute right-3 top-1/2 -translate-y-1/2 flex items-center space-x-1 text-[10px] font-mono text-slate-400 bg-slate-100 border border-slate-200 px-1.5 py-0.5 rounded-md">
            <span>⌘</span>
            <span>K</span>
          </div>
        </div>
      </div>

      {/* Right: Actions */}
      <div className="flex items-center space-x-3">
        
        {/* AI Task Generator Button */}
        <button
          onClick={onOpenAIModal}
          className="hidden sm:flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-[#E5F4C7] hover:bg-[#D5E8B1] text-[#171E2D] text-xs font-bold border border-[#D5E8B1] transition-all"
        >
          <Sparkles className="w-3.5 h-3.5 text-[#171E2D]" />
          <span>AI Generator</span>
        </button>

        {/* Export Standup Button */}
        <button
          onClick={onOpenExportModal}
          className="hidden md:flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-white hover:bg-slate-50 text-[#171E2D] text-xs font-bold border border-[#E4E8DF] transition-all shadow-2xs"
        >
          <FileText className="w-3.5 h-3.5 text-slate-500" />
          <span>Standup</span>
        </button>

        {/* Primary Action Button: + Add task */}
        <button
          onClick={onOpenAddTaskModal}
          className="fieldnote-btn-primary flex items-center space-x-1.5 px-3.5 py-1.5 rounded-xl text-xs font-bold shadow-sm"
        >
          <Plus className="w-4 h-4" />
          <span>Add task</span>
        </button>

        {/* Notifications Button */}
        <div className="relative">
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            className="p-2 text-slate-600 hover:text-slate-900 rounded-xl hover:bg-slate-200/50 relative"
          >
            <Bell className="w-4 h-4" />
            {unreadCount > 0 && (
              <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-[#9BE838] rounded-full ring-2 ring-white" />
            )}
          </button>

          {/* Notifications Popover */}
          {showNotifications && (
            <div className="absolute right-0 mt-2 w-80 fieldnote-card rounded-2xl p-4 z-50 animate-in fade-in slide-in-from-top-2">
              <div className="flex items-center justify-between pb-2 mb-2 border-b border-slate-100">
                <span className="font-bold text-xs">Notifications</span>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-[#E5F4C7] text-[#171E2D]">
                  {unreadCount} new
                </span>
              </div>
              <div className="space-y-2 max-h-56 overflow-y-auto">
                {notifications.map(n => (
                  <div key={n.id} className="p-2.5 rounded-xl bg-slate-50 text-xs space-y-1">
                    <div className="font-bold text-[#171E2D]">{n.title}</div>
                    <div className="text-slate-500 text-[11px]">{n.message}</div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* User Profile Pill Avatar */}
        <div className="relative">
          <button
            onClick={() => setShowProfileMenu(!showProfileMenu)}
            className="flex items-center space-x-1.5 p-1 rounded-full hover:bg-slate-200/50 transition-all"
          >
            <div className="w-7 h-7 rounded-full bg-[#9BE838] text-[#171E2D] font-extrabold text-xs flex items-center justify-center">
              {initials}
            </div>
            <ChevronDown className="w-3.5 h-3.5 text-slate-500" />
          </button>

          {/* Profile Menu */}
          {showProfileMenu && (
            <div className="absolute right-0 mt-2 w-56 fieldnote-card rounded-2xl p-3 z-50 space-y-2 animate-in fade-in slide-in-from-top-2">
              <div className="p-2 bg-slate-50 rounded-xl">
                <div className="font-bold text-xs text-[#171E2D]">{userProfile.name}</div>
                <div className="text-[10px] text-slate-500">{userProfile.email}</div>
              </div>
              <button
                onClick={() => { setShowProfileMenu(false); onLogout && onLogout(); }}
                className="w-full flex items-center justify-center space-x-1.5 py-2 rounded-xl bg-rose-50 hover:bg-rose-100 text-rose-600 font-bold text-xs transition-colors"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span>Sign Out</span>
              </button>
            </div>
          )}
        </div>

      </div>
    </header>
  );
}
