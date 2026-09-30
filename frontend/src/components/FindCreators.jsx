import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function FindCreators({ onOpenChat }) {
  const { user } = useAuth();
  const [creators, setCreators] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedSkill, setSelectedSkill] = useState('All');
  const [minRating, setMinRating] = useState('');
  const [maxRate, setMaxRate] = useState('');
  const [selectedCreator, setSelectedCreator] = useState(null);

  const skillOptions = ['All', 'React', 'Node.js', 'Python', 'Blender', 'Photoshop', 'Figma', 'Video Editing', 'Copywriting', 'SEO', 'AI'];

  const loadCreators = async () => {
    setLoading(true);
    try {
      const params = {};
      if (search) params.search = search;
      if (selectedSkill && selectedSkill !== 'All') params.skill = selectedSkill;
      if (minRating) params.min_rating = minRating;
      if (maxRate) params.max_rate = maxRate;

      const data = await api.getCreators(params);
      setCreators(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCreators();
  }, [selectedSkill, minRating, maxRate]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    loadCreators();
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-amber-500/10 via-orange-500/5 to-slate-900 border border-amber-500/20 rounded-3xl p-8 mb-8 backdrop-blur-sm">
        <span className="text-xs font-semibold px-3 py-1 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/30 uppercase tracking-wider">
          Client Discovery Portal
        </span>
        <h1 className="text-3xl font-extrabold text-white mt-3 tracking-tight">
          Find & Evaluate Top Creators
        </h1>
        <p className="text-slate-400 text-sm mt-2 max-w-2xl">
          Discover vetted service providers across design, video editing, development, and AI engineering. Review portfolios, ratings, and rates before inviting them to your projects.
        </p>

        {/* Search & Filters */}
        <form onSubmit={handleSearchSubmit} className="mt-6 flex flex-wrap gap-3">
          <div className="flex-1 min-w-[280px]">
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by creator name, specialty, bio..."
              className="w-full bg-slate-950/80 border border-slate-700/60 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 transition"
            />
          </div>
          <select
            value={selectedSkill}
            onChange={(e) => setSelectedSkill(e.target.value)}
            className="bg-slate-950/80 border border-slate-700/60 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:border-amber-500 transition"
          >
            {skillOptions.map((s) => (
              <option key={s} value={s}>{s === 'All' ? 'All Skills' : s}</option>
            ))}
          </select>
          <select
            value={minRating}
            onChange={(e) => setMinRating(e.target.value)}
            className="bg-slate-950/80 border border-slate-700/60 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:border-amber-500 transition"
          >
            <option value="">Any Rating</option>
            <option value="4.5">★ 4.5+ Rating</option>
            <option value="4.0">★ 4.0+ Rating</option>
          </select>
          <button
            type="submit"
            className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold px-6 py-3 rounded-xl text-sm transition shadow-lg shadow-amber-500/10"
          >
            Search Creators
          </button>
        </form>
      </div>

      {/* Creator Grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 animate-pulse">
          {[1, 2, 3, 4, 5, 6].map((n) => (
            <div key={n} className="bg-slate-900/60 border border-slate-800 rounded-2xl h-64 p-6" />
          ))}
        </div>
      ) : creators.length === 0 ? (
        <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-16 text-center">
          <p className="text-slate-400 text-base">No Creators match your filters.</p>
          <button
            onClick={() => { setSearch(''); setSelectedSkill('All'); setMinRating(''); setMaxRate(''); }}
            className="mt-3 text-amber-400 text-sm font-semibold hover:underline"
          >
            Reset Filters
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {creators.map((c) => (
            <div
              key={c.id}
              className="bg-slate-900/80 border border-slate-800/80 hover:border-amber-500/40 rounded-2xl p-6 flex flex-col justify-between transition-all duration-300 hover:shadow-xl hover:shadow-amber-500/5 group"
            >
              <div>
                <div className="flex items-start justify-between gap-4">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-full bg-gradient-to-tr from-amber-500 to-orange-500 flex items-center justify-center text-slate-950 font-bold text-lg shadow-md">
                      {c.full_name?.charAt(0) || 'C'}
                    </div>
                    <div>
                      <h3 className="text-white font-bold text-base group-hover:text-amber-400 transition">
                        {c.full_name}
                      </h3>
                      <p className="text-xs text-slate-400 line-clamp-1">{c.headline || 'Professional Freelancer'}</p>
                    </div>
                  </div>
                  <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 whitespace-nowrap">
                    {c.availability || 'Available'}
                  </span>
                </div>

                <p className="text-xs text-slate-300 mt-4 line-clamp-3 leading-relaxed">
                  {c.bio || 'Experienced service provider delivering high quality client results on TheHub.'}
                </p>

                {/* Skills Badges */}
                <div className="flex flex-wrap gap-1.5 mt-4">
                  {(c.skills ? c.skills.split(',') : ['Creative', 'Specialist']).slice(0, 4).map((skill, idx) => (
                    <span key={idx} className="text-[11px] px-2.5 py-0.5 rounded-md bg-slate-800 text-slate-300 border border-slate-700/60">
                      {skill.trim()}
                    </span>
                  ))}
                </div>
              </div>

              {/* Stats & Actions */}
              <div className="mt-6 pt-4 border-t border-slate-800/80 flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-1 text-xs font-bold text-white">
                    <span className="text-amber-400">★</span> {c.rating_avg > 0 ? c.rating_avg.toFixed(1) : '5.0'}
                    <span className="text-slate-500 text-[10px]">({c.rating_count || 12} reviews)</span>
                  </div>
                  <div className="text-[11px] text-slate-400 mt-0.5">
                    {c.completed_projects || 8} projects completed
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setSelectedCreator(c)}
                    className="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-white transition border border-slate-700/60"
                  >
                    Profile
                  </button>
                  <button
                    onClick={() => {
                      if (onOpenChat) onOpenChat(null, c);
                      else setSelectedCreator(c);
                    }}
                    className="px-3.5 py-1.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 text-xs font-bold transition shadow-md shadow-amber-500/10"
                  >
                    Chat
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Creator Profile Modal */}
      {selectedCreator && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-in fade-in overflow-y-auto">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl w-full max-w-2xl shadow-2xl p-8 my-8">
            <div className="flex items-start justify-between pb-6 border-b border-slate-800">
              <div className="flex items-center gap-4">
                <div className="w-16 h-16 rounded-full bg-gradient-to-tr from-amber-500 to-orange-500 flex items-center justify-center text-slate-950 font-extrabold text-2xl shadow-lg">
                  {selectedCreator.full_name?.charAt(0) || 'C'}
                </div>
                <div>
                  <h2 className="text-2xl font-bold text-white">{selectedCreator.full_name}</h2>
                  <p className="text-sm text-amber-400 font-medium">{selectedCreator.headline || 'Verified Creator'}</p>
                  <p className="text-xs text-slate-400 mt-0.5">{selectedCreator.email}</p>
                </div>
              </div>
              <button
                onClick={() => setSelectedCreator(null)}
                className="text-slate-400 hover:text-white p-2 rounded-lg hover:bg-slate-800"
              >
                ✕
              </button>
            </div>

            <div className="mt-6 space-y-6">
              <div>
                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">About Creator</h4>
                <p className="text-sm text-slate-300 leading-relaxed bg-slate-950/60 p-4 rounded-xl border border-slate-800">
                  {selectedCreator.bio || 'Dedicated professional on TheHub with a track record of on-time delivery and clean client satisfaction.'}
                </p>
              </div>

              <div className="grid grid-cols-3 gap-4 bg-slate-950/40 p-4 rounded-xl border border-slate-800 text-center">
                <div>
                  <div className="text-xl font-bold text-white">₹{selectedCreator.hourly_rate || '1,200'}/hr</div>
                  <div className="text-[11px] text-slate-500 mt-1">Starting Rate</div>
                </div>
                <div>
                  <div className="text-xl font-bold text-amber-400">★ {selectedCreator.rating_avg > 0 ? selectedCreator.rating_avg.toFixed(1) : '5.0'}</div>
                  <div className="text-[11px] text-slate-500 mt-1">Platform Rating</div>
                </div>
                <div>
                  <div className="text-xl font-bold text-white">{selectedCreator.completed_projects || 8}</div>
                  <div className="text-[11px] text-slate-500 mt-1">Completed Gigs</div>
                </div>
              </div>

              <div>
                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">Verified Skills</h4>
                <div className="flex flex-wrap gap-2">
                  {(selectedCreator.skills ? selectedCreator.skills.split(',') : ['Full-Stack', 'UI/UX', 'Video Production']).map((s, i) => (
                    <span key={i} className="px-3 py-1 rounded-lg bg-amber-500/10 text-amber-300 border border-amber-500/20 text-xs font-medium">
                      {s.trim()}
                    </span>
                  ))}
                </div>
              </div>

              <div className="pt-4 border-t border-slate-800 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setSelectedCreator(null)}
                  className="px-5 py-2.5 text-sm text-slate-400 hover:text-white"
                >
                  Close
                </button>
                <button
                  type="button"
                  onClick={() => {
                    const c = selectedCreator;
                    setSelectedCreator(null);
                    if (onOpenChat) onOpenChat(null, c);
                  }}
                  className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold px-6 py-2.5 rounded-xl text-sm transition shadow-lg shadow-amber-500/10"
                >
                  Message Creator
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
