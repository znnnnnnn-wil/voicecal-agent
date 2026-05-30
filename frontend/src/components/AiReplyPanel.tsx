export type ReplyState = 'empty' | 'idle' | 'loading' | 'success' | 'error'

type AiReplyPanelProps = {
  reply: string
  state: ReplyState
}

const stateMeta = {
  empty: { label: '空状态', className: 'border-slate-200 bg-slate-50 text-slate-600' },
  idle: { label: '待命', className: 'border-blue-100 bg-blue-50 text-blue-700' },
  loading: { label: '请求中', className: 'border-amber-100 bg-amber-50 text-amber-700' },
  success: { label: '已完成', className: 'border-emerald-100 bg-emerald-50 text-emerald-700' },
  error: { label: '需处理', className: 'border-rose-100 bg-rose-50 text-rose-700' },
}

function AiReplyPanel({ reply, state }: AiReplyPanelProps) {
  const meta = stateMeta[state]

  return (
    <section className="rounded-2xl border border-[#dadce0] bg-white p-4 shadow-sm">
      <div className="flex items-center justify-between gap-3">
        <div>
          <p className="text-sm font-semibold text-[#202124]">AI 助手回复</p>
          <p className="mt-1 text-xs text-[#5f6368]">最近一次日程指令结果</p>
        </div>
        <span className={`rounded-full border px-2.5 py-1 text-xs font-medium ${meta.className}`}>
          {meta.label}
        </span>
      </div>

      <div className="mt-4 min-h-32 rounded-xl border border-blue-100 bg-blue-50/70 p-4">
        {state === 'loading' ? (
          <div className="space-y-3">
            <div className="h-3 w-4/5 animate-pulse rounded-full bg-blue-100" />
            <div className="h-3 w-2/3 animate-pulse rounded-full bg-blue-100/80" />
            <div className="h-3 w-1/2 animate-pulse rounded-full bg-blue-100/70" />
            <p className="pt-1 text-xs text-[#5f6368]">正在等待后端 AI 接口响应...</p>
          </div>
        ) : (
          <p className="whitespace-pre-wrap text-sm leading-7 text-[#174ea6]">
            {renderReply(reply)}
          </p>
        )}
      </div>
    </section>
  )
}

function renderReply(reply: string) {
  const linkPattern = /(\/api\/calendar\/events\/(?:\d+\/ics|ics\?[^\s，。；\n]+))/g
  const exactLinkPattern = /^\/api\/calendar\/events\/(?:\d+\/ics|ics\?[^\s，。；\n]+)$/
  return reply.split(linkPattern).map((part, index) => {
    if (!exactLinkPattern.test(part)) {
      return part
    }
    return (
      <a
        className="font-semibold text-[#1a73e8] underline underline-offset-2 hover:text-[#1765cc]"
        download
        href={part}
        key={`${part}-${index}`}
      >
        下载 ICS
      </a>
    )
  })
}

export default AiReplyPanel
