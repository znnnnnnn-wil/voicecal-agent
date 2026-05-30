import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { transcribeAudio } from '../services/aiService'

const MAX_RECORDING_MS = 20_000
const MIME_TYPE_CANDIDATES = [
  'audio/webm;codecs=opus',
  'audio/webm',
  'audio/ogg;codecs=opus',
  'audio/ogg',
]

function selectMimeType() {
  if (typeof MediaRecorder === 'undefined') {
    return ''
  }
  return MIME_TYPE_CANDIDATES.find((mimeType) => MediaRecorder.isTypeSupported(mimeType)) ?? ''
}

export function useSpeechRecognition() {
  const isSupported = useMemo(() => {
    return typeof navigator !== 'undefined' && Boolean(navigator.mediaDevices?.getUserMedia) && typeof MediaRecorder !== 'undefined'
  }, [])
  const [isListening, setIsListening] = useState(false)
  const [transcript, setTranscript] = useState('')
  const [error, setError] = useState<string | null>(null)
  const chunksRef = useRef<BlobPart[]>([])
  const mediaRecorderRef = useRef<MediaRecorder | null>(null)
  const streamRef = useRef<MediaStream | null>(null)
  const stopTimerRef = useRef<number | undefined>(undefined)

  const releaseStream = useCallback(() => {
    streamRef.current?.getTracks().forEach((track) => track.stop())
    streamRef.current = null
  }, [])

  const stopListening = useCallback(() => {
    window.clearTimeout(stopTimerRef.current)
    const recorder = mediaRecorderRef.current
    if (recorder && recorder.state !== 'inactive') {
      recorder.stop()
    } else {
      releaseStream()
      setIsListening(false)
    }
  }, [releaseStream])

  const startListening = useCallback(async () => {
    if (!isSupported) {
      setError('当前浏览器不支持录音上传，请改用文本输入。')
      return
    }

    try {
      setError(null)
      setTranscript('')
      chunksRef.current = []
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
        },
      })
      streamRef.current = stream
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
        releaseStream()
        setIsListening(false)
      }

      recorder.onstop = async () => {
        window.clearTimeout(stopTimerRef.current)
        setIsListening(false)
        releaseStream()

        const audioBlob = new Blob(chunksRef.current, { type: recorder.mimeType || 'audio/webm' })
        chunksRef.current = []
        if (audioBlob.size === 0) {
          setError('没有录到有效语音，请靠近麦克风后重试。')
          return
        }

        try {
          const response = await transcribeAudio(audioBlob)
          setTranscript(response.text.trim())
        } catch (exception) {
          setError(exception instanceof Error ? exception.message : '语音识别失败，请改用文本输入。')
        }
      }

      recorder.start(250)
      setIsListening(true)
      stopTimerRef.current = window.setTimeout(() => {
        if (mediaRecorderRef.current?.state === 'recording') {
          mediaRecorderRef.current.stop()
        }
      }, MAX_RECORDING_MS)
    } catch (exception) {
      releaseStream()
      setIsListening(false)
      setError(exception instanceof Error ? exception.message : '无法启动麦克风，请检查浏览器权限。')
    }
  }, [isSupported, releaseStream])

  const resetTranscript = useCallback(() => {
    setTranscript('')
    setError(null)
  }, [])

  useEffect(() => {
    return () => {
      window.clearTimeout(stopTimerRef.current)
      if (mediaRecorderRef.current?.state === 'recording') {
        mediaRecorderRef.current.stop()
      }
      releaseStream()
    }
  }, [releaseStream])

  return {
    isSupported,
    isListening,
    transcript,
    error,
    startListening,
    stopListening,
    resetTranscript,
  }
}
