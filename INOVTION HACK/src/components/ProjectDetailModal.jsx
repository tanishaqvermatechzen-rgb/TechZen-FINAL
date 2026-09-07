import React from 'react';
import { X, Calendar, Tag, Trash2 } from 'lucide-react';
import { telemetryService } from '../services/telemetryService';

export default function ProjectDetailModal({ project, isOpen, onClose, onDeleteProject, tasks = [] }) {
  if (!isOpen || !project) return null;

  const linkedTasks = tasks.filter(t => t.project?.toLowerCase() === project.name?.toLowerCase());

  // Dynamic Task & Progress Calculation
  const totalTasksCount = linkedTasks.length > 0 ? linkedTasks.length : (project.totalTasks || 0);
  const completedTasksCount = linkedTasks.length > 0 
    ? linkedTasks.filter(t => t.status === 'Completed').length 
    : (project.completedTasks || 0);
  const calculatedProgress = totalTasksCount > 0 
    ? Math.round((completedTasksCount / totalTasksCount) * 100) 
    : (project.progress || 0);

  const handleDelete = async () => {
    if (window.confirm(`Are you sure you want to delete project "${project.name}"?`)) {
      await telemetryService.deleteProject(project.id);
      if (onDeleteProject) onDeleteProject(project.id);
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-in fade-in duration-150">
      <div className="w-full max-w-lg fieldnote-card rounded-2xl p-6 space-y-5 shadow-2xl max-h-[90vh] overflow-y-auto">
        
        {/* Header */}
        <div className="flex items-start justify-between pb-3 border-b border-slate-100">
          <div className="space-y-1">
            <span className="text-[10px] font-extrabold uppercase tracking-widest px-2.5 py-0.5 rounded-full bg-[#E5F4C7] text-[#171E2D]">
              {project.category || 'Repository'}
            </span>
            <h2 className="text-xl font-extrabold text-[#171E2D] tracking-tight">{project.name}</h2>
            <p className="text-xs text-slate-500 font-medium">{project.description}</p>
          </div>

          <button 
            onClick={onClose}
            className="p-1 text-slate-400 hover:text-slate-700 rounded-lg hover:bg-slate-100"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Dynamic Completion Progress Bar Box */}
        <div className="p-4 rounded-xl bg-[#F3F5F2]/80 space-y-2 border border-[#E4E8DF]">
          <div className="flex justify-between items-center text-xs font-bold text-[#171E2D]">
            <span>Completion Progress</span>
            <span className="text-emerald-700">{calculatedProgress}% Complete</span>
          </div>

          {/* Progress Bar Track */}
          <div className="w-full bg-slate-200 rounded-full h-2 overflow-hidden">
            <div 
              className="bg-emerald-500 h-2 rounded-full transition-all duration-500" 
              style={{ width: `${calculatedProgress}%` }}
            />
          </div>

          <div className="flex justify-between text-[11px] text-slate-500 font-medium pt-1">
            <span>{completedTasksCount} of {totalTasksCount} tasks completed</span>
            <span>Due: {project.dueDate || 'Jun 28'}</span>
          </div>
        </div>

        {/* Tech Stack Tags */}
        {project.tags && project.tags.length > 0 && (
          <div>
            <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-2">
              Tech Stack:
            </div>
            <div className="flex flex-wrap gap-1.5">
              {project.tags.map(tag => (
                <span key={tag} className="px-2.5 py-1 rounded-lg bg-slate-100 text-[#171E2D] text-xs font-bold border border-slate-200">
                  {tag}
                </span>
              ))}
            </div>
          </div>
        )}

        {/* Linked Sprint Tasks */}
        <div>
          <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-2">
            Linked Sprint Tasks ({linkedTasks.length})
          </div>

          {linkedTasks.length > 0 ? (
            <div className="space-y-2 max-h-40 overflow-y-auto pr-1">
              {linkedTasks.map(t => (
                <div key={t.id} className="p-2.5 rounded-xl bg-white border border-slate-200 flex items-center justify-between text-xs">
                  <div className="font-semibold text-[#171E2D] truncate pr-2">{t.title}</div>
                  <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${t.status === 'Completed' ? 'bg-[#E5F4C7] text-[#171E2D]' : 'bg-amber-100 text-amber-800'}`}>
                    {t.status}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-slate-400 italic">No sprint tasks linked to this project yet.</p>
          )}
        </div>

        {/* Actions Footer */}
        <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
          <button
            onClick={handleDelete}
            className="flex items-center space-x-1.5 px-3 py-2 rounded-xl bg-rose-50 hover:bg-rose-100 text-rose-600 text-xs font-bold transition-colors"
          >
            <Trash2 className="w-3.5 h-3.5" />
            <span>Delete Project</span>
          </button>

          <button
            onClick={onClose}
            className="fieldnote-btn-primary px-4 py-2 rounded-xl text-xs font-bold"
          >
            Close
          </button>
        </div>

      </div>
    </div>
  );
}
