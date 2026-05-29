import type { PropsWithChildren } from 'react'

function AppShell({ children }: PropsWithChildren) {
  return (
    <div className="relative min-h-screen overflow-hidden bg-[#090d12] text-slate-100">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_18%_12%,rgba(45,212,191,0.28),transparent_28%),radial-gradient(circle_at_84%_18%,rgba(168,85,247,0.24),transparent_30%),radial-gradient(circle_at_50%_92%,rgba(251,191,36,0.12),transparent_28%)]" />
      <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.045)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.045)_1px,transparent_1px)] bg-[size:72px_72px] opacity-30 [mask-image:linear-gradient(to_bottom,black,transparent_82%)]" />
      <div className="relative z-10">{children}</div>
    </div>
  )
}

export default AppShell
