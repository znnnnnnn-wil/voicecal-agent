import { useCallback, useEffect, useState } from 'react'
import type { ReplyState } from './components/AiReplyPanel'
import AppShell from './components/AppShell'
import CalendarView from './components/CalendarView'
import DailySummaryCard from './components/DailySummaryCard'
import EventDetailPanel from './components/EventDetailPanel'
import OperationLog from './components/OperationLog'
import StatusPreview from './components/StatusPreview'
import TodaySchedule from './components/TodaySchedule'
import TopNav from './components/TopNav'
import VoiceAssistantCard from './components/VoiceAssistantCard'
import WeekSummary from './components/WeekSummary'
import {
  demoCalendarEvents,
  demoDailySummary,
  demoTodayEvents,
  operationLogs,
  type OperationLogItem,
} from './data/demoData'
import { chatWithAi, getDailySummary as fetchDailySummary } from './services/aiService'
import {
  getTodayEvents as fetchTodayEvents,
  getWeekEvents as fetchWeekEvents,
  listCalendarEvents as fetchCalendarEvents,
} from './services/calendarService'
import type { DailySummary } from './types/ai'
import type { CalendarEvent } from './types/calendar'

type FeedbackState = 'idle' | 'loading' | 'success' | 'error'

const initialReply =
  '我可以帮助你创建、检查和整理日历事件。试着让我安排一次会议，或者帮你梳理今天的日程。'

function App() {
  const [command, setCommand] = useState('帮我安排明天上午 10 点的设计评审会')
  const [calendarEvents, setCalendarEvents] = useState<CalendarEvent[]>([])
  const [todayEvents, setTodayEvents] = useState<CalendarEvent[]>([])
  const [weekEvents, setWeekEvents] = useState<CalendarEvent[]>([])
  const [selectedEvent, setSelectedEvent] = useState<CalendarEvent | null>(null)
  const [dailySummary, setDailySummary] = useState<DailySummary | null>(null)
  const [chatError, setChatError] = useState<string | null>(null)
  const [chatSuccess, setChatSuccess] = useState(false)
  const [calendarError, setCalendarError] = useState<string | null>(null)
  const [todayError, setTodayError] = useState<string | null>(null)
  const [weekError, setWeekError] = useState<string | null>(null)
  const [summaryError, setSummaryError] = useState<string | null>(null)
  const [reply, setReply] = useState(initialReply)
  const [replyState, setReplyState] = useState<ReplyState>('idle')
  const [feedback, setFeedback] = useState<FeedbackState>('idle')
  const [isCalendarLoading, setIsCalendarLoading] = useState(false)
  const [isTodayLoading, setIsTodayLoading] = useState(false)
  const [isWeekLoading, setIsWeekLoading] = useState(false)
  const [isSummaryLoading, setIsSummaryLoading] = useState(false)
  const [isUsingDemoCalendar, setIsUsingDemoCalendar] = useState(false)
  const [isUsingDemoToday, setIsUsingDemoToday] = useState(false)
  const [isUsingDemoWeek, setIsUsingDemoWeek] = useState(false)
  const [isUsingDemoSummary, setIsUsingDemoSummary] = useState(false)
  const [logs, setLogs] = useState<OperationLogItem[]>(operationLogs)

  const appendLog = useCallback((item: Omit<OperationLogItem, 'time'>) => {
    setLogs((currentLogs) => [{ time: '刚刚', ...item }, ...currentLogs].slice(0, 6))
  }, [])

  const appendAssistantLog = useCallback(
    (label: string, detail: string, status: OperationLogItem['status']) => {
      appendLog({ label, detail, status })
    },
    [appendLog],
  )

  const loadCalendarEvents = useCallback(async () => {
    setIsCalendarLoading(true)
    setCalendarError(null)
    setIsUsingDemoCalendar(false)
    appendLog({
      label: '加载日历视图',
      detail: '正在请求 GET /api/calendar/events',
      status: 'info',
    })

    try {
      const events = await fetchCalendarEvents()
      setCalendarEvents(events)
      appendLog({
        label: '日历视图加载成功',
        detail: `后端返回 ${events.length} 个日程事件`,
        status: 'success',
      })
    } catch (error) {
      const message = getErrorMessage(error)
      setCalendarEvents(demoCalendarEvents)
      setCalendarError(message)
      setIsUsingDemoCalendar(true)
      appendLog({
        label: '日历视图加载失败',
        detail: '后端不可用，已切换到 demo fallback 日历数据',
        status: 'pending',
      })
    } finally {
      setIsCalendarLoading(false)
    }
  }, [appendLog])

  const loadTodayEvents = useCallback(async () => {
    setIsTodayLoading(true)
    setTodayError(null)
    setIsUsingDemoToday(false)
    appendLog({
      label: '加载今日日程',
      detail: '正在请求 GET /api/calendar/events/today',
      status: 'info',
    })

    try {
      const events = await fetchTodayEvents()
      setTodayEvents(events)
      appendLog({
        label: '今日日程加载成功',
        detail: `后端返回 ${events.length} 个今日事件`,
        status: 'success',
      })
    } catch (error) {
      const message = getErrorMessage(error)
      setTodayEvents(demoTodayEvents)
      setTodayError(message)
      setIsUsingDemoToday(true)
      appendLog({
        label: '今日日程加载失败',
        detail: '后端不可用，已切换到 demo fallback 数据',
        status: 'pending',
      })
    } finally {
      setIsTodayLoading(false)
    }
  }, [appendLog])

  const loadWeekEvents = useCallback(async () => {
    setIsWeekLoading(true)
    setWeekError(null)
    setIsUsingDemoWeek(false)
    appendLog({
      label: '加载本周日程',
      detail: '正在请求 GET /api/calendar/events/week',
      status: 'info',
    })

    try {
      const events = await fetchWeekEvents()
      setWeekEvents(events)
      appendLog({
        label: '本周日程加载成功',
        detail: `后端返回 ${events.length} 个本周事件`,
        status: 'success',
      })
    } catch (error) {
      const message = getErrorMessage(error)
      setWeekEvents(demoCalendarEvents)
      setWeekError(message)
      setIsUsingDemoWeek(true)
      appendLog({
        label: '本周日程加载失败',
        detail: '后端不可用，已切换到 demo fallback 数据',
        status: 'pending',
      })
    } finally {
      setIsWeekLoading(false)
    }
  }, [appendLog])

  const loadDailySummary = useCallback(async () => {
    setIsSummaryLoading(true)
    setSummaryError(null)
    setIsUsingDemoSummary(false)
    appendLog({
      label: '加载每日摘要',
      detail: '正在请求 GET /api/ai/daily-summary',
      status: 'info',
    })

    try {
      const summary = await fetchDailySummary()
      setDailySummary(summary)
      appendLog({
        label: '每日摘要加载成功',
        detail: `后端返回 ${summary.eventCount} 个摘要事件`,
        status: 'success',
      })
    } catch (error) {
      const message = getErrorMessage(error)
      setDailySummary(demoDailySummary)
      setSummaryError(message)
      setIsUsingDemoSummary(true)
      appendLog({
        label: '每日摘要加载失败',
        detail: '后端不可用，已切换到 demo fallback 摘要',
        status: 'pending',
      })
    } finally {
      setIsSummaryLoading(false)
    }
  }, [appendLog])

  useEffect(() => {
    void loadCalendarEvents()
    void loadTodayEvents()
    void loadWeekEvents()
    void loadDailySummary()
  }, [loadCalendarEvents, loadDailySummary, loadTodayEvents, loadWeekEvents])

  useEffect(() => {
    if (!selectedEvent) {
      return
    }
    const updatedEvent = calendarEvents.find((event) => event.id === selectedEvent.id)
    setSelectedEvent(updatedEvent ?? null)
  }, [calendarEvents, selectedEvent])

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
      label: 'AI command sent',
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
        label: 'AI reply received',
        detail: '后端 AI 对话接口已返回 reply',
        status: 'success',
      })
      await Promise.all([loadCalendarEvents(), loadTodayEvents(), loadWeekEvents(), loadDailySummary()])
      appendLog({
        label: 'Calendar data refreshed',
        detail: 'AI 对话完成后已刷新日历、今日、本周和每日摘要',
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

  const dashboardError = calendarError || todayError || weekError || summaryError
  const isUsingAnyDemoData =
    isUsingDemoCalendar || isUsingDemoToday || isUsingDemoWeek || isUsingDemoSummary

  return (
    <AppShell>
      <TopNav />
      <main className="mx-auto grid w-full max-w-7xl items-start gap-5 px-5 py-6 sm:px-8 lg:grid-cols-12 lg:px-10">
        <section className="grid self-start gap-5 lg:col-span-8">
          <VoiceAssistantCard
            command={command}
            error={chatError}
            isLoading={replyState === 'loading'}
            isSuccess={chatSuccess}
            onLog={appendAssistantLog}
            onCommandChange={setCommand}
            onRunCommand={handleRunCommand}
            reply={reply}
          />
          <CalendarView
            error={calendarError}
            events={calendarEvents}
            isLoading={isCalendarLoading}
            isUsingDemoEvents={isUsingDemoCalendar}
            onEventSelect={setSelectedEvent}
            onRetry={loadCalendarEvents}
          />
        </section>

        <aside className="grid self-start gap-5 lg:col-span-4">
          <EventDetailPanel event={selectedEvent} onClose={() => setSelectedEvent(null)} />
          <TodaySchedule
            error={todayError}
            events={todayEvents}
            isLoading={isTodayLoading}
            isUsingDemoEvents={isUsingDemoToday}
            onRetry={loadTodayEvents}
          />
          <WeekSummary
            events={weekEvents}
            isLoading={isWeekLoading}
            isUsingDemoEvents={isUsingDemoWeek}
          />
          <DailySummaryCard
            error={summaryError}
            isLoading={isSummaryLoading}
            isUsingDemoSummary={isUsingDemoSummary}
            onRetry={loadDailySummary}
            summary={dailySummary}
          />
          <OperationLog logs={logs} />
          <StatusPreview
            chatError={chatError}
            eventsError={dashboardError}
            feedback={feedback}
            isUsingDemoEvents={isUsingAnyDemoData}
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
