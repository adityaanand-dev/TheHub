const API_BASE = '/api';

function getHeaders(token) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

export const api = {
  // Stats
  async getStats() {
    const res = await fetch(`${API_BASE}/stats`);
    if (!res.ok) throw new Error('Failed to fetch stats');
    return res.json();
  },

  // Auth
  async login(email, password) {
    const res = await fetch(`${API_BASE}/v1/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || err.detail || 'Login failed');
    }
    return res.json();
  },

  async register(data) {
    const res = await fetch(`${API_BASE}/v1/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || err.detail || 'Registration failed');
    }
    return res.json();
  },

  async getMe(token) {
    const res = await fetch(`${API_BASE}/v1/auth/me`, {
      headers: getHeaders(token)
    });
    if (!res.ok) throw new Error('Unauthorized');
    return res.json();
  },

  // =========================================================================
  // TWO-SIDED MARKETPLACE PROJECTS (Client posts, Creator discovers)
  // =========================================================================

  async getProjects(params = {}, token) {
    const query = new URLSearchParams();
    if (params.category && params.category !== 'All') query.append('category', params.category);
    if (params.search) query.append('search', params.search);
    if (params.skills) query.append('skills', params.skills);
    if (params.sort_by) query.append('sort_by', params.sort_by);
    if (params.min_budget) query.append('min_budget', params.min_budget);
    if (params.max_budget) query.append('max_budget', params.max_budget);
    if (params.experience_level) query.append('experience_level', params.experience_level);

    const res = await fetch(`${API_BASE}/v1/projects?${query.toString()}`, {
      headers: getHeaders(token)
    });
    if (!res.ok) throw new Error('Failed to fetch projects');
    return res.json();
  },

  async getProjectById(id, token) {
    const res = await fetch(`${API_BASE}/v1/projects/${id}`, {
      headers: getHeaders(token)
    });
    if (!res.ok) throw new Error('Project not found');
    return res.json();
  },

  async createProject(payload, token) {
    const res = await fetch(`${API_BASE}/v1/projects`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || err.detail || 'Failed to create project');
    }
    return res.json();
  },

  async getClientProjects(token) {
    const res = await fetch(`${API_BASE}/v1/projects/my`, {
      headers: getHeaders(token)
    });
    if (!res.ok) throw new Error('Failed to fetch your projects');
    return res.json();
  },

  async getClientOverview(token) {
    const res = await fetch(`${API_BASE}/v1/projects/overview/client`, {
      headers: getHeaders(token)
    });
    if (!res.ok) throw new Error('Failed to fetch client overview');
    return res.json();
  },

  async getCreatorOverview(token) {
    const res = await fetch(`${API_BASE}/v1/projects/overview/creator`, {
      headers: getHeaders(token)
    });
    if (!res.ok) throw new Error('Failed to fetch creator overview');
    return res.json();
  },

  async applyToProject(projectId, payload, token) {
    const res = await fetch(`${API_BASE}/v1/projects/${projectId}/apply`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || err.detail || 'Failed to submit proposal');
    }
    return res.json();
  },

  async getProjectApplications(projectId, token) {
    const res = await fetch(`${API_BASE}/v1/projects/${projectId}/applications`, {
      headers: getHeaders(token)
    });
    if (!res.ok) throw new Error('Failed to fetch applications');
    return res.json();
  },

  async getMyApplications(token) {
    const res = await fetch(`${API_BASE}/v1/projects/applications/my`, {
      headers: getHeaders(token)
    });
    if (!res.ok) throw new Error('Failed to fetch your applications');
    return res.json();
  },

  async getAssignedProjects(token) {
    const res = await fetch(`${API_BASE}/v1/projects/assigned`, {
      headers: getHeaders(token)
    });
    if (!res.ok) throw new Error('Failed to fetch assigned projects');
    return res.json();
  },

  async hireCreator(applicationId, token) {
    const res = await fetch(`${API_BASE}/v1/projects/applications/${applicationId}/accept`, {
      method: 'POST',
      headers: getHeaders(token)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || err.detail || 'Failed to hire creator');
    }
    return res.json();
  },

  async rejectApplication(applicationId, token) {
    const res = await fetch(`${API_BASE}/v1/projects/applications/${applicationId}/reject`, {
      method: 'POST',
      headers: getHeaders(token)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || err.detail || 'Failed to reject application');
    }
    return res.json();
  },

  async submitWork(projectId, payload, token) {
    const res = await fetch(`${API_BASE}/v1/projects/${projectId}/submit`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || err.detail || 'Failed to submit work');
    }
    return res.json();
  },

  async requestRevision(projectId, payload, token) {
    const res = await fetch(`${API_BASE}/v1/projects/${projectId}/request-revision`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || err.detail || 'Failed to request revision');
    }
    return res.json();
  },

  async approveWork(projectId, token) {
    const res = await fetch(`${API_BASE}/v1/projects/${projectId}/approve`, {
      method: 'POST',
      headers: getHeaders(token)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || err.detail || 'Failed to approve work');
    }
    return res.json();
  },

  // =========================================================================
  // CREATOR DISCOVERY (Client finds Creators)
  // =========================================================================

  async getCreators(params = {}) {
    const query = new URLSearchParams();
    if (params.search) query.append('search', params.search);
    if (params.skill && params.skill !== 'All') query.append('skill', params.skill);
    if (params.min_rating) query.append('min_rating', params.min_rating);
    if (params.min_rate) query.append('min_rate', params.min_rate);
    if (params.max_rate) query.append('max_rate', params.max_rate);

    const res = await fetch(`${API_BASE}/v1/creators?${query.toString()}`);
    if (!res.ok) throw new Error('Failed to fetch creators');
    return res.json();
  },

  async getCreatorProfile(id) {
    const res = await fetch(`${API_BASE}/v1/creators/${id}`);
    if (!res.ok) throw new Error('Creator not found');
    return res.json();
  },

  // =========================================================================
  // PROJECT CHAT
  // =========================================================================

  async sendProjectMessage(projectId, content, recipientId, token) {
    const res = await fetch(`${API_BASE}/v1/chat/projects/${projectId}/messages`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify({ content, recipient_id: recipientId || null })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || err.detail || 'Failed to send message');
    }
    return res.json();
  },

  async getProjectMessages(projectId, token) {
    const res = await fetch(`${API_BASE}/v1/chat/projects/${projectId}/messages`, {
      headers: getHeaders(token)
    });
    if (!res.ok) throw new Error('Failed to load conversation');
    return res.json();
  },

  // Reviews
  async submitReview(payload, token) {
    const res = await fetch(`${API_BASE}/v1/reviews`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || err.detail || 'Failed to submit review');
    }
    return res.json();
  },

  // Notifications
  async getNotifications(token) {
    const res = await fetch(`${API_BASE}/v1/notifications`, {
      headers: getHeaders(token)
    });
    if (!res.ok) return [];
    return res.json();
  },

  // Admin
  async getAdminUsers(token) {
    const res = await fetch(`${API_BASE}/v1/admin/users`, {
      headers: getHeaders(token)
    });
    if (!res.ok) throw new Error('Forbidden');
    return res.json();
  },

  async toggleService(id, token) {
    const res = await fetch(`${API_BASE}/v1/admin/services/${id}/toggle`, {
      method: 'PATCH',
      headers: getHeaders(token)
    });
    if (!res.ok) throw new Error('Failed to update service');
    return res.json();
  }
};
