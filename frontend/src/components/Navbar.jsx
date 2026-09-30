import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Sparkles, Briefcase, PlusCircle, LayoutDashboard, Shield, LogOut, LogIn, Users, Search, UserCheck } from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab, onOpenAuth, onOpenPostProject }) {
  const { user, logout, quickDemoLogin, isClient, isCreator, isAdmin } = useAuth();

  const handleQuickSwitch = async (roleType) => {
    try {
      await quickDemoLogin(roleType);
      if (roleType === 'client') {
        setActiveTab('client-dashboard');
      } else if (roleType === 'creator') {
        setActiveTab('creator-dashboard');
      } else if (roleType === 'admin') {
        setActiveTab('admin');
      }
    } catch (err) {
      console.error('Demo switch failed:', err);
    }
  };

  return (
    <header className="sticky top-0 z-40 w-full border-b border-slate-800 bg-[#070b16]/90 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
        
        {/* Brand */}
        <div
          className="flex items-center gap-3 cursor-pointer shrink-0"
          onClick={() => setActiveTab(isClient ? 'client-dashboard' : isCreator ? 'creator-dashboard' : 'find-projects')}
        >
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-indigo-500 via-purple-500 to-pink-500 flex items-center justify-center shadow-lg shadow-indigo-500/25">
            <Sparkles className="w-5 h-5 text-white" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-black text-xl tracking-tight text-white">TheHub</span>
              <span className="text-[10px] uppercase font-bold tracking-widest px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                PROD
              </span>
            </div>
          </div>
        </div>

        {/* Dynamic Navigation Tabs based on Role */}
        <nav className="hidden md:flex items-center gap-1.5 bg-slate-900/70 p-1.5 rounded-2xl border border-slate-800/80">
          {/* Guest / Visitor Tabs */}
          {!user && (
            <>
              <button
                onClick={() => setActiveTab('find-projects')}
                className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition ${
                  activeTab === 'find-projects'
                    ? 'bg-cyan-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                }`}
              >
                <Search className="w-3.5 h-3.5" />
                Browse Projects
              </button>
              <button
                onClick={() => setActiveTab('find-creators')}
                className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition ${
                  activeTab === 'find-creators'
                    ? 'bg-amber-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                }`}
              >
                <Users className="w-3.5 h-3.5" />
                Find Creators
              </button>
            </>
          )}

          {/* Client Navigation */}
          {user && isClient && (
            <>
              <button
                onClick={() => setActiveTab('client-dashboard')}
                className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition ${
                  activeTab === 'client-dashboard'
                    ? 'bg-amber-500 text-slate-950 font-bold shadow-md shadow-amber-500/20'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                }`}
              >
                <LayoutDashboard className="w-3.5 h-3.5" />
                Client Dashboard
              </button>
              <button
                onClick={() => setActiveTab('find-creators')}
                className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition ${
                  activeTab === 'find-creators'
                    ? 'bg-amber-500 text-slate-950 font-bold shadow-md shadow-amber-500/20'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                }`}
              >
                <Users className="w-3.5 h-3.5" />
                Find Creators
              </button>
              <button
                onClick={onOpenPostProject}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold bg-amber-500/10 text-amber-300 border border-amber-500/30 hover:bg-amber-500/20 transition ml-1"
              >
                <PlusCircle className="w-3.5 h-3.5 text-amber-400" />
                Post Project
              </button>
            </>
          )}

          {/* Creator Navigation */}
          {user && isCreator && (
            <>
              <button
                onClick={() => setActiveTab('find-projects')}
                className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition ${
                  activeTab === 'find-projects'
                    ? 'bg-cyan-500 text-slate-950 font-bold shadow-md shadow-cyan-500/20'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                }`}
              >
                <Search className="w-3.5 h-3.5" />
                Find Projects
              </button>
              <button
                onClick={() => setActiveTab('creator-dashboard')}
                className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition ${
                  activeTab === 'creator-dashboard'
                    ? 'bg-cyan-500 text-slate-950 font-bold shadow-md shadow-cyan-500/20'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                }`}
              >
                <LayoutDashboard className="w-3.5 h-3.5" />
                Creator Dashboard
              </button>
            </>
          )}

          {/* Admin Navigation */}
          {user && isAdmin && (
            <>
              <button
                onClick={() => setActiveTab('admin')}
                className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition ${
                  activeTab === 'admin'
                    ? 'bg-purple-600 text-white shadow-sm'
                    : 'text-purple-400 hover:text-white hover:bg-slate-800/50'
                }`}
              >
                <Shield className="w-3.5 h-3.5" />
                Admin Portal
              </button>
              <button
                onClick={() => setActiveTab('find-projects')}
                className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition ${
                  activeTab === 'find-projects'
                    ? 'bg-slate-700 text-white'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                }`}
              >
                Projects
              </button>
              <button
                onClick={() => setActiveTab('find-creators')}
                className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition ${
                  activeTab === 'find-creators'
                    ? 'bg-slate-700 text-white'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                }`}
              >
                Creators
              </button>
            </>
          )}
        </nav>

        {/* 1-Click Role Switcher & Auth */}
        <div className="flex items-center gap-3">
          {/* Quick Role Switcher Pills */}
          <div className="hidden lg:flex items-center bg-slate-950/80 p-1 rounded-xl border border-slate-800 text-[11px]">
            <span className="text-slate-500 px-2 font-mono font-medium">Switch:</span>
            <button
              onClick={() => handleQuickSwitch('client')}
              title="Switch to Client (Ava Johnson)"
              className={`px-2.5 py-1 rounded-lg font-medium transition flex items-center gap-1 ${
                isClient
                  ? 'bg-amber-500/20 text-amber-300 font-bold border border-amber-500/30'
                  : 'text-slate-400 hover:text-amber-300 hover:bg-slate-900'
              }`}
            >
              <span>👔</span> Client
            </button>
            <button
              onClick={() => handleQuickSwitch('creator')}
              title="Switch to Creator (Alex Rivera)"
              className={`px-2.5 py-1 rounded-lg font-medium transition flex items-center gap-1 ${
                isCreator
                  ? 'bg-cyan-500/20 text-cyan-300 font-bold border border-cyan-500/30'
                  : 'text-slate-400 hover:text-cyan-300 hover:bg-slate-900'
              }`}
            >
              <span>🎨</span> Creator
            </button>
            <button
              onClick={() => handleQuickSwitch('admin')}
              title="Switch to Admin"
              className={`px-2 py-1 rounded-lg font-medium transition flex items-center gap-1 ${
                isAdmin
                  ? 'bg-purple-500/20 text-purple-300 font-bold border border-purple-500/30'
                  : 'text-slate-400 hover:text-purple-300 hover:bg-slate-900'
              }`}
            >
              <span>🛡️</span> Admin
            </button>
          </div>

          {/* User Account / Auth Actions */}
          {user ? (
            <div className="flex items-center gap-3">
              <div className="text-right hidden sm:block">
                <div className="text-xs font-bold text-white">{user.fullName}</div>
                <div className="text-[10px] text-indigo-400 uppercase font-bold tracking-wider">
                  {user.role === 'ROLE_CLIENT' ? 'Client (Buyer)' : user.role === 'ROLE_ADMIN' ? 'Admin' : 'Creator (Provider)'}
                </div>
              </div>
              <button
                onClick={logout}
                title="Sign out"
                className="p-2 rounded-xl text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 border border-transparent hover:border-rose-500/20 transition"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <button
              onClick={onOpenAuth}
              className="flex items-center gap-2 px-4 py-2 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white text-xs font-bold shadow-lg shadow-indigo-500/25 transition"
            >
              <LogIn className="w-4 h-4" />
              Sign In
            </button>
          )}
        </div>
      </div>
    </header>
  );
}
