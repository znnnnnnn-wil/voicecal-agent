import type { CalendarEvent } from '../types/calendar'

type EventDetailPanelProps = {
  event: CalendarEvent | null
  onClose: () => void
}

const dateTimeFormatter = new Intl.DateTimeFormat('zh-CN', {
  month: 'short',
  day: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

function EventDetailPanel({ event, onClose }: EventDetailPanelProps) {
  return (
    <section className="h-fit self-start rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-white">日程详情</p>
          <p className="mt-1 text-xs text-slate-400">
            {event ? '点击日历事件后展示' : '选择一个日程查看详情'}
          </p>
        </div>
        {event && (
          <button
            className="rounded-full border border-white/15 bg-white/[0.06] px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-white/[0.1]"
            onClick={onClose}
            type="button"
          >
            关闭
          </button>
        )}
      </div>

      {!event && (
        <div className="mt-5 rounded-2xl border border-dashed border-white/15 bg-[#0d131a]/70 p-5 text-sm leading-6 text-slate-400">
          在月视图或周视图中点击任意日程，可在这里查看时间、地点、描述和提醒信息。
        </div>
      )}

      {event && (
        <div className="mt-5 space-y-4">
          <div className="rounded-2xl border border-cyan-300/15 bg-cyan-300/[0.08] p-4">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <p className="break-words text-lg font-semibold text-white">{event.title}</p>
                <p className="mt-2 text-xs text-cyan-100">事件 ID #{event.id}</p>
              </div>
              <ReminderBadge event={event} />
            </div>
          </div>

          <DetailRow label="开始时间" value={formatDateTime(event.startTime)} />
          <DetailRow label="结束时间" value={formatDateTime(event.endTime)} />
          <DetailRow label="地点" value={event.location || '未设置'} />
          <DetailRow label="分类" value={event.category || '未设置'} />
          <DetailRow label="提醒" value={formatReminder(event)} />
          {event.remindedAt && <DetailRow label="提醒时间" value={formatDateTime(event.remindedAt)} />}

          <div className="rounded-2xl border border-white/10 bg-[#0d131a]/70 p-4">
            <p className="text-xs font-semibold text-slate-400">描述</p>
            <p className="mt-2 break-words text-sm leading-6 text-slate-200">
              {event.description || '暂无描述'}
            </p>
          </div>
        </div>
      )}
    </section>
  )
}

function ReminderBadge({ event }: { event: CalendarEvent }) {
  if (event.reminderMinutes === null || event.reminderMinutes === undefined) {
    return (
      <span className="shrink-0 rounded-full border border-white/10 bg-white/[0.06] px-2.5 py-1 text-[11px] text-slate-300">
        无提醒
      </span>
    )
  }

  const isTriggered = event.reminderTriggered === true
  return (
    <span
      className={`shrink-0 rounded-full border px-2.5 py-1 text-[11px] ${
        isTriggered
          ? 'border-emerald-300/20 bg-emerald-300/10 text-emerald-100'
          : 'border-amber-200/20 bg-amber-200/10 text-amber-100'
      }`}
    >
      {isTriggered ? '已提醒' : '未提醒'}
    </span>
  )
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-start justify-between gap-4 border-b border-white/10 pb-3 last:border-b-0">
      <span className="text-xs font-semibold text-slate-500">{label}</span>
      <span className="text-right text-sm leading-6 text-slate-100">{value}</span>
    </div>
  )
}

function formatReminder(event: CalendarEvent) {
  if (event.reminderMinutes === null || event.reminderMinutes === undefined) {
    return '无提醒'
  }
  return `提前 ${event.reminderMinutes} 分钟，${event.reminderTriggered ? '已提醒' : '未提醒'}`
}

function formatDateTime(value: string) {
  return dateTimeFormatter.format(new Date(value))
}

export default EventDetailPanel
