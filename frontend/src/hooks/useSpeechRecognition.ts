import { useCallback, useMemo, useRef, useState } from 'react'

const recognitionErrorMessages: Record<string, string> = {
  aborted: 'Voice recognition was stopped.',
  'audio-capture': 'No microphone was detected. You can still type your command.',
  'network': 'Voice recognition network request failed. Please try typing instead.',
  'not-allowed': 'Microphone permission was denied. You can still type your command.',
  'no-speech': 'No speech was detected. Please try again or type your command.',
  service_not_allowed: 'Voice recognition is not allowed in this browser.',
}

export function useSpeechRecognition() {
  const RecognitionConstructor = useMemo(() => {
    if (typeof window === 'undefined') {
      return undefined
    }
    return window.SpeechRecognition ?? window.webkitSpeechRecognition
  }, [])
  const [isListening, setIsListening] = useState(false)
  const [transcript, setTranscript] = useState('')
  const [error, setError] = useState<string | null>(null)
  const recognitionRef = useRef<SpeechRecognition | null>(null)

  const stopListening = useCallback(() => {
    recognitionRef.current?.stop()
    setIsListening(false)
  }, [])

  const startListening = useCallback(() => {
    if (!RecognitionConstructor) {
      setError('Voice recognition is not supported in this browser. You can still type your command.')
      return
    }

    try {
      recognitionRef.current?.abort()
      const recognition = new RecognitionConstructor()
      recognition.lang = 'zh-CN'
      recognition.continuous = false
      recognition.interimResults = true

      recognition.onstart = () => {
        setError(null)
        setIsListening(true)
      }

      recognition.onresult = (event) => {
        let nextTranscript = ''
        for (let index = event.resultIndex; index < event.results.length; index += 1) {
          nextTranscript += event.results[index][0]?.transcript ?? ''
        }
        setTranscript(nextTranscript.trim())
      }

      recognition.onerror = (event) => {
        setError(
          recognitionErrorMessages[event.error] ??
            'Voice recognition failed. Please try again or type your command.',
        )
        setIsListening(false)
      }

      recognition.onend = () => {
        setIsListening(false)
      }

      recognitionRef.current = recognition
      recognition.start()
    } catch {
      setIsListening(false)
      setError('Voice recognition could not start. Please try typing your command.')
    }
  }, [RecognitionConstructor])

  const resetTranscript = useCallback(() => {
    setTranscript('')
    setError(null)
  }, [])

  return {
    isSupported: Boolean(RecognitionConstructor),
    isListening,
    transcript,
    error,
    startListening,
    stopListening,
    resetTranscript,
  }
}
