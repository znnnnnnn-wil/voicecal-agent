import type { OperationLogItem } from '../data/demoData'

const statusClass = {
  success: 'bg-emerald-300 text-slate-950',
  info: 'bg-cyan-300 text-slate-950',
  pending: 'bg-amber-200 text-slate-950',
}

type OperationLogProps = {
  logs: OperationLogItem[]
}

function OperationLog({ logs }: OperationLogProps) {
  return (
    <section className="h-fit self-start rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <div className="mb-5">
        <p className="text-sm font-semibold text-white">操作日志</p>
        <p className="mt-1 text-xs text-slate-400">最近系统动作</p>
      </div>

      {logs.length === 0 && (
        <div className="rounded-2xl border border-dashed border-white/15 bg-[#0d131a]/70 p-5 text-sm text-slate-400">
          暂无操作日志。
        </div>
      )}

      <div className="max-h-72 space-y-4 overflow-y-auto pr-1">
        {logs.map((log) => (
          <div className="flex gap-3" key={`${log.time}-${log.label}`}>
            <span className={`mt-1 size-2.5 rounded-full ${statusClass[log.status]}`} />
            <div className="min-w-0 flex-1 border-b border-white/10 pb-4 last:border-b-0 last:pb-0">
              <div className="flex items-center justify-between gap-3">
                <p className="truncate text-sm font-semibold text-white">{log.label}</p>
                <span className="shrink-0 text-[11px] text-slate-500">{log.time}</span>
              </div>
              <p className="mt-1 text-xs leading-5 text-slate-400">{log.detail}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}

export default OperationLog
