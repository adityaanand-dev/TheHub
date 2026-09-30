import React, { useState } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { PlusCircle, Sparkles, CheckCircle, AlertCircle, Loader2 } from 'lucide-react';
import GigCard from './GigCard';

export default function PostGig({ onPublished }) {
  const { user, token } = useAuth();
  const [creatorName, setCreatorName] = useState(user?.fullName || 'Alex Rivera');
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('Video & UGC');
  const [rate, setRate] = useState(75.0);
  const [deliveryDays, setDeliveryDays] = useState(2);
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim() || !description.trim() || !rate) {
      setError('Please fill out all required fields.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const payload = {
        creator_name: creatorName.trim(),
        title: title.trim(),
        category,
        rate: parseFloat(rate),
        delivery_days: parseInt(deliveryDays) || 3,
        description: description.trim()
      };

      await api.createGig(payload, token);
      setSuccess(true);
      if (onPublished) onPublished();
    } catch (err) {
      setError(err.message || 'Failed to publish gig listing');
    } finally {
      setLoading(false);
    }
  };

  const previewGig = {
    id: 9999,
    creator_name: creatorName || 'Your Name',
    title: title || 'Your Gig Title Preview',
    category,
    rate: Number(rate) || 75.0,
    delivery_days: deliveryDays || 2,
    description: description || 'Provide a compelling description of what you will deliver and client expectations...',
    rating_avg: 5.0,
    rating_count: 0
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl sm:text-3xl font-black text-white">List Your Creator Skill</h1>
        <p className="text-sm text-slate-400 mt-1">
          Publish your service to the live marketplace and start receiving booking proposals.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Form Column */}
        <div className="lg:col-span-7 rounded-3xl bg-slate-900/60 border border-slate-800/80 p-6 sm:p-8 shadow-xl">
          {success && (
            <div className="mb-6 p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 text-sm flex items-center gap-3">
              <CheckCircle className="w-5 h-5 shrink-0" />
              <div>
                <strong>Awesome! Your service is now live on TheHub.</strong>
                <p className="text-xs text-emerald-400/80 mt-0.5">Head over to the Marketplace tab to view it in the public catalog.</p>
              </div>
            </div>
          )}

          {error && (
            <div className="mb-6 p-4 rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-sm flex items-center gap-2">
              <AlertCircle className="w-5 h-5 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Creator / Studio Handle</label>
              <input
                type="text"
                required
                value={creatorName}
                onChange={(e) => setCreatorName(e.target.value)}
                placeholder="e.g. Maya Chen, DevStudio"
                className="w-full px-4 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-sm focus:outline-none focus:border-indigo-500 transition"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Gig Title</label>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. High-Converting 4K TikTok Video Ads"
                className="w-full px-4 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-sm focus:outline-none focus:border-indigo-500 transition"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">Category</label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  className="w-full px-3 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-sm focus:outline-none focus:border-indigo-500"
                >
                  <option value="Video & UGC">Video & UGC</option>
                  <option value="Design & Graphics">Design & Graphics</option>
                  <option value="Tech & AI">Tech & AI</option>
                  <option value="Writing & Translation">Writing & Translation</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">Rate ($ USD)</label>
                <input
                  type="number"
                  min="1"
                  step="5"
                  required
                  value={rate}
                  onChange={(e) => setRate(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-sm focus:outline-none focus:border-indigo-500 transition"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">Turnaround (Days)</label>
                <input
                  type="number"
                  min="1"
                  max="30"
                  required
                  value={deliveryDays}
                  onChange={(e) => setDeliveryDays(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-sm focus:outline-none focus:border-indigo-500 transition"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Detailed Deliverables & Scope</label>
              <textarea
                required
                rows={5}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="List what you will provide, turnaround milestones, and what you need from the client..."
                className="w-full px-4 py-3 rounded-xl bg-slate-950 border border-slate-800 text-white text-sm focus:outline-none focus:border-indigo-500 transition"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3.5 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white font-bold text-sm shadow-xl shadow-indigo-600/30 flex items-center justify-center gap-2 transition disabled:opacity-50"
            >
              {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : <Sparkles className="w-5 h-5" />}
              Publish Gig to Live Marketplace
            </button>
          </form>
        </div>

        {/* Live Preview Column */}
        <div className="lg:col-span-5 space-y-3 sticky top-24">
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-slate-400">
            <span>Live Listing Preview</span>
          </div>
          <GigCard gig={previewGig} onBook={() => {}} />
        </div>
      </div>
    </div>
  );
}
