export type ReplyState = 'empty' | 'idle' | 'loading' | 'success' | 'error'

type AiReplyPanelProps = {
  reply: string
  state: ReplyState
}

const stateMeta = {
  empty: {
    label: '空状态',
    className: 'border-slate-300/15 bg-slate-300/10 text-slate-200',
  },
  idle: {
    label: '待命',
    className: 'border-cyan-300/20 bg-cyan-300/10 text-cyan-100',
  },
  loading: {
    label: '生成中',
    className: 'border-amber-200/20 bg-amber-200/10 text-amber-100',
  },
  success: {
    label: '已完成',
    className: 'border-emerald-300/20 bg-emerald-300/10 text-emerald-100',
  },
  error: {
    label: '需输入',
    className: 'border-rose-300/20 bg-rose-300/10 text-rose-100',
  },
}

function AiReplyPanel({ reply, state }: AiReplyPanelProps) {
  const meta = stateMeta[state]

  return (
    <section className="rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-white">AI 回复</p>
          <p className="mt-1 text-xs text-slate-400">本地 mock 结果</p>
        </div>
        <span className={`rounded-full border px-3 py-1 text-xs font-medium ${meta.className}`}>
          {meta.label}
        </span>
      </div>

      <div className="mt-5 min-h-40 rounded-3xl border border-white/10 bg-[#0d131a]/70 p-5">
        {state === 'loading' ? (
          <div className="space-y-3">
            <div className="h-3 w-4/5 animate-pulse rounded-full bg-white/15" />
            <div className="h-3 w-2/3 animate-pulse rounded-full bg-white/10" />
            <div className="h-3 w-1/2 animate-pulse rounded-full bg-white/10" />
          </div>
        ) : (
          <p className="text-sm leading-7 text-slate-200">{reply}</p>
        )}
      </div>
    </section>
  )
}

export default AiReplyPanel
