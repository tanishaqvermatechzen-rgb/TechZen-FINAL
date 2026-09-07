/**
 * Telemetry, Auth & AI Service Layer
 * Connected directly to Supabase PostgreSQL & Express Backend (http://localhost:5001/api)
 */

import { initialUserProfile, initialMetrics, initialProjects, initialTasks, initialActivities } from '../data/mockData';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:5001/api';

export const telemetryService = {
  /**
   * Login User via Supabase PostgreSQL Auth Endpoint
   */
  async login(email, password) {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'Login failed');
    
    if (data.token) {
      localStorage.setItem('devpulse_token', data.token);
      localStorage.setItem('devpulse_user', JSON.stringify(data.user));
    }
    return data;
  },

  /**
   * Register New Account via Supabase PostgreSQL Auth Endpoint
   */
  async signup(email, password, name, role) {
    const response = await fetch(`${API_BASE_URL}/auth/signup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, name, role })
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'Signup failed');
    
    if (data.token) {
      localStorage.setItem('devpulse_token', data.token);
      localStorage.setItem('devpulse_user', JSON.stringify(data.user));
    }
    return data;
  },

  /**
   * Get Current Authenticated User
   */
  getCurrentUser() {
    const saved = localStorage.getItem('devpulse_user');
    if (saved) {
      try { return JSON.parse(saved); } catch (e) { return null; }
    }
    return null;
  },

  /**
   * Logout User
   */
  logout() {
    localStorage.removeItem('devpulse_token');
    localStorage.removeItem('devpulse_user');
  },

  /**
   * TASK 4 AI FEATURE: Generate Sprint Tasks with AI Engine
   */
  async generateAITasks(goal, project) {
    if (API_BASE_URL) {
      const response = await fetch(`${API_BASE_URL}/ai/generate-tasks`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ goal, project })
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.error || 'AI generation failed');
      return data;
    }
  },

  /**
   * Fetch developer metrics
   */
  async fetchMetrics() {
    if (API_BASE_URL) {
      try {
        const response = await fetch(`${API_BASE_URL}/telemetry/metrics`);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
      } catch (error) {
        console.warn("[Telemetry] Supabase connection unavailable, fallback to stream:", error);
      }
    }
    return initialMetrics;
  },

  /**
   * Fetch active projects
   */
  async fetchProjects() {
    if (API_BASE_URL) {
      try {
        const response = await fetch(`${API_BASE_URL}/projects`);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
      } catch (error) {
        console.warn("[Telemetry] Supabase connection unavailable, fallback to stream:", error);
      }
    }
    return initialProjects;
  },

  /**
   * Create Project
   */
  async createProject(projectData) {
    if (API_BASE_URL) {
      const response = await fetch(`${API_BASE_URL}/projects`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(projectData)
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.error || 'Project creation failed');
      return data;
    }
  },

  /**
   * Delete Project
   */
  async deleteProject(projectId) {
    if (API_BASE_URL) {
      const response = await fetch(`${API_BASE_URL}/projects/${projectId}`, {
        method: 'DELETE'
      });
      return await response.json();
    }
  },

  /**
   * Fetch sprint backlog tasks
   */
  async fetchTasks() {
    if (API_BASE_URL) {
      try {
        const response = await fetch(`${API_BASE_URL}/tasks`);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
      } catch (error) {
        console.warn("[Telemetry] Supabase connection unavailable, fallback to stream:", error);
      }
    }
    return initialTasks;
  },

  /**
   * Fetch developer profile
   */
  async fetchUserProfile() {
    if (API_BASE_URL) {
      try {
        const response = await fetch(`${API_BASE_URL}/user/profile`);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
      } catch (error) {
        console.warn("[Telemetry] Supabase connection unavailable, fallback to stream:", error);
      }
    }
    return initialUserProfile;
  },

  /**
   * Fetch activities
   */
  async fetchActivities() {
    if (API_BASE_URL) {
      try {
        const response = await fetch(`${API_BASE_URL}/activities`);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
      } catch (error) {
        console.warn("[Telemetry] Supabase connection unavailable, fallback to stream:", error);
      }
    }
    return initialActivities;
  },

  /**
   * Post new task into Supabase PostgreSQL
   */
  async createTask(newTask) {
    if (API_BASE_URL) {
      try {
        const response = await fetch(`${API_BASE_URL}/tasks`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(newTask)
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
      } catch (error) {
        console.warn("[Telemetry] Supabase save failed, stored in local state:", error);
      }
    }
    return newTask;
  },

  /**
   * Update task status
   */
  async updateTaskStatus(taskId, newStatus) {
    if (API_BASE_URL) {
      try {
        const response = await fetch(`${API_BASE_URL}/tasks/${taskId}`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ status: newStatus })
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
      } catch (error) {
        console.warn("[Telemetry] Supabase patch failed:", error);
      }
    }
  },

  /**
   * Delete task
   */
  async deleteTask(taskId) {
    if (API_BASE_URL) {
      const response = await fetch(`${API_BASE_URL}/tasks/${taskId}`, {
        method: 'DELETE'
      });
      return await response.json();
    }
  }
};
