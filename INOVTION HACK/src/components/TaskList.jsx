import React, { useState } from 'react';
import { 
  CheckCircle2, 
  Circle, 
  Plus, 
  Filter,
  Sparkles,
  Trash2,
  ArrowUpRight,
  Search
} from 'lucide-react';

export default function TaskList({ 
  tasks = [], 
  onToggleTask, 
  onDeleteTask,
  onAIPrioritize,
  searchQuery = '', 
  onOpenAddTaskModal 
}) {
  const [filterStatus, setFilterStatus] = useState('All'); // 'All' | 'Todo' | 'InProgress' | 'Done'
  const [priorityFilter, setPriorityFilter] = useState('All');

  const filteredTasks = tasks.filter(task => {
    // Status Filter
    if (filterStatus === 'Todo' && (task.status === 'Completed' || task.status === 'In Progress')) return false;
    if (filterStatus === 'InProgress' && task.status !== 'In Progress') return false;
    if (filterStatus === 'Done' && task.status !== 'Completed') return false;

    // Priority Filter
    if (priorityFilter !== 'All' && task.priority !== priorityFilter) return false;

    // Search Query Filter
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      return (
        task.title.toLowerCase().includes(q) ||
        task.project.toLowerCase().includes(q)
      );
    }
    return true;
  });

  const getPriorityBadge = (priority) => {
    switch (priority) {
      case 'High':
        return 'bg-rose-100 text-rose-700';
      case 'Medium':
        return 'bg-amber-100 text-amber-800';
      case 'Low':
        return 'bg-emerald-100 text-emerald-800';
      default:
        return 'bg-slate-100 text-slate-700';
    }
  };

  const todoCount = tasks.filter(t => t.status === 'Pending' || t.status === 'To Do').length;
  const inProgressCount = tasks.filter(t => t.status === 'In Progress').length;
  const doneCount = tasks.filter(t => t.status === 'Completed').length;

  return (
    <div className="fieldnote-card p-6 rounded-2xl space-y-4">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-100">
        <div>
          <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Your Queue</div>
          <h2 className="text-xl font-extrabold text-[#171E2D] tracking-tight">Up next</h2>
        </div>

        {/* Action Controls */}
        <div className="flex items-center space-x-2 flex-wrap gap-y-1">
          {/* AI Prioritize Button */}
          {onAIPrioritize && tasks.length > 0 && (
            <button
              onClick={onAIPrioritize}
              className="flex items-center space-x-1 px-3 py-1.5 rounded-xl bg-[#E5F4C7] hover:bg-[#D5E8B1] text-[#171E2D] text-xs font-bold transition-all"
            >
              <Sparkles className="w-3.5 h-3.5" />
              <span>AI Prioritize</span>
            </button>
          )}

          {/* Add Task Button */}
          <button
            onClick={onOpenAddTaskModal}
            className="fieldnote-btn-primary flex items-center space-x-1 px-3.5 py-1.5 rounded-xl text-xs font-bold shadow-xs"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Add task</span>
          </button>
        </div>
      </div>

      {/* Filter Tabs Header */}
      <div className="flex flex-wrap items-center justify-between gap-3 pt-1">
        <div className="flex items-center space-x-2 border-b border-slate-100 pb-2 flex-1">
          {[
            { id: 'All', label: 'All tasks', count: tasks.length },
            { id: 'Todo', label: 'To do', count: todoCount },
            { id: 'InProgress', label: 'In progress', count: inProgressCount },
            { id: 'Done', label: 'Done', count: doneCount },
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setFilterStatus(tab.id)}
              className={`text-xs font-bold px-3 py-1 rounded-lg transition-all ${
                filterStatus === tab.id 
                  ? 'bg-slate-200 text-[#171E2D]' 
                  : 'text-slate-500 hover:text-slate-900'
              }`}
            >
              {tab.label} <span className="text-[10px] text-slate-400 font-mono ml-1">{tab.count}</span>
            </button>
          ))}
        </div>

        {/* Priority Dropdown */}
        <select
          value={priorityFilter}
          onChange={(e) => setPriorityFilter(e.target.value)}
          className="px-3 py-1 bg-white border border-[#E4E8DF] rounded-xl text-xs font-bold text-slate-600 focus:outline-none"
        >
          <option value="All">All priorities</option>
          <option value="High">High Priority</option>
          <option value="Medium">Medium Priority</option>
          <option value="Low">Low Priority</option>
        </select>
      </div>

      {/* Task List Items */}
      <div className="space-y-2 pt-2">
        {filteredTasks.length > 0 ? (
          filteredTasks.map((task) => {
            const isCompleted = task.status === 'Completed';

            return (
              <div
                key={task.id}
                className={`
                  group p-3.5 rounded-xl border transition-all duration-150 flex items-center justify-between gap-3
                  ${isCompleted 
                    ? 'bg-slate-50 border-slate-100 opacity-70' 
                    : 'bg-white hover:bg-slate-50 border-slate-200/80 shadow-2xs'
                  }
                `}
              >
                {/* Left: Radio Button & Details */}
                <div className="flex items-center space-x-3.5 min-w-0 flex-1">
                  <button
                    onClick={() => onToggleTask(task.id)}
                    className="shrink-0 focus:outline-none transition-transform active:scale-95"
                  >
                    {isCompleted ? (
                      <CheckCircle2 className="w-5 h-5 text-emerald-500 fill-emerald-100" />
                    ) : (
                      <Circle className="w-5 h-5 text-slate-300 group-hover:text-slate-500 transition-colors" />
                    )}
                  </button>

                  <div className="min-w-0 flex-1">
                    <div className={`text-xs font-bold transition-colors ${
                      isCompleted ? 'line-through text-slate-400' : 'text-[#171E2D]'
                    }`}>
                      {task.title}
                    </div>

                    <div className="flex items-center space-x-2 text-[11px] text-slate-400 mt-0.5 font-medium">
                      <span className="font-semibold text-slate-600">{task.project}</span>
                      <span>•</span>
                      <span>{task.dueDate || 'Today'}</span>
                    </div>
                  </div>
                </div>

                {/* Right: Status, Priority & Avatar */}
                <div className="flex items-center space-x-2.5 shrink-0">
                  {/* Status Indicator */}
                  <span className="text-[11px] font-bold text-slate-500 flex items-center gap-1">
                    <span className={`w-1.5 h-1.5 rounded-full ${
                      isCompleted ? 'bg-emerald-500' : task.status === 'In Progress' ? 'bg-amber-500' : 'bg-slate-400'
                    }`} />
                    <span className="hidden sm:inline">{task.status || 'To do'}</span>
                  </span>

                  {/* Priority Badge */}
                  <span className={`px-2 py-0.5 rounded-md text-[10px] font-extrabold tracking-wider ${getPriorityBadge(task.priority)}`}>
                    {task.priority?.toUpperCase()}
                  </span>

                  {/* Avatar */}
                  <div className="w-6 h-6 rounded-full bg-slate-200 text-[#171E2D] font-extrabold text-[10px] flex items-center justify-center">
                    MC
                  </div>

                  {/* Delete Action */}
                  {onDeleteTask && (
                    <button
                      onClick={() => onDeleteTask(task.id)}
                      className="p-1 text-slate-300 hover:text-rose-600 rounded hover:bg-rose-50"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  )}
                </div>
              </div>
            );
          })
        ) : (
          <div className="p-8 text-center bg-slate-50 rounded-xl border border-slate-100 space-y-2">
            <p className="text-xs text-slate-500 font-medium">No tasks in queue.</p>
            <button
              onClick={onOpenAddTaskModal}
              className="text-xs font-bold text-[#171E2D] hover:underline"
            >
              + Create New Task
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
