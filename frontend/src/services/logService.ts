import { apiGet } from '../lib/apiClient'
import type { VoiceCommandLog } from '../types/log'

export function getRecentLogs(limit = 20) {
  return apiGet<VoiceCommandLog[]>(`/api/logs/recent?limit=${limit}`)
}
