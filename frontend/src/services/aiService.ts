import { apiGet, apiPost } from '../lib/apiClient'
import type { AiChatRequest, AiChatResponse, DailySummary } from '../types/ai'

const DEFAULT_TIMEZONE = 'Asia/Shanghai'

export function chatWithAi(message: string) {
  return apiPost<AiChatRequest, AiChatResponse>('/api/ai/chat', { message })
}

export function getDailySummary(date?: string, timezone = DEFAULT_TIMEZONE) {
  return apiGet<DailySummary>(`/api/ai/daily-summary${buildQuery({ date, timezone })}`)
}

function buildQuery(params: Record<string, string | undefined>) {
  const query = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value) {
      query.set(key, value)
    }
  })

  const queryString = query.toString()
  return queryString ? `?${queryString}` : ''
}
