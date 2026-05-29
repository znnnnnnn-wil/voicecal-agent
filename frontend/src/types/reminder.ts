export type Reminder = {
  eventId: number
  title: string
  startTime: string
  reminderMinutes: number
  reminderTriggered: boolean
  remindedAt: string | null
}
