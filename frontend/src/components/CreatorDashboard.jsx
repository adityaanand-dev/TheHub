import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import FindProjects from './FindProjects';
import SubmitWorkModal from './SubmitWorkModal';

export default function CreatorDashboard({ onOpenChat }) {
  const { user, token } = useAuth();
  const [activeTab, setActiveTab] = useState('overview');
  const [overview, setOverview] = useState(null);
  const [myApplications, setMyApplications] = useState([]);
  const [assignedProjects, setAssignedProjects] = useState([]);
  const [loading, setLoading] = useState(true);

  // Submit work modal
  const [submittingProject, setSubmittingProject] = useState(null);

  const loadData = async () => {
    if (!token) return;
    setLoading(true);
    try {
      const [overviewData, apps, assigned] = await Promise.all([
        api.getCreatorOverview(token).catch(() => null),
        api.getMyApplications(token).catch(() => []),
        api.getAssignedProjects(token).catch(() => [])
      ]);
      setOverview(overviewData);
      setMyApplications(apps);
      setAssignedProjects(assigned);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [token]);

  const activeWork = assignedProjects.filter(
    (p) => p.status === 'IN_PROGRESS' || p.status === 'SUBMITTED' || p.status === 'REVISION_REQUESTED'
  );
  const completedProjects = assignedProjects.filter((p) => p.status === 'COMPLETED');

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Top Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-slate-800">
        <div>
          <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
            SERVICE PROVIDER WORKSPACE
          </span>
          <h1 className="text-2xl font-bold text-white mt-1">Creator Dashboard</h1>
          <p className="text-xs text-slate-400">
            Welcome, <strong className="text-slate-200">{user?.fullName || 'Creator'}</strong>. Discover projects, submit competitive proposals, and deliver high-impact work.
          </p>
        </div>

        <button
          onClick={() => setActiveTab('find')}
          className="bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold px-5 py-2.5 rounded-xl text-sm transition shadow-lg shadow-cyan-500/10 flex items-center gap-2 self-start md:self-auto"
        >
          <span>🔍</span> Find Client Projects
        </button>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-2 mt-6 border-b border-slate-800/80 pb-3">
        {[
          { id: 'overview', label: 'Overview' },
          { id: 'find', label: 'Find Projects' },
          { id: 'applications', label: `My Proposals (${myApplications.length})` },
          { id: 'active', label: `Active Work (${activeWork.length})` },
          { id: 'completed', label: `Completed (${completedProjects.length})` }
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`px-4 py-2 rounded-xl text-xs font-semibold transition ${
              activeTab === tab.id
                ? 'bg-cyan-500 text-slate-950 shadow-md shadow-cyan-500/10'
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
              <div className="text-2xl font-black text-cyan-400">{overview?.available_projects_count ?? 5}</div>
              <div className="text-xs text-slate-400 mt-1 font-medium">Available Client Projects</div>
            </div>
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
              <div className="text-2xl font-black text-amber-400">{overview?.my_applications_count ?? myApplications.length}</div>
              <div className="text-xs text-slate-400 mt-1 font-medium">My Submitted Proposals</div>
            </div>
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
              <div className="text-2xl font-black text-emerald-400">{overview?.active_projects_count ?? activeWork.length}</div>
              <div className="text-xs text-slate-400 mt-1 font-medium">Active Assigned Projects</div>
            </div>
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
              <div className="text-2xl font-black text-purple-400">{overview?.completed_projects_count ?? completedProjects.length}</div>
              <div className="text-xs text-slate-400 mt-1 font-medium">Completed Projects</div>
            </div>
          </div>

          {/* Active Work Quick Tile */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-base font-bold text-white">Active Assignments</h3>
              <button
                onClick={() => setActiveTab('active')}
                className="text-xs text-cyan-400 font-semibold hover:underline"
              >
                View Work →
              </button>
            </div>

            {activeWork.length === 0 ? (
              <p className="text-xs text-slate-500 py-4">No active work currently. Browse available projects to submit proposals.</p>
            ) : (
              <div className="divide-y divide-slate-800/80">
                {activeWork.slice(0, 3).map((p) => (
                  <div key={p.id} className="py-4 flex items-center justify-between gap-4">
                    <div>
                      <h4 className="text-sm font-semibold text-white">{p.title}</h4>
                      <p className="text-xs text-slate-400 mt-0.5">
                        Client: {p.client_name} • Budget: ₹{p.budget?.toLocaleString()} • Status: <span className="text-amber-400 font-medium">{p.status}</span>
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => {
                          if (onOpenChat) onOpenChat(p, { id: p.client_id, full_name: p.client_name });
                        }}
                        className="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-white border border-slate-700"
                      >
                        Chat
                      </button>
                      <button
                        onClick={() => setSubmittingProject(p)}
                        className="bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold px-3 py-1.5 rounded-xl text-xs transition"
                      >
                        Submit Work
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* TAB 2: FIND PROJECTS */}
      {activeTab === 'find' && (
        <div className="mt-4">
          <FindProjects onOpenChat={onOpenChat} />
        </div>
      )}

      {/* TAB 3: MY PROPOSALS */}
      {activeTab === 'applications' && (
        <div className="mt-6 space-y-4">
          {myApplications.length === 0 ? (
            <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-16 text-center">
              <p className="text-slate-400 text-sm">You haven't submitted any proposals yet.</p>
              <button
                onClick={() => setActiveTab('find')}
                className="mt-4 bg-cyan-500 text-slate-950 font-bold px-5 py-2.5 rounded-xl text-xs transition"
              >
                Browse Client Projects
              </button>
            </div>
          ) : (
            myApplications.map((app) => (
              <div key={app.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
                <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1.5">
                      <span className={`text-xs font-semibold px-2.5 py-0.5 rounded-full ${
                        app.status === 'ACCEPTED' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' :
                        app.status === 'REJECTED' ? 'bg-red-500/10 text-red-400 border border-red-500/20' :
                        'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                      }`}>
                        {app.status}
                      </span>
                      <span className="text-xs text-slate-400">
                        Client: <strong className="text-slate-200">{app.client_name}</strong>
                      </span>
                      <span className="text-xs text-slate-500 ml-auto">
                        Applied {new Date(app.created_at).toLocaleDateString()}
                      </span>
                    </div>

                    <h3 className="text-base font-bold text-white">{app.project_title}</h3>

                    <div className="mt-3 bg-slate-950/60 p-3.5 rounded-xl border border-slate-800 text-xs text-slate-300">
                      <strong className="text-slate-400 block mb-1">Your Proposal Pitch:</strong>
                      {app.cover_letter}
                    </div>
                  </div>

                  <div className="md:text-right flex md:flex-col justify-between items-center md:items-end gap-3 pt-3 md:pt-0 border-t md:border-t-0 border-slate-800">
                    <div>
                      <div className="text-lg font-bold text-white">₹{app.proposed_price?.toLocaleString()}</div>
                      <span className="text-[11px] text-slate-500">{app.estimated_days} Days delivery</span>
                    </div>

                    <button
                      onClick={() => {
                        if (onOpenChat) onOpenChat({ id: app.project_id, title: app.project_title, budget: app.proposed_price, status: app.status }, { id: app.client_id, full_name: app.client_name });
                      }}
                      className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-white border border-slate-700"
                    >
                      Chat with Client
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* TAB 4: ACTIVE WORK */}
      {activeTab === 'active' && (
        <div className="mt-6 space-y-4">
          {activeWork.length === 0 ? (
            <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-16 text-center">
              <p className="text-slate-400 text-sm">No active assignments in progress.</p>
              <p className="text-xs text-slate-500 mt-1">Once a Client accepts your proposal, the project will appear here.</p>
            </div>
          ) : (
            activeWork.map((p) => (
              <div key={p.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
                <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1.5">
                      <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                        {p.status}
                      </span>
                      <span className="text-xs text-slate-400">
                        Client: <strong className="text-slate-200">{p.client_name}</strong>
                      </span>
                    </div>

                    <h3 className="text-lg font-bold text-white">{p.title}</h3>
                    <p className="text-xs text-slate-400 mt-1">{p.description}</p>

                    {p.status === 'REVISION_REQUESTED' && (
                      <div className="mt-4 p-3 bg-amber-500/10 border border-amber-500/30 rounded-xl text-xs text-amber-300">
                        <strong>⚠️ Revision Requested:</strong> {p.revision_notes}
                      </div>
                    )}

                    {p.status === 'SUBMITTED' && (
                      <div className="mt-4 p-3 bg-blue-500/10 border border-blue-500/30 rounded-xl text-xs text-blue-300">
                        <strong>✓ Work Submitted:</strong> Awaiting Client review & approval.
                      </div>
                    )}
                  </div>

                  <div className="md:text-right flex md:flex-col justify-between items-center md:items-end gap-3 pt-3 md:pt-0 border-t md:border-t-0 border-slate-800">
                    <div>
                      <div className="text-xl font-bold text-white">₹{p.budget?.toLocaleString()}</div>
                      <span className="text-[11px] text-slate-500">Funded</span>
                    </div>

                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => {
                          if (onOpenChat) onOpenChat(p, { id: p.client_id, full_name: p.client_name });
                        }}
                        className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-white border border-slate-700"
                      >
                        Chat
                      </button>
                      <button
                        onClick={() => setSubmittingProject(p)}
                        className="bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold px-4 py-2 rounded-xl text-xs transition shadow-md shadow-emerald-500/10"
                      >
                        {p.status === 'SUBMITTED' ? 'Resubmit Work' : 'Submit Work'}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* TAB 5: COMPLETED PROJECTS */}
      {activeTab === 'completed' && (
        <div className="mt-6 space-y-4">
          {completedProjects.length === 0 ? (
            <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-16 text-center">
              <p className="text-slate-400 text-sm">No completed projects yet.</p>
            </div>
          ) : (
            completedProjects.map((p) => (
              <div key={p.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-purple-500/10 text-purple-400 border border-purple-500/20">
                      COMPLETED & PAID
                    </span>
                    <h3 className="text-base font-bold text-white mt-1">{p.title}</h3>
                    <p className="text-xs text-slate-400">Client: {p.client_name}</p>
                  </div>
                  <div className="text-right">
                    <div className="text-lg font-bold text-emerald-400">₹{p.budget?.toLocaleString()}</div>
                    <span className="text-[11px] text-slate-500">Released</span>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Submit Work Modal */}
      {submittingProject && (
        <SubmitWorkModal
          project={submittingProject}
          onClose={() => setSubmittingProject(null)}
          onSuccess={() => loadData()}
        />
      )}
    </div>
  );
}
