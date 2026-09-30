import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('thehub_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('thehub_token') || null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (token) {
      localStorage.setItem('thehub_token', token);
    } else {
      localStorage.removeItem('thehub_token');
      localStorage.removeItem('thehub_user');
    }
  }, [token]);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const res = await api.login(email, password);
      setToken(res.token);
      const userData = { id: res.id, email: res.email, fullName: res.fullName, role: res.role };
      setUser(userData);
      localStorage.setItem('thehub_user', JSON.stringify(userData));
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const register = async (data) => {
    setLoading(true);
    try {
      const res = await api.register(data);
      setToken(res.token);
      const userData = { id: res.id, email: res.email, fullName: res.fullName, role: res.role };
      setUser(userData);
      localStorage.setItem('thehub_user', JSON.stringify(userData));
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const quickDemoLogin = async (type) => {
    if (type === 'creator') {
      return login('creator@thehub.com', 'creator123');
    } else if (type === 'client') {
      return login('client@thehub.com', 'client123');
    } else if (type === 'admin') {
      return login('admin@thehub.com', 'admin123');
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('thehub_token');
    localStorage.removeItem('thehub_user');
  };

  const isClient = user?.role === 'ROLE_CLIENT';
  const isCreator = user?.role === 'ROLE_FREELANCER' || user?.role === 'ROLE_CREATOR';
  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <AuthContext.Provider value={{ user, token, loading, isClient, isCreator, isAdmin, login, register, logout, quickDemoLogin }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
