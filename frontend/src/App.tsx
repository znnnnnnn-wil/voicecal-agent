import { useCallback, useEffect, useState } from 'react'
import AiReplyPanel, { type ReplyState } from './components/AiReplyPanel'
import AppShell from './components/AppShell'
import CalendarView from './components/CalendarView'
import DailySummaryCard from './components/DailySummaryCard'
import EventDetailPanel from './components/EventDetailPanel'
import OperationLog from './components/OperationLog'
import RecentReminders from './components/RecentReminders'
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
import { getRecentLogs as fetchRecentLogs } from './services/logService'
import { getRecentReminders as fetchRecentReminders } from './services/reminderService'
import type { DailySummary } from './types/ai'
import type { CalendarEvent } from './types/calendar'
import type { VoiceCommandLog } from './types/log'
import type { Reminder } from './types/reminder'

type FeedbackState = 'idle' | 'loading' | 'success' | 'error'

const initialReply =
  '我可以帮你创建、查询、整理日程，也可以处理提醒、冲突检测、空闲时间查询和 ICS 导出。'

function App() {
  const [command, setCommand] = useState('明天下午三点提醒我提交项目代码')
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
  const [logsError, setLogsError] = useState<string | null>(null)
  const [remindersError, setRemindersError] = useState<string | null>(null)
  const [reply, setReply] = useState(initialReply)
  const [replyState, setReplyState] = useState<ReplyState>('idle')
  const [feedback, setFeedback] = useState<FeedbackState>('idle')
  const [isCalendarLoading, setIsCalendarLoading] = useState(false)
  const [isTodayLoading, setIsTodayLoading] = useState(false)
  const [isWeekLoading, setIsWeekLoading] = useState(false)
  const [isSummaryLoading, setIsSummaryLoading] = useState(false)
  const [isLogsLoading, setIsLogsLoading] = useState(false)
  const [isRemindersLoading, setIsRemindersLoading] = useState(false)
  const [isUsingDemoCalendar, setIsUsingDemoCalendar] = useState(false)
  const [isUsingDemoToday, setIsUsingDemoToday] = useState(false)
  const [isUsingDemoWeek, setIsUsingDemoWeek] = useState(false)
  const [isUsingDemoSummary, setIsUsingDemoSummary] = useState(false)
  const [isUsingDemoLogs, setIsUsingDemoLogs] = useState(false)
  const [isUsingDemoReminders, setIsUsingDemoReminders] = useState(false)
  const [logs, setLogs] = useState<VoiceCommandLog[]>([])
  const [recentReminders, setRecentReminders] = useState<Reminder[]>([])

  const appendLog = useCallback((item: Omit<OperationLogItem, 'time'>) => {
    void item
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
    try {
      const events = await fetchCalendarEvents()
      setCalendarEvents(events)
    } catch (error) {
      setCalendarEvents(demoCalendarEvents)
      setCalendarError(getErrorMessage(error))
      setIsUsingDemoCalendar(true)
    } finally {
      setIsCalendarLoading(false)
    }
  }, [])

  const loadVoiceCommandLogs = useCallback(async () => {
    setIsLogsLoading(true)
    setLogsError(null)
    setIsUsingDemoLogs(false)
    try {
      setLogs(await fetchRecentLogs(20))
    } catch (error) {
      setLogs(buildDemoLogs())
      setLogsError(getErrorMessage(error))
      setIsUsingDemoLogs(true)
    } finally {
      setIsLogsLoading(false)
    }
  }, [])

  const loadRecentReminders = useCallback(async () => {
    setIsRemindersLoading(true)
    setRemindersError(null)
    setIsUsingDemoReminders(false)
    try {
      setRecentReminders(await fetchRecentReminders(20))
    } catch (error) {
      setRecentReminders(buildDemoReminders())
      setRemindersError(getErrorMessage(error))
      setIsUsingDemoReminders(true)
    } finally {
      setIsRemindersLoading(false)
    }
  }, [])

  const loadTodayEvents = useCallback(async () => {
    setIsTodayLoading(true)
    setTodayError(null)
    setIsUsingDemoToday(false)
    try {
      setTodayEvents(await fetchTodayEvents())
    } catch (error) {
      setTodayEvents(demoTodayEvents)
      setTodayError(getErrorMessage(error))
      setIsUsingDemoToday(true)
    } finally {
      setIsTodayLoading(false)
    }
  }, [])

  const loadWeekEvents = useCallback(async () => {
    setIsWeekLoading(true)
    setWeekError(null)
    setIsUsingDemoWeek(false)
    try {
      setWeekEvents(await fetchWeekEvents())
    } catch (error) {
      setWeekEvents(demoCalendarEvents)
      setWeekError(getErrorMessage(error))
      setIsUsingDemoWeek(true)
    } finally {
      setIsWeekLoading(false)
    }
  }, [])

  const loadDailySummary = useCallback(async () => {
    setIsSummaryLoading(true)
    setSummaryError(null)
    setIsUsingDemoSummary(false)
    try {
      setDailySummary(await fetchDailySummary())
    } catch (error) {
      setDailySummary(demoDailySummary)
      setSummaryError(getErrorMessage(error))
      setIsUsingDemoSummary(true)
    } finally {
      setIsSummaryLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadCalendarEvents()
    void loadTodayEvents()
    void loadWeekEvents()
    void loadDailySummary()
    void loadVoiceCommandLogs()
    void loadRecentReminders()
  }, [
    loadCalendarEvents,
    loadDailySummary,
    loadRecentReminders,
    loadTodayEvents,
    loadVoiceCommandLogs,
    loadWeekEvents,
  ])

  useEffect(() => {
    if (!selectedEvent) return
    const updatedEvent = calendarEvents.find((event) => event.id === selectedEvent.id)
    setSelectedEvent(updatedEvent ?? null)
  }, [calendarEvents, selectedEvent])

  const handleRunCommand = async (nextCommand?: string) => {
    const commandText = (nextCommand ?? command).trim()
    if (!commandText) {
      setReplyState('error')
      setFeedback('error')
      setChatSuccess(false)
      setChatError('请输入日程指令后再发送。')
      setReply('我还没有收到有效指令。请先输入会议主题、时间或想整理的日程内容。')
      return
    }

    setReplyState('loading')
    setFeedback('loading')
    setChatError(null)
    setChatSuccess(false)

    try {
      const response = await chatWithAi(commandText)
      setReplyState('success')
      setFeedback('success')
      setChatSuccess(true)
      setReply(response.reply)
      await Promise.all([
        loadCalendarEvents(),
        loadTodayEvents(),
        loadWeekEvents(),
        loadDailySummary(),
        loadVoiceCommandLogs(),
        loadRecentReminders(),
      ])
    } catch (error) {
      const message = getErrorMessage(error)
      setReplyState('error')
      setFeedback('error')
      setChatError(message)
      setReply(`AI 请求失败：${message}`)
    }
  }

  const dashboardError = calendarError || todayError || weekError || summaryError || logsError || remindersError
  const isUsingAnyDemoData =
    isUsingDemoCalendar ||
    isUsingDemoToday ||
    isUsingDemoWeek ||
    isUsingDemoSummary ||
    isUsingDemoLogs ||
    isUsingDemoReminders

  return (
    <AppShell>
      <TopNav />
      <main className="grid min-h-[calc(100vh-4rem)] grid-cols-1 gap-4 p-4 lg:grid-cols-[300px_minmax(0,1fr)] xl:grid-cols-[300px_minmax(0,1fr)_330px]">
        <aside className="space-y-4 lg:sticky lg:top-20 lg:max-h-[calc(100vh-6rem)] lg:overflow-y-auto">
          <button
            className="flex w-full items-center justify-center gap-2 rounded-2xl bg-[#1a73e8] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#1765cc]"
            type="button"
          >
            <span className="text-lg leading-none">＋</span>
            创建日程
          </button>
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
          <TodaySchedule
            error={todayError}
            events={todayEvents}
            isLoading={isTodayLoading}
            isUsingDemoEvents={isUsingDemoToday}
            onRetry={loadTodayEvents}
          />
        </aside>

        <section className="min-w-0">
          <CalendarView
            error={calendarError}
            events={calendarEvents}
            isLoading={isCalendarLoading}
            isUsingDemoEvents={isUsingDemoCalendar}
            onEventSelect={setSelectedEvent}
            onRetry={loadCalendarEvents}
          />
        </section>

        <aside className="space-y-4 lg:col-span-2 xl:sticky xl:top-20 xl:col-span-1 xl:max-h-[calc(100vh-6rem)] xl:overflow-y-auto">
          <EventDetailPanel event={selectedEvent} onClose={() => setSelectedEvent(null)} />
          <AiReplyPanel reply={reply} state={replyState} />
          <OperationLog
            error={logsError}
            isLoading={isLogsLoading}
            isUsingDemoLogs={isUsingDemoLogs}
            logs={logs}
            onRetry={loadVoiceCommandLogs}
          />
          <RecentReminders
            error={remindersError}
            isLoading={isRemindersLoading}
            isUsingDemoReminders={isUsingDemoReminders}
            onRetry={loadRecentReminders}
            reminders={recentReminders}
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

function buildDemoLogs(): VoiceCommandLog[] {
  return operationLogs.map((log, index) => ({
    id: -(index + 1),
    conversationId: 'demo',
    rawText: log.label,
    assistantReply: log.detail,
    intent: 'DEMO',
    toolName: null,
    toolArgsJson: null,
    toolResultJson: null,
    success: log.status === 'success' || log.status === 'info',
    createdAt: new Date(Date.now() - index * 60_000).toISOString(),
  }))
}

function buildDemoReminders(): Reminder[] {
  return demoTodayEvents
    .filter((event) => event.reminderMinutes !== null && event.reminderMinutes !== undefined && event.reminderTriggered)
    .slice(0, 3)
    .map((event, index) => ({
      eventId: event.id,
      title: event.title,
      startTime: event.startTime,
      reminderMinutes: event.reminderMinutes ?? 15,
      reminderTriggered: true,
      remindedAt: new Date(Date.now() - index * 120_000).toISOString(),
    }))
}

export default App
