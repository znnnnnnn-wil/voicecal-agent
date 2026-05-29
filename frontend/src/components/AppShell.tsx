import type { PropsWithChildren } from 'react'

function AppShell({ children }: PropsWithChildren) {
  return (
    <div className="relative min-h-screen overflow-x-hidden bg-[#070b10] text-slate-100">
      <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(135deg,rgba(14,165,233,0.18),transparent_32%),linear-gradient(225deg,rgba(16,185,129,0.14),transparent_30%),linear-gradient(to_bottom,#0b1018_0%,#070b10_45%,#090d12_100%)]" />
      <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.04)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.04)_1px,transparent_1px)] bg-[size:72px_72px] opacity-35 [mask-image:linear-gradient(to_bottom,black,transparent_86%)]" />
      <div className="pointer-events-none absolute inset-x-0 top-0 h-40 bg-gradient-to-b from-cyan-300/10 to-transparent" />
      <div className="relative z-10">{children}</div>
    </div>
  )
}

export default AppShell
