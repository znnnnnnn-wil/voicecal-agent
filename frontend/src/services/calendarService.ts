import { apiGet } from '../lib/apiClient'
import type { CalendarEvent } from '../types/calendar'

const DEFAULT_TIMEZONE = 'Asia/Shanghai'

export function listCalendarEvents() {
  return apiGet<CalendarEvent[]>('/api/calendar/events')
}

export function getTodayEvents(timezone = DEFAULT_TIMEZONE) {
  return apiGet<CalendarEvent[]>(`/api/calendar/events/today${buildQuery({ timezone })}`)
}

export function getWeekEvents(timezone = DEFAULT_TIMEZONE) {
  return apiGet<CalendarEvent[]>(`/api/calendar/events/week${buildQuery({ timezone })}`)
}

function buildQuery(params: Record<string, string | undefined>) {
  const query = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value) {
      query.set(key, value)
    }
  })

  const queryString = query.toString()
  return queryString ? `?${queryString}` : ''
}
