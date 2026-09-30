import React, { useState, useEffect } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import StatsBar from './components/StatsBar';
import FindProjects from './components/FindProjects';
import FindCreators from './components/FindCreators';
import ClientDashboard from './components/ClientDashboard';
import CreatorDashboard from './components/CreatorDashboard';
import AdminPortal from './components/AdminPortal';
import AuthModal from './components/AuthModal';
import PostProjectModal from './components/PostProjectModal';
import ChatModal from './components/ChatModal';

export function TheHubApp() {
  const { user, isClient, isCreator, isAdmin } = useAuth();
  const [activeTab, setActiveTab] = useState(() => {
    if (user?.role === 'ROLE_CLIENT') return 'client-dashboard';
    if (user?.role === 'ROLE_FREELANCER' || user?.role === 'ROLE_CREATOR') return 'creator-dashboard';
    if (user?.role === 'ROLE_ADMIN') return 'admin';
    return 'find-projects';
  });

  const [showAuthModal, setShowAuthModal] = useState(false);
  const [showPostModal, setShowPostModal] = useState(false);
  const [activeChat, setActiveChat] = useState(null); // { project, recipient }

  // Synchronize tab when auth role changes (e.g. switching demo account)
  useEffect(() => {
    if (isClient && (activeTab === 'creator-dashboard' || activeTab === 'browse')) {
      setActiveTab('client-dashboard');
    } else if (isCreator && (activeTab === 'client-dashboard' || activeTab === 'browse' || activeTab === 'post-gig')) {
      setActiveTab('creator-dashboard');
    } else if (isAdmin && activeTab === 'browse') {
      setActiveTab('admin');
    }
  }, [user?.role]);

  const handleOpenChat = (project, recipient) => {
    setActiveChat({ project, recipient });
  };

  const handleOpenPostProject = () => {
    if (!user) {
      setShowAuthModal(true);
      return;
    }
    if (!isClient) {
      alert('Only Clients can post projects. Please switch to or register as a Client.');
      return;
    }
    setShowPostModal(true);
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#070b16] text-slate-100 font-sans selection:bg-indigo-500 selection:text-white">
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onOpenAuth={() => setShowAuthModal(true)}
        onOpenPostProject={handleOpenPostProject}
      />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <StatsBar />

        {/* Client Views */}
        {activeTab === 'client-dashboard' && (
          <ClientDashboard onOpenChat={handleOpenChat} />
        )}

        {/* Creator Views */}
        {activeTab === 'creator-dashboard' && (
          <CreatorDashboard onOpenChat={handleOpenChat} />
        )}

        {/* Shared Marketplace Views */}
        {activeTab === 'find-projects' && (
          <FindProjects onOpenChat={handleOpenChat} />
        )}

        {activeTab === 'find-creators' && (
          <FindCreators onOpenChat={handleOpenChat} />
        )}

        {/* Admin Portal */}
        {activeTab === 'admin' && (
          <AdminPortal />
        )}
      </main>

      {/* Footer */}
      <footer className="w-full border-t border-slate-900 bg-slate-950/80 py-8 mt-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500">
          <div>
            © 2026 <strong className="text-slate-400">TheHub</strong> — Production-Style Event-Driven Freelancing Marketplace.
          </div>
          <div className="flex flex-wrap items-center gap-2.5 font-mono text-[11px] text-slate-400">
            <span className="px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800">React 18</span>
            <span className="px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800">Spring Boot 3</span>
            <span className="px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800">PostgreSQL 16</span>
            <span className="px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800">Redis 7</span>
            <span className="px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800">Apache Kafka (KRaft)</span>
            <span className="px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800">Docker Containerized</span>
          </div>
        </div>
      </footer>

      {/* Global Modals */}
      {showAuthModal && (
        <AuthModal onClose={() => setShowAuthModal(false)} />
      )}

      {showPostModal && (
        <PostProjectModal
          onClose={() => setShowPostModal(false)}
          onSuccess={() => {
            setShowPostModal(false);
            setActiveTab('client-dashboard');
          }}
        />
      )}

      {activeChat && (
        <ChatModal
          project={activeChat.project}
          recipient={activeChat.recipient}
          onClose={() => setActiveChat(null)}
        />
      )}
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <TheHubApp />
    </AuthProvider>
  );
}
