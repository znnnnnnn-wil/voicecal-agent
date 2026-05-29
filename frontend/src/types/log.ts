export type VoiceCommandLog = {
  id: number
  conversationId: string
  rawText: string
  assistantReply: string | null
  intent?: string | null
  toolName?: string | null
  toolArgsJson?: string | null
  toolResultJson?: string | null
  success: boolean
  createdAt: string
}
