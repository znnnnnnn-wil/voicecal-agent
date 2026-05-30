import { useEffect, useMemo, useState } from 'react'
import { useSpeechRecognition } from '../hooks/useSpeechRecognition'
import { useSpeechSynthesis } from '../hooks/useSpeechSynthesis'

type AssistantStatus = 'idle' | 'listening' | 'transcribed' | 'sending' | 'replied' | 'error'

type VoiceAssistantCardProps = {
  command: string
  error: string | null
  isLoading: boolean
  isSuccess: boolean
  reply: string
  onCommandChange: (value: string) => void
  onRunCommand: (command?: string) => void
  onLog: (label: string, detail: string, status: 'success' | 'info' | 'pending') => void
}

const examples = [
  '明天下午三点提醒我提交项目代码',
  '我明天有什么安排？',
  '帮我查下周五下午有空吗？',
  '删除明天下午的会议',
  '导出本周日程为 ICS',
]

const statusMeta: Record<AssistantStatus, { label: string; className: string }> = {
  idle: { label: '待命', className: 'border-blue-100 bg-blue-50 text-blue-700' },
  listening: { label: '录音中', className: 'border-rose-100 bg-rose-50 text-rose-700' },
  transcribed: { label: '已识别', className: 'border-sky-100 bg-sky-50 text-sky-700' },
  sending: { label: '发送中', className: 'border-amber-100 bg-amber-50 text-amber-700' },
  replied: { label: '已回复', className: 'border-emerald-100 bg-emerald-50 text-emerald-700' },
  error: { label: '需处理', className: 'border-rose-100 bg-rose-50 text-rose-700' },
}

function VoiceAssistantCard({
  command,
  error,
  isLoading,
  isSuccess,
  reply,
  onCommandChange,
  onRunCommand,
  onLog,
}: VoiceAssistantCardProps) {
  const {
    isSupported: isRecognitionSupported,
    isListening,
    transcript,
    error: recognitionError,
    startListening,
    stopListening,
    resetTranscript,
  } = useSpeechRecognition()
  const { isSupported: isSpeechSupported, speak } = useSpeechSynthesis()
  const [lastSpokenReply, setLastSpokenReply] = useState('')
  const [lastSubmittedTranscript, setLastSubmittedTranscript] = useState('')

  const status = useMemo<AssistantStatus>(() => {
    if (isLoading) return 'sending'
    if (error || recognitionError) return 'error'
    if (isListening) return 'listening'
    if (transcript) return isSuccess ? 'replied' : 'transcribed'
    if (isSuccess) return 'replied'
    return 'idle'
  }, [error, isListening, isLoading, isSuccess, recognitionError, transcript])

  useEffect(() => {
    if (!transcript) {
      return
    }
    onCommandChange(transcript)
    onLog('Voice transcript captured', transcript, 'success')
    if (transcript !== lastSubmittedTranscript && !isLoading) {
      setLastSubmittedTranscript(transcript)
      onRunCommand(transcript)
    }
  }, [isLoading, lastSubmittedTranscript, onCommandChange, onLog, onRunCommand, transcript])

  useEffect(() => {
    if (isSuccess && reply && isSpeechSupported && reply !== lastSpokenReply) {
      speak(reply)
      setLastSpokenReply(reply)
      onLog('Speech playback started', 'AI reply is being read aloud', 'info')
    }
  }, [isSpeechSupported, isSuccess, lastSpokenReply, onLog, reply, speak])

  const handleMicClick = () => {
    if (isListening) {
      stopListening()
      return
    }
    resetTranscript()
    setLastSubmittedTranscript('')
    onLog('Voice recording started', 'Recording audio for server-side transcription', 'info')
    startListening()
  }

  return (
    <section className="rounded-2xl border border-[#dadce0] bg-white p-4 shadow-sm">
      <div className="flex items-center justify-between gap-3">
        <div>
          <p className="text-sm font-semibold text-[#202124]">语音助手</p>
          <p className="mt-1 text-xs text-[#5f6368]">录音完成后自动发送给 AI</p>
        </div>
        <span className={`rounded-full border px-2.5 py-1 text-xs font-medium ${statusMeta[status].className}`}>
          {statusMeta[status].label}
        </span>
      </div>

      <div className="mt-5 flex flex-col items-center rounded-xl border border-[#e5e7eb] bg-[#f8fafc] px-4 py-5 text-center">
        <button
          aria-label={isListening ? '停止语音识别' : '开始语音识别'}
          className={`relative grid size-16 place-items-center rounded-full border text-2xl transition focus:outline-none focus:ring-2 focus:ring-[#1a73e8] focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${
            isListening
              ? 'border-rose-200 bg-rose-50 text-rose-600 shadow-sm'
              : 'border-blue-100 bg-white text-[#1a73e8] shadow-sm hover:bg-blue-50'
          }`}
          disabled={!isRecognitionSupported}
          onClick={handleMicClick}
          type="button"
        >
          {isListening && <span className="absolute inset-0 animate-ping rounded-full bg-rose-200/50" />}
          <span className="relative">●</span>
        </button>
        <p className="mt-3 text-xs text-[#5f6368]">
          {isRecognitionSupported ? '点击麦克风开始说话' : '当前浏览器不支持语音识别，请使用文本输入'}
        </p>
      </div>

      {(recognitionError || error) && (
        <div className="mt-3 rounded-lg border border-rose-200 bg-rose-50 p-3 text-xs leading-5 text-rose-700">
          {recognitionError || error}
        </div>
      )}

      {transcript && (
        <div className="mt-3 rounded-lg border border-blue-100 bg-blue-50 p-3">
          <p className="text-xs font-semibold text-blue-700">识别文本</p>
          <p className="mt-1 text-sm leading-6 text-[#202124]">{transcript}</p>
        </div>
      )}

      <textarea
        className="mt-4 min-h-28 w-full resize-none rounded-xl border border-[#dadce0] bg-white px-3 py-3 text-sm leading-6 text-[#202124] outline-none transition placeholder:text-[#9aa0a6] focus:border-[#1a73e8] focus:ring-3 focus:ring-blue-100"
        onChange={(event) => onCommandChange(event.target.value)}
        placeholder="输入日程指令，例如：明天下午三点提醒我提交项目代码"
        value={command}
      />

      <div className="mt-3 flex gap-2">
        <button
          className="flex-1 rounded-lg bg-[#1a73e8] px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-[#1765cc] disabled:cursor-not-allowed disabled:opacity-70"
          disabled={isLoading}
          onClick={() => onRunCommand()}
          type="button"
        >
          {isLoading ? '发送中...' : '发送给 AI'}
        </button>
        <button
          className="rounded-lg border border-[#dadce0] bg-white px-3 py-2.5 text-sm font-medium text-[#3c4043] transition hover:bg-[#f8fafc]"
          onClick={() => {
            resetTranscript()
            onCommandChange('')
          }}
          type="button"
        >
          清空
        </button>
      </div>

      <div className="mt-4">
        <p className="mb-2 text-xs font-semibold text-[#5f6368]">示例指令</p>
        <div className="flex flex-wrap gap-2">
          {examples.map((example) => (
            <button
              className="rounded-full border border-[#dadce0] bg-white px-3 py-1.5 text-xs text-[#3c4043] transition hover:border-blue-200 hover:bg-blue-50 hover:text-[#1a73e8]"
              key={example}
              onClick={() => onCommandChange(example)}
              type="button"
            >
              {example}
            </button>
          ))}
        </div>
      </div>
    </section>
  )
}

export default VoiceAssistantCard
