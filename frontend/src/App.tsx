import { useCallback, useEffect, useState } from 'react'
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
import { demoCalendarEvents, operationLogs, type OperationLogItem } from './data/demoData'
import { chatWithAi } from './services/aiService'
import { listCalendarEvents } from './services/calendarService'
import type { CalendarEvent } from './types/calendar'

type FeedbackState = 'idle' | 'loading' | 'success' | 'error'

const initialReply =
  '我可以帮助你创建、检查和整理日历事件。试着让我安排一次会议，或者帮你梳理今天的日程。'

function App() {
  const [command, setCommand] = useState('帮我安排明天上午 10 点的设计评审会')
  const [calendarEvents, setCalendarEvents] = useState<CalendarEvent[]>([])
  const [chatError, setChatError] = useState<string | null>(null)
  const [chatSuccess, setChatSuccess] = useState(false)
  const [eventsError, setEventsError] = useState<string | null>(null)
  const [reply, setReply] = useState(initialReply)
  const [replyState, setReplyState] = useState<ReplyState>('idle')
  const [feedback, setFeedback] = useState<FeedbackState>('idle')
  const [isEventsLoading, setIsEventsLoading] = useState(false)
  const [isUsingDemoEvents, setIsUsingDemoEvents] = useState(false)
  const [logs, setLogs] = useState<OperationLogItem[]>(operationLogs)

  const appendLog = useCallback((item: Omit<OperationLogItem, 'time'>) => {
    setLogs((currentLogs) => [{ time: '刚刚', ...item }, ...currentLogs].slice(0, 5))
  }, [])

  const loadCalendarEvents = useCallback(async () => {
    setIsEventsLoading(true)
    setEventsError(null)
    setIsUsingDemoEvents(false)
    appendLog({
      label: '加载日历事件',
      detail: '正在请求 GET /api/calendar/events',
      status: 'info',
    })

    try {
      const events = await listCalendarEvents()
      setCalendarEvents(events)
      appendLog({
        label: '日历事件加载成功',
        detail: `后端返回 ${events.length} 个事件`,
        status: 'success',
      })
    } catch (error) {
      const message = getErrorMessage(error)
      setCalendarEvents(demoCalendarEvents)
      setEventsError(message)
      setIsUsingDemoEvents(true)
      appendLog({
        label: '日历事件加载失败',
        detail: '后端不可用，已切换到 demo fallback 数据',
        status: 'pending',
      })
    } finally {
      setIsEventsLoading(false)
    }
  }, [appendLog])

  useEffect(() => {
    void loadCalendarEvents()
  }, [loadCalendarEvents])

  const handleRunCommand = async () => {
    if (!command.trim()) {
      setReplyState('error')
      setFeedback('error')
      setChatSuccess(false)
      setChatError('请输入日程指令后再运行。')
      setReply('我还没有收到有效指令。请先输入会议主题、时间或想要整理的日程内容。')
      return
    }

    setReplyState('loading')
    setFeedback('loading')
    setChatError(null)
    setChatSuccess(false)
    appendLog({
      label: '提交 AI 指令',
      detail: '正在请求 POST /api/ai/chat',
      status: 'info',
    })

    try {
      const response = await chatWithAi(command.trim())
      setReplyState('success')
      setFeedback('success')
      setChatSuccess(true)
      setReply(response.reply)
      appendLog({
        label: '收到 AI 回复',
        detail: '后端 AI 对话接口已返回 reply',
        status: 'success',
      })
    } catch (error) {
      const message = getErrorMessage(error)
      setReplyState('error')
      setFeedback('error')
      setChatError(message)
      setReply(`AI 请求失败：${message}`)
      appendLog({
        label: 'AI 请求失败',
        detail: message,
        status: 'pending',
      })
    }
  }

  return (
    <AppShell>
      <TopNav />
      <main className="mx-auto grid w-full max-w-7xl items-start gap-5 px-5 py-6 sm:px-8 lg:grid-cols-12 lg:px-10">
        <section className="grid self-start gap-5 lg:col-span-8">
          <AiCommandPanel
            command={command}
            error={chatError}
            isLoading={replyState === 'loading'}
            isSuccess={chatSuccess}
            onCommandChange={setCommand}
            onRunCommand={handleRunCommand}
          />
          <div className="grid items-start gap-5 xl:grid-cols-[0.95fr_1.05fr]">
            <VoiceInputCard />
            <AiReplyPanel reply={reply} state={replyState} />
          </div>
          <CalendarBoard
            error={eventsError}
            events={calendarEvents}
            isLoading={isEventsLoading}
            isUsingDemoEvents={isUsingDemoEvents}
            onRetry={loadCalendarEvents}
          />
        </section>

        <aside className="grid self-start gap-5 lg:col-span-4">
          <TodaySchedule
            error={eventsError}
            events={calendarEvents}
            isLoading={isEventsLoading}
            isUsingDemoEvents={isUsingDemoEvents}
            onRetry={loadCalendarEvents}
          />
          <WeekSummary
            events={calendarEvents}
            isLoading={isEventsLoading}
            isUsingDemoEvents={isUsingDemoEvents}
          />
          <OperationLog logs={logs} />
          <StatusPreview
            chatError={chatError}
            eventsError={eventsError}
            feedback={feedback}
            isUsingDemoEvents={isUsingDemoEvents}
          />
        </aside>
      </main>
    </AppShell>
  )
}

function getErrorMessage(error: unknown) {
  if (error instanceof Error) {
    return error.message
  }
  return '请求失败，请稍后重试。'
}

export default App
