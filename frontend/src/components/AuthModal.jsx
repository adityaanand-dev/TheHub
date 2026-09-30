import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { X, Sparkles, AlertCircle, Loader2, UserCheck, Shield, Briefcase } from 'lucide-react';

export default function AuthModal({ onClose }) {
  const { login, register, quickDemoLogin, loading } = useAuth();
  const [isRegister, setIsRegister] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [role, setRole] = useState('ROLE_CLIENT');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (isRegister) {
        await register({ email, password, fullName, role });
      } else {
        await login(email, password);
      }
      onClose();
    } catch (err) {
      setError(err.message || 'Authentication failed');
    }
  };

  const handleDemo = async (type) => {
    setError('');
    try {
      await quickDemoLogin(type);
      onClose();
    } catch (err) {
      setError(err.message || 'Demo login failed');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
      <div className="relative w-full max-w-md rounded-3xl bg-slate-900 border border-slate-800 p-6 sm:p-8 shadow-2xl">
        <button
          onClick={onClose}
          className="absolute top-5 right-5 p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="mb-6 text-center">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-indigo-500 to-purple-500 flex items-center justify-center mx-auto mb-3 shadow-lg shadow-indigo-500/25">
            <Sparkles className="w-6 h-6 text-white" />
          </div>
          <h2 className="text-2xl font-black text-white">
            {isRegister ? 'Create Your Account' : 'Welcome Back to TheHub'}
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            {isRegister ? 'Join top creators and modern brands today.' : 'Sign in to access your projects and inquiries.'}
          </p>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {isRegister && (
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Full Name</label>
              <input
                type="text"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="e.g. Jordan Smith"
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-sm focus:outline-none focus:border-indigo-500"
              />
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Email Address</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="e.g. creator@thehub.com"
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-sm focus:outline-none focus:border-indigo-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Password</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-sm focus:outline-none focus:border-indigo-500"
            />
          </div>

          {isRegister && (
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">I want to:</label>
              <div className="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => setRole('ROLE_FREELANCER')}
                  className={`p-2.5 rounded-xl text-xs font-semibold border transition ${
                    role === 'ROLE_FREELANCER'
                      ? 'bg-indigo-600/20 border-indigo-500 text-indigo-300'
                      : 'bg-slate-950 border-slate-800 text-slate-400'
                  }`}
                >
                  Publish Services (Creator)
                </button>
                <button
                  type="button"
                  onClick={() => setRole('ROLE_CLIENT')}
                  className={`p-2.5 rounded-xl text-xs font-semibold border transition ${
                    role === 'ROLE_CLIENT'
                      ? 'bg-indigo-600/20 border-indigo-500 text-indigo-300'
                      : 'bg-slate-950 border-slate-800 text-slate-400'
                  }`}
                >
                  Hire Talent (Client)
                </button>
              </div>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white font-bold text-sm shadow-lg shadow-indigo-600/30 flex items-center justify-center gap-2 transition disabled:opacity-50"
          >
            {loading && <Loader2 className="w-4 h-4 animate-spin" />}
            {isRegister ? 'Create Account' : 'Sign In'}
          </button>
        </form>

        {/* 1-Click Demo Logins */}
        <div className="mt-6 pt-5 border-t border-slate-800/80">
          <div className="text-[11px] font-bold uppercase tracking-wider text-slate-500 mb-2.5 text-center">
            ⚡ 1-Click Demo Profiles
          </div>
          <div className="grid grid-cols-3 gap-2">
            <button
              onClick={() => handleDemo('creator')}
              className="p-2 rounded-xl bg-slate-950 border border-slate-800 hover:border-indigo-500/50 hover:bg-slate-800 text-center transition group"
            >
              <Briefcase className="w-4 h-4 text-indigo-400 mx-auto mb-1 group-hover:scale-110 transition" />
              <div className="text-[11px] font-bold text-white">Creator</div>
              <div className="text-[9px] text-slate-500">Alex Rivera</div>
            </button>

            <button
              onClick={() => handleDemo('client')}
              className="p-2 rounded-xl bg-slate-950 border border-slate-800 hover:border-purple-500/50 hover:bg-slate-800 text-center transition group"
            >
              <UserCheck className="w-4 h-4 text-purple-400 mx-auto mb-1 group-hover:scale-110 transition" />
              <div className="text-[11px] font-bold text-white">Client</div>
              <div className="text-[9px] text-slate-500">Ava Johnson</div>
            </button>

            <button
              onClick={() => handleDemo('admin')}
              className="p-2 rounded-xl bg-slate-950 border border-slate-800 hover:border-pink-500/50 hover:bg-slate-800 text-center transition group"
            >
              <Shield className="w-4 h-4 text-pink-400 mx-auto mb-1 group-hover:scale-110 transition" />
              <div className="text-[11px] font-bold text-white">Admin</div>
              <div className="text-[9px] text-slate-500">Platform Admin</div>
            </button>
          </div>
        </div>

        <div className="mt-4 text-center">
          <button
            onClick={() => { setIsRegister(!isRegister); setError(''); }}
            className="text-xs text-indigo-400 hover:underline"
          >
            {isRegister ? 'Already have an account? Sign in' : "Don't have an account? Register"}
          </button>
        </div>
      </div>
    </div>
  );
}
