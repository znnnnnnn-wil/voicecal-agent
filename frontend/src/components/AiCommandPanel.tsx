type AiCommandPanelProps = {
  command: string
  error: string | null
  isLoading: boolean
  isSuccess: boolean
  onCommandChange: (value: string) => void
  onRunCommand: () => void
}

function AiCommandPanel({
  command,
  error,
  isLoading,
  isSuccess,
  onCommandChange,
  onRunCommand,
}: AiCommandPanelProps) {
  return (
    <section className="rounded-[32px] border border-white/10 bg-white/[0.08] p-5 shadow-2xl shadow-black/30 backdrop-blur-2xl sm:p-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <p className="text-sm font-semibold text-cyan-100">询问 VoiceCal</p>
          <h1 className="mt-2 text-3xl font-semibold leading-tight text-white sm:text-4xl">
            用一句话规划今天的日程
          </h1>
          <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-400">
            使用自然语言安排会议、整理日程和准备待办。指令会发送到后端 AI 对话接口。
          </p>
        </div>
        <span className="w-fit rounded-full border border-cyan-300/20 bg-cyan-300/10 px-3 py-1 text-xs font-medium text-cyan-100">
          已连接后端
        </span>
      </div>

      <div className="mt-6 rounded-3xl border border-white/10 bg-[#0d131a]/80 p-4">
        <textarea
          className="min-h-32 w-full resize-none rounded-2xl border border-white/10 bg-white/[0.06] px-4 py-4 text-sm leading-6 text-slate-100 outline-none transition placeholder:text-slate-500 focus:border-cyan-200/50 focus:bg-white/[0.08] focus:ring-4 focus:ring-cyan-300/10"
          onChange={(event) => onCommandChange(event.target.value)}
          placeholder="帮我安排明天上午 10 点的设计评审会"
          value={command}
        />
        <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-3">
            <button
              className="rounded-full bg-white px-5 py-3 text-sm font-semibold text-slate-950 shadow-xl shadow-cyan-950/30 transition hover:-translate-y-0.5 hover:bg-cyan-100 focus:outline-none focus:ring-2 focus:ring-cyan-200 focus:ring-offset-2 focus:ring-offset-[#0d131a] disabled:cursor-not-allowed disabled:opacity-70 disabled:hover:translate-y-0"
              disabled={isLoading}
              onClick={onRunCommand}
              type="button"
            >
              {isLoading ? '思考中...' : '运行指令'}
            </button>
            <button
              className="rounded-full border border-white/15 bg-white/[0.06] px-5 py-3 text-sm font-semibold text-white transition hover:border-white/25 hover:bg-white/[0.1] focus:outline-none focus:ring-2 focus:ring-white/30 focus:ring-offset-2 focus:ring-offset-[#0d131a]"
              type="button"
            >
              语音输入
            </button>
          </div>
          <p className="text-xs text-amber-100">即将上线</p>
        </div>

        {error && (
          <div className="mt-4 rounded-2xl border border-rose-300/20 bg-rose-300/10 p-4">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <p className="text-sm leading-6 text-rose-50">{error}</p>
              <button
                className="w-fit rounded-full border border-rose-200/30 bg-rose-200/10 px-3 py-1.5 text-xs font-semibold text-rose-50 transition hover:bg-rose-200/15"
                disabled={isLoading}
                onClick={onRunCommand}
                type="button"
              >
                重试
              </button>
            </div>
          </div>
        )}

        {isSuccess && !error && (
          <div className="mt-4 rounded-2xl border border-emerald-300/20 bg-emerald-300/10 p-4 text-sm text-emerald-50">
            后端已返回 AI 回复。
          </div>
        )}
      </div>
    </section>
  )
}

export default AiCommandPanel
