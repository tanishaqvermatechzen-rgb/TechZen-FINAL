import React from 'react';
import { Route, Switch, useLocation } from 'wouter';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ClerkProvider } from '@clerk/react';
import { GoogleOAuthProvider } from '@react-oauth/google';
// Extensions are explicit on purpose: src/ still holds legacy .jsx twins of these
// modules (Home.jsx, EventDetail.jsx, UserPortal.jsx, NotFound.jsx, EventCard.jsx,
// ErrorBoundary.jsx). Vite resolves .jsx before .tsx, so an extensionless specifier
// picks the .jsx twin on case-insensitive macOS and the .tsx file on Linux/Vercel —
// i.e. dev and production would render different components.
import { ErrorBoundary } from './components/error-boundary.tsx';
import { AuthProvider } from './context/AuthContext';
import { EventProvider } from './context/EventContext';

import Home from './pages/home.tsx';
import AllEvents from './pages/all-events.tsx';
import EventDetail from './pages/event-detail.tsx';
import UserPortal from './pages/user-portal.tsx';
import NotFound from './pages/not-found.tsx';

import CreateEventModal from './components/CreateEventModal';
import TicketModal from './components/TicketModal';
import AuthModal from './components/AuthModal';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
      refetchOnWindowFocus: false,
    },
  },
});

const CLERK_PUBLISHABLE_KEY = import.meta.env.VITE_CLERK_PUBLISHABLE_KEY || '';
const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || '1015290855644-ca1cfggbdu0oo5euqtg2bjs819tmehu4.apps.googleusercontent.com';

function SafeClerkProvider({ children }) {
  // Only mount ClerkProvider if user has configured a valid live or registered key
  if (CLERK_PUBLISHABLE_KEY && CLERK_PUBLISHABLE_KEY.startsWith('pk_live_')) {
    return <ClerkProvider publishableKey={CLERK_PUBLISHABLE_KEY}>{children}</ClerkProvider>;
  }
  return <>{children}</>;
}

export default function App() {
  const [location] = useLocation();
  return (
    <ErrorBoundary resetKey={location}>
      <GoogleOAuthProvider clientId={GOOGLE_CLIENT_ID}>
        <SafeClerkProvider>
          <QueryClientProvider client={queryClient}>
            <AuthProvider>
              <EventProvider>
                <Switch>
                  <Route path="/" component={Home} />
                  <Route path="/all-events" component={AllEvents} />
                  <Route path="/events" component={AllEvents} />
                  <Route path="/our-code" component={Home} />
                  <Route path="/manifesto" component={Home} />
                  <Route path="/events/:eventId" component={EventDetail} />
                  <Route path="/user-portal" component={UserPortal} />
                  <Route path="/dashboard" component={UserPortal} />
                  <Route path="/sign-in" component={Home} />
                  <Route path="/sign-up" component={Home} />
                  <Route component={NotFound} />
                </Switch>

                {/* Modals */}
                <CreateEventModal />
                <TicketModal />
                <AuthModal />
              </EventProvider>
            </AuthProvider>
          </QueryClientProvider>
        </SafeClerkProvider>
      </GoogleOAuthProvider>
    </ErrorBoundary>
  );
}
