import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import GigCard from './GigCard';
import BookModal from './BookModal';
import { Search, SlidersHorizontal, Sparkles, Loader2 } from 'lucide-react';

const CATEGORIES = [
  'All',
  'Video & UGC',
  'Design & Graphics',
  'Tech & AI',
  'Writing & Translation'
];

export default function BrowseGigs({ prefillData, onClearPrefill }) {
  const [gigs, setGigs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [category, setCategory] = useState('All');
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('newest'); // 'newest' | 'cheapest' | 'priciest'
  const [selectedGigForBooking, setSelectedGigForBooking] = useState(null);

  const loadGigs = async () => {
    setLoading(true);
    try {
      const data = await api.getGigs({
        category,
        search,
        sort_by: sortBy
      });
      setGigs(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadGigs();
  }, [category, sortBy]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    loadGigs();
  };

  return (
    <div>
      {/* Banner */}
      <div className="relative rounded-3xl overflow-hidden mb-8 p-8 bg-gradient-to-r from-indigo-950/60 via-purple-950/40 to-slate-900 border border-indigo-500/20 shadow-2xl">
        <div className="max-w-2xl">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 mb-3">
            <Sparkles className="w-3.5 h-3.5" />
            Vetted Creator Marketplace
          </span>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight mb-2">
            Find the right creator for your next digital breakthrough.
          </h1>
          <p className="text-slate-300 text-sm leading-relaxed">
            From high-conversion UGC video ads to viral 3D thumbnails, copywriting, and autonomous AI pipelines.
          </p>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="mb-6 space-y-4">
        {/* Category Pills */}
        <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => setCategory(cat)}
              className={`px-4 py-2 rounded-xl text-xs font-semibold whitespace-nowrap transition ${
                category === cat
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                  : 'bg-slate-900/60 hover:bg-slate-800 text-slate-400 hover:text-white border border-slate-800'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Search & Sort Controls */}
        <div className="flex flex-col sm:flex-row gap-3">
          <form onSubmit={handleSearchSubmit} className="relative flex-1">
            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search services, creator handles, skills (e.g. TikTok, Blender, AI automation)..."
              className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-900/80 border border-slate-800 text-white text-sm focus:outline-none focus:border-indigo-500 transition"
            />
          </form>

          {/* DP3 Sorting Dropdown */}
          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-400 font-medium hidden sm:inline">Rank by:</span>
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white text-xs font-semibold focus:outline-none focus:border-indigo-500"
            >
              <option value="newest">✨ Newest First (DP3 Default)</option>
              <option value="cheapest">💰 Price: Low to High</option>
              <option value="priciest">💎 Price: High to Low</option>
            </select>
          </div>
        </div>
      </div>

      {/* Service Listings Grid */}
      {loading ? (
        <div className="flex flex-col items-center justify-center py-20 text-slate-500 gap-3">
          <Loader2 className="w-8 h-8 animate-spin text-indigo-500" />
          <span className="text-sm">Fetching marketplace catalog...</span>
        </div>
      ) : gigs.length === 0 ? (
        <div className="text-center py-20 p-8 rounded-3xl bg-slate-900/40 border border-slate-800/80">
          <p className="text-slate-400 text-base mb-3">No creator services match your filter criteria.</p>
          <button
            onClick={() => { setCategory('All'); setSearch(''); setSortBy('newest'); }}
            className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold transition"
          >
            Reset Filters
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {gigs.map((gig) => (
            <GigCard
              key={gig.id}
              gig={gig}
              onBook={(g) => setSelectedGigForBooking(g)}
            />
          ))}
        </div>
      )}

      {/* Booking Modal */}
      {selectedGigForBooking && (
        <BookModal
          gig={selectedGigForBooking}
          onClose={() => {
            setSelectedGigForBooking(null);
            if (onClearPrefill) onClearPrefill();
          }}
          initialValues={prefillData || {}}
        />
      )}
    </div>
  );
}
