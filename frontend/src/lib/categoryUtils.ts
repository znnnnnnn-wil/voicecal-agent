import type { CalendarEventCategory } from '../types/calendar'

const CATEGORY_LABELS: Record<CalendarEventCategory, string> = {
  WORK: 'Work',
  STUDY: 'Study',
  LIFE: 'Life',
  MEETING: 'Meeting',
  INTERVIEW: 'Interview',
  OTHER: 'Other',
}

const CATEGORY_BADGE_CLASS: Record<CalendarEventCategory, string> = {
  WORK: 'border-sky-300/25 bg-sky-300/10 text-sky-100',
  STUDY: 'border-violet-300/25 bg-violet-300/10 text-violet-100',
  LIFE: 'border-emerald-300/25 bg-emerald-300/10 text-emerald-100',
  MEETING: 'border-cyan-300/25 bg-cyan-300/10 text-cyan-100',
  INTERVIEW: 'border-amber-200/25 bg-amber-200/10 text-amber-100',
  OTHER: 'border-white/10 bg-white/[0.06] text-slate-300',
}

const CATEGORY_EVENT_COLOR: Record<CalendarEventCategory, { backgroundColor: string; borderColor: string }> = {
  WORK: { backgroundColor: '#2563eb', borderColor: '#93c5fd' },
  STUDY: { backgroundColor: '#7c3aed', borderColor: '#c4b5fd' },
  LIFE: { backgroundColor: '#059669', borderColor: '#6ee7b7' },
  MEETING: { backgroundColor: '#0891b2', borderColor: '#67e8f9' },
  INTERVIEW: { backgroundColor: '#d97706', borderColor: '#fcd34d' },
  OTHER: { backgroundColor: '#475569', borderColor: '#cbd5e1' },
}

export function normalizeCategory(category?: string | null): CalendarEventCategory {
  if (
    category === 'WORK' ||
    category === 'STUDY' ||
    category === 'LIFE' ||
    category === 'MEETING' ||
    category === 'INTERVIEW' ||
    category === 'OTHER'
  ) {
    return category
  }
  return 'OTHER'
}

export function getCategoryLabel(category?: string | null) {
  return CATEGORY_LABELS[normalizeCategory(category)]
}

export function getCategoryBadgeClass(category?: string | null) {
  return CATEGORY_BADGE_CLASS[normalizeCategory(category)]
}

export function getCategoryEventColor(category?: string | null) {
  return CATEGORY_EVENT_COLOR[normalizeCategory(category)]
}
