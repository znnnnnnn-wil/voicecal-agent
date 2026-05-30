import type { CalendarEventCategory } from '../types/calendar'

const CATEGORY_LABELS: Record<CalendarEventCategory, string> = {
  WORK: '工作',
  STUDY: '学习',
  LIFE: '生活',
  MEETING: '会议',
  INTERVIEW: '面试',
  OTHER: '其他',
}

const CATEGORY_BADGE_CLASS: Record<CalendarEventCategory, string> = {
  WORK: 'border-blue-100 bg-blue-50 text-blue-700',
  STUDY: 'border-emerald-100 bg-emerald-50 text-emerald-700',
  LIFE: 'border-rose-100 bg-rose-50 text-rose-700',
  MEETING: 'border-amber-100 bg-amber-50 text-amber-700',
  INTERVIEW: 'border-violet-100 bg-violet-50 text-violet-700',
  OTHER: 'border-slate-200 bg-slate-50 text-slate-600',
}

const CATEGORY_EVENT_COLOR: Record<CalendarEventCategory, { backgroundColor: string; borderColor: string; textColor: string }> = {
  WORK: { backgroundColor: '#e8f0fe', borderColor: '#1a73e8', textColor: '#174ea6' },
  STUDY: { backgroundColor: '#e6f4ea', borderColor: '#34a853', textColor: '#137333' },
  LIFE: { backgroundColor: '#fce8e6', borderColor: '#ea4335', textColor: '#a50e0e' },
  MEETING: { backgroundColor: '#fef7e0', borderColor: '#fbbc04', textColor: '#8a5a00' },
  INTERVIEW: { backgroundColor: '#f3e8ff', borderColor: '#9333ea', textColor: '#6b21a8' },
  OTHER: { backgroundColor: '#f1f3f4', borderColor: '#9aa0a6', textColor: '#3c4043' },
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
