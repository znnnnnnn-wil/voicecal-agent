export type CalendarEventCategory = 'WORK' | 'STUDY' | 'LIFE' | 'MEETING' | 'INTERVIEW' | 'OTHER'

export type CalendarEvent = {
  id: number
  title: string
  description?: string | null
  startTime: string
  endTime: string
  location?: string | null
  category?: CalendarEventCategory | null
  reminderMinutes?: number | null
  reminderTriggered?: boolean | null
  remindedAt?: string | null
  createdAt?: string
  updatedAt?: string
}
