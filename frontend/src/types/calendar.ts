export type CalendarEvent = {
  id: number
  title: string
  description?: string | null
  startTime: string
  endTime: string
  location?: string | null
  createdAt?: string
  updatedAt?: string
}
