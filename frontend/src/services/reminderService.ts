import { apiGet } from '../lib/apiClient'
import type { Reminder } from '../types/reminder'

export function getRecentReminders(limit = 20): Promise<Reminder[]> {
  const params = new URLSearchParams({ limit: String(limit) })
  return apiGet<Reminder[]>(`/api/reminders/recent?${params.toString()}`)
}
