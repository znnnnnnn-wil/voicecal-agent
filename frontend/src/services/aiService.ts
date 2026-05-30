import { apiGet, apiPost, apiPostForm } from '../lib/apiClient'
import type { AiChatRequest, AiChatResponse, DailySummary, SpeechTranscriptionResponse } from '../types/ai'

const DEFAULT_TIMEZONE = 'Asia/Shanghai'

export function chatWithAi(message: string) {
  return apiPost<AiChatRequest, AiChatResponse>('/api/ai/chat', { message })
}

export function transcribeAudio(audio: Blob) {
  const formData = new FormData()
  formData.append('audio', audio, 'voicecal-command.webm')
  return apiPostForm<SpeechTranscriptionResponse>('/api/ai/speech/transcriptions', formData)
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
