export type ReminderToast = {
  toastId: string
  title: string
  startTime: string
  label: string
}

type ReminderToastStackProps = {
  reminders: ReminderToast[]
  onDismiss: (toastId: string) => void
}

const dateTimeFormatter = new Intl.DateTimeFormat('zh-CN', {
  hour: '2-digit',
  minute: '2-digit',
})

function ReminderToastStack({ reminders, onDismiss }: ReminderToastStackProps) {
  if (reminders.length === 0) {
    return null
  }

  return (
    <div className="pointer-events-none fixed right-4 top-20 z-50 flex w-[min(360px,calc(100vw-2rem))] flex-col gap-3">
      {reminders.map((reminder) => (
        <article
          aria-live="polite"
          className="pointer-events-auto rounded-xl border border-[#fbbc04]/40 bg-white p-4 shadow-[0_12px_32px_rgba(32,33,36,0.18)]"
          key={reminder.toastId}
          role="status"
        >
          <div className="flex items-start gap-3">
            <div className="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-[#fef7e0] text-lg text-[#b06000]">
              !
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold text-[#202124]">{reminder.label}</p>
              <p className="mt-1 break-words text-sm font-medium leading-5 text-[#202124]">{reminder.title}</p>
              <p className="mt-1 text-xs leading-5 text-[#5f6368]">
                开始时间：{formatTime(reminder.startTime)}
              </p>
            </div>
            <button
              aria-label="关闭提醒弹窗"
              className="grid h-7 w-7 shrink-0 place-items-center rounded-full text-lg leading-none text-[#5f6368] transition hover:bg-[#f1f3f4] hover:text-[#202124]"
              onClick={() => onDismiss(reminder.toastId)}
              type="button"
            >
              x
            </button>
          </div>
        </article>
      ))}
    </div>
  )
}

function formatTime(value: string) {
  return dateTimeFormatter.format(new Date(value))
}

export default ReminderToastStack
