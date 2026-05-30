import type { PropsWithChildren } from 'react'

function AppShell({ children }: PropsWithChildren) {
  return (
    <div className="min-h-screen bg-[#f6f8fb] text-[#202124]">
      {children}
    </div>
  )
}

export default AppShell
