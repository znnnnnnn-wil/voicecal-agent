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

function RecentReminders({
  reminders,
  error,
  isLoading,
  isUsingDemoReminders,
  onRetry,
}: RecentRemindersProps) {
  return (
    <section className="h-fit self-start rounded-[28px] border border-white/10 bg-white/[0.065] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <div className="mb-5 flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-white">最近提醒</p>
          <p className="mt-1 text-xs text-slate-400">
            {isUsingDemoReminders ? 'Demo fallback data' : `${reminders.length} 条已触发提醒`}
          </p>
        </div>
        <span className="rounded-full border border-cyan-300/20 bg-cyan-300/10 px-3 py-1 text-xs text-cyan-100">
          MVP
        </span>
      </div>

      {error && (
        <div className="mb-3 rounded-2xl border border-amber-200/20 bg-amber-200/10 p-3">
          <p className="text-xs leading-5 text-amber-50">
            Showing demo reminders because the backend is unavailable.
          </p>
          <button
            aria-label="重新加载最近提醒"
            className="mt-2 rounded-full border border-amber-200/25 bg-amber-200/10 px-3 py-1.5 text-xs font-semibold text-amber-100 transition hover:bg-amber-200/15"
            onClick={onRetry}
            type="button"
          >
            重试加载
          </button>
        </div>
      )}

      {isLoading && (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, index) => (
            <div className="h-16 animate-pulse rounded-2xl bg-white/10" key={index} />
          ))}
        </div>
      )}

      {!isLoading && reminders.length === 0 && (
        <div className="rounded-2xl border border-dashed border-white/15 bg-[#0d131a]/70 p-4">
          <p className="text-sm font-semibold text-white">暂无已触发提醒</p>
          <p className="mt-2 text-xs leading-5 text-slate-400">
            设置了提醒的日程到达提醒时间后，会在这里显示最近触发记录。
          </p>
        </div>
      )}

      {!isLoading && reminders.length > 0 && (
        <div className="space-y-3">
          {reminders.map((reminder) => (
            <div className="rounded-2xl border border-white/10 bg-[#0d131a]/70 p-4 shadow-inner shadow-white/[0.02]" key={reminder.eventId}>
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold text-white">{reminder.title}</p>
                  <p className="mt-1 text-xs text-slate-400">
                    日程开始：{formatDateTime(reminder.startTime)}
                  </p>
                  <p className="mt-1 text-xs text-slate-500">
                    触发时间：{reminder.remindedAt ? formatDateTime(reminder.remindedAt) : '未记录'}
                  </p>
                </div>
                <span className="shrink-0 rounded-full border border-emerald-300/20 bg-emerald-300/10 px-2.5 py-1 text-[11px] text-emerald-100">
                  提前 {reminder.reminderMinutes} 分钟
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
