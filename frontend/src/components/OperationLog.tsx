import type { VoiceCommandLog } from '../types/log'

const statusClass = {
  success: 'bg-emerald-300 text-slate-950',
  failed: 'bg-rose-300 text-slate-950',
}

type OperationLogProps = {
  error: string | null
  isLoading: boolean
  isUsingDemoLogs: boolean
  logs: VoiceCommandLog[]
  onRetry: () => void
}

function OperationLog({ error, isLoading, isUsingDemoLogs, logs, onRetry }: OperationLogProps) {
  return (
    <section className="h-fit self-start rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <div className="mb-5 flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-white">操作日志</p>
          <p className="mt-1 text-xs text-slate-400">最近 AI 对话记录</p>
        </div>
        <button
          className="rounded-full border border-white/15 bg-white/[0.06] px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-white/[0.1] disabled:cursor-not-allowed disabled:opacity-60"
          disabled={isLoading}
          onClick={onRetry}
          type="button"
        >
          {isLoading ? '加载中' : 'Retry'}
        </button>
      </div>

      {isUsingDemoLogs && (
        <div className="mb-4 rounded-2xl border border-amber-200/20 bg-amber-200/10 p-4 text-sm leading-6 text-amber-50">
          Showing demo logs because the backend is unavailable.
        </div>
      )}

      {error && (
        <div className="mb-4 rounded-2xl border border-rose-300/20 bg-rose-300/10 p-4 text-sm leading-6 text-rose-50">
          {error}
        </div>
      )}

      {isLoading && (
        <div className="space-y-4">
          {Array.from({ length: 3 }, (_, index) => (
            <div className="rounded-2xl border border-white/10 bg-[#0d131a]/70 p-4" key={index}>
              <div className="h-3 w-2/3 animate-pulse rounded-full bg-white/15" />
              <div className="mt-3 h-3 w-full animate-pulse rounded-full bg-white/10" />
              <div className="mt-2 h-3 w-1/2 animate-pulse rounded-full bg-white/10" />
            </div>
          ))}
        </div>
      )}

      {!isLoading && logs.length === 0 && (
        <div className="rounded-2xl border border-dashed border-white/15 bg-[#0d131a]/70 p-5 text-sm text-slate-400">
          暂无操作日志。发送一条 AI 指令后，这里会展示最近对话记录。
        </div>
      )}

      {!isLoading && logs.length > 0 && <div className="max-h-96 space-y-4 overflow-y-auto pr-1">
        {logs.map((log) => (
          <div className="flex gap-3" key={log.id}>
            <span className={`mt-1 size-2.5 rounded-full ${log.success ? statusClass.success : statusClass.failed}`} />
            <div className="min-w-0 flex-1 border-b border-white/10 pb-4 last:border-b-0 last:pb-0">
              <div className="flex items-center justify-between gap-3">
                <p className="truncate text-sm font-semibold text-white">{log.rawText}</p>
                <span className="shrink-0 text-[11px] text-slate-500">{formatDateTime(log.createdAt)}</span>
              </div>
              <p className="mt-1 line-clamp-3 text-xs leading-5 text-slate-400">
                {log.assistantReply || '暂无 AI 回复'}
              </p>
              <div className="mt-2 flex flex-wrap gap-2">
                <span className="rounded-full border border-white/10 bg-white/[0.05] px-2 py-1 text-[11px] text-slate-400">
                  {log.intent || 'CHAT'}
                </span>
                {log.toolName && (
                  <span className="rounded-full border border-cyan-300/15 bg-cyan-300/10 px-2 py-1 text-[11px] text-cyan-100">
                    {log.toolName}
                  </span>
                )}
                <span className="rounded-full border border-white/10 bg-white/[0.05] px-2 py-1 text-[11px] text-slate-500">
                  {log.conversationId}
                </span>
              </div>
            </div>
          </div>
        ))}
      </div>}
    </section>
  )
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

export default OperationLog
