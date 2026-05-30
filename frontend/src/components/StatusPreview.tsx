type StatusPreviewProps = {
  chatError: string | null
  eventsError: string | null
  feedback: 'idle' | 'loading' | 'success' | 'error'
  isUsingDemoEvents: boolean
}

const feedbackText = {
  idle: '等待输入指令',
  loading: '正在等待后端响应...',
  success: '后端请求已成功返回',
  error: '请检查指令或接口状态',
}

function StatusPreview({ chatError, eventsError, feedback, isUsingDemoEvents }: StatusPreviewProps) {
  return (
    <section className="rounded-2xl border border-[#dadce0] bg-white p-4 shadow-sm">
      <div className="mb-4">
        <p className="text-sm font-semibold text-[#202124]">系统状态</p>
        <p className="mt-1 text-xs text-[#5f6368]">本地 Demo 与后端连接状态</p>
      </div>

      <div className="space-y-2">
        <StatusRow label="AI 交互" value={feedbackText[feedback]} tone={feedback} />
        <StatusRow label="数据源" value={isUsingDemoEvents ? 'Demo fallback' : '后端 API'} tone={isUsingDemoEvents ? 'loading' : 'success'} />
        <StatusRow label="日历工具" value="Tool Calling Ready" tone="success" />
      </div>

      {(chatError || eventsError || isUsingDemoEvents) && (
        <div className="mt-3 rounded-lg border border-amber-200 bg-amber-50 p-3">
          <p className="text-xs font-semibold text-amber-800">数据源状态</p>
          <p className="mt-1 text-xs leading-5 text-amber-800">
            {isUsingDemoEvents ? '当前存在 demo fallback 数据。' : '日历数据来自后端接口。'}
            {chatError ? ` AI 请求错误：${chatError}` : ''}
            {eventsError ? ` 日历请求错误：${eventsError}` : ''}
          </p>
        </div>
      )}
    </section>
  )
}

function StatusRow({
  label,
  value,
  tone,
}: {
  label: string
  value: string
  tone: 'idle' | 'loading' | 'success' | 'error'
}) {
  const dotClass = {
    idle: 'bg-slate-400',
    loading: 'bg-amber-400',
    success: 'bg-emerald-500',
    error: 'bg-rose-500',
  }[tone]

  return (
    <div className="flex items-center justify-between gap-3 rounded-xl bg-[#f8fafc] px-3 py-2">
      <div className="flex min-w-0 items-center gap-2">
        <span className={`size-2 shrink-0 rounded-full ${dotClass}`} />
        <span className="text-xs font-medium text-[#5f6368]">{label}</span>
      </div>
      <span className="truncate text-right text-xs text-[#202124]">{value}</span>
    </div>
  )
}

export default StatusPreview
