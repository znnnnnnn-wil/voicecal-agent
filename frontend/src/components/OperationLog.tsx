import type { VoiceCommandLog } from '../types/log'

type OperationLogProps = {
  error: string | null
  isLoading: boolean
  isUsingDemoLogs: boolean
  logs: VoiceCommandLog[]
  onRetry: () => void
}

function OperationLog({ error, isLoading, isUsingDemoLogs, logs, onRetry }: OperationLogProps) {
  return (
    <section className="rounded-2xl border border-[#dadce0] bg-white p-4 shadow-sm">
      <div className="mb-4 flex items-start justify-between gap-3">
        <div>
          <p className="text-sm font-semibold text-[#202124]">最近操作日志</p>
          <p className="mt-1 text-xs text-[#5f6368]">最近 AI 对话记录</p>
        </div>
        <button
          aria-label="重新加载操作日志"
          className="rounded-lg border border-[#dadce0] bg-white px-2.5 py-1.5 text-xs font-medium text-[#3c4043] hover:bg-[#f8fafc] disabled:cursor-not-allowed disabled:opacity-60"
          disabled={isLoading}
          onClick={onRetry}
          type="button"
        >
          {isLoading ? '加载中' : '刷新'}
        </button>
      </div>

      {isUsingDemoLogs && (
        <div className="mb-3 rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs leading-5 text-amber-800">
          后端暂不可用，当前展示 demo logs。
        </div>
      )}

      {error && <div className="mb-3 rounded-lg border border-rose-200 bg-rose-50 p-3 text-xs leading-5 text-rose-700">{error}</div>}

      {isLoading && (
        <div className="space-y-3">
          {Array.from({ length: 3 }, (_, index) => (
            <div className="rounded-xl border border-[#e5e7eb] bg-[#f8fafc] p-3" key={index}>
              <div className="h-3 w-2/3 animate-pulse rounded-full bg-slate-200" />
              <div className="mt-3 h-3 w-full animate-pulse rounded-full bg-slate-100" />
            </div>
          ))}
        </div>
      )}

      {!isLoading && logs.length === 0 && (
        <div className="rounded-xl border border-dashed border-[#dadce0] bg-[#f8fafc] p-4 text-sm text-[#5f6368]">
          暂无操作日志。发送一条 AI 指令后，这里会展示最近对话记录。
        </div>
      )}

      {!isLoading && logs.length > 0 && (
        <div className="max-h-80 space-y-3 overflow-y-auto pr-1">
          {logs.slice(0, 6).map((log) => (
            <div className="rounded-xl border border-[#e5e7eb] bg-[#f8fafc] p-3" key={log.id}>
              <div className="flex gap-3">
                <span className={`mt-1 size-2.5 shrink-0 rounded-full ${log.success ? 'bg-emerald-500' : 'bg-rose-500'}`} />
                <div className="min-w-0 flex-1">
                  <div className="flex items-center justify-between gap-3">
                    <p className="min-w-0 truncate text-sm font-semibold text-[#202124]">{log.rawText}</p>
                    <span className="shrink-0 text-[11px] text-[#5f6368]">{formatDateTime(log.createdAt)}</span>
                  </div>
                  <p className="mt-1 line-clamp-2 break-words text-xs leading-5 text-[#5f6368]">
                    {log.assistantReply || '暂无 AI 回复'}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
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
