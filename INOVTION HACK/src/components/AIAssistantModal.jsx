import React, { useState } from 'react';
import { X, Sparkles, Bot, AlertCircle, CheckCircle2 } from 'lucide-react';
import { telemetryService } from '../services/telemetryService';

export default function AIAssistantModal({ isOpen, onClose, onTasksGenerated, projects = [] }) {
  const [goal, setGoal] = useState('');
  const [project, setProject] = useState(projects[0]?.name || 'Atlas UI');
  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleGenerate = async (e) => {
    e.preventDefault();
    if (!goal.trim()) return;

    setLoading(true);
    setError('');
    setSuccessMsg('');

    try {
      const data = await telemetryService.generateAITasks(goal.trim(), project);
      setSuccessMsg(data.message || 'AI Tasks generated successfully!');
      if (data.tasks) {
        onTasksGenerated(data.tasks);
      }
      setTimeout(() => {
        onClose();
        setGoal('');
        setSuccessMsg('');
      }, 1500);
    } catch (err) {
      setError(err.message || 'AI Generation failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-in fade-in duration-150">
      <div className="w-full max-w-lg fieldnote-card rounded-2xl p-6 space-y-5 shadow-2xl">
        
        {/* Modal Header */}
        <div className="flex items-start justify-between pb-3 border-b border-slate-100">
          <div>
            <h3 className="text-lg font-extrabold text-[#171E2D] flex items-center gap-2">
              <Bot className="w-4 h-4 text-emerald-700" />
              AI Task Generator
              <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-[#E5F4C7] text-[#171E2D]">
                AI Powered
              </span>
            </h3>
            <p className="text-xs text-slate-500 font-medium mt-0.5">
              Describe a high-level feature to auto-generate sprint tasks.
            </p>
          </div>

          <button 
            onClick={onClose}
            className="p-1 text-slate-400 hover:text-slate-700 rounded-lg hover:bg-slate-100 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Notifications */}
        {error && (
          <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs font-semibold flex items-center gap-2">
            <AlertCircle className="w-4 h-4 text-rose-600" />
            <span>{error}</span>
          </div>
        )}

        {successMsg && (
          <div className="p-3 rounded-xl bg-[#E5F4C7] border border-[#D5E8B1] text-[#171E2D] text-xs font-bold flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-700" />
            <span>{successMsg}</span>
          </div>
        )}

        {/* AI Prompt Form */}
        <form onSubmit={handleGenerate} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-600 mb-1">
              Target Project
            </label>
            <select
              value={project}
              onChange={(e) => setProject(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs font-bold text-[#171E2D] focus:outline-none focus:bg-white focus:border-[#171E2D]"
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
              Goal / Feature Description *
            </label>
            <textarea
              required
              rows={3}
              placeholder="e.g. Build JWT session validation and rate-limiting middleware for microservices..."
              value={goal}
              onChange={(e) => setGoal(e.target.value)}
              className="w-full p-3 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs text-[#171E2D] placeholder-slate-400 focus:outline-none focus:bg-white focus:border-[#171E2D] font-medium leading-relaxed"
            />
          </div>

          {/* Preset Prompts */}
          <div>
            <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1.5 flex items-center gap-1">
              <Sparkles className="w-3 h-3 text-emerald-700" /> Preset AI Prompts:
            </div>
            <div className="flex flex-wrap gap-1.5">
              {[
                'Build WebSocket realtime chat module',
                'Setup Auth0 OAuth2 SSO authentication',
                'Optimize PostgreSQL index query speeds'
              ].map((p) => (
                <button
                  key={p}
                  type="button"
                  onClick={() => setGoal(p)}
                  className="px-2.5 py-1 text-[11px] font-semibold rounded-lg bg-slate-100 hover:bg-[#E5F4C7] hover:text-[#171E2D] text-slate-600 border border-slate-200 transition-colors"
                >
                  +{p}
                </button>
              ))}
            </div>
          </div>

          {/* Action Buttons */}
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
              disabled={loading}
              className="fieldnote-btn-primary px-4 py-2.5 rounded-xl text-xs font-bold shadow-xs flex items-center space-x-1.5 disabled:opacity-50"
            >
              {loading ? (
                <span className="animate-spin text-white">AI Generating...</span>
              ) : (
                <>
                  <Sparkles className="w-4 h-4 text-[#9BE838]" />
                  <span>Generate Sprint Tasks</span>
                </>
              )}
            </button>
          </div>
        </form>

      </div>
    </div>
  );
}
