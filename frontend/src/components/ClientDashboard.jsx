import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import PostProjectModal from './PostProjectModal';
import RevisionModal from './RevisionModal';
import ReviewModal from './ReviewModal';

export default function ClientDashboard({ onOpenChat }) {
  const { user, token, isClient } = useAuth();
  const [activeTab, setActiveTab] = useState('overview');
  const [overview, setOverview] = useState(null);
  const [projects, setProjects] = useState([]);
  const [selectedProjectForApps, setSelectedProjectForApps] = useState(null);
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingApps, setLoadingApps] = useState(false);

  // Modals
  const [showPostModal, setShowPostModal] = useState(false);
  const [revisionProject, setRevisionProject] = useState(null);
  const [reviewProject, setReviewProject] = useState(null);

  const loadData = async () => {
    if (!token) return;
    setLoading(true);
    try {
      const [overviewData, myProjects] = await Promise.all([
        api.getClientOverview(token).catch(() => null),
        api.getClientProjects(token).catch(() => [])
      ]);
      setOverview(overviewData);
      setProjects(myProjects);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [token]);

  const loadApplicationsForProject = async (proj) => {
    setSelectedProjectForApps(proj);
    setActiveTab('proposals');
    setLoadingApps(true);
    try {
      const apps = await api.getProjectApplications(proj.id, token);
      setApplications(apps);
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingApps(false);
    }
  };

  const handleHireCreator = async (appId) => {
    if (!window.confirm('Are you sure you want to hire this Creator? This will activate the project.')) return;
    try {
      await api.hireCreator(appId, token);
      alert('Creator hired successfully! The project is now active.');
      loadData();
      if (selectedProjectForApps) {
        loadApplicationsForProject(selectedProjectForApps);
      }
    } catch (err) {
      alert(err.message || 'Failed to hire creator');
    }
  };

  const handleRejectApplication = async (appId) => {
    if (!window.confirm('Reject this proposal?')) return;
    try {
      await api.rejectApplication(appId, token);
      if (selectedProjectForApps) {
        loadApplicationsForProject(selectedProjectForApps);
      }
    } catch (err) {
      alert(err.message || 'Failed to reject application');
    }
  };

  const activeProjects = projects.filter(
    (p) => p.status === 'IN_PROGRESS' || p.status === 'SUBMITTED' || p.status === 'REVISION_REQUESTED'
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Top Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-slate-800">
        <div>
          <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20">
            BUYER PORTAL
          </span>
          <h1 className="text-2xl font-bold text-white mt-1">Client Dashboard</h1>
          <p className="text-xs text-slate-400">
            Welcome back, <strong className="text-slate-200">{user?.fullName || 'Client'}</strong>. Manage your projects, review proposals, and hire top talent.
          </p>
        </div>

        <button
          onClick={() => setShowPostModal(true)}
          className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold px-5 py-2.5 rounded-xl text-sm transition shadow-lg shadow-amber-500/10 flex items-center gap-2 self-start md:self-auto"
        >
          <span>+</span> Post New Project
        </button>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-2 mt-6 border-b border-slate-800/80 pb-3">
        {[
          { id: 'overview', label: 'Overview' },
          { id: 'projects', label: `My Projects (${projects.length})` },
          { id: 'proposals', label: selectedProjectForApps ? `Proposals (${selectedProjectForApps.title.slice(0, 18)}...)` : 'Proposals' },
          { id: 'active', label: `Active Work (${activeProjects.length})` }
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`px-4 py-2 rounded-xl text-xs font-semibold transition ${
              activeTab === tab.id
                ? 'bg-amber-500 text-slate-950 shadow-md shadow-amber-500/10'
                : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* TAB 1: OVERVIEW */}
      {activeTab === 'overview' && (
        <div className="mt-6 space-y-6">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
              <div className="text-2xl font-black text-amber-400">{overview?.open_projects_count ?? projects.length}</div>
              <div className="text-xs text-slate-400 mt-1 font-medium">Open Gigs / Projects</div>
            </div>
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
              <div className="text-2xl font-black text-cyan-400">{overview?.pending_applications_count ?? 3}</div>
              <div className="text-xs text-slate-400 mt-1 font-medium">Pending Creator Applications</div>
            </div>
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
              <div className="text-2xl font-black text-emerald-400">{overview?.active_projects_count ?? activeProjects.length}</div>
              <div className="text-xs text-slate-400 mt-1 font-medium">Active In-Progress Projects</div>
            </div>
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
              <div className="text-2xl font-black text-purple-400">{overview?.completed_projects_count ?? 1}</div>
              <div className="text-xs text-slate-400 mt-1 font-medium">Completed Projects</div>
            </div>
          </div>

          {/* Quick Actions & Recent Projects */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-base font-bold text-white">Recent Projects</h3>
              <button
                onClick={() => setActiveTab('projects')}
                className="text-xs text-amber-400 font-semibold hover:underline"
              >
                View All →
              </button>
            </div>

            <div className="divide-y divide-slate-800/80">
              {projects.slice(0, 3).map((p) => (
                <div key={p.id} className="py-4 flex items-center justify-between gap-4">
                  <div>
                    <h4 className="text-sm font-semibold text-white">{p.title}</h4>
                    <p className="text-xs text-slate-400 mt-0.5">
                      Budget: ₹{p.budget?.toLocaleString()} • {p.deadline_days} Days • {p.category}
                    </p>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="text-xs px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                      {p.status}
                    </span>
                    <button
                      onClick={() => loadApplicationsForProject(p)}
                      className="text-xs bg-amber-500/10 text-amber-400 border border-amber-500/30 px-3 py-1.5 rounded-xl font-semibold hover:bg-amber-500 hover:text-slate-950 transition"
                    >
                      Proposals ({p.application_count})
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: MY PROJECTS */}
      {activeTab === 'projects' && (
        <div className="mt-6 space-y-4">
          {projects.length === 0 ? (
            <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-16 text-center">
              <p className="text-slate-400 text-sm">You haven't posted any projects yet.</p>
              <button
                onClick={() => setShowPostModal(true)}
                className="mt-4 bg-amber-500 text-slate-950 font-bold px-5 py-2.5 rounded-xl text-xs transition"
              >
                Post Your First Project
              </button>
            </div>
          ) : (
            projects.map((p) => (
              <div key={p.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1.5">
                      <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-300">
                        {p.category}
                      </span>
                      <span className="text-xs px-2.5 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20 font-semibold">
                        {p.status}
                      </span>
                    </div>
                    <h3 className="text-lg font-bold text-white">{p.title}</h3>
                    <p className="text-xs text-slate-400 mt-1 line-clamp-2">{p.description}</p>
                    <div className="flex items-center gap-3 mt-3 text-xs text-slate-400">
                      <span>Budget: <strong className="text-white">₹{p.budget?.toLocaleString()}</strong></span>
                      <span>•</span>
                      <span>Deadline: <strong className="text-white">{p.deadline_days} Days</strong></span>
                      <span>•</span>
                      <span>Proposals: <strong className="text-cyan-400">{p.application_count}</strong></span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => loadApplicationsForProject(p)}
                      className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold px-4 py-2 rounded-xl text-xs transition shadow-md shadow-amber-500/10"
                    >
                      Review Proposals ({p.application_count})
                    </button>
                    {p.selected_creator_id && (
                      <button
                        onClick={() => {
                          if (onOpenChat) onOpenChat(p, { id: p.selected_creator_id, full_name: p.selected_creator_name });
                        }}
                        className="px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-white border border-slate-700"
                      >
                        Chat
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* TAB 3: PROPOSALS REVIEW */}
      {activeTab === 'proposals' && (
        <div className="mt-6">
          {selectedProjectForApps && (
            <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-5 mb-6 flex items-center justify-between">
              <div>
                <span className="text-xs font-semibold text-amber-400">REVIEWING PROPOSALS FOR:</span>
                <h3 className="text-lg font-bold text-white mt-0.5">{selectedProjectForApps.title}</h3>
                <p className="text-xs text-slate-400">Budget: ₹{selectedProjectForApps.budget?.toLocaleString()} • Status: {selectedProjectForApps.status}</p>
              </div>
              <button
                onClick={() => setActiveTab('projects')}
                className="text-xs text-slate-400 hover:text-white px-3 py-1.5 rounded-lg bg-slate-800"
              >
                ← Back to Projects
              </button>
            </div>
          )}

          {loadingApps ? (
            <div className="text-center py-12 text-slate-400 text-sm">Loading Creator proposals...</div>
          ) : applications.length === 0 ? (
            <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-16 text-center">
              <p className="text-slate-400 text-sm">No proposals received for this project yet.</p>
            </div>
          ) : (
            <div className="space-y-4">
              {applications.map((app) => (
                <div key={app.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
                  <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
                    <div className="flex-1">
                      <div className="flex items-center gap-3">
                        <div className="w-12 h-12 rounded-full bg-gradient-to-tr from-cyan-500 to-blue-500 flex items-center justify-center text-slate-950 font-bold text-lg">
                          {app.creator_name?.charAt(0) || 'C'}
                        </div>
                        <div>
                          <h4 className="text-base font-bold text-white">{app.creator_name}</h4>
                          <p className="text-xs text-slate-400">{app.creator_headline || 'Creator'}</p>
                          <div className="flex items-center gap-2 mt-0.5 text-xs text-slate-400">
                            <span className="text-amber-400">★ {app.creator_rating > 0 ? app.creator_rating.toFixed(1) : '5.0'}</span>
                            <span>•</span>
                            <span>{app.creator_completed_projects || 6} completed projects</span>
                          </div>
                        </div>
                        <span className={`ml-auto md:ml-4 text-xs font-semibold px-2.5 py-0.5 rounded-full ${
                          app.status === 'ACCEPTED' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' :
                          app.status === 'REJECTED' ? 'bg-red-500/10 text-red-400 border border-red-500/20' :
                          'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                        }`}>
                          {app.status}
                        </span>
                      </div>

                      <div className="mt-4 bg-slate-950/60 p-4 rounded-xl border border-slate-800/80">
                        <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-1">Proposal & Pitch:</span>
                        <p className="text-xs text-slate-200 leading-relaxed whitespace-pre-wrap">{app.cover_letter}</p>

                        {app.relevant_experience && (
                          <div className="mt-3 pt-3 border-t border-slate-800/80">
                            <span className="text-[11px] font-semibold text-slate-400 block mb-0.5">Relevant Experience:</span>
                            <p className="text-xs text-slate-300">{app.relevant_experience}</p>
                          </div>
                        )}
                      </div>
                    </div>

                    <div className="md:text-right flex md:flex-col justify-between items-center md:items-end gap-3 pt-3 md:pt-0 border-t md:border-t-0 border-slate-800">
                      <div>
                        <div className="text-xl font-bold text-white">₹{app.proposed_price?.toLocaleString()}</div>
                        <span className="text-[11px] text-slate-400 block">{app.estimated_days} Days turnaround</span>
                      </div>

                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => {
                            if (onOpenChat) onOpenChat(selectedProjectForApps, { id: app.creator_id, full_name: app.creator_name });
                          }}
                          className="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-white border border-slate-700"
                        >
                          Chat
                        </button>

                        {app.status === 'PENDING' && (
                          <>
                            <button
                              onClick={() => handleRejectApplication(app.id)}
                              className="px-3 py-1.5 rounded-xl bg-red-500/10 text-red-400 hover:bg-red-500/20 text-xs font-semibold border border-red-500/30 transition"
                            >
                              Reject
                            </button>
                            <button
                              onClick={() => handleHireCreator(app.id)}
                              className="bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold px-4 py-1.5 rounded-xl text-xs transition shadow-md shadow-emerald-500/10"
                            >
                              Hire Creator
                            </button>
                          </>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB 4: ACTIVE WORK IN PROGRESS */}
      {activeTab === 'active' && (
        <div className="mt-6 space-y-6">
          {activeProjects.length === 0 ? (
            <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-16 text-center">
              <p className="text-slate-400 text-sm">No projects currently in progress.</p>
              <p className="text-xs text-slate-500 mt-1">Accept a Creator's proposal to activate a project.</p>
            </div>
          ) : (
            activeProjects.map((p) => (
              <div key={p.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
                <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20">
                        {p.status}
                      </span>
                      <span className="text-xs text-slate-400">
                        Hired Creator: <strong className="text-white">{p.selected_creator_name || 'Assigned'}</strong>
                      </span>
                    </div>

                    <h3 className="text-lg font-bold text-white">{p.title}</h3>
                    <p className="text-xs text-slate-400 mt-1">{p.description}</p>

                    {/* Work Submission Preview */}
                    {p.status === 'SUBMITTED' && (
                      <div className="mt-4 p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30">
                        <div className="flex items-center gap-2 text-emerald-400 text-xs font-bold mb-1">
                          <span>✓</span> Work Delivered by Creator — Awaiting Your Review
                        </div>
                        <p className="text-xs text-slate-200 mt-1">{p.submission_notes}</p>
                        {p.submission_url && (
                          <a
                            href={p.submission_url}
                            target="_blank"
                            rel="noreferrer"
                            className="text-xs text-cyan-400 hover:underline mt-2 inline-block break-all"
                          >
                            🔗 View Deliverable: {p.submission_url}
                          </a>
                        )}
                      </div>
                    )}

                    {p.status === 'REVISION_REQUESTED' && (
                      <div className="mt-4 p-3 rounded-xl bg-amber-500/10 border border-amber-500/30 text-xs text-amber-300">
                        <strong>Revision Notes Sent:</strong> {p.revision_notes}
                      </div>
                    )}
                  </div>

                  <div className="flex flex-col items-end gap-3">
                    <div className="text-right">
                      <div className="text-xl font-bold text-white">₹{p.budget?.toLocaleString()}</div>
                      <span className="text-[11px] text-slate-500">Funded / Escrow</span>
                    </div>

                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => {
                          if (onOpenChat) onOpenChat(p, { id: p.selected_creator_id, full_name: p.selected_creator_name });
                        }}
                        className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-white border border-slate-700"
                      >
                        Chat
                      </button>

                      {p.status === 'SUBMITTED' && (
                        <>
                          <button
                            onClick={() => setRevisionProject(p)}
                            className="px-3.5 py-2 rounded-xl bg-amber-500/10 text-amber-400 hover:bg-amber-500/20 text-xs font-semibold border border-amber-500/30 transition"
                          >
                            Request Revision
                          </button>
                          <button
                            onClick={() => setReviewProject(p)}
                            className="bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold px-4 py-2 rounded-xl text-xs transition shadow-md shadow-emerald-500/10"
                          >
                            Approve & Pay
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Modals */}
      {showPostModal && (
        <PostProjectModal
          onClose={() => setShowPostModal(false)}
          onSuccess={() => {
            loadData();
            setActiveTab('projects');
          }}
        />
      )}

      {revisionProject && (
        <RevisionModal
          project={revisionProject}
          onClose={() => setRevisionProject(null)}
          onSuccess={() => loadData()}
        />
      )}

      {reviewProject && (
        <ReviewModal
          project={reviewProject}
          onClose={() => setReviewProject(null)}
          onSuccess={() => loadData()}
        />
      )}
    </div>
  );
}
