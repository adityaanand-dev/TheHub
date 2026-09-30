import React, { useState, useEffect, useRef } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { MessageSquare, Send, X, AlertCircle, Briefcase } from 'lucide-react';

export default function ChatModal({ project: initialProject, recipient, onClose }) {
  const { user, token, isClient } = useAuth();
  const [currentProject, setCurrentProject] = useState(initialProject || null);
  const [availableProjects, setAvailableProjects] = useState([]);
  const [messages, setMessages] = useState([]);
  const [newMsg, setNewMsg] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState(null);
  const messagesEndRef = useRef(null);

  // If no project was explicitly passed, load user's projects to link to this conversation
  useEffect(() => {
    if (!initialProject && token) {
      if (isClient) {
        api.getClientProjects(token).then((projs) => {
          setAvailableProjects(projs);
          if (projs.length > 0) setCurrentProject(projs[0]);
        }).catch(console.error);
      } else {
        api.getAssignedProjects(token).then((projs) => {
          setAvailableProjects(projs);
          if (projs.length > 0) setCurrentProject(projs[0]);
        }).catch(console.error);
      }
    }
  }, [initialProject, token, isClient]);

  const loadMessages = async () => {
    if (!currentProject?.id || !token) {
      setLoading(false);
      return;
    }
    try {
      const data = await api.getProjectMessages(currentProject.id, token);
      setMessages(data);
    } catch (err) {
      console.error(err);
      setError(err.message || 'Failed to load messages');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMessages();
    const interval = setInterval(loadMessages, 4000);
    return () => clearInterval(interval);
  }, [currentProject?.id, token]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!newMsg.trim() || sending || !currentProject?.id) return;
    setSending(true);
    try {
      const recipientId = recipient?.id || (user.id === currentProject.client_id ? currentProject.selected_creator_id : currentProject.client_id);
      if (!recipientId) {
        alert('Please specify or select a recipient for this project conversation.');
        return;
      }
      await api.sendProjectMessage(currentProject.id, newMsg.trim(), recipientId, token);
      setNewMsg('');
      loadMessages();
    } catch (err) {
      alert(err.message || 'Failed to send message');
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-sm p-4 animate-in fade-in">
      <div className="bg-slate-900 border border-slate-800 rounded-3xl w-full max-w-2xl flex flex-col h-[620px] shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-800 bg-slate-950/70 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-amber-500 to-orange-500 flex items-center justify-center text-slate-950 font-bold text-lg shadow-md shadow-amber-500/20">
              <MessageSquare className="w-5 h-5 text-slate-950" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-white font-bold text-base truncate max-w-sm">
                  {currentProject?.title || (recipient ? `Chat with ${recipient.fullName || recipient.full_name || 'User'}` : 'Project Chat')}
                </h3>
                {currentProject?.status && (
                  <span className="text-[10px] px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20 font-bold uppercase">
                    {currentProject.status}
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-400">
                {currentProject?.budget ? `Budget: ₹${currentProject.budget.toLocaleString()} • ` : ''}
                Recipient: <strong className="text-slate-300">{recipient?.fullName || recipient?.full_name || (user?.id === currentProject?.client_id ? (currentProject?.selected_creator_name || 'Creator') : (currentProject?.client_name || 'Client'))}</strong>
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {availableProjects.length > 1 && (
              <select
                value={currentProject?.id || ''}
                onChange={(e) => {
                  const p = availableProjects.find(item => item.id === e.target.value);
                  if (p) setCurrentProject(p);
                }}
                className="bg-slate-800 text-xs text-slate-200 border border-slate-700 rounded-lg px-2 py-1 focus:outline-none"
              >
                {availableProjects.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.title}
                  </option>
                ))}
              </select>
            )}
            <button
              onClick={onClose}
              className="text-slate-400 hover:text-white p-2 rounded-xl hover:bg-slate-800 transition"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Project Notice if none */}
        {!currentProject?.id && (
          <div className="p-4 bg-amber-500/10 border-b border-amber-500/20 text-amber-300 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>No active project selected. Chat messages are scoped to specific projects. Please post or select a project first.</span>
          </div>
        )}

        {/* Message List */}
        <div className="flex-1 overflow-y-auto p-6 space-y-4 bg-slate-950/40">
          {loading && messages.length === 0 ? (
            <div className="flex items-center justify-center h-full text-slate-400 text-sm">
              Loading conversation...
            </div>
          ) : error ? (
            <div className="text-center text-red-400 py-8 text-sm">{error}</div>
          ) : messages.length === 0 ? (
            <div className="text-center text-slate-500 py-16">
              <Briefcase className="w-10 h-10 text-slate-700 mx-auto mb-2" />
              <p className="text-sm font-medium text-slate-400">No messages yet regarding this project.</p>
              <p className="text-xs mt-1 text-slate-600">Send a greeting to start direct communication with project deliverables.</p>
            </div>
          ) : (
            messages.map((m) => {
              const isMe = m.sender_id === user?.id;
              return (
                <div key={m.id} className={`flex flex-col ${isMe ? 'items-end' : 'items-start'}`}>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-xs font-semibold text-slate-400">
                      {isMe ? 'You' : m.sender_name}
                    </span>
                    <span className="text-[10px] text-slate-500">
                      {new Date(m.created_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <div
                    className={`max-w-[75%] rounded-2xl px-4 py-2.5 text-sm leading-relaxed ${
                      isMe
                        ? 'bg-amber-500 text-slate-950 font-medium rounded-tr-none shadow-md shadow-amber-500/10'
                        : 'bg-slate-800 text-slate-200 border border-slate-700/60 rounded-tl-none'
                    }`}
                  >
                    {m.content}
                  </div>
                </div>
              );
            })
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* Footer Input */}
        <form onSubmit={handleSend} className="p-4 border-t border-slate-800 bg-slate-900/90 flex gap-3">
          <input
            type="text"
            disabled={!currentProject?.id}
            value={newMsg}
            onChange={(e) => setNewMsg(e.target.value)}
            placeholder={currentProject?.id ? "Type your message regarding requirements, milestones, deliverables..." : "Select or create a project to send messages..."}
            className="flex-1 bg-slate-950 border border-slate-700 text-white text-sm rounded-xl px-4 py-2.5 focus:outline-none focus:border-amber-500 transition disabled:opacity-50"
          />
          <button
            type="submit"
            disabled={sending || !newMsg.trim() || !currentProject?.id}
            className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold px-5 py-2.5 rounded-xl text-sm transition flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-amber-500/20"
          >
            <Send className="w-4 h-4" />
            <span>{sending ? 'Sending...' : 'Send'}</span>
          </button>
        </form>
      </div>
    </div>
  );
}
