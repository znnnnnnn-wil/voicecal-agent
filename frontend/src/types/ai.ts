import type { CalendarEvent } from './calendar'

export type AiChatRequest = {
  message: string
}

export type AiChatResponse = {
  reply: string
}

export type DailySummary = {
  date: string
  timezone: string
  eventCount: number
  busyMinutes: number
  summary: string
  events: CalendarEvent[]
}
