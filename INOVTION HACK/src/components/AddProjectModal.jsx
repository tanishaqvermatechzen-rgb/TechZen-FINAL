import React, { useState } from 'react';
import { X } from 'lucide-react';
import { telemetryService } from '../services/telemetryService';

export default function AddProjectModal({ isOpen, onClose, onProjectAdded }) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const defaultDate = new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
  const [dueDate, setDueDate] = useState(defaultDate);
  const [selectedColor, setSelectedColor] = useState('#D5E8B1');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const colorOptions = ['#D5E8B1', '#9BE838', '#60A5FA', '#F59E0B', '#EC4899', '#A855F7'];

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;

    setLoading(true);
    setError('');

    try {
      const newProject = {
        name: name.trim(),
        category: 'Backend / Microservices',
        description: description.trim() || 'What does this project make possible?',
        status: 'In Progress',
        dueDate,
        tags: ['New', selectedColor]
      };

      const savedProject = await telemetryService.createProject(newProject);
      if (onProjectAdded) onProjectAdded(savedProject);

      onClose();
      setName('');
      setDescription('');
    } catch (err) {
      setError(err.message || 'Failed to create project');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-in fade-in duration-150">
      <div className="w-full max-w-md fieldnote-card rounded-2xl p-6 space-y-5 shadow-2xl">
        
        {/* Header */}
        <div className="flex items-start justify-between">
          <div>
            <h3 className="text-lg font-extrabold text-[#171E2D]">Create a project</h3>
            <p className="text-xs text-slate-500 font-medium mt-0.5">
              Set up a home for the next meaningful thing.
            </p>
          </div>

          <button 
            onClick={onClose}
            className="p-1 text-slate-400 hover:text-slate-700 rounded-lg hover:bg-slate-100"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Error */}
        {error && (
          <div className="p-2.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs font-semibold">
            {error}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-600 mb-1">
              Project name
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Lighthouse"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs text-[#171E2D] placeholder-slate-400 focus:outline-none focus:bg-white focus:border-[#171E2D] font-medium"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-600 mb-1">
              Description
            </label>
            <textarea
              rows={3}
              placeholder="What does this project make possible?"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full p-3 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs text-[#171E2D] placeholder-slate-400 focus:outline-none focus:bg-white focus:border-[#171E2D] font-medium"
            />
          </div>

          <div className="flex items-center space-x-3">
            <div className="flex-1">
              <label className="block text-xs font-bold text-slate-600 mb-1">
                Target date
              </label>
              <input
                type="date"
                required
                value={dueDate}
                onChange={(e) => setDueDate(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs font-semibold text-[#171E2D] focus:outline-none focus:bg-white focus:border-[#171E2D]"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-600 mb-1">
                Color
              </label>
              <div className="flex items-center space-x-1.5 pt-1">
                {colorOptions.slice(0, 4).map(c => (
                  <button
                    key={c}
                    type="button"
                    onClick={() => setSelectedColor(c)}
                    className={`w-7 h-7 rounded-lg border transition-all ${
                      selectedColor === c ? 'ring-2 ring-[#171E2D] scale-110' : 'border-black/10'
                    }`}
                    style={{ backgroundColor: c }}
                  />
                ))}
              </div>
            </div>
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
              disabled={loading}
              className="fieldnote-btn-primary px-4 py-2 text-xs font-bold rounded-xl shadow-xs"
            >
              {loading ? 'Creating...' : 'Create project'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
