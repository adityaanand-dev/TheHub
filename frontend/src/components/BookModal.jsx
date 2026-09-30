import React, { useState } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { X, CheckCircle, AlertCircle, Loader2 } from 'lucide-react';

export default function BookModal({ gig, onClose, onSuccess, initialValues = {} }) {
  const { user, token } = useAuth();
  const [clientName, setClientName] = useState(initialValues.clientName || user?.fullName || '');
  const [clientEmail, setClientEmail] = useState(initialValues.clientEmail || user?.email || '');
  const [requirements, setRequirements] = useState(initialValues.requirements || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successId, setSuccessId] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!clientName.trim() || !clientEmail.trim() || !requirements.trim()) {
      setError('Please complete all fields to submit your booking.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const payload = {
        gig_id: gig.id,
        service_id: gig.id,
        client_name: clientName.trim(),
        client_email: clientEmail.trim().toLowerCase(),
        requirements: requirements.trim()
      };

      const res = await api.bookGig(payload, token);
      setSuccessId(res.id);
      if (onSuccess) onSuccess(res.id);
    } catch (err) {
      setError(err.message || 'Failed to submit booking inquiry');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
      <div className="relative w-full max-w-lg rounded-3xl bg-slate-900 border border-slate-800 p-6 shadow-2xl">
        <button
          onClick={onClose}
          className="absolute top-5 right-5 p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition"
        >
          <X className="w-5 h-5" />
        </button>

        {successId ? (
          <div className="text-center py-6">
            <div className="w-16 h-16 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 mx-auto flex items-center justify-center mb-4">
              <CheckCircle className="w-8 h-8" />
            </div>
            <h3 className="text-xl font-bold text-white mb-2">Booking Request Sent!</h3>
            <p className="text-sm text-slate-300 mb-6">
              Inquiry <span className="font-mono font-bold text-indigo-400">#{successId}</span> has been dispatched to <strong>{gig.creator_name || gig.creatorName}</strong>. You can track its status under <strong>My Bookings</strong>.
            </p>
            <button
              onClick={onClose}
              className="w-full py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold transition"
            >
              Done
            </button>
          </div>
        ) : (
          <div>
            <div className="mb-4">
              <span className="text-xs uppercase font-bold tracking-widest text-indigo-400">Book Service</span>
              <h2 className="text-xl font-bold text-white mt-1">{gig.title}</h2>
              <div className="text-sm text-slate-400 mt-0.5">
                Creator: <span className="text-white font-medium">{gig.creator_name || gig.creatorName}</span> • Rate: <span className="text-emerald-400 font-bold">${Number(gig.rate).toFixed(2)}</span>
              </div>
            </div>

            {error && (
              <div className="mb-4 p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Your Full Name</label>
                <input
                  type="text"
                  required
                  value={clientName}
                  onChange={(e) => setClientName(e.target.value)}
                  placeholder="e.g. Ava Johnson"
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-sm focus:outline-none focus:border-indigo-500 transition"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Your Email Address</label>
                <input
                  type="email"
                  required
                  value={clientEmail}
                  onChange={(e) => setClientEmail(e.target.value)}
                  placeholder="e.g. ava@auramedia.io"
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-sm focus:outline-none focus:border-indigo-500 transition"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Project Brief & Requirements</label>
                <textarea
                  required
                  rows={4}
                  value={requirements}
                  onChange={(e) => setRequirements(e.target.value)}
                  placeholder="Detail your goals, format requirements, turnaround expectations..."
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-sm focus:outline-none focus:border-indigo-500 transition"
                />
              </div>

              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={onClose}
                  className="flex-1 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-sm font-semibold transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 py-2.5 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white text-sm font-bold shadow-lg shadow-indigo-500/25 flex items-center justify-center gap-2 transition disabled:opacity-50"
                >
                  {loading && <Loader2 className="w-4 h-4 animate-spin" />}
                  Confirm Booking
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
    </div>
  );
}
