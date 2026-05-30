import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import timeGridPlugin from '@fullcalendar/timegrid'
import type { EventClickArg, EventInput } from '@fullcalendar/core'
import { getCategoryEventColor, getCategoryLabel } from '../lib/categoryUtils'
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
    <section className="min-w-0 rounded-2xl border border-[#dadce0] bg-white shadow-sm">
      <div className="flex flex-col gap-3 border-b border-[#e5e7eb] px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm font-semibold text-[#202124]">日历视图</p>
          <p className="mt-1 text-xs text-[#5f6368]">月 / 周 / 日视图展示真实日程数据</p>
        </div>
        <button
          className="w-fit rounded-lg border border-[#dadce0] bg-white px-3 py-2 text-xs font-medium text-[#3c4043] transition hover:bg-[#f8fafc] disabled:cursor-not-allowed disabled:opacity-60"
          disabled={isLoading}
          onClick={onRetry}
          type="button"
        >
          {isLoading ? '刷新中...' : '刷新'}
        </button>
      </div>

      <div className="p-3 sm:p-4">
        {isUsingDemoEvents && (
          <div className="mb-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs leading-5 text-amber-800">
            后端暂不可用，当前展示 demo fallback 数据。
          </div>
        )}

        {error && (
          <div className="mb-3 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-xs leading-5 text-rose-700">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <span>{error}</span>
              <button
                className="w-fit rounded-md border border-rose-200 bg-white px-2 py-1 font-medium text-rose-700"
                disabled={isLoading}
                onClick={onRetry}
                type="button"
              >
                重试
              </button>
            </div>
          </div>
        )}

        {isLoading ? (
          <div className="rounded-xl border border-[#e5e7eb] bg-white p-4">
            <div className="mb-4 h-8 w-40 animate-pulse rounded-full bg-slate-100" />
            <div className="grid grid-cols-7 gap-px overflow-hidden rounded-lg border border-[#e5e7eb] bg-[#e5e7eb]">
              {Array.from({ length: 35 }, (_, index) => (
                <div className="aspect-square animate-pulse bg-white" key={index} />
              ))}
            </div>
          </div>
        ) : (
          <div className="voicecal-calendar">
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
              <div className="mt-3 rounded-xl border border-dashed border-[#dadce0] bg-[#f8fafc] p-4 text-sm text-[#5f6368]">
                暂无日程，试试用语音创建一个。
              </div>
            )}
          </div>
        )}
      </div>
    </section>
  )
}

function toCalendarEvent(event: CalendarEvent): EventInput {
  const categoryColor = getCategoryEventColor(event.category)
  return {
    id: String(event.id),
    title: `[${getCategoryLabel(event.category)}] ${event.title}`,
    start: event.startTime,
    end: event.endTime,
    backgroundColor: event.reminderTriggered ? '#e6f4ea' : categoryColor.backgroundColor,
    borderColor: event.reminderTriggered ? '#34a853' : categoryColor.borderColor,
    textColor: event.reminderTriggered ? '#137333' : categoryColor.textColor,
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
