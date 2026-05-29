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
  error: '请输入日程指令后再运行',
}

function StatusPreview({ chatError, eventsError, feedback, isUsingDemoEvents }: StatusPreviewProps) {
  return (
    <section className="rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <div className="mb-5">
        <p className="text-sm font-semibold text-white">状态反馈</p>
        <p className="mt-1 text-xs text-slate-400">界面状态样式示例</p>
      </div>

      <div className="space-y-3">
        <div className="rounded-2xl border border-slate-300/15 bg-slate-300/10 p-4">
          <p className="text-xs font-semibold text-slate-200">空状态</p>
          <p className="mt-1 text-xs text-slate-400">暂无选中的日程草稿。</p>
        </div>
        <div className="rounded-2xl border border-amber-200/20 bg-amber-200/10 p-4">
          <p className="text-xs font-semibold text-amber-100">加载状态</p>
          <div className="mt-3 h-2 w-2/3 overflow-hidden rounded-full bg-white/10">
            <div className="h-full w-1/2 rounded-full bg-amber-200" />
          </div>
        </div>
        <div className="rounded-2xl border border-rose-300/20 bg-rose-300/10 p-4">
          <p className="text-xs font-semibold text-rose-100">错误提示</p>
          <p className="mt-1 text-xs text-rose-50/80">未识别到有效时间或标题。</p>
        </div>
        <div className="rounded-2xl border border-emerald-300/20 bg-emerald-300/10 p-4">
          <p className="text-xs font-semibold text-emerald-100">成功反馈</p>
          <p className="mt-1 text-xs text-emerald-50/80">日程草稿已准备完成。</p>
        </div>
      </div>

      <div className="mt-4 rounded-2xl border border-cyan-300/15 bg-cyan-300/10 p-4">
        <p className="text-xs font-semibold text-cyan-100">当前交互</p>
        <p className="mt-1 text-xs text-cyan-50/80">{feedbackText[feedback]}</p>
      </div>

      {(chatError || eventsError || isUsingDemoEvents) && (
        <div className="mt-3 rounded-2xl border border-amber-200/20 bg-amber-200/10 p-4">
          <p className="text-xs font-semibold text-amber-100">数据源状态</p>
          <p className="mt-1 text-xs leading-5 text-amber-50/80">
            {isUsingDemoEvents
              ? '日历正在显示 demo fallback 数据。'
              : '日历数据来自后端接口。'}
            {chatError ? ` AI 请求错误：${chatError}` : ''}
            {eventsError ? ` 日历请求错误：${eventsError}` : ''}
          </p>
        </div>
      )}
    </section>
  )
}

export default StatusPreview
