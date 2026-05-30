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
  '我周五下午有空吗？',
  '删除明天下午的会议',
  '确认',
  '取消',
]

const waveBars = ['h-5', 'h-9', 'h-14', 'h-8', 'h-12', 'h-6', 'h-10']

const statusMeta: Record<AssistantStatus, { label: string; className: string }> = {
  idle: {
    label: '待命',
    className: 'border-cyan-300/20 bg-cyan-300/10 text-cyan-100',
  },
  listening: {
    label: 'Listening...',
    className: 'border-rose-300/25 bg-rose-300/10 text-rose-100',
  },
  transcribed: {
    label: '已识别',
    className: 'border-sky-300/25 bg-sky-300/10 text-sky-100',
  },
  sending: {
    label: '发送中',
    className: 'border-amber-200/25 bg-amber-200/10 text-amber-100',
  },
  replied: {
    label: '已回复',
    className: 'border-emerald-300/25 bg-emerald-300/10 text-emerald-100',
  },
  error: {
    label: '需处理',
    className: 'border-rose-300/25 bg-rose-300/10 text-rose-100',
  },
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
  const {
    isSupported: isSpeechSupported,
    isSpeaking,
    speak,
    stop,
  } = useSpeechSynthesis()
  const [lastSpokenReply, setLastSpokenReply] = useState('')
  const [lastSubmittedTranscript, setLastSubmittedTranscript] = useState('')

  const status = useMemo<AssistantStatus>(() => {
    if (isLoading) {
      return 'sending'
    }
    if (error || recognitionError) {
      return 'error'
    }
    if (isListening) {
      return 'listening'
    }
    if (transcript) {
      return isSuccess ? 'replied' : 'transcribed'
    }
    if (isSuccess) {
      return 'replied'
    }
    return 'idle'
  }, [error, isListening, isLoading, isSuccess, recognitionError, transcript])

  const meta = statusMeta[status]

  useEffect(() => {
    if (transcript) {
      onCommandChange(transcript)
      onLog('Voice transcript captured', transcript, 'success')
      if (transcript !== lastSubmittedTranscript && !isLoading) {
        setLastSubmittedTranscript(transcript)
        onRunCommand(transcript)
      }
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

  const handleReplay = () => {
    speak(reply)
    onLog('Speech playback started', 'Replay requested for the latest AI reply', 'info')
  }

  return (
    <section className="h-fit self-start rounded-[32px] border border-white/10 bg-white/[0.08] p-5 shadow-2xl shadow-black/30 backdrop-blur-2xl sm:p-6">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <p className="text-sm font-semibold text-cyan-100">VoiceCal Assistant</p>
          <h1 className="mt-2 text-3xl font-semibold leading-tight text-white sm:text-4xl">
            说出或输入你的日程指令
          </h1>
          <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-400">
            Use voice or text to manage your calendar with AI tool calling. Conflict detection,
            free-time search, reminders, logs, and ICS export are available.
          </p>
        </div>
        <span className={`w-fit rounded-full border px-3 py-1 text-xs font-medium ${meta.className}`}>
          {meta.label}
        </span>
      </div>

      <div className="mt-6 grid gap-4 xl:grid-cols-[0.75fr_1.25fr]">
        <div className="rounded-3xl border border-white/10 bg-[#0d131a]/80 p-4 shadow-inner shadow-white/[0.02]">
          <div className="flex items-center justify-between gap-4">
            <div>
              <p className="text-sm font-semibold text-white">语音录入</p>
              <p className="mt-1 text-xs text-slate-400">
                {isRecognitionSupported
                  ? '点击麦克风开始识别'
                  : 'Voice recognition is not supported in this browser.'}
              </p>
            </div>
            <button
              aria-label={isListening ? '停止语音识别' : '开始语音识别'}
              className={`relative grid size-16 place-items-center rounded-full border text-2xl transition focus:outline-none focus:ring-2 focus:ring-cyan-200 focus:ring-offset-2 focus:ring-offset-[#0d131a] disabled:cursor-not-allowed disabled:opacity-50 ${
                isListening
                  ? 'border-rose-200/40 bg-rose-300/20 text-rose-50 shadow-xl shadow-rose-950/40'
                  : 'border-cyan-200/30 bg-cyan-300/15 text-cyan-50 shadow-lg shadow-cyan-950/30 hover:bg-cyan-300/25'
              }`}
              disabled={!isRecognitionSupported}
              onClick={handleMicClick}
              type="button"
            >
              {isListening && <span className="absolute inset-0 animate-ping rounded-full bg-rose-300/20" />}
              <span className="relative grid size-7 place-items-center rounded-full bg-white/10 text-base">●</span>
            </button>
          </div>

          <div className="mt-5 flex h-28 items-center justify-center rounded-3xl border border-cyan-300/10 bg-cyan-300/[0.06]">
            <div className="flex items-center gap-2">
              {waveBars.map((height, index) => (
                <span
                  className={`${height} w-2 rounded-full bg-gradient-to-t from-cyan-400 to-emerald-200 opacity-80 ${
                    isListening ? 'animate-pulse' : ''
                  }`}
                  key={`${height}-${index}`}
                />
              ))}
            </div>
          </div>

          {!isRecognitionSupported && (
            <div className="mt-4 rounded-2xl border border-amber-200/20 bg-amber-200/10 p-4 text-sm leading-6 text-amber-50">
              Voice recognition is not supported in this browser. You can still type your command.
            </div>
          )}

          {recognitionError && (
            <div className="mt-4 rounded-2xl border border-rose-300/20 bg-rose-300/10 p-4 text-sm leading-6 text-rose-50">
              {recognitionError}
            </div>
          )}

          {transcript && (
            <div className="mt-4 rounded-2xl border border-sky-300/20 bg-sky-300/10 p-4">
              <p className="text-xs font-semibold text-sky-100">识别文本</p>
              <p className="mt-2 text-sm leading-6 text-slate-100">{transcript}</p>
            </div>
          )}
        </div>

        <div className="rounded-3xl border border-white/10 bg-[#0d131a]/80 p-4 shadow-inner shadow-white/[0.02]">
          <textarea
            className="min-h-32 w-full resize-none rounded-2xl border border-white/10 bg-white/[0.06] px-4 py-4 text-sm leading-6 text-slate-100 outline-none transition placeholder:text-slate-500 focus:border-cyan-200/50 focus:bg-white/[0.08] focus:ring-4 focus:ring-cyan-300/10"
            onChange={(event) => onCommandChange(event.target.value)}
            placeholder="我周五下午有空吗？"
            value={command}
          />

          <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex flex-wrap items-center gap-3">
              <button
                aria-label="发送指令给 AI"
                className="rounded-full bg-white px-5 py-3 text-sm font-semibold text-slate-950 shadow-xl shadow-cyan-950/30 transition hover:-translate-y-0.5 hover:bg-cyan-100 focus:outline-none focus:ring-2 focus:ring-cyan-200 focus:ring-offset-2 focus:ring-offset-[#0d131a] disabled:cursor-not-allowed disabled:opacity-70 disabled:hover:translate-y-0"
                disabled={isLoading}
                onClick={() => onRunCommand()}
                type="button"
              >
                {isLoading ? '发送中...' : '发送给 AI'}
              </button>
              <button
                aria-label="清空当前指令"
                className="rounded-full border border-white/15 bg-white/[0.06] px-4 py-3 text-sm font-semibold text-white transition hover:border-white/25 hover:bg-white/[0.1] focus:outline-none focus:ring-2 focus:ring-white/30 focus:ring-offset-2 focus:ring-offset-[#0d131a]"
                onClick={() => {
                  resetTranscript()
                  onCommandChange('')
                }}
                type="button"
              >
                清空
              </button>
            </div>
            <p className="text-xs text-slate-500">发送前可编辑识别文本</p>
          </div>

          <div className="mt-5 rounded-2xl border border-white/10 bg-white/[0.035] p-3">
            <div className="mb-3 flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
              <p className="text-xs font-semibold text-white">Try one of these demo commands.</p>
              <p className="text-[11px] text-amber-100/80">Delete and update actions require confirmation.</p>
            </div>
            <div className="flex flex-wrap gap-2">
            {examples.map((example) => (
              <button
                className="rounded-full border border-white/10 bg-white/[0.06] px-3 py-2 text-xs font-medium text-slate-200 transition hover:border-cyan-200/30 hover:bg-cyan-300/10 hover:text-cyan-50 focus:outline-none focus:ring-2 focus:ring-cyan-200/40 focus:ring-offset-2 focus:ring-offset-[#0d131a]"
                key={example}
                onClick={() => onCommandChange(example)}
                type="button"
              >
                {example}
              </button>
            ))}
            </div>
          </div>

          {error && (
            <div className="mt-4 rounded-2xl border border-rose-300/20 bg-rose-300/10 p-4 text-sm leading-6 text-rose-50">
              {error}
            </div>
          )}
        </div>
      </div>

      <div className="mt-5 rounded-3xl border border-white/10 bg-[#0d131a]/70 p-5">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-sm font-semibold text-white">AI 回复</p>
            <p className="mt-1 text-xs text-slate-400">
              {isSpeechSupported ? '回复会自动播报，可随时重播或停止' : 'Speech synthesis is not supported in this browser.'}
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            <button
              aria-label="重新播报 AI 回复"
              className="rounded-full border border-cyan-200/20 bg-cyan-300/10 px-4 py-2 text-xs font-semibold text-cyan-50 transition hover:bg-cyan-300/15 disabled:cursor-not-allowed disabled:opacity-50"
              disabled={!isSpeechSupported || isLoading || !reply.trim()}
              onClick={handleReplay}
              type="button"
            >
              重新播报
            </button>
            <button
              aria-label="停止语音播报"
              className="rounded-full border border-white/15 bg-white/[0.06] px-4 py-2 text-xs font-semibold text-white transition hover:bg-white/[0.1] disabled:cursor-not-allowed disabled:opacity-50"
              disabled={!isSpeechSupported || !isSpeaking}
              onClick={stop}
              type="button"
            >
              停止播报
            </button>
          </div>
        </div>

        <div className="mt-4 min-h-32 rounded-2xl border border-white/10 bg-white/[0.04] p-4">
          {isLoading ? (
            <div className="space-y-3">
              <div className="h-3 w-4/5 animate-pulse rounded-full bg-white/15" />
              <div className="h-3 w-2/3 animate-pulse rounded-full bg-white/10" />
              <div className="h-3 w-1/2 animate-pulse rounded-full bg-white/10" />
              <p className="pt-2 text-xs text-slate-500">正在等待后端 AI 接口响应...</p>
            </div>
          ) : (
            <p className="whitespace-pre-wrap text-sm leading-7 text-slate-200">{reply}</p>
          )}
        </div>
      </div>
    </section>
  )
}

export default VoiceAssistantCard
