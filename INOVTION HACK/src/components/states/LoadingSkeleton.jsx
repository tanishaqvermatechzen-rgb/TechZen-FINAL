import React from 'react';

export default function LoadingSkeleton() {
  return (
    <div className="space-y-6 animate-pulse">
      {/* Stat Cards Skeleton */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {[1, 2, 3].map((i) => (
          <div key={i} className="fieldnote-card h-32 rounded-2xl p-5 space-y-3 relative overflow-hidden">
            <div className="flex justify-between">
              <div className="w-24 h-4 bg-slate-200 rounded" />
              <div className="w-8 h-8 bg-slate-200 rounded-full" />
            </div>
            <div className="w-32 h-6 bg-slate-200 rounded" />
            <div className="w-full h-2 bg-slate-200 rounded-full" />
          </div>
        ))}
      </div>

      {/* Main Grid Skeleton */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <div className="fieldnote-card h-64 rounded-2xl p-5 space-y-4">
            <div className="w-48 h-6 bg-slate-200 rounded" />
            <div className="grid grid-cols-2 gap-4">
              <div className="h-36 bg-slate-100 rounded-xl" />
              <div className="h-36 bg-slate-100 rounded-xl" />
            </div>
          </div>
        </div>

        <div className="space-y-6">
          <div className="fieldnote-card h-80 rounded-2xl p-5 space-y-4">
            <div className="w-36 h-6 bg-slate-200 rounded mb-4" />
            <div className="space-y-3">
              {[1, 2, 3].map(k => (
                <div key={k} className="h-16 bg-slate-100 rounded-xl" />
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
