import { useCallback, useEffect, useRef, useState } from 'react'
import AiReplyPanel, { type ReplyState } from './components/AiReplyPanel'
import AppShell from './components/AppShell'
import CalendarView from './components/CalendarView'
import DailySummaryCard from './components/DailySummaryCard'
import EventDetailPanel from './components/EventDetailPanel'
import OperationLog from './components/OperationLog'
import RecentReminders from './components/RecentReminders'
import ReminderToastStack, { type ReminderToast } from './components/ReminderToastStack'
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

const initialReply =
  '我可以帮你创建、查询、整理日程，也可以处理提醒、冲突检测、空闲时间查询和 ICS 导出。'
const REMINDER_REFRESH_INTERVAL_MS = 15_000
const REMINDER_TOAST_VISIBLE_MS = 10_000

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
  const [reminderToasts, setReminderToasts] = useState<ReminderToast[]>([])
  const knownReminderKeysRef = useRef<Set<string>>(new Set())
  const hasInitializedReminderKeysRef = useRef(false)
  const knownStartedEventKeysRef = useRef<Set<string>>(new Set())
  const hasInitializedStartedEventKeysRef = useRef(false)
  const lastSubmitRef = useRef<{ text: string; time: number } | null>(null)

  const appendLog = useCallback((item: Omit<OperationLogItem, 'time'>) => {
    void item
  }, [])

  const appendAssistantLog = useCallback(
    (label: string, detail: string, status: OperationLogItem['status']) => {
      appendLog({ label, detail, status })
    },
    [appendLog],
  )

  const syncReminderToasts = useCallback((reminders: Reminder[], shouldNotifyNewReminders: boolean) => {
    const nextKeys = new Set(reminders.map(getReminderKey))
    if (!hasInitializedReminderKeysRef.current) {
      knownReminderKeysRef.current = nextKeys
      hasInitializedReminderKeysRef.current = true
      return
    }

    if (shouldNotifyNewReminders) {
      const newReminders = reminders.filter((reminder) => !knownReminderKeysRef.current.has(getReminderKey(reminder)))
      if (newReminders.length > 0) {
        setReminderToasts((current) => {
          const currentKeys = new Set(current.map((reminder) => reminder.toastId))
          const incoming = newReminders.map((reminder) => ({
            toastId: getReminderKey(reminder),
            title: reminder.title,
            startTime: reminder.startTime,
            label: '日程提醒',
          }))
          return [...incoming.filter((reminder) => !currentKeys.has(reminder.toastId)), ...current].slice(0, 4)
        })
      }
    }

    knownReminderKeysRef.current = nextKeys
  }, [])

  const syncStartedEventToasts = useCallback((events: CalendarEvent[], shouldNotifyNewEvents: boolean) => {
    const now = Date.now()
    const startedEvents = events.filter((event) => {
      const startTime = new Date(event.startTime).getTime()
      return Number.isFinite(startTime) && startTime <= now
    })
    const nextKeys = new Set(startedEvents.map(getStartedEventKey))

    if (!hasInitializedStartedEventKeysRef.current) {
      knownStartedEventKeysRef.current = nextKeys
      hasInitializedStartedEventKeysRef.current = true
      return
    }

    if (shouldNotifyNewEvents) {
      const newStartedEvents = startedEvents.filter(
        (event) => !knownStartedEventKeysRef.current.has(getStartedEventKey(event)),
      )
      if (newStartedEvents.length > 0) {
        setReminderToasts((current) => {
          const currentKeys = new Set(current.map((reminder) => reminder.toastId))
          const incoming = newStartedEvents.map((event) => ({
            toastId: getStartedEventKey(event),
            title: event.title,
            startTime: event.startTime,
            label: '日程开始',
          }))
          return [...incoming.filter((reminder) => !currentKeys.has(reminder.toastId)), ...current].slice(0, 4)
        })
      }
    }

    knownStartedEventKeysRef.current = nextKeys
  }, [])

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
      const reminders = await fetchRecentReminders(20)
      setRecentReminders(reminders)
      syncReminderToasts(reminders, false)
    } catch (error) {
      setRecentReminders(buildDemoReminders())
      setRemindersError(getErrorMessage(error))
      setIsUsingDemoReminders(true)
    } finally {
      setIsRemindersLoading(false)
    }
  }, [syncReminderToasts])

  const refreshReminderStateSilently = useCallback(async () => {
    try {
      const [reminders, events, today] = await Promise.all([
        fetchRecentReminders(20),
        fetchCalendarEvents(),
        fetchTodayEvents(),
      ])
      setRecentReminders(reminders)
      syncReminderToasts(reminders, true)
      setCalendarEvents(events)
      setTodayEvents(today)
      syncStartedEventToasts(today, true)
      setRemindersError(null)
      setCalendarError(null)
      setTodayError(null)
      setIsUsingDemoReminders(false)
      setIsUsingDemoCalendar(false)
      setIsUsingDemoToday(false)
    } catch {
      // 后台刷新失败时保留当前页面数据，避免轮询造成闪烁或 fallback 抖动。
    }
  }, [syncReminderToasts, syncStartedEventToasts])

  const loadTodayEvents = useCallback(async () => {
    setIsTodayLoading(true)
    setTodayError(null)
    setIsUsingDemoToday(false)
    try {
      const events = await fetchTodayEvents()
      setTodayEvents(events)
      syncStartedEventToasts(events, false)
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

  useEffect(() => {
    const timer = window.setInterval(() => {
      void refreshReminderStateSilently()
    }, REMINDER_REFRESH_INTERVAL_MS)

    return () => window.clearInterval(timer)
  }, [refreshReminderStateSilently])

  useEffect(() => {
    if (reminderToasts.length === 0) {
      return
    }

    const timer = window.setTimeout(() => {
      setReminderToasts((current) => current.slice(1))
    }, REMINDER_TOAST_VISIBLE_MS)

    return () => window.clearTimeout(timer)
  }, [reminderToasts])

  const dismissReminderToast = useCallback((toastId: string) => {
    setReminderToasts((current) => current.filter((reminder) => reminder.toastId !== toastId))
  }, [])

  const handleRunCommand = async (nextCommand?: string) => {
    const commandText = (nextCommand ?? command).trim()
    const now = Date.now()
    if (lastSubmitRef.current?.text === commandText && now - lastSubmitRef.current.time < 3_000) {
      return
    }
    if (!commandText) {
      setReplyState('error')
      setChatSuccess(false)
      setChatError('请输入日程指令后再发送。')
      setReply('我还没有收到有效指令。请先输入会议主题、时间或想整理的日程内容。')
      return
    }

    setReplyState('loading')
    setChatError(null)
    setChatSuccess(false)
    lastSubmitRef.current = { text: commandText, time: now }
    const chatStartedAt = Date.now()

    try {
      const response = await chatWithAi(commandText)
      setReplyState('success')
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
      appendAssistantLog('AI command completed', `Chat ${Date.now() - chatStartedAt}ms`, 'success')
    } catch (error) {
      const message = getErrorMessage(error)
      setReplyState('error')
      setChatError(message)
      setReply(`AI 请求失败：${message}`)
    }
  }

  return (
    <AppShell>
      <TopNav />
      <ReminderToastStack reminders={reminderToasts} onDismiss={dismissReminderToast} />
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
          <DailySummaryCard
            error={summaryError}
            isLoading={isSummaryLoading}
            isUsingDemoSummary={isUsingDemoSummary}
            onRetry={loadDailySummary}
            summary={dailySummary}
          />
          <RecentReminders
            error={remindersError}
            isLoading={isRemindersLoading}
            isUsingDemoReminders={isUsingDemoReminders}
            onRetry={loadRecentReminders}
            reminders={recentReminders}
          />
          <WeekSummary
            error={weekError}
            events={weekEvents}
            isLoading={isWeekLoading}
            isUsingDemoEvents={isUsingDemoWeek}
          />
          <OperationLog
            error={logsError}
            isLoading={isLogsLoading}
            isUsingDemoLogs={isUsingDemoLogs}
            logs={logs}
            onRetry={loadVoiceCommandLogs}
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

function getReminderKey(reminder: Reminder) {
  if (reminder.reminderMinutes === 0) {
    return `event:${reminder.eventId}:${reminder.startTime}`
  }
  return `${reminder.eventId}:${reminder.remindedAt ?? reminder.startTime}`
}

function getStartedEventKey(event: CalendarEvent) {
  return `event:${event.id}:${event.startTime}`
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
