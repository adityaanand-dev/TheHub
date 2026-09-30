import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import ApplyModal from './ApplyModal';

export default function FindProjects({ onOpenChat }) {
  const { user, token, isCreator } = useAuth();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('All');
  const [experienceLevel, setExperienceLevel] = useState('All');
  const [sortBy, setSortBy] = useState('newest');
  const [selectedProject, setSelectedProject] = useState(null);
  const [applyingProject, setApplyingProject] = useState(null);

  const categories = ['All', 'Tech & AI', 'Design & Graphics', 'Video & UGC', 'Writing & Translation'];

  const loadProjects = async () => {
    setLoading(true);
    try {
      const params = {};
      if (search) params.search = search;
      if (category !== 'All') params.category = category;
      if (experienceLevel !== 'All') params.experience_level = experienceLevel;
      if (sortBy) params.sort_by = sortBy;

      const data = await api.getProjects(params, token);
      setProjects(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProjects();
  }, [category, experienceLevel, sortBy, token]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    loadProjects();
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-cyan-500/10 via-blue-500/5 to-slate-900 border border-cyan-500/20 rounded-3xl p-8 mb-8 backdrop-blur-sm">
        <span className="text-xs font-semibold px-3 py-1 rounded-full bg-cyan-500/20 text-cyan-300 border border-cyan-500/30 uppercase tracking-wider">
          Creator Marketplace
        </span>
        <h1 className="text-3xl font-extrabold text-white mt-3 tracking-tight">
          Find Projects Posted by Clients
        </h1>
        <p className="text-slate-400 text-sm mt-2 max-w-2xl">
          Browse verified client requirements, submit competitive proposals, chat directly regarding deliverables, and get hired on your terms.
        </p>

        {/* Filters */}
        <form onSubmit={handleSearchSubmit} className="mt-6 flex flex-wrap gap-3">
          <div className="flex-1 min-w-[280px]">
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by keywords, technologies, skills..."
              className="w-full bg-slate-950/80 border border-slate-700/60 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500 transition"
            />
          </div>
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className="bg-slate-950/80 border border-slate-700/60 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:border-cyan-500 transition"
          >
            {categories.map((c) => (
              <option key={c} value={c}>{c === 'All' ? 'All Categories' : c}</option>
            ))}
          </select>
          <select
            value={experienceLevel}
            onChange={(e) => setExperienceLevel(e.target.value)}
            className="bg-slate-950/80 border border-slate-700/60 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:border-cyan-500 transition"
          >
            <option value="All">All Experience</option>
            <option value="Entry Level">Entry Level</option>
            <option value="Intermediate">Intermediate</option>
            <option value="Expert">Expert</option>
          </select>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="bg-slate-950/80 border border-slate-700/60 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:border-cyan-500 transition"
          >
            <option value="newest">Newest First</option>
            <option value="budget_desc">Highest Budget</option>
            <option value="budget_asc">Lowest Budget</option>
          </select>
          <button
            type="submit"
            className="bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold px-6 py-3 rounded-xl text-sm transition shadow-lg shadow-cyan-500/10"
          >
            Filter Gigs
          </button>
        </form>
      </div>

      {/* Projects List */}
      {loading ? (
        <div className="space-y-4 animate-pulse">
          {[1, 2, 3, 4].map((n) => (
            <div key={n} className="bg-slate-900/60 border border-slate-800 rounded-2xl h-44 p-6" />
          ))}
        </div>
      ) : projects.length === 0 ? (
        <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-16 text-center">
          <p className="text-slate-400 text-base">No client projects available matching your criteria.</p>
          <button
            onClick={() => { setSearch(''); setCategory('All'); setExperienceLevel('All'); setSortBy('newest'); }}
            className="mt-3 text-cyan-400 text-sm font-semibold hover:underline"
          >
            Clear Filters
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {projects.map((p) => {
            const isOpen = p.status === 'OPEN' || p.status === 'APPLICATION_RECEIVED';
            return (
              <div
                key={p.id}
                className="bg-slate-900/80 border border-slate-800/80 hover:border-cyan-500/40 rounded-2xl p-6 transition-all duration-300 hover:shadow-xl hover:shadow-cyan-500/5 group"
              >
                <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex flex-wrap items-center gap-2 mb-2">
                      <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
                        {p.category}
                      </span>
                      <span className="text-xs font-medium px-2 py-0.5 rounded bg-slate-800 text-slate-400">
                        {p.experience_level || 'Intermediate'}
                      </span>
                      <span className={`text-xs font-medium px-2 py-0.5 rounded ${
                        p.status === 'OPEN' ? 'bg-emerald-500/10 text-emerald-400' :
                        p.status === 'APPLICATION_RECEIVED' ? 'bg-blue-500/10 text-blue-400' :
                        'bg-amber-500/10 text-amber-400'
                      }`}>
                        {p.status}
                      </span>
                      <span className="text-xs text-slate-500 ml-auto">
                        Posted {new Date(p.created_at).toLocaleDateString()}
                      </span>
                    </div>

                    <h3
                      onClick={() => setSelectedProject(p)}
                      className="text-lg font-bold text-white group-hover:text-cyan-400 transition cursor-pointer"
                    >
                      {p.title}
                    </h3>

                    <p className="text-xs text-slate-400 mt-1 line-clamp-2 leading-relaxed">
                      {p.description}
                    </p>

                    {/* Required Skills */}
                    <div className="flex flex-wrap gap-1.5 mt-3">
                      {(p.required_skills ? p.required_skills.split(',') : ['Full-Stack']).map((sk, idx) => (
                        <span key={idx} className="text-[11px] px-2.5 py-0.5 rounded-md bg-slate-950 text-slate-300 border border-slate-800">
                          {sk.trim()}
                        </span>
                      ))}
                    </div>

                    {/* Client info */}
                    <div className="flex items-center gap-3 mt-4 text-xs text-slate-400">
                      <span>Client: <strong className="text-slate-200">{p.client_name}</strong> {p.client_company ? `(${p.client_company})` : ''}</span>
                      <span>•</span>
                      <span>Deadline: <strong className="text-slate-200">{p.deadline_days} Days</strong></span>
                      <span>•</span>
                      <span>Proposals: <strong className="text-cyan-400">{p.application_count}</strong></span>
                    </div>
                  </div>

                  {/* Budget & Actions */}
                  <div className="md:text-right flex md:flex-col justify-between items-center md:items-end gap-3 pt-3 md:pt-0 border-t md:border-t-0 border-slate-800">
                    <div>
                      <div className="text-2xl font-extrabold text-white">
                        ₹{p.budget?.toLocaleString()}
                      </div>
                      <span className="text-[11px] text-slate-500 uppercase tracking-wider block">Fixed Budget</span>
                    </div>

                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => setSelectedProject(p)}
                        className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-white transition border border-slate-700/60"
                      >
                        Details
                      </button>
                      {p.has_applied ? (
                        <span className="px-4 py-2 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-xs font-bold">
                          ✓ Applied
                        </span>
                      ) : isOpen ? (
                        <button
                          onClick={() => setApplyingProject(p)}
                          className="bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold px-5 py-2 rounded-xl text-xs transition shadow-lg shadow-cyan-500/10"
                        >
                          Apply Now
                        </button>
                      ) : (
                        <span className="px-3 py-1.5 rounded-xl bg-slate-800 text-slate-500 text-xs">
                          Closed
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Project Details Modal */}
      {selectedProject && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-in fade-in overflow-y-auto">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl w-full max-w-2xl shadow-2xl p-8 my-8">
            <div className="flex items-start justify-between pb-6 border-b border-slate-800">
              <div>
                <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
                  {selectedProject.category}
                </span>
                <h2 className="text-2xl font-bold text-white mt-2">{selectedProject.title}</h2>
                <p className="text-xs text-slate-400 mt-1">
                  Posted by <span className="text-slate-200 font-semibold">{selectedProject.client_name}</span> {selectedProject.client_company ? `• ${selectedProject.client_company}` : ''}
                </p>
              </div>
              <button
                onClick={() => setSelectedProject(null)}
                className="text-slate-400 hover:text-white p-2 rounded-lg hover:bg-slate-800"
              >
                ✕
              </button>
            </div>

            <div className="mt-6 space-y-6">
              <div className="grid grid-cols-3 gap-4 bg-slate-950/40 p-4 rounded-xl border border-slate-800 text-center">
                <div>
                  <div className="text-xl font-bold text-white">₹{selectedProject.budget?.toLocaleString()}</div>
                  <div className="text-[11px] text-slate-500 mt-1">Budget</div>
                </div>
                <div>
                  <div className="text-xl font-bold text-white">{selectedProject.deadline_days} Days</div>
                  <div className="text-[11px] text-slate-500 mt-1">Target Timeline</div>
                </div>
                <div>
                  <div className="text-xl font-bold text-cyan-400">{selectedProject.application_count}</div>
                  <div className="text-[11px] text-slate-500 mt-1">Proposals Submitted</div>
                </div>
              </div>

              <div>
                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">Scope & Requirements</h4>
                <p className="text-sm text-slate-300 leading-relaxed bg-slate-950/60 p-4 rounded-xl border border-slate-800 whitespace-pre-wrap">
                  {selectedProject.description}
                </p>
              </div>

              <div>
                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">Required Skills</h4>
                <div className="flex flex-wrap gap-2">
                  {(selectedProject.required_skills ? selectedProject.required_skills.split(',') : ['Full-Stack']).map((s, i) => (
                    <span key={i} className="px-3 py-1 rounded-lg bg-slate-800 text-slate-200 border border-slate-700/60 text-xs font-medium">
                      {s.trim()}
                    </span>
                  ))}
                </div>
              </div>

              {selectedProject.attachments && (
                <div>
                  <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">Specification URL</h4>
                  <a
                    href={selectedProject.attachments}
                    target="_blank"
                    rel="noreferrer"
                    className="text-xs text-cyan-400 hover:underline break-all"
                  >
                    {selectedProject.attachments}
                  </a>
                </div>
              )}

              <div className="pt-4 border-t border-slate-800 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setSelectedProject(null)}
                  className="px-5 py-2.5 text-sm text-slate-400 hover:text-white"
                >
                  Close
                </button>
                <button
                  type="button"
                  onClick={() => {
                    const p = selectedProject;
                    setSelectedProject(null);
                    if (onOpenChat) onOpenChat(p, { id: p.client_id, full_name: p.client_name });
                  }}
                  className="px-5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-white transition border border-slate-700"
                >
                  Chat with Client
                </button>
                {selectedProject.has_applied ? (
                  <span className="px-5 py-2.5 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-xs font-bold">
                    ✓ Already Applied
                  </span>
                ) : (selectedProject.status === 'OPEN' || selectedProject.status === 'APPLICATION_RECEIVED') && (
                  <button
                    type="button"
                    onClick={() => {
                      const p = selectedProject;
                      setSelectedProject(null);
                      setApplyingProject(p);
                    }}
                    className="bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold px-6 py-2.5 rounded-xl text-sm transition shadow-lg shadow-cyan-500/10"
                  >
                    Apply for this Project
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Apply Proposal Modal */}
      {applyingProject && (
        <ApplyModal
          project={applyingProject}
          onClose={() => setApplyingProject(null)}
          onSuccess={() => {
            loadProjects();
          }}
        />
      )}
    </div>
  );
}
