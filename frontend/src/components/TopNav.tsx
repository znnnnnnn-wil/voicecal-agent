import { useEffect, useState } from 'react'

const dateTimeFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'short',
  hour: '2-digit',
  minute: '2-digit',
})

function TopNav() {
  const [now, setNow] = useState(() => new Date())

  useEffect(() => {
    const timer = window.setInterval(() => setNow(new Date()), 1_000)
    return () => window.clearInterval(timer)
  }, [])

  return (
    <header className="sticky top-0 z-30 border-b border-[#dadce0] bg-white/95 backdrop-blur">
      <div className="grid h-16 w-full grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-4 px-5 lg:px-7">
        <div className="flex min-w-0 items-center gap-4">
          <div className="grid size-10 shrink-0 place-items-center rounded-xl bg-[#1a73e8] text-sm font-semibold text-white shadow-sm">
            VC
          </div>
          <div className="min-w-0">
            <p className="truncate text-base font-semibold text-[#202124]">VoiceCal Agent</p>
            <p className="text-xs text-[#5f6368]">AI Calendar Assistant</p>
          </div>
        </div>

        <time className="truncate text-xl font-medium text-[#202124]" dateTime={now.toISOString()}>
          {dateTimeFormatter.format(now)}
        </time>

        <div aria-hidden="true" />
      </div>
    </header>
  )
}

export default TopNav
