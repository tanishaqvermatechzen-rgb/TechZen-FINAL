import React, { useState } from 'react';
import { X } from 'lucide-react';

export default function AddTaskModal({ isOpen, onClose, onAddTask, projects = [] }) {
  const [title, setTitle] = useState('');
  const [project, setProject] = useState(projects[0]?.name || 'Atlas UI');
  const [priority, setPriority] = useState('Medium');
  
  const tomorrowDate = new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString().split('T')[0];
  const [dueDate, setDueDate] = useState(tomorrowDate);

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!title.trim()) return;

    const newTask = {
      id: `task-${Date.now()}`,
      title: title.trim(),
      project,
      priority,
      status: 'Pending',
      dueDate,
      estimatedHours: 2.5,
      tags: ['Feature']
    };

    onAddTask(newTask);
    setTitle('');
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-in fade-in duration-150">
      <div className="w-full max-w-md fieldnote-card rounded-2xl p-6 space-y-5 shadow-2xl">
        
        {/* Header */}
        <div className="flex items-start justify-between">
          <div>
            <h3 className="text-lg font-extrabold text-[#171E2D]">Add a task</h3>
            <p className="text-xs text-slate-500 font-medium mt-0.5">
              Give the next piece of work a clear shape.
            </p>
          </div>

          <button 
            onClick={onClose}
            className="p-1 text-slate-400 hover:text-slate-700 rounded-lg hover:bg-slate-100"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-600 mb-1">
              Task title
            </label>
            <input
              type="text"
              required
              placeholder="What needs doing?"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs text-[#171E2D] placeholder-slate-400 focus:outline-none focus:bg-white focus:border-[#171E2D] font-medium"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-600 mb-1">
                Project
              </label>
              <select
                value={project}
                onChange={(e) => setProject(e.target.value)}
                className="w-full px-3 py-2.5 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs font-bold text-[#171E2D] focus:outline-none focus:bg-white focus:border-[#171E2D]"
              >
                {projects.length > 0 ? (
                  projects.map(p => (
                    <option key={p.id} value={p.name}>{p.name}</option>
                  ))
                ) : (
                  <option value="Atlas UI">Atlas UI</option>
                )}
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-600 mb-1">
                Priority
              </label>
              <select
                value={priority}
                onChange={(e) => setPriority(e.target.value)}
                className="w-full px-3 py-2.5 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs font-bold text-[#171E2D] focus:outline-none focus:bg-white focus:border-[#171E2D]"
              >
                <option value="High">High</option>
                <option value="Medium">Medium</option>
                <option value="Low">Low</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-600 mb-1">
              Due date
            </label>
            <input
              type="date"
              required
              value={dueDate}
              onChange={(e) => setDueDate(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs font-semibold text-[#171E2D] focus:outline-none focus:bg-white focus:border-[#171E2D]"
            />
          </div>

          {/* Actions */}
          <div className="pt-3 border-t border-slate-100 flex items-center justify-end space-x-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-bold text-slate-500 hover:text-slate-900 rounded-xl"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="fieldnote-btn-primary px-4 py-2 text-xs font-bold rounded-xl shadow-xs"
            >
              Create task
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
