import React, { useState } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function ApplyModal({ project, onClose, onSuccess }) {
  const { token, isCreator } = useAuth();
  const [formData, setFormData] = useState({
    cover_letter: '',
    proposed_price: project?.budget || '',
    estimated_days: project?.deadline_days || 7,
    relevant_experience: '',
    attachments: ''
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!token) {
      alert('Please log in as a Creator to apply to projects.');
      return;
    }
    if (!isCreator) {
      alert('Only Creators can apply to projects. Switch to Creator role to submit proposals.');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      await api.applyToProject(project.id, {
        cover_letter: formData.cover_letter,
        proposed_price: parseFloat(formData.proposed_price),
        estimated_days: parseInt(formData.estimated_days, 10),
        relevant_experience: formData.relevant_experience,
        attachments: formData.attachments || null
      }, token);
      onSuccess();
      onClose();
    } catch (err) {
      setError(err.message || 'Failed to submit proposal');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-in fade-in overflow-y-auto">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-xl shadow-2xl p-6 my-8">
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div>
            <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
              CREATOR PROPOSAL
            </span>
            <h3 className="text-xl font-bold text-white mt-1">Submit Proposal</h3>
            <p className="text-xs text-slate-400 truncate max-w-md">Project: {project?.title}</p>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-white p-2">✕</button>
        </div>

        {error && (
          <div className="mt-4 p-3 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-xs">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="mt-5 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Your Proposed Price (₹) *</label>
              <input
                type="number"
                min="100"
                step="50"
                required
                value={formData.proposed_price}
                onChange={(e) => setFormData({ ...formData, proposed_price: e.target.value })}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-amber-500"
              />
              <span className="text-[10px] text-slate-500 mt-1 block">Client Budget: ₹{project?.budget?.toLocaleString()}</span>
            </div>
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Est. Delivery (Days) *</label>
              <input
                type="number"
                min="1"
                required
                value={formData.estimated_days}
                onChange={(e) => setFormData({ ...formData, estimated_days: e.target.value })}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-amber-500"
              />
              <span className="text-[10px] text-slate-500 mt-1 block">Target: {project?.deadline_days} days</span>
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">Cover Letter & Approach *</label>
            <textarea
              rows={4}
              required
              value={formData.cover_letter}
              onChange={(e) => setFormData({ ...formData, cover_letter: e.target.value })}
              placeholder="Introduce yourself, explain how you will execute the project, and mention your milestones..."
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-amber-500"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">Relevant Experience & Case Studies</label>
            <textarea
              rows={2}
              value={formData.relevant_experience}
              onChange={(e) => setFormData({ ...formData, relevant_experience: e.target.value })}
              placeholder="Similar projects you have shipped, metrics achieved, tools used..."
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-amber-500"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">Portfolio Sample Link (Optional)</label>
            <input
              type="url"
              value={formData.attachments}
              onChange={(e) => setFormData({ ...formData, attachments: e.target.value })}
              placeholder="https://behance.net/... or https://github.com/..."
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-amber-500"
            />
          </div>

          <div className="pt-3 flex justify-end gap-3 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm text-slate-400 hover:text-white transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold px-6 py-2.5 rounded-xl text-sm transition shadow-lg shadow-cyan-500/10 disabled:opacity-50"
            >
              {submitting ? 'Submitting...' : 'Send Proposal'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
