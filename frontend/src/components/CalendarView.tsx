import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import timeGridPlugin from '@fullcalendar/timegrid'
import type { EventClickArg, EventInput } from '@fullcalendar/core'
import type { CalendarEvent } from '../types/calendar'

type CalendarViewProps = {
  error: string | null
  events: CalendarEvent[]
  isLoading: boolean
  isUsingDemoEvents: boolean
  onEventSelect: (event: CalendarEvent) => void
  onRetry: () => void
}

function CalendarView({
  error,
  events,
  isLoading,
  isUsingDemoEvents,
  onEventSelect,
  onRetry,
}: CalendarViewProps) {
  const calendarEvents = events.map(toCalendarEvent)

  return (
    <section className="h-fit self-start rounded-[32px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl sm:p-6">
      <div className="mb-5 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <p className="text-sm font-semibold text-white">日历视图</p>
          <p className="mt-1 text-xs text-slate-400">月视图和周视图展示真实日程数据</p>
        </div>
        <button
          className="w-fit rounded-full border border-white/15 bg-white/[0.06] px-4 py-2 text-xs font-semibold text-white transition hover:bg-white/[0.1] disabled:cursor-not-allowed disabled:opacity-60"
          disabled={isLoading}
          onClick={onRetry}
          type="button"
        >
          {isLoading ? '刷新中...' : '刷新日历'}
        </button>
      </div>

      {isUsingDemoEvents && (
        <div className="mb-4 rounded-2xl border border-amber-200/20 bg-amber-200/10 p-4 text-sm leading-6 text-amber-50">
          Showing demo data because the backend is unavailable.
        </div>
      )}

      {error && (
        <div className="mb-4 rounded-2xl border border-rose-300/20 bg-rose-300/10 p-4">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-sm leading-6 text-rose-50">{error}</p>
            <button
              className="w-fit rounded-full border border-rose-200/30 bg-rose-200/10 px-3 py-1.5 text-xs font-semibold text-rose-50 transition hover:bg-rose-200/15"
              disabled={isLoading}
              onClick={onRetry}
              type="button"
            >
              Retry
            </button>
          </div>
        </div>
      )}

      {isLoading ? (
        <div className="rounded-3xl border border-white/10 bg-[#0d131a]/70 p-5">
          <div className="mb-5 h-8 w-2/5 animate-pulse rounded-full bg-white/15" />
          <div className="grid grid-cols-7 gap-2">
            {Array.from({ length: 35 }, (_, index) => (
              <div
                className="aspect-square animate-pulse rounded-2xl bg-white/[0.06]"
                key={index}
              />
            ))}
          </div>
        </div>
      ) : (
        <div className="voicecal-calendar rounded-3xl border border-white/10 bg-[#0d131a]/80 p-3 sm:p-4">
          <FullCalendar
            allDaySlot={false}
            dayMaxEvents={3}
            eventClick={handleEventClick(onEventSelect)}
            events={calendarEvents}
            headerToolbar={{
              left: 'prev,next today',
              center: 'title',
              right: 'dayGridMonth,timeGridWeek,timeGridDay',
            }}
            height="auto"
            initialView="dayGridMonth"
            locale="zh-cn"
            nowIndicator
            plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
            slotMinTime="07:00:00"
          />
          {events.length === 0 && (
            <div className="mt-4 rounded-2xl border border-dashed border-white/15 bg-white/[0.04] p-5 text-sm leading-6 text-slate-400">
              No events yet. Ask VoiceCal to create one or use the calendar API.
            </div>
          )}
        </div>
      )}
    </section>
  )
}

function toCalendarEvent(event: CalendarEvent): EventInput {
  return {
    id: String(event.id),
    title: event.title,
    start: event.startTime,
    end: event.endTime,
    backgroundColor: event.reminderTriggered ? '#059669' : '#0891b2',
    borderColor: event.reminderTriggered ? '#6ee7b7' : '#67e8f9',
    textColor: '#ecfeff',
    extendedProps: {
      description: event.description,
      location: event.location,
      category: event.category,
      reminderMinutes: event.reminderMinutes,
      reminderTriggered: event.reminderTriggered,
      remindedAt: event.remindedAt,
      rawEvent: event,
    },
  }
}

function handleEventClick(onEventSelect: (event: CalendarEvent) => void) {
  return (clickInfo: EventClickArg) => {
    const rawEvent = clickInfo.event.extendedProps.rawEvent
    if (isCalendarEvent(rawEvent)) {
      onEventSelect(rawEvent)
    }
  }
}

function isCalendarEvent(value: unknown): value is CalendarEvent {
  return (
    typeof value === 'object' &&
    value !== null &&
    'id' in value &&
    'title' in value &&
    'startTime' in value &&
    'endTime' in value
  )
}

export default CalendarView
