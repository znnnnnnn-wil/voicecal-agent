import { getCategoryBadgeClass, getCategoryLabel } from '../lib/categoryUtils'
import type { DailySummary, DailySummaryEvent } from '../types/ai'

type DailySummaryCardProps = {
  error: string | null
  isLoading: boolean
  isUsingDemoSummary: boolean
  onRetry: () => void
  summary: DailySummary | null
}

function DailySummaryCard({
  error,
  isLoading,
  isUsingDemoSummary,
  onRetry,
  summary,
}: DailySummaryCardProps) {
  const categoryStats = Object.entries(summary?.categoryStats ?? {})

  return (
    <section className="h-fit self-start rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <div className="mb-5 flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-white">每日摘要</p>
          <p className="mt-1 text-xs text-slate-400">
            {isUsingDemoSummary ? '基于 demo fallback 数据' : '来自后端摘要接口'}
          </p>
        </div>
        <span className="rounded-full bg-white/[0.08] px-3 py-1 text-xs text-slate-300">
          {summary?.date ?? '今天'}
        </span>
      </div>

      {error && (
        <div className="mb-4 rounded-2xl border border-amber-200/20 bg-amber-200/10 p-3">
          <p className="text-xs leading-5 text-amber-50">
            Showing demo data because the backend is unavailable. {error}
          </p>
          <button
            className="mt-2 text-xs font-semibold text-amber-100 underline underline-offset-4"
            onClick={onRetry}
            type="button"
          >
            重试摘要
          </button>
        </div>
      )}

      {isLoading && (
        <div className="space-y-3 rounded-3xl border border-white/10 bg-[#0d131a]/70 p-4">
          <div className="h-3 w-4/5 animate-pulse rounded-full bg-white/15" />
          <div className="h-3 w-2/3 animate-pulse rounded-full bg-white/10" />
          <div className="h-3 w-1/2 animate-pulse rounded-full bg-white/10" />
        </div>
      )}

      {!isLoading && summary && (
        <div className="space-y-4">
          <p className="rounded-3xl border border-white/10 bg-[#0d131a]/70 p-4 text-sm leading-7 text-slate-200">
            {summary.summary}
          </p>
          <div className="grid grid-cols-2 gap-3">
            <Metric label="日程数" value={String(summary.eventCount)} />
            <Metric label="已占用" value={`${summary.busyMinutes}m`} />
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <SummaryEvent label="最早安排" event={summary.earliestEvent} />
            <SummaryEvent label="最晚结束" event={summary.latestEvent} />
          </div>

          <div className="rounded-2xl border border-white/10 bg-[#0d131a]/70 p-4">
            <p className="text-xs font-semibold text-slate-400">分类统计</p>
            {categoryStats.length === 0 ? (
              <p className="mt-3 text-xs text-slate-500">暂无分类数据</p>
            ) : (
              <div className="mt-3 flex flex-wrap gap-2">
                {categoryStats.map(([category, count]) => (
                  <span
                    className={`rounded-full border px-2.5 py-1 text-[11px] ${getCategoryBadgeClass(category)}`}
                    key={category}
                  >
                    {getCategoryLabel(category)} · {count}
                  </span>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {!isLoading && !summary && !error && (
        <div className="rounded-3xl border border-dashed border-white/15 bg-[#0d131a]/70 p-5">
          <p className="text-sm font-semibold text-white">暂无摘要</p>
          <p className="mt-2 text-xs leading-5 text-slate-400">
            后端当前没有返回每日摘要。请稍后重试。
          </p>
        </div>
      )}
    </section>
  )
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-[#0d131a]/70 p-4">
      <p className="text-2xl font-semibold text-white">{value}</p>
      <p className="mt-1 text-xs text-slate-400">{label}</p>
    </div>
  )
}

function SummaryEvent({ label, event }: { label: string; event?: DailySummaryEvent | null }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-[#0d131a]/70 p-4">
      <p className="text-xs font-semibold text-slate-400">{label}</p>
      {event ? (
        <>
          <p className="mt-2 break-words text-sm font-semibold text-white">{event.title}</p>
          <div className="mt-2 flex items-center justify-between gap-2">
            <span className="text-xs text-slate-500">{formatTime(event.startTime)}</span>
            <span className={`rounded-full border px-2 py-0.5 text-[11px] ${getCategoryBadgeClass(event.category)}`}>
              {getCategoryLabel(event.category)}
            </span>
          </div>
        </>
      ) : (
        <p className="mt-2 text-xs text-slate-500">暂无</p>
      )}
    </div>
  )
}

function formatTime(value: string) {
  return value.slice(11, 16)
}

export default DailySummaryCard
