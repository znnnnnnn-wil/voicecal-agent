import type { Reminder } from '../types/reminder'

type RecentRemindersProps = {
  reminders: Reminder[]
  error: string | null
  isLoading: boolean
  isUsingDemoReminders: boolean
  onRetry: () => void
}

const dateTimeFormatter = new Intl.DateTimeFormat('zh-CN', {
  month: 'short',
  day: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

function RecentReminders({ reminders, error, isLoading, isUsingDemoReminders, onRetry }: RecentRemindersProps) {
  return (
    <section className="rounded-2xl border border-[#dadce0] bg-white p-4 shadow-sm">
      <div className="mb-4 flex items-start justify-between gap-3">
        <div>
          <p className="text-sm font-semibold text-[#202124]">最近提醒</p>
          <p className="mt-1 text-xs text-[#5f6368]">
            {isUsingDemoReminders ? 'Demo fallback data' : `${reminders.length} 条已触发提醒`}
          </p>
        </div>
        <span className="rounded-full bg-amber-50 px-2.5 py-1 text-xs font-medium text-amber-700">Reminder</span>
      </div>

      {error && (
        <div className="mb-3 rounded-lg border border-amber-200 bg-amber-50 p-3">
          <p className="text-xs leading-5 text-amber-800">后端暂不可用，当前展示 demo reminders。</p>
          <button aria-label="重新加载最近提醒" className="mt-2 text-xs font-semibold text-amber-700 underline underline-offset-4" onClick={onRetry} type="button">
            重试加载
          </button>
        </div>
      )}

      {isLoading && <div className="space-y-2">{Array.from({ length: 3 }).map((_, index) => <div className="h-14 animate-pulse rounded-xl bg-slate-100" key={index} />)}</div>}

      {!isLoading && reminders.length === 0 && (
        <div className="rounded-xl border border-dashed border-[#dadce0] bg-[#f8fafc] p-4">
          <p className="text-sm font-semibold text-[#202124]">暂无已触发提醒</p>
          <p className="mt-1 text-xs leading-5 text-[#5f6368]">到达提醒时间后，这里会展示最近触发记录。</p>
        </div>
      )}

      {!isLoading && reminders.length > 0 && (
        <div className="space-y-2">
          {reminders.slice(0, 4).map((reminder) => (
            <div className="rounded-xl border border-[#e5e7eb] bg-[#f8fafc] p-3" key={reminder.eventId}>
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold text-[#202124]">{reminder.title}</p>
                  <p className="mt-1 text-xs text-[#5f6368]">开始：{formatDateTime(reminder.startTime)}</p>
                  <p className="mt-1 text-xs text-[#5f6368]">
                    触发：{reminder.remindedAt ? formatDateTime(reminder.remindedAt) : '未记录'}
                  </p>
                </div>
                <span className="shrink-0 rounded-full border border-emerald-100 bg-emerald-50 px-2 py-0.5 text-[11px] text-emerald-700">
                  {reminder.reminderMinutes} 分钟
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

function formatDateTime(value: string) {
  return dateTimeFormatter.format(new Date(value))
}

export default RecentReminders
