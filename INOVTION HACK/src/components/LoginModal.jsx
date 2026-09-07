import React, { useState } from 'react';
import { Lock, Mail, User, Eye, EyeOff, ArrowRight, Sparkles, AlertCircle, LayoutGrid } from 'lucide-react';
import { telemetryService } from '../services/telemetryService';

export default function LoginModal({ onLoginSuccess }) {
  const [isSignUp, setIsSignUp] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [role, setRole] = useState('Senior Full Stack Engineer');
  const [showPassword, setShowPassword] = useState(false);
  const [keepSignedIn, setKeepSignedIn] = useState(true);
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isSignUp) {
        const data = await telemetryService.signup(email, password, name, role);
        onLoginSuccess(data.user);
      } else {
        const data = await telemetryService.login(email, password);
        onLoginSuccess(data.user);
      }
    } catch (err) {
      setError(err.message || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  // Quick fill demo user credentials
  const fillDemoAccount = () => {
    setIsSignUp(false);
    setEmail('alex.rivera@innovationhacks.dev');
    setPassword('Password123!');
    setError('');
  };

  return (
    <div className="min-h-screen bg-gradient-to-tr from-[#E6F4F1] via-[#F3F5F2] to-[#EEF7D9] text-[#171E2D] flex flex-col justify-between p-6 sm:p-10 font-sans">
      
      {/* Top Bar Header */}
      <div className="flex items-center justify-between max-w-6xl w-full mx-auto">
        <div className="flex items-center space-x-2.5">
          <div className="w-8 h-8 rounded-xl bg-[#9BE838] text-[#171E2D] flex items-center justify-center font-bold shadow-xs">
            <LayoutGrid className="w-4 h-4" />
          </div>
          <span className="font-extrabold text-xl text-[#171E2D] tracking-tight flex items-center">
            fieldnote<span className="text-[#9BE838] font-black text-2xl leading-none">.</span>
          </span>
        </div>

        <div className="hidden sm:block text-xs font-semibold text-slate-500">
          A clearer way to move work forward
        </div>
      </div>

      {/* Main Centered Fieldnote Login Card */}
      <div className="w-full max-w-md mx-auto my-8 animate-in fade-in duration-300">
        <div className="fieldnote-card p-8 rounded-3xl bg-white/95 backdrop-blur-xl shadow-2xl space-y-6 border border-[#E4E8DF]">
          
          {/* Lock Icon Badge */}
          <div className="w-10 h-10 rounded-2xl bg-[#E5F4C7] text-emerald-800 flex items-center justify-center border border-[#D5E8B1]">
            <Lock className="w-5 h-5" />
          </div>

          {/* Headline & Subtitle */}
          <div className="space-y-1">
            <div className="text-[10px] font-extrabold text-slate-400 uppercase tracking-widest">
              {isSignUp ? 'NEW MEMBER REGISTRATION' : 'MEMBER SIGN IN'}
            </div>
            <h2 className="text-2xl font-extrabold text-[#171E2D] tracking-tight">
              {isSignUp ? 'Create your account.' : 'Good to see you.'}
            </h2>
            <p className="text-xs text-slate-500 font-medium leading-relaxed">
              {isSignUp ? 'Join your team workspace and start collaborating.' : 'Sign in to return to your workspace and keep the momentum going.'}
            </p>
          </div>

          {/* Quick Fill Demo Credentials Banner */}
          {!isSignUp && (
            <button
              type="button"
              onClick={fillDemoAccount}
              className="w-full p-2.5 rounded-xl bg-[#E5F4C7] hover:bg-[#D5E8B1] text-[#171E2D] text-xs font-bold border border-[#D5E8B1] flex items-center justify-center gap-2 transition-all shadow-2xs"
            >
              <Sparkles className="w-4 h-4 text-[#171E2D]" />
              <span>Click to Fill Demo Account (Alex Rivera)</span>
            </button>
          )}

          {/* Error Notification */}
          {error && (
            <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs font-semibold flex items-center gap-2 animate-in fade-in">
              <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            
            {isSignUp && (
              <div>
                <label className="block text-xs font-bold text-slate-600 mb-1">
                  Full name *
                </label>
                <div className="relative">
                  <User className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <input
                    type="text"
                    required
                    placeholder="e.g. Maya Chen"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs text-[#171E2D] focus:outline-none focus:bg-white focus:border-[#171E2D] font-medium"
                  />
                </div>
              </div>
            )}

            <div>
              <label className="block text-xs font-bold text-slate-600 mb-1">
                Email address *
              </label>
              <div className="relative">
                <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                <input
                  type="email"
                  required
                  placeholder="maya@northstar.dev"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs text-[#171E2D] focus:outline-none focus:bg-white focus:border-[#171E2D] font-medium"
                />
              </div>
            </div>

            <div>
              <div className="flex items-center justify-between mb-1">
                <label className="block text-xs font-bold text-slate-600">
                  Password *
                </label>
                {!isSignUp && (
                  <button type="button" className="text-xs font-bold text-[#171E2D] hover:underline">
                    Forgot password?
                  </button>
                )}
              </div>
              <div className="relative">
                <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-10 pr-10 py-2.5 bg-[#F3F5F2]/80 border border-[#E4E8DF] rounded-xl text-xs text-[#171E2D] focus:outline-none focus:bg-white focus:border-[#171E2D] font-medium"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-700"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {!isSignUp && (
              <div className="flex items-center space-x-2 pt-1">
                <input
                  type="checkbox"
                  id="keepSignedIn"
                  checked={keepSignedIn}
                  onChange={(e) => setKeepSignedIn(e.target.checked)}
                  className="w-4 h-4 accent-[#171E2D] rounded border-slate-300 cursor-pointer"
                />
                <label htmlFor="keepSignedIn" className="text-xs font-semibold text-slate-600 cursor-pointer select-none">
                  Keep me signed in on this device
                </label>
              </div>
            )}

            {/* Primary Sign in → Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 rounded-xl fieldnote-btn-primary font-bold text-xs shadow-md flex items-center justify-center space-x-2 transition-all hover:scale-[1.005] active:scale-[0.995] disabled:opacity-50 mt-2"
            >
              {loading ? (
                <span className="animate-spin text-white">Authenticating...</span>
              ) : isSignUp ? (
                <>
                  <span>Create Account</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              ) : (
                <>
                  <span>Sign in</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          {/* Legal Disclaimer */}
          <div className="text-[11px] text-slate-400 text-center font-medium pt-2">
            By continuing, you agree to fieldnote's <span className="font-bold text-[#171E2D]">Terms</span> and <span className="font-bold text-[#171E2D]">Privacy Policy</span>.
          </div>

        </div>

        {/* Bottom Switch Mode Link */}
        <div className="mt-4 text-center">
          <button
            type="button"
            onClick={() => { setIsSignUp(!isSignUp); setError(''); }}
            className="text-xs font-semibold text-slate-600"
          >
            {isSignUp ? (
              <>Already have an account? <span className="font-bold text-[#171E2D] hover:underline">Sign in</span></>
            ) : (
              <>New to fieldnote? <span className="font-bold text-[#171E2D] hover:underline">Request an invite</span></>
            )}
          </button>
        </div>

      </div>

      {/* Bottom Footer */}
      <div className="text-[11px] text-slate-400 text-center font-medium">
        © 2026 Fieldnote Productivity Platform. Built for Innovation Hacks.
      </div>

    </div>
  );
}
