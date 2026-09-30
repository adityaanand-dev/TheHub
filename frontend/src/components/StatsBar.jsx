import React, { useEffect, useState } from 'react';
import { api } from '../api/client';
import { Target, Clock, Handshake, DollarSign, Activity } from 'lucide-react';

export default function StatsBar() {
  const [stats, setStats] = useState({
    total_gigs: 6,
    total_bookings: 0,
    pending_bookings: 0,
    accepted_bookings: 0,
    total_volume: 0.0
  });
  const [online, setOnline] = useState(true);

  const loadStats = async () => {
    try {
      const data = await api.getStats();
      setStats(data);
      setOnline(true);
    } catch {
      setOnline(false);
    }
  };

  useEffect(() => {
    loadStats();
    const interval = setInterval(loadStats, 10000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="w-full mb-8">
      {/* Platform Status Badge */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <span className={`inline-block w-2.5 h-2.5 rounded-full ${online ? 'bg-emerald-400 animate-pulse' : 'bg-rose-500'}`} />
          <span className="text-xs font-semibold text-slate-300">
            {online ? 'Spring Boot 3 + PostgreSQL + Kafka Operational' : 'Backend Disconnected'}
          </span>
        </div>
        <span className="text-[11px] text-slate-500 font-mono">Code2Career 2026 • TheHub</span>
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="p-4 rounded-2xl bg-slate-900/60 border border-slate-800/80 shadow-lg flex items-center gap-3">
          <div className="w-12 h-12 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
            <Target className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-black text-white">{stats.total_gigs}</div>
            <div className="text-xs text-slate-400 font-medium">Active Services</div>
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-slate-900/60 border border-slate-800/80 shadow-lg flex items-center gap-3">
          <div className="w-12 h-12 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-black text-white">{stats.pending_bookings}</div>
            <div className="text-xs text-slate-400 font-medium">Pending Requests</div>
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-slate-900/60 border border-slate-800/80 shadow-lg flex items-center gap-3">
          <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <Handshake className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-black text-white">{stats.accepted_bookings}</div>
            <div className="text-xs text-slate-400 font-medium">Accepted Deals</div>
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-slate-900/60 border border-slate-800/80 shadow-lg flex items-center gap-3">
          <div className="w-12 h-12 rounded-xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center text-cyan-400">
            <DollarSign className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-black text-white">
              ${Number(stats.total_volume || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </div>
            <div className="text-xs text-slate-400 font-medium">Platform Volume</div>
          </div>
        </div>
      </div>
    </div>
  );
}
