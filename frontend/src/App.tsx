import { useEffect, useRef, useState } from 'react'
import AiCommandPanel from './components/AiCommandPanel'
import AiReplyPanel, { type ReplyState } from './components/AiReplyPanel'
import AppShell from './components/AppShell'
import CalendarBoard from './components/CalendarBoard'
import OperationLog from './components/OperationLog'
import StatusPreview from './components/StatusPreview'
import TodaySchedule from './components/TodaySchedule'
import TopNav from './components/TopNav'
import VoiceInputCard from './components/VoiceInputCard'
import WeekSummary from './components/WeekSummary'

type FeedbackState = 'idle' | 'loading' | 'success' | 'error'

const initialReply =
  '我可以帮助你创建、检查和整理日历事件。试着让我安排一次会议，或者帮你梳理今天的日程。'

function App() {
  const [command, setCommand] = useState('帮我安排明天上午 10 点的设计评审会')
  const [reply, setReply] = useState(initialReply)
  const [replyState, setReplyState] = useState<ReplyState>('idle')
  const [feedback, setFeedback] = useState<FeedbackState>('idle')
  const timeoutRef = useRef<number | null>(null)

  useEffect(() => {
    return () => {
      if (timeoutRef.current !== null) {
        window.clearTimeout(timeoutRef.current)
      }
    }
  }, [])

  const handleRunCommand = () => {
    if (timeoutRef.current !== null) {
      window.clearTimeout(timeoutRef.current)
    }

    if (!command.trim()) {
      setReplyState('error')
      setFeedback('error')
      setReply('我还没有收到有效指令。请先输入会议主题、时间或想要整理的日程内容。')
      return
    }

    setReplyState('loading')
    setFeedback('loading')

    timeoutRef.current = window.setTimeout(() => {
      setReplyState('success')
      setFeedback('success')
      setReply('我找到了可用时间段，并准备了一份日程草稿。后续 PR 会把这里连接到真实后端执行。')
    }, 500)
  }

  return (
    <AppShell>
      <TopNav />
      <main className="mx-auto grid w-full max-w-7xl gap-5 px-5 py-6 sm:px-8 lg:grid-cols-12 lg:px-10">
        <section className="grid gap-5 lg:col-span-8">
          <AiCommandPanel
            command={command}
            isLoading={replyState === 'loading'}
            onCommandChange={setCommand}
            onRunCommand={handleRunCommand}
          />
          <div className="grid gap-5 xl:grid-cols-[0.95fr_1.05fr]">
            <VoiceInputCard />
            <AiReplyPanel reply={reply} state={replyState} />
          </div>
          <CalendarBoard />
        </section>

        <aside className="grid gap-5 lg:col-span-4">
          <TodaySchedule />
          <WeekSummary />
          <OperationLog />
          <StatusPreview feedback={feedback} />
        </aside>
      </main>
    </AppShell>
  )
}

export default App
