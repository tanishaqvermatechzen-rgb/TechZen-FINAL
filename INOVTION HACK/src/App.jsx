import React, { useState, useEffect } from 'react';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import MetricsOverview from './components/MetricsOverview';
import ProjectGrid from './components/ProjectGrid';
import TaskList from './components/TaskList';
import AddTaskModal from './components/AddTaskModal';
import AddProjectModal from './components/AddProjectModal';
import ProjectDetailModal from './components/ProjectDetailModal';
import ExportStandupModal from './components/ExportStandupModal';
import AIAssistantModal from './components/AIAssistantModal';
import ActivityFeed from './components/ActivityFeed';
import PersonalFocusCard from './components/PersonalFocusCard';
import UserProfile from './components/UserProfile';
import LoginModal from './components/LoginModal';
import LoadingSkeleton from './components/states/LoadingSkeleton';
import EmptyState from './components/states/EmptyState';
import ErrorState from './components/states/ErrorState';

import { telemetryService } from './services/telemetryService';
import { Plus, Sparkles } from 'lucide-react';

import { 
  initialUserProfile, 
  initialMetrics, 
  initialProjects, 
  initialTasks, 
  initialActivities,
  initialNotifications 
} from './data/mockData';

export default function App() {
  const [authenticatedUser, setAuthenticatedUser] = useState(() => telemetryService.getCurrentUser());
  const [activeTab, setActiveTab] = useState('dashboard');
  const [viewState, setViewState] = useState('active');
  const [searchQuery, setSearchQuery] = useState('');
  const [mobileOpen, setMobileOpen] = useState(false);
  
  // Modals State
  const [isAddTaskModalOpen, setIsAddTaskModalOpen] = useState(false);
  const [isAddProjectModalOpen, setIsAddProjectModalOpen] = useState(false);
  const [selectedProject, setSelectedProject] = useState(null);
  const [isExportModalOpen, setIsExportModalOpen] = useState(false);
  const [isAIModalOpen, setIsAIModalOpen] = useState(false);

  // Application Data States
  const [userProfile, setUserProfile] = useState(initialUserProfile);
  const [metrics, setMetrics] = useState(initialMetrics);
  const [projects, setProjects] = useState(initialProjects);
  const [tasks, setTasks] = useState(initialTasks);
  const [activities, setActivities] = useState(initialActivities);
  const [notifications, setNotifications] = useState(initialNotifications);

  // Live Laptop Session Tracker
  const [sessionSeconds, setSessionSeconds] = useState(0);
  const [baseHoursFromDB, setBaseHoursFromDB] = useState(0);

  // Sync authenticated user info
  useEffect(() => {
    if (authenticatedUser) {
      setUserProfile(prev => ({
        ...prev,
        name: authenticatedUser.name || prev.name,
        email: authenticatedUser.email || prev.email,
        role: authenticatedUser.role || prev.role,
        avatar: authenticatedUser.avatar || prev.avatar
      }));
    }
  }, [authenticatedUser]);

  // Real-time Laptop Active Session Tracker
  useEffect(() => {
    if (!authenticatedUser) return;
    const timer = setInterval(() => {
      if (document.visibilityState === 'visible') {
        setSessionSeconds(prev => prev + 1);
      }
    }, 1000);
    return () => clearInterval(timer);
  }, [authenticatedUser]);

  // Sync Live Session Seconds with Coding Hours Metric
  useEffect(() => {
    if (!authenticatedUser) return;
    const extraHours = sessionSeconds / 3600;
    const currentTotalHours = (baseHoursFromDB + extraHours).toFixed(2);
    const target = 40;
    const percent = Math.min(100, Math.round((currentTotalHours / target) * 100));

    setMetrics(prev => ({
      ...prev,
      hoursCoded: {
        ...prev.hoursCoded,
        value: currentTotalHours,
        percent,
        trend: `+${sessionSeconds}s live session`
      }
    }));
  }, [sessionSeconds, baseHoursFromDB, authenticatedUser]);

  // Load Data directly from Supabase PostgreSQL Backend
  useEffect(() => {
    if (!authenticatedUser) return;
    async function loadData() {
      try {
        const [p, t, u, m, act] = await Promise.all([
          telemetryService.fetchProjects(),
          telemetryService.fetchTasks(),
          telemetryService.fetchUserProfile(),
          telemetryService.fetchMetrics(),
          telemetryService.fetchActivities()
        ]);
        if (p) setProjects(p);
        if (t) setTasks(t);
        if (u) setUserProfile(prev => ({ ...prev, ...u, name: authenticatedUser.name || u.name }));
        if (m) {
          setMetrics(m);
          setBaseHoursFromDB(parseFloat(m.hoursCoded?.value) || 0);
        }
        if (act) setActivities(act);
      } catch (err) {
        console.warn("Supabase fetch fallback:", err);
      }
    }
    loadData();
  }, [authenticatedUser]);

  // Toggle Task Completion State
  const handleToggleTask = async (taskId) => {
    let nextStatus = 'Completed';
    setTasks(prevTasks => {
      const updated = prevTasks.map(task => {
        if (task.id === taskId) {
          nextStatus = task.status === 'Completed' ? 'Pending' : 'Completed';
          return { ...task, status: nextStatus };
        }
        return task;
      });

      const completedCount = updated.filter(t => t.status === 'Completed').length;
      const totalCount = updated.length;
      const percent = totalCount > 0 ? Math.round((completedCount / totalCount) * 100) : 0;

      setMetrics(prev => ({
        ...prev,
        tasksCompleted: {
          ...prev.tasksCompleted,
          value: completedCount,
          total: totalCount,
          percent
        }
      }));

      return updated;
    });

    await telemetryService.updateTaskStatus(taskId, nextStatus);
  };

  // Add New Task
  const handleAddTask = async (newTask) => {
    const savedTask = await telemetryService.createTask(newTask);
    setTasks(prev => [savedTask, ...prev]);
  };

  // Delete Task
  const handleDeleteTask = async (taskId) => {
    await telemetryService.deleteTask(taskId);
    setTasks(prev => prev.filter(t => t.id !== taskId));
  };

  // Add New Project
  const handleProjectAdded = (newProject) => {
    setProjects(prev => [newProject, ...prev]);
  };

  // Delete Project
  const handleProjectDeleted = (projectId) => {
    setProjects(prev => prev.filter(p => p.id !== projectId));
    setSelectedProject(null);
  };

  // AI Task Prioritizer
  const handleAIPrioritize = () => {
    const priorityMap = { High: 1, Medium: 2, Low: 3 };
    setTasks(prev => [...prev].sort((a, b) => (priorityMap[a.priority] || 4) - (priorityMap[b.priority] || 4)));
  };

  // Handle AI Generated Tasks
  const handleAITasksGenerated = (newAITasks) => {
    setTasks(prev => [...newAITasks, ...prev]);
    telemetryService.fetchActivities().then(act => act && setActivities(act));
  };

  // Logout Handler
  const handleLogout = () => {
    telemetryService.logout();
    setAuthenticatedUser(null);
  };

  const isSearchEmpty = searchQuery.trim().length > 0 && 
    !projects.some(p => p.name.toLowerCase().includes(searchQuery.toLowerCase())) &&
    !tasks.some(t => t.title.toLowerCase().includes(searchQuery.toLowerCase()));

  // RENDER LOGIN SCREEN IF NOT AUTHENTICATED
  if (!authenticatedUser) {
    return <LoginModal onLoginSuccess={(user) => setAuthenticatedUser(user)} />;
  }

  return (
    <div className="min-h-screen bg-[#F3F5F2] text-[#171E2D] flex flex-col font-sans">
      {/* Navigation Sidebar */}
      <Sidebar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab}
        mobileOpen={mobileOpen}
        setMobileOpen={setMobileOpen}
        userProfile={userProfile}
        projectsCount={projects.length}
        tasksCount={tasks.filter(t => t.status !== 'Completed').length}
        onOpenAddTaskModal={() => setIsAddTaskModalOpen(true)}
        onOpenAddProjectModal={() => setIsAddProjectModalOpen(true)}
      />

      {/* Main Content Area */}
      <div className="lg:pl-64 flex-1 flex flex-col min-w-0">
        {/* Top Header */}
        <Header 
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          setMobileOpen={setMobileOpen}
          onOpenAddTaskModal={() => setIsAddTaskModalOpen(true)}
          onOpenAIModal={() => setIsAIModalOpen(true)}
          onOpenExportModal={() => setIsExportModalOpen(true)}
          userProfile={userProfile}
          notifications={notifications}
          onLogout={handleLogout}
        />

        {/* Dynamic Page Body */}
        <main className="flex-1 p-4 lg:p-8 space-y-8 max-w-7xl w-full mx-auto">
          
          {/* OVERVIEW LANDING VIEW */}
          {activeTab === 'dashboard' && (
            <>
              {/* Fieldnote Greeting Banner */}
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                    TUESDAY, JUNE 17, 2025
                  </div>
                  <h1 className="text-3xl font-extrabold text-[#171E2D] tracking-tight mt-1 flex items-center">
                    Good morning, {userProfile.name?.split(' ')[0] || 'Maya'}<span className="text-[#9BE838] font-black text-4xl leading-none">.</span>
                  </h1>
                  <p className="text-xs text-slate-500 font-medium mt-1">
                    Your team has a clear runway today. Here's where the momentum is.
                  </p>
                </div>

                <button
                  onClick={() => setIsAddTaskModalOpen(true)}
                  className="fieldnote-btn-primary px-4 py-2.5 rounded-xl text-xs font-bold shadow-sm flex items-center space-x-1.5 w-fit"
                >
                  <Plus className="w-4 h-4" />
                  <span>Add a task</span>
                </button>
              </div>

              {/* 3 Metric Summary Cards */}
              <MetricsOverview metrics={metrics} onSelectTab={setActiveTab} />

              {/* Main Content Grid */}
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Left Column (2 cols): Projects & Task Queue */}
                <div className="lg:col-span-2 space-y-6">
                  <ProjectGrid 
                    projects={projects} 
                    tasks={tasks}
                    searchQuery={searchQuery} 
                    onSelectProject={(p) => setSelectedProject(p)}
                    onOpenAddProjectModal={() => setIsAddProjectModalOpen(true)}
                  />
                  <TaskList 
                    tasks={tasks} 
                    onToggleTask={handleToggleTask}
                    onDeleteTask={handleDeleteTask}
                    onAIPrioritize={handleAIPrioritize}
                    searchQuery={searchQuery}
                    onOpenAddTaskModal={() => setIsAddTaskModalOpen(true)}
                  />
                </div>

                {/* Right Column (1 col): Personal Focus & Activity Feed */}
                <div className="space-y-6">
                  <PersonalFocusCard onOpenTasks={() => setActiveTab('tasks')} />
                  <ActivityFeed activities={activities} />
                </div>
              </div>
            </>
          )}

          {/* PROJECTS VIEW */}
          {activeTab === 'projects' && (
            <div className="space-y-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200/60">
                <div>
                  <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                    WORKSPACE / PROJECTS
                  </div>
                  <h1 className="text-3xl font-extrabold text-[#171E2D] tracking-tight mt-1 flex items-center">
                    Projects<span className="text-[#9BE838] font-black text-4xl leading-none">.</span>
                  </h1>
                  <p className="text-xs text-slate-500 font-medium mt-1">
                    A clear view of the work moving your team forward.
                  </p>
                </div>

                <button
                  onClick={() => setIsAddProjectModalOpen(true)}
                  className="fieldnote-btn-primary px-4 py-2.5 rounded-xl text-xs font-bold shadow-sm flex items-center space-x-1.5 w-fit"
                >
                  <Plus className="w-4 h-4" />
                  <span>New project</span>
                </button>
              </div>

              <ProjectGrid 
                projects={projects} 
                tasks={tasks}
                searchQuery={searchQuery} 
                onSelectProject={(p) => setSelectedProject(p)}
                onOpenAddProjectModal={() => setIsAddProjectModalOpen(true)}
              />
            </div>
          )}

          {/* TASKS VIEW */}
          {activeTab === 'tasks' && (
            <div className="space-y-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200/60">
                <div>
                  <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                    WORKSPACE / TASKS
                  </div>
                  <h1 className="text-3xl font-extrabold text-[#171E2D] tracking-tight mt-1 flex items-center">
                    Tasks<span className="text-[#9BE838] font-black text-4xl leading-none">.</span>
                  </h1>
                  <p className="text-xs text-slate-500 font-medium mt-1">
                    The next right thing, all in one place.
                  </p>
                </div>

                <button
                  onClick={() => setIsAddTaskModalOpen(true)}
                  className="fieldnote-btn-primary px-4 py-2.5 rounded-xl text-xs font-bold shadow-sm flex items-center space-x-1.5 w-fit"
                >
                  <Plus className="w-4 h-4" />
                  <span>Add task</span>
                </button>
              </div>

              <TaskList 
                tasks={tasks} 
                onToggleTask={handleToggleTask}
                onDeleteTask={handleDeleteTask}
                onAIPrioritize={handleAIPrioritize}
                searchQuery={searchQuery}
                onOpenAddTaskModal={() => setIsAddTaskModalOpen(true)}
              />
            </div>
          )}

          {/* ANALYTICS & SETTINGS VIEW */}
          {activeTab === 'analytics' && (
            <div className="space-y-6">
              <MetricsOverview metrics={metrics} onSelectTab={setActiveTab} />
              <ActivityFeed activities={activities} />
            </div>
          )}

          {activeTab === 'settings' && (
            <div className="max-w-2xl">
              <UserProfile profile={userProfile} />
            </div>
          )}

        </main>
      </div>

      {/* Add Task Modal */}
      <AddTaskModal 
        isOpen={isAddTaskModalOpen}
        onClose={() => setIsAddTaskModalOpen(false)}
        onAddTask={handleAddTask}
        projects={projects}
      />

      {/* Add Project Modal */}
      <AddProjectModal
        isOpen={isAddProjectModalOpen}
        onClose={() => setIsAddProjectModalOpen(false)}
        onProjectAdded={handleProjectAdded}
      />

      {/* Project Detail Modal */}
      <ProjectDetailModal
        project={selectedProject}
        isOpen={!!selectedProject}
        onClose={() => setSelectedProject(null)}
        onDeleteProject={handleProjectDeleted}
        tasks={tasks}
      />

      {/* AI Task Assistant Modal */}
      <AIAssistantModal
        isOpen={isAIModalOpen}
        onClose={() => setIsAIModalOpen(false)}
        onTasksGenerated={handleAITasksGenerated}
        projects={projects}
      />

      {/* Export Standup Modal */}
      <ExportStandupModal
        isOpen={isExportModalOpen}
        onClose={() => setIsExportModalOpen(false)}
        userProfile={userProfile}
        tasks={tasks}
        projects={projects}
        metrics={metrics}
      />
    </div>
  );
}
