import { useCallback, useEffect, useMemo, useState } from 'react'

export function useSpeechSynthesis() {
  const synthesis = useMemo(() => {
    if (typeof window === 'undefined') {
      return undefined
    }
    return window.speechSynthesis
  }, [])
  const [isSpeaking, setIsSpeaking] = useState(false)

  const stop = useCallback(() => {
    synthesis?.cancel()
    setIsSpeaking(false)
  }, [synthesis])

  const speak = useCallback(
    (text: string) => {
      if (!synthesis || !text.trim()) {
        return
      }

      synthesis.cancel()
      const utterance = new SpeechSynthesisUtterance(text)
      utterance.lang = 'zh-CN'
      utterance.rate = 1
      utterance.pitch = 1
      utterance.onstart = () => setIsSpeaking(true)
      utterance.onend = () => setIsSpeaking(false)
      utterance.onerror = () => setIsSpeaking(false)
      synthesis.speak(utterance)
    },
    [synthesis],
  )

  useEffect(() => stop, [stop])

  return {
    isSupported: Boolean(synthesis),
    isSpeaking,
    speak,
    stop,
  }
}
