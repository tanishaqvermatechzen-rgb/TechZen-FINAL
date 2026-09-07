import React from 'react';
import { ArrowUpRight, Calendar, Plus } from 'lucide-react';

export default function ProjectGrid({ projects = [], tasks = [], searchQuery = '', onSelectProject, onOpenAddProjectModal }) {
  const filteredProjects = projects.filter(p => {
    if (!searchQuery) return true;
    const q = searchQuery.toLowerCase();
    return (
      p.name.toLowerCase().includes(q) ||
      (p.category && p.category.toLowerCase().includes(q))
    );
  });

  const getAvatarBg = (name) => {
    const char = name.charAt(0).toUpperCase();
    if (char === 'A') return 'bg-emerald-500 text-white';
    if (char === 'R') return 'bg-amber-500 text-white';
    if (char === 'S') return 'bg-purple-500 text-white';
    return 'bg-teal-500 text-white';
  };

  const getStatusColor = (status) => {
    if (status === 'Completed') return { text: 'text-slate-500', dot: 'bg-slate-400', label: 'Completed' };
    if (status === 'In Review' || status === 'At Risk') return { text: 'text-amber-600', dot: 'bg-amber-500', label: 'At risk' };
    return { text: 'text-emerald-600', dot: 'bg-emerald-500', label: 'On track' };
  };

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Active Projects</div>
          <h2 className="text-xl font-extrabold text-[#171E2D] tracking-tight">Project momentum</h2>
        </div>

        <button
          onClick={() => onSelectProject ? onSelectProject(null) : null}
          className="text-xs font-bold text-[#171E2D] hover:underline flex items-center gap-1"
        >
          <span>View all</span>
          <ArrowUpRight className="w-3.5 h-3.5" />
        </button>
      </div>

      {/* Projects Grid */}
      {filteredProjects.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredProjects.map((p) => {
            const statusInfo = getStatusColor(p.status);
            const initial = p.name.charAt(0).toUpperCase();

            // Dynamic progress calculation from linked sprint tasks
            const linked = tasks.filter(t => t.project?.toLowerCase() === p.name?.toLowerCase());
            const totalTasksCount = linked.length > 0 ? linked.length : (p.totalTasks || 0);
            const completedTasksCount = linked.length > 0 ? linked.filter(t => t.status === 'Completed').length : (p.completedTasks || 0);
            const calculatedProgress = totalTasksCount > 0 ? Math.round((completedTasksCount / totalTasksCount) * 100) : (p.progress || 0);

            return (
              <div 
                key={p.id}
                onClick={() => onSelectProject && onSelectProject(p)}
                className="fieldnote-card p-5 rounded-2xl flex flex-col justify-between space-y-4 hover:border-slate-300 transition-all cursor-pointer group"
              >
                {/* Header */}
                <div className="space-y-2">
                  <div className="flex items-start justify-between">
                    <div className={`w-9 h-9 rounded-xl ${getAvatarBg(p.name)} font-extrabold text-sm flex items-center justify-center shadow-xs`}>
                      {initial}
                    </div>

                    <div className={`flex items-center gap-1.5 text-xs font-bold ${statusInfo.text}`}>
                      <span className={`w-2 h-2 rounded-full ${statusInfo.dot}`} />
                      <span>{statusInfo.label}</span>
                    </div>
                  </div>

                  <div>
                    <h3 className="text-base font-bold text-[#171E2D] group-hover:text-emerald-700 transition-colors">
                      {p.name}
                    </h3>
                    <p className="text-xs text-slate-500 font-medium line-clamp-2 mt-1">
                      {p.description}
                    </p>
                  </div>
                </div>

                {/* Dynamic Progress & Team Footer */}
                <div className="space-y-3 pt-3 border-t border-slate-100">
                  <div className="space-y-1">
                    <div className="flex justify-between text-[11px] font-bold text-slate-400">
                      <span>{completedTasksCount}/{totalTasksCount} tasks</span>
                      <span className="text-[#171E2D]">{calculatedProgress}%</span>
                    </div>
                    <div className="w-full bg-slate-100 rounded-full h-1.5 overflow-hidden">
                      <div 
                        className={`h-1.5 rounded-full transition-all duration-500 ${statusInfo.dot}`}
                        style={{ width: `${calculatedProgress}%` }}
                      />
                    </div>
                  </div>

                  <div className="flex items-center justify-between text-xs text-slate-400 pt-1">
                    <div className="flex items-center space-x-[-6px]">
                      {['MC', 'AL', 'RK'].map((av, idx) => (
                        <div key={idx} className="w-5 h-5 rounded-full bg-slate-200 text-[#171E2D] font-extrabold text-[9px] flex items-center justify-center ring-2 ring-white">
                          {av}
                        </div>
                      ))}
                    </div>
                    <div className="flex items-center gap-1 text-[11px] font-medium text-slate-400">
                      <Calendar className="w-3 h-3 text-slate-400" />
                      <span>{p.dueDate || 'Jun 28'}</span>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="fieldnote-card p-8 rounded-2xl text-center space-y-3">
          <p className="text-xs text-slate-500">No active projects found.</p>
          <button
            onClick={onOpenAddProjectModal}
            className="fieldnote-btn-primary px-4 py-2 rounded-xl text-xs font-bold"
          >
            + Create New Project
          </button>
        </div>
      )}
    </div>
  );
}
