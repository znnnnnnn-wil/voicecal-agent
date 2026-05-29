import { apiPost } from '../lib/apiClient'
import type { AiChatRequest, AiChatResponse } from '../types/ai'

export function chatWithAi(message: string) {
  return apiPost<AiChatRequest, AiChatResponse>('/api/ai/chat', { message })
}
