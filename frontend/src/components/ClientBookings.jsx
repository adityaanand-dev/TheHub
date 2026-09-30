import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { Clock, CheckCircle2, XCircle, RotateCcw, Star, RefreshCw, Loader2, MessageSquare } from 'lucide-react';

export default function ClientBookings({ onRebook }) {
  const { user, token } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterEmail, setFilterEmail] = useState(user?.email || 'client@thehub.com');
  const [reviewOrder, setReviewOrder] = useState(null);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [reviewLoading, setReviewLoading] = useState(false);

  const loadBookings = async () => {
    setLoading(true);
    try {
      const data = await api.getClientBookings(filterEmail.trim(), '', token);
      setBookings(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBookings();
  }, [user]);

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    if (!reviewOrder) return;
    setReviewLoading(true);
    try {
      await api.submitReview({
        order_id: reviewOrder.id,
        rating: reviewRating,
        comment: reviewComment
      }, token);
      alert('Thank you! Your review has been submitted.');
      setReviewOrder(null);
      setReviewComment('');
    } catch (err) {
      alert(err.message);
    } finally {
      setReviewLoading(false);
    }
  };

  return (
    <div>
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white">Client Project Tracker</h1>
          <p className="text-sm text-slate-400 mt-1">
            Track status across all your booked creator services in real time.
          </p>
        </div>
        <button
          onClick={loadBookings}
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-900 border border-slate-800 hover:bg-slate-800 text-slate-300 text-xs font-semibold transition"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          Refresh Orders
        </button>
      </div>

      {/* Filter by Client Identity */}
      <div className="mb-6 flex gap-3 max-w-md">
        <input
          type="email"
          value={filterEmail}
          onChange={(e) => setFilterEmail(e.target.value)}
          placeholder="Filter by client email address..."
          className="flex-1 px-4 py-2 rounded-xl bg-slate-900 border border-slate-800 text-white text-xs focus:outline-none focus:border-indigo-500"
        />
        <button
          onClick={loadBookings}
          className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold"
        >
          Filter
        </button>
      </div>

      {/* Bookings List */}
      {loading ? (
        <div className="flex justify-center py-20">
          <Loader2 className="w-8 h-8 animate-spin text-indigo-500" />
        </div>
      ) : bookings.length === 0 ? (
        <div className="text-center py-20 p-8 rounded-3xl bg-slate-900/40 border border-slate-800/80">
          <Clock className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <h3 className="text-lg font-bold text-white mb-1">No Orders Found</h3>
          <p className="text-sm text-slate-400">You haven't placed any bookings yet. Head to the Marketplace to begin!</p>
        </div>
      ) : (
        <div className="space-y-4">
          {bookings.map((b) => (
            <div
              key={b.id}
              className="p-5 rounded-2xl bg-slate-900/70 border border-slate-800/80 hover:border-slate-700 transition flex flex-col md:flex-row md:items-center justify-between gap-5"
            >
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1.5">
                  <span className="text-xs font-bold text-indigo-400 font-mono">Order #{b.id}</span>
                  <span className="text-xs font-semibold text-slate-400">•</span>
                  <span className="text-xs font-semibold text-slate-300">{b.category || 'Creative'}</span>
                  <span className="text-xs font-semibold text-slate-400">•</span>
                  <span className="text-xs text-slate-500">{new Date(b.created_at || Date.now()).toLocaleDateString()}</span>
                </div>

                <h3 className="text-base font-bold text-white mb-1.5">{b.gig_title}</h3>

                <div className="text-xs text-slate-300 mb-2">
                  Creator: <strong className="text-white">{b.creator_name}</strong> • Rate: <span className="text-emerald-400 font-bold">${Number(b.rate || b.price).toFixed(2)}</span>
                </div>

                <div className="p-3 rounded-xl bg-slate-950/80 border border-slate-800/60 text-xs text-slate-300 font-mono leading-relaxed">
                  {b.requirements}
                </div>

                {/* DP1 Rejection Feedback */}
                {b.status === 'Declined' && b.rejection_reason && (
                  <div className="mt-3 p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs">
                    <div className="flex items-center gap-1.5 font-bold mb-1">
                      <MessageSquare className="w-3.5 h-3.5" />
                      Creator Feedback:
                    </div>
                    <p className="italic">"{b.rejection_reason}"</p>
                  </div>
                )}
              </div>

              {/* Status and Action Column */}
              <div className="flex flex-col items-end gap-3 shrink-0">
                {b.status === 'Pending' ? (
                  <div className="text-right">
                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-amber-500/10 text-amber-400 border border-amber-500/20">
                      <Clock className="w-3.5 h-3.5 animate-spin" />
                      Pending Creator Review
                    </span>
                    <p className="text-[11px] text-slate-500 mt-1">Creator has been notified</p>
                  </div>
                ) : b.status === 'Accepted' ? (
                  <div className="text-right">
                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 mb-2">
                      <CheckCircle2 className="w-3.5 h-3.5" />
                      Accepted & In Production
                    </span>
                    <div>
                      <button
                        onClick={() => setReviewOrder(b)}
                        className="px-3 py-1.5 rounded-lg bg-indigo-600/20 hover:bg-indigo-600 text-indigo-300 hover:text-white border border-indigo-500/30 text-xs font-semibold transition"
                      >
                        Rate Experience
                      </button>
                    </div>
                  </div>
                ) : (
                  <div className="text-right space-y-2">
                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-rose-500/10 text-rose-400 border border-rose-500/20">
                      <XCircle className="w-3.5 h-3.5" />
                      Booking Declined
                    </span>

                    {/* DP1 Actionable Recovery */}
                    <div>
                      <button
                        onClick={() => onRebook(b)}
                        className="px-3 py-1.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold shadow-md shadow-indigo-600/30 flex items-center gap-1.5 transition"
                      >
                        <RotateCcw className="w-3.5 h-3.5" />
                        1-Click Re-book Alternative
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Review Modal */}
      {reviewOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-3xl bg-slate-900 border border-slate-800 p-6 shadow-2xl">
            <h3 className="text-lg font-bold text-white mb-1">Leave a Review</h3>
            <p className="text-xs text-slate-300 mb-4">Rate your experience with <strong>{reviewOrder.creator_name}</strong> for {reviewOrder.gig_title}.</p>

            <form onSubmit={handleReviewSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Rating</label>
                <div className="flex gap-2">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      type="button"
                      key={star}
                      onClick={() => setReviewRating(star)}
                      className="p-2 rounded-xl bg-slate-950 border border-slate-800 text-amber-400 hover:scale-110 transition"
                    >
                      <Star className={`w-5 h-5 ${star <= reviewRating ? 'fill-amber-400' : 'text-slate-600'}`} />
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Feedback Comment</label>
                <textarea
                  rows={3}
                  required
                  value={reviewComment}
                  onChange={(e) => setReviewComment(e.target.value)}
                  placeholder="Share details of your delivery, communication, and speed..."
                  className="w-full p-3 rounded-xl bg-slate-950 border border-slate-800 text-white text-xs focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setReviewOrder(null)}
                  className="flex-1 py-2.5 rounded-xl bg-slate-800 text-slate-300 text-xs font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={reviewLoading}
                  className="flex-1 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold shadow-md shadow-indigo-600/30 flex items-center justify-center gap-1.5"
                >
                  {reviewLoading && <Loader2 className="w-4 h-4 animate-spin" />}
                  Submit Review
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
