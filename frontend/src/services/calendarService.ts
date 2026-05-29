import { apiGet } from '../lib/apiClient'
import type { CalendarEvent } from '../types/calendar'

export function listCalendarEvents() {
  return apiGet<CalendarEvent[]>('/api/calendar/events')
}
