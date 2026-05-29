export type CalendarEvent = {
  id: number
  title: string
  description?: string | null
  startTime: string
  endTime: string
  location?: string | null
  category?: string | null
  reminderMinutes?: number | null
  reminderTriggered?: boolean | null
  remindedAt?: string | null
  createdAt?: string
  updatedAt?: string
}
