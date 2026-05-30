import type { CalendarEvent } from './calendar'

export type DailySummaryEvent = {
  id: number
  title: string
  startTime: string
  endTime: string
  category?: string | null
}

export type AiChatRequest = {
  message: string
}

export type AiChatResponse = {
  reply: string
  routedBy?: string
}

export type SpeechTranscriptionResponse = {
  text: string
  model?: string
  durationMs?: number
  success?: boolean
  message?: string
}

export type DailySummary = {
  date: string
  timezone: string
  eventCount: number
  busyMinutes: number
  categoryStats?: Record<string, number>
  earliestEvent?: DailySummaryEvent | null
  latestEvent?: DailySummaryEvent | null
  summary: string
  events: CalendarEvent[]
}
