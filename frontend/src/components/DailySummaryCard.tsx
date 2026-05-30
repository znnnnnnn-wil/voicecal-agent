import { getCategoryBadgeClass, getCategoryLabel } from '../lib/categoryUtils'
import type { DailySummary, DailySummaryEvent } from '../types/ai'

type DailySummaryCardProps = {
  error: string | null
  isLoading: boolean
  isUsingDemoSummary: boolean
  onRetry: () => void
  summary: DailySummary | null
}

function DailySummaryCard({ error, isLoading, isUsingDemoSummary, onRetry, summary }: DailySummaryCardProps) {
  const categoryStats = Object.entries(summary?.categoryStats ?? {})

  return (
    <section className="rounded-2xl border border-[#dadce0] bg-white p-4 shadow-sm">
      <div className="mb-4 flex items-start justify-between gap-3">
        <div>
          <p className="text-sm font-semibold text-[#202124]">每日摘要</p>
          <p className="mt-1 text-xs text-[#5f6368]">
            {isUsingDemoSummary ? '基于 demo fallback 数据' : '来自后端摘要接口'}
          </p>
        </div>
        <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs text-[#5f6368]">{summary?.date ?? '今天'}</span>
      </div>

      {error && (
        <div className="mb-3 rounded-lg border border-amber-200 bg-amber-50 p-3">
          <p className="text-xs leading-5 text-amber-800">后端暂不可用，当前展示 demo 摘要。</p>
          <button className="mt-2 text-xs font-semibold text-amber-700 underline underline-offset-4" onClick={onRetry} type="button">
            重试摘要
          </button>
        </div>
      )}

      {isLoading && (
        <div className="space-y-3 rounded-xl bg-[#f8fafc] p-3">
          <div className="h-3 w-4/5 animate-pulse rounded-full bg-slate-200" />
          <div className="h-3 w-2/3 animate-pulse rounded-full bg-slate-100" />
        </div>
      )}

      {!isLoading && summary && (
        <div className="space-y-3">
          <p className="rounded-xl bg-[#f8fafc] p-3 text-sm leading-6 text-[#3c4043]">{summary.summary}</p>
          <div className="grid grid-cols-2 gap-2">
            <Metric label="日程数" value={String(summary.eventCount)} />
            <Metric label="占用" value={`${summary.busyMinutes}m`} />
          </div>
          <div className="grid gap-2 sm:grid-cols-2">
            <SummaryEvent label="最早安排" event={summary.earliestEvent} />
            <SummaryEvent label="最晚结束" event={summary.latestEvent} />
          </div>
          <div className="flex flex-wrap gap-2">
            {categoryStats.map(([category, count]) => (
              <span className={`rounded-full border px-2.5 py-1 text-[11px] ${getCategoryBadgeClass(category)}`} key={category}>
                {getCategoryLabel(category)} · {count}
              </span>
            ))}
          </div>
        </div>
      )}

      {!isLoading && !summary && !error && (
        <div className="rounded-xl border border-dashed border-[#dadce0] bg-[#f8fafc] p-4">
          <p className="text-sm font-semibold text-[#202124]">暂无摘要</p>
          <p className="mt-1 text-xs leading-5 text-[#5f6368]">后端当前没有返回每日摘要。</p>
        </div>
      )}
    </section>
  )
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl bg-[#f8fafc] p-3">
      <p className="text-lg font-semibold text-[#202124]">{value}</p>
      <p className="mt-1 text-[11px] text-[#5f6368]">{label}</p>
    </div>
  )
}

function SummaryEvent({ label, event }: { label: string; event?: DailySummaryEvent | null }) {
  return (
    <div className="rounded-xl bg-[#f8fafc] p-3">
      <p className="text-xs font-semibold text-[#5f6368]">{label}</p>
      {event ? (
        <>
          <p className="mt-2 truncate text-sm font-semibold text-[#202124]">{event.title}</p>
          <div className="mt-2 flex items-center justify-between gap-2">
            <span className="text-xs text-[#5f6368]">{formatTime(event.startTime)}</span>
            <span className={`rounded-full border px-2 py-0.5 text-[11px] ${getCategoryBadgeClass(event.category)}`}>
              {getCategoryLabel(event.category)}
            </span>
          </div>
        </>
      ) : (
        <p className="mt-2 text-xs text-[#5f6368]">暂无</p>
      )}
    </div>
  )
}

function formatTime(value: string) {
  return value.slice(11, 16)
}

export default DailySummaryCard
