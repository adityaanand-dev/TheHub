import React from 'react';
import { Star, Video, Palette, Cpu, PenTool, CheckCircle2 } from 'lucide-react';

export default function GigCard({ gig, onBook }) {
  const getCategoryTheme = (category) => {
    const cat = (category || '').toLowerCase();
    if (cat.includes('video') || cat.includes('ugc')) {
      return {
        icon: <Video className="w-3.5 h-3.5" />,
        badge: 'bg-purple-500/10 text-purple-400 border-purple-500/20'
      };
    } else if (cat.includes('design') || cat.includes('graphics')) {
      return {
        icon: <Palette className="w-3.5 h-3.5" />,
        badge: 'bg-pink-500/10 text-pink-400 border-pink-500/20'
      };
    } else if (cat.includes('writing') || cat.includes('translation')) {
      return {
        icon: <PenTool className="w-3.5 h-3.5" />,
        badge: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
      };
    } else {
      return {
        icon: <Cpu className="w-3.5 h-3.5" />,
        badge: 'bg-blue-500/10 text-blue-400 border-blue-500/20'
      };
    }
  };

  const theme = getCategoryTheme(gig.category);

  return (
    <div className="group relative rounded-2xl bg-slate-900/60 border border-slate-800/80 p-5 hover:border-indigo-500/40 hover:bg-slate-900/90 transition-all duration-200 shadow-lg flex flex-col justify-between">
      <div>
        {/* Header: Category & Rate */}
        <div className="flex items-center justify-between gap-2 mb-3">
          <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border ${theme.badge}`}>
            {theme.icon}
            {gig.category}
          </span>
          <div className="text-xl font-black text-emerald-400">
            ${Number(gig.rate).toFixed(2)}
          </div>
        </div>

        {/* Gig Title */}
        <h3 className="text-lg font-bold text-white group-hover:text-indigo-300 transition mb-1 leading-snug line-clamp-2">
          {gig.title}
        </h3>

        {/* Creator Info */}
        <div className="flex items-center gap-2 mb-3 text-xs text-slate-400">
          <span className="font-semibold text-slate-200">{gig.creator_name || gig.creatorName}</span>
          <CheckCircle2 className="w-3.5 h-3.5 text-indigo-400" />
          <span>•</span>
          <span className="flex items-center gap-1 text-amber-400 font-medium">
            <Star className="w-3.5 h-3.5 fill-amber-400" />
            {gig.rating_avg ? gig.rating_avg.toFixed(1) : '5.0'}
            <span className="text-slate-500">({gig.rating_count || 1})</span>
          </span>
        </div>

        {/* Description */}
        <p className="text-sm text-slate-400 line-clamp-3 mb-4 leading-relaxed">
          {gig.description}
        </p>
      </div>

      {/* Footer / CTA */}
      <div className="pt-3 border-t border-slate-800/60 flex items-center justify-between">
        <span className="text-xs text-slate-500">
          ⏱️ {gig.delivery_days || 3}-day turnaround
        </span>
        <button
          onClick={() => onBook(gig)}
          className="px-4 py-2 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white text-xs font-bold shadow-md shadow-indigo-500/20 transition active:scale-95"
        >
          Book Now
        </button>
      </div>
    </div>
  );
}
