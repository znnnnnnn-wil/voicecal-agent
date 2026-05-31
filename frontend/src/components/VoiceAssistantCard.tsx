import { useEffect, useMemo, useState } from 'react'
import { useSpeechRecognition, type VoiceRecognitionPhase } from '../hooks/useSpeechRecognition'
import { useSpeechSynthesis } from '../hooks/useSpeechSynthesis'

type AssistantStatus = VoiceRecognitionPhase | 'sending' | 'done'

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

const autoSendAfterTranscription = true

const examples = [
  '明天下午三点提醒我提交项目代码',
  '我明天有什么安排？',
  '帮我查下周五下午有空吗？',
  '删除明天下午的会议',
  '导出本周日程为 ICS',
]

const statusMeta: Record<AssistantStatus, { label: string; hint: string; className: string }> = {
  idle: { label: '等待输入', hint: '点击麦克风开始说话', className: 'border-blue-100 bg-blue-50 text-blue-700' },
  recording: { label: '正在聆听', hint: '请说出你的日程安排', className: 'border-blue-100 bg-blue-50 text-blue-700' },
  speaking: { label: '检测到语音', hint: '正在录音，停顿后会自动识别', className: 'border-emerald-100 bg-emerald-50 text-emerald-700' },
  'silence-detected': { label: '检测到停顿', hint: '检测到你说完了，正在识别', className: 'border-amber-100 bg-amber-50 text-amber-700' },
  transcribing: { label: '正在识别文字', hint: '正在调用 qwen3-asr-flash', className: 'border-amber-100 bg-amber-50 text-amber-700' },
  recognized: { label: '识别完成', hint: '识别结果已填入输入框', className: 'border-sky-100 bg-sky-50 text-sky-700' },
  sending: { label: '正在执行', hint: '正在为你执行日历操作', className: 'border-amber-100 bg-amber-50 text-amber-700' },
  done: { label: '已完成', hint: '日历数据已刷新', className: 'border-emerald-100 bg-emerald-50 text-emerald-700' },
  error: { label: '失败', hint: '请重试或改用文本输入', className: 'border-rose-100 bg-rose-50 text-rose-700' },
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
    phase,
    volume,
    recordingDurationMs,
    transcribeDurationMs,
    startListening,
    stopListening,
    resetTranscript,
  } = useSpeechRecognition()
  const { isSupported: isSpeechSupported, speak } = useSpeechSynthesis()
  const [lastSpokenReply, setLastSpokenReply] = useState('')
  const [lastSubmittedTranscript, setLastSubmittedTranscript] = useState('')
  const [lastAppliedTranscript, setLastAppliedTranscript] = useState('')

  const status = useMemo<AssistantStatus>(() => {
    if (isLoading) return 'sending'
    if (error || recognitionError) return 'error'
    if (isSuccess) return 'done'
    return phase
  }, [error, isLoading, isSuccess, phase, recognitionError])

  const meta = statusMeta[status]

  useEffect(() => {
    if (!transcript || transcript === lastAppliedTranscript) {
      return
    }
    setLastAppliedTranscript(transcript)
    onCommandChange(transcript)
    onLog('Voice transcript captured', transcript, 'success')
    if (autoSendAfterTranscription && transcript !== lastSubmittedTranscript && !isLoading) {
      setLastSubmittedTranscript(transcript)
      onRunCommand(transcript)
    }
  }, [isLoading, lastAppliedTranscript, lastSubmittedTranscript, onCommandChange, onLog, onRunCommand, transcript])

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
    setLastAppliedTranscript('')
    onLog('Voice recording started', 'Recording audio with VAD', 'info')
    startListening()
  }

  return (
    <section className="rounded-2xl border border-[#dadce0] bg-white p-4 shadow-sm">
      <div className="flex items-center justify-between gap-3">
        <div>
          <p className="text-sm font-semibold text-[#202124]">语音助手</p>
          <p className="mt-1 text-xs text-[#5f6368]">说完自动识别并执行</p>
        </div>
        <span className={`rounded-full border px-2.5 py-1 text-xs font-medium ${meta.className}`}>
          {meta.label}
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
          disabled={!isRecognitionSupported || isLoading}
          onClick={handleMicClick}
          type="button"
        >
          {isListening && <span className="absolute inset-0 animate-ping rounded-full bg-rose-200/50" />}
          <span className="relative">●</span>
        </button>
        <p className="mt-3 text-xs text-[#5f6368]">
          {isRecognitionSupported ? meta.hint : '当前浏览器不支持录音或 Web Audio，请使用文本输入'}
        </p>
        <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-slate-200">
          <div
            className="h-full rounded-full bg-[#1a73e8] transition-all"
            style={{ width: `${Math.min(100, Math.round(volume * 600))}%` }}
          />
        </div>
        <p className="mt-2 text-[11px] text-[#5f6368]">
          录音 {Math.round(recordingDurationMs / 1000)}s
          {transcribeDurationMs > 0 ? ` · 识别 ${transcribeDurationMs}ms` : ''}
        </p>
      </div>

      {(recognitionError || error) && (
        <div className="mt-3 rounded-lg border border-rose-200 bg-rose-50 p-3 text-xs leading-5 text-rose-700">
          {recognitionError || error}
        </div>
      )}

      {transcript && (
        <div className="mt-3 rounded-lg border border-blue-100 bg-blue-50 p-3">
          <p className="text-xs font-semibold text-blue-700">识别结果</p>
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
            setLastAppliedTranscript('')
            setLastSubmittedTranscript('')
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
              className="max-w-full rounded-full border border-[#dadce0] bg-white px-3 py-1.5 text-left text-xs leading-5 text-[#3c4043] transition hover:border-blue-200 hover:bg-blue-50 hover:text-[#1a73e8]"
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
