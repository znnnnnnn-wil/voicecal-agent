import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { transcribeAudio } from '../services/aiService'

const VOICE_RECORDING_CONFIG = {
  silenceThreshold: 0.015,
  silenceDurationMs: 1000,
  minRecordingMs: 600,
  maxRecordingMs: 15_000,
  meterIntervalMs: 80,
}

const MIME_TYPE_CANDIDATES = [
  'audio/webm;codecs=opus',
  'audio/webm',
  'audio/ogg;codecs=opus',
  'audio/ogg',
]

export type VoiceRecognitionPhase =
  | 'idle'
  | 'recording'
  | 'speaking'
  | 'silence-detected'
  | 'transcribing'
  | 'recognized'
  | 'error'

function selectMimeType() {
  if (typeof MediaRecorder === 'undefined') {
    return ''
  }
  return MIME_TYPE_CANDIDATES.find((mimeType) => MediaRecorder.isTypeSupported(mimeType)) ?? ''
}

export function useSpeechRecognition() {
  const isSupported = useMemo(() => {
    return typeof navigator !== 'undefined'
      && Boolean(navigator.mediaDevices?.getUserMedia)
      && typeof MediaRecorder !== 'undefined'
      && typeof AudioContext !== 'undefined'
  }, [])
  const [isListening, setIsListening] = useState(false)
  const [transcript, setTranscript] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [phase, setPhase] = useState<VoiceRecognitionPhase>('idle')
  const [volume, setVolume] = useState(0)
  const [recordingDurationMs, setRecordingDurationMs] = useState(0)
  const [transcribeDurationMs, setTranscribeDurationMs] = useState(0)
  const chunksRef = useRef<BlobPart[]>([])
  const mediaRecorderRef = useRef<MediaRecorder | null>(null)
  const streamRef = useRef<MediaStream | null>(null)
  const audioContextRef = useRef<AudioContext | null>(null)
  const analyserRef = useRef<AnalyserNode | null>(null)
  const processorRef = useRef<ScriptProcessorNode | null>(null)
  const silentGainRef = useRef<GainNode | null>(null)
  const vadTimerRef = useRef<number | undefined>(undefined)
  const maxTimerRef = useRef<number | undefined>(undefined)
  const startedAtRef = useRef(0)
  const lastVoiceAtRef = useRef(0)
  const pcmChunksRef = useRef<Float32Array[]>([])
  const sampleRateRef = useRef(16_000)

  const cleanupVad = useCallback(() => {
    window.clearInterval(vadTimerRef.current)
    window.clearTimeout(maxTimerRef.current)
    vadTimerRef.current = undefined
    maxTimerRef.current = undefined
    processorRef.current?.disconnect()
    silentGainRef.current?.disconnect()
    processorRef.current = null
    silentGainRef.current = null
    analyserRef.current = null
    void audioContextRef.current?.close()
    audioContextRef.current = null
  }, [])

  const releaseStream = useCallback(() => {
    streamRef.current?.getTracks().forEach((track) => track.stop())
    streamRef.current = null
  }, [])

  const stopListening = useCallback(() => {
    const recorder = mediaRecorderRef.current
    if (recorder && recorder.state !== 'inactive') {
      recorder.stop()
    } else {
      cleanupVad()
      releaseStream()
      setIsListening(false)
      setPhase('idle')
    }
  }, [cleanupVad, releaseStream])

  const startVad = useCallback((stream: MediaStream) => {
    const AudioContextCtor = window.AudioContext
    const audioContext = new AudioContextCtor()
    const source = audioContext.createMediaStreamSource(stream)
    const analyser = audioContext.createAnalyser()
    const processor = audioContext.createScriptProcessor(4096, 1, 1)
    const silentGain = audioContext.createGain()
    analyser.fftSize = 1024
    silentGain.gain.value = 0
    source.connect(analyser)
    source.connect(processor)
    processor.connect(silentGain)
    silentGain.connect(audioContext.destination)
    audioContextRef.current = audioContext
    analyserRef.current = analyser
    processorRef.current = processor
    silentGainRef.current = silentGain
    sampleRateRef.current = audioContext.sampleRate
    const samples = new Uint8Array(analyser.fftSize)

    processor.onaudioprocess = (event) => {
      const input = event.inputBuffer.getChannelData(0)
      pcmChunksRef.current.push(new Float32Array(input))
    }

    vadTimerRef.current = window.setInterval(() => {
      analyser.getByteTimeDomainData(samples)
      let sum = 0
      for (const sample of samples) {
        const normalized = (sample - 128) / 128
        sum += normalized * normalized
      }
      const rms = Math.sqrt(sum / samples.length)
      setVolume(rms)
      const elapsed = Date.now() - startedAtRef.current
      setRecordingDurationMs(elapsed)
      if (rms >= VOICE_RECORDING_CONFIG.silenceThreshold) {
        lastVoiceAtRef.current = Date.now()
        setPhase('speaking')
        return
      }
      if (elapsed >= VOICE_RECORDING_CONFIG.minRecordingMs
          && Date.now() - lastVoiceAtRef.current >= VOICE_RECORDING_CONFIG.silenceDurationMs) {
        setPhase('silence-detected')
        stopListening()
      }
    }, VOICE_RECORDING_CONFIG.meterIntervalMs)
  }, [stopListening])

  const startListening = useCallback(async () => {
    if (!isSupported) {
      setError('当前浏览器不支持录音或 Web Audio 静音检测，请改用文本输入。')
      setPhase('error')
      return
    }

    try {
      setError(null)
      setTranscript('')
      setVolume(0)
      setRecordingDurationMs(0)
      setTranscribeDurationMs(0)
      chunksRef.current = []
      pcmChunksRef.current = []
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
        },
      })
      streamRef.current = stream
      startedAtRef.current = Date.now()
      lastVoiceAtRef.current = Date.now()
      const mimeType = selectMimeType()
      const recorder = new MediaRecorder(stream, mimeType ? { mimeType } : undefined)
      mediaRecorderRef.current = recorder

      recorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          chunksRef.current.push(event.data)
        }
      }

      recorder.onerror = () => {
        setError('录音失败，请检查麦克风权限后重试。')
        cleanupVad()
        releaseStream()
        setIsListening(false)
        setPhase('error')
      }

      recorder.onstop = async () => {
        setPhase('transcribing')
        cleanupVad()
        setIsListening(false)
        releaseStream()
        const audioBlob = pcmChunksRef.current.length > 0
          ? encodeWav(pcmChunksRef.current, sampleRateRef.current)
          : new Blob(chunksRef.current, { type: recorder.mimeType || 'audio/webm' })
        chunksRef.current = []
        pcmChunksRef.current = []
        if (audioBlob.size === 0) {
          setError('没有识别到有效语音，请重试或改用文本输入。')
          setPhase('error')
          return
        }

        try {
          const transcribeStartedAt = Date.now()
          const response = await transcribeAudio(audioBlob)
          setTranscribeDurationMs(response.durationMs ?? Date.now() - transcribeStartedAt)
          const text = response.text.trim()
          if (!text) {
            setError('没有识别到有效语音，请重试或改用文本输入。')
            setPhase('error')
            return
          }
          setTranscript(text)
          setPhase('recognized')
        } catch (exception) {
          setError(exception instanceof Error ? exception.message : '语音识别失败，请改用文本输入。')
          setPhase('error')
        }
      }

      recorder.start(250)
      setIsListening(true)
      setPhase('recording')
      startVad(stream)
      maxTimerRef.current = window.setTimeout(() => {
        if (mediaRecorderRef.current?.state === 'recording') {
          setPhase('silence-detected')
          mediaRecorderRef.current.stop()
        }
      }, VOICE_RECORDING_CONFIG.maxRecordingMs)
    } catch (exception) {
      cleanupVad()
      releaseStream()
      setIsListening(false)
      setPhase('error')
      setError(exception instanceof Error ? exception.message : '无法启动麦克风，请检查浏览器权限。')
    }
  }, [cleanupVad, isSupported, releaseStream, startVad])

  const resetTranscript = useCallback(() => {
    setTranscript('')
    setError(null)
    setPhase('idle')
    setVolume(0)
  }, [])

  useEffect(() => {
    return () => {
      cleanupVad()
      if (mediaRecorderRef.current?.state === 'recording') {
        mediaRecorderRef.current.stop()
      }
      releaseStream()
    }
  }, [cleanupVad, releaseStream])

  return {
    isSupported,
    isListening,
    transcript,
    error,
    phase,
    volume,
    recordingDurationMs,
    transcribeDurationMs,
    startListening,
    stopListening,
    resetTranscript,
  }
}

function encodeWav(chunks: Float32Array[], sampleRate: number) {
  const samples = mergeSamples(chunks)
  const buffer = new ArrayBuffer(44 + samples.length * 2)
  const view = new DataView(buffer)

  writeAscii(view, 0, 'RIFF')
  view.setUint32(4, 36 + samples.length * 2, true)
  writeAscii(view, 8, 'WAVE')
  writeAscii(view, 12, 'fmt ')
  view.setUint32(16, 16, true)
  view.setUint16(20, 1, true)
  view.setUint16(22, 1, true)
  view.setUint32(24, sampleRate, true)
  view.setUint32(28, sampleRate * 2, true)
  view.setUint16(32, 2, true)
  view.setUint16(34, 16, true)
  writeAscii(view, 36, 'data')
  view.setUint32(40, samples.length * 2, true)

  let offset = 44
  for (const sample of samples) {
    const clamped = Math.max(-1, Math.min(1, sample))
    view.setInt16(offset, clamped < 0 ? clamped * 0x8000 : clamped * 0x7fff, true)
    offset += 2
  }

  return new Blob([buffer], { type: 'audio/wav' })
}

function mergeSamples(chunks: Float32Array[]) {
  const totalLength = chunks.reduce((sum, chunk) => sum + chunk.length, 0)
  const result = new Float32Array(totalLength)
  let offset = 0
  chunks.forEach((chunk) => {
    result.set(chunk, offset)
    offset += chunk.length
  })
  return result
}

function writeAscii(view: DataView, offset: number, value: string) {
  for (let index = 0; index < value.length; index += 1) {
    view.setUint8(offset + index, value.charCodeAt(index))
  }
}
