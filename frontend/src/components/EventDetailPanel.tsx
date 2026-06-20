import { useState } from 'react'
import { getCategoryBadgeClass, getCategoryLabel } from '../lib/categoryUtils'
import { downloadEventIcs } from '../services/icsService'
import type { CalendarEvent } from '../types/calendar'

type EventDetailPanelProps = {
  event: CalendarEvent | null
  onClose: () => void
  onDelete: (event: CalendarEvent) => Promise<void>
}

const dateTimeFormatter = new Intl.DateTimeFormat('zh-CN', {
  month: 'short',
  day: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

function EventDetailPanel({ event, onClose, onDelete }: EventDetailPanelProps) {
  const [isExporting, setIsExporting] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)

  const handleExportIcs = async () => {
    if (!event) return
    setIsExporting(true)
    setActionError(null)
    try {
      await downloadEventIcs(event.id)
    } catch (error) {
      setActionError(getErrorMessage(error, '导出 ICS 失败，请稍后重试。'))
    } finally {
      setIsExporting(false)
    }
  }

  const handleDelete = async () => {
    if (!event) return
    const confirmed = window.confirm(`确定删除日程「${event.title}」吗？此操作不可撤销。`)
    if (!confirmed) return

    setIsDeleting(true)
    setActionError(null)
    try {
      await onDelete(event)
    } catch (error) {
      setActionError(getErrorMessage(error, '删除日程失败，请稍后重试。'))
    } finally {
      setIsDeleting(false)
    }
  }

  return (
    <section className="rounded-2xl border border-[#dadce0] bg-white p-4 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm font-semibold text-[#202124]">日程详情</p>
          <p className="mt-1 text-xs text-[#5f6368]">
            {event ? '当前选中日程' : '点击日历事件查看详情'}
          </p>
        </div>
        {event && (
          <button
            aria-label="关闭日程详情"
            className="rounded-lg border border-[#dadce0] bg-white px-2.5 py-1.5 text-xs font-medium text-[#3c4043] hover:bg-[#f8fafc]"
            onClick={onClose}
            type="button"
          >
            关闭
          </button>
        )}
      </div>

      {!event && (
        <div className="mt-4 rounded-xl border border-dashed border-[#dadce0] bg-[#f8fafc] p-4">
          <p className="text-sm font-semibold text-[#202124]">请选择一个日程</p>
          <p className="mt-2 text-xs leading-5 text-[#5f6368]">
            在月视图、周视图或日视图中点击任意事件，可查看时间、地点、分类、提醒和 ICS 导出信息。
          </p>
        </div>
      )}

      {event && (
        <div className="mt-4 space-y-3">
          <div className="rounded-xl border border-blue-100 bg-blue-50 p-4">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <p className="break-words text-base font-semibold text-[#202124]">{event.title}</p>
                <p className="mt-1 text-xs text-[#5f6368]">ID #{event.id}</p>
              </div>
              <ReminderBadge event={event} />
            </div>
            <div className="mt-3 grid grid-cols-1 gap-2 sm:grid-cols-2">
              <button
                aria-label="导出当前日程 ICS 文件"
                className="rounded-lg border border-blue-200 bg-white px-3 py-2 text-xs font-semibold text-[#1a73e8] transition hover:bg-blue-50 disabled:cursor-not-allowed disabled:opacity-60"
                disabled={isExporting || isDeleting}
                onClick={handleExportIcs}
                type="button"
              >
                {isExporting ? '导出中...' : '导出 ICS'}
              </button>
              <button
                aria-label="删除当前日程"
                className="rounded-lg border border-rose-200 bg-white px-3 py-2 text-xs font-semibold text-rose-600 transition hover:bg-rose-50 disabled:cursor-not-allowed disabled:opacity-60"
                disabled={isExporting || isDeleting}
                onClick={handleDelete}
                type="button"
              >
                {isDeleting ? '删除中...' : '删除日程'}
              </button>
            </div>
            {actionError && (
              <p className="mt-3 rounded-lg border border-rose-200 bg-rose-50 p-3 text-xs leading-5 text-rose-700">
                {actionError}
              </p>
            )}
          </div>

          <DetailRow label="开始时间" value={formatDateTime(event.startTime)} />
          <DetailRow label="结束时间" value={formatDateTime(event.endTime)} />
          <DetailRow label="地点" value={event.location || '未设置'} />
          <div className="flex items-start justify-between gap-4 border-b border-[#e5e7eb] pb-3">
            <span className="text-xs font-semibold text-[#5f6368]">分类</span>
            <span className={`rounded-full border px-2.5 py-1 text-[11px] ${getCategoryBadgeClass(event.category)}`}>
              {getCategoryLabel(event.category)}
            </span>
          </div>
          <DetailRow label="提醒" value={formatReminder(event)} />
          {event.remindedAt && <DetailRow label="提醒时间" value={formatDateTime(event.remindedAt)} />}

          <div className="rounded-xl border border-[#e5e7eb] bg-[#f8fafc] p-3">
            <p className="text-xs font-semibold text-[#5f6368]">描述</p>
            <p className="mt-2 break-words text-sm leading-6 text-[#3c4043]">
              {event.description || '暂无描述'}
            </p>
          </div>
        </div>
      )}
    </section>
  )
}

function ReminderBadge({ event }: { event: CalendarEvent }) {
  if (event.reminderMinutes === null || event.reminderMinutes === undefined) {
    return <span className="shrink-0 rounded-full border border-slate-200 bg-white px-2.5 py-1 text-[11px] text-slate-500">无提醒</span>
  }

  return (
    <span
      className={`shrink-0 rounded-full border px-2.5 py-1 text-[11px] ${
        event.reminderTriggered
          ? 'border-emerald-100 bg-emerald-50 text-emerald-700'
          : 'border-amber-100 bg-amber-50 text-amber-700'
      }`}
    >
      {event.reminderTriggered ? '已提醒' : '未提醒'}
    </span>
  )
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-start justify-between gap-4 border-b border-[#e5e7eb] pb-3 last:border-b-0">
      <span className="text-xs font-semibold text-[#5f6368]">{label}</span>
      <span className="text-right text-sm leading-6 text-[#3c4043]">{value}</span>
    </div>
  )
}

function formatReminder(event: CalendarEvent) {
  if (event.reminderMinutes === null || event.reminderMinutes === undefined) {
    return '无提醒'
  }
  return `提前 ${event.reminderMinutes} 分钟，${event.reminderTriggered ? '已提醒' : '未提醒'}`
}

function formatDateTime(value: string) {
  return dateTimeFormatter.format(new Date(value))
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof Error) return error.message
  return fallback
}

export default EventDetailPanel
