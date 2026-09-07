export const initialUserProfile = {
  name: "Developer",
  role: "Software Engineer",
  email: "developer@domain.dev",
  avatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=256",
  github: "@developer",
  status: "Active",
  streakDays: 1,
  commitsToday: 0,
  rank: "Developer"
};

export const initialMetrics = {
  tasksCompleted: { value: 0, total: 0, percent: 0, trend: "No tasks yet" },
  activeProjects: { value: 0, total: 0, percent: 0, trend: "No active projects" },
  hoursCoded: { value: 0, target: 40, percent: 0, trend: "0.0 hrs/day" },
  sprintVelocity: { value: 0, target: 100, percent: 0, trend: "0 pts" }
};

export const initialProjects = [];

export const initialTasks = [];

export const initialActivities = [];

export const initialNotifications = [
  { id: "n1", title: "Welcome to DevPulse", message: "Your database is clean and ready for real data telemetry.", time: "Just now", unread: true }
];
