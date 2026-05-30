import { useCallback, useEffect, useMemo, useState } from 'react'

function normalizeTextForSpeech(text: string) {
  return text
    .replace(/```[\s\S]*?```/g, '代码块已省略。')
    .replace(/[`*_>#-]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
}

function scoreVoice(voice: SpeechSynthesisVoice) {
  const name = voice.name.toLowerCase()
  const lang = voice.lang.toLowerCase()
  let score = 0

  if (lang === 'zh-cn') {
    score += 80
  } else if (lang.startsWith('zh')) {
    score += 50
  }
  if (name.includes('xiaoxiao') || name.includes('huihui') || name.includes('tingting')) {
    score += 30
  }
  if (name.includes('natural') || name.includes('neural') || name.includes('online')) {
    score += 20
  }
  if (name.includes('google') || name.includes('microsoft')) {
    score += 10
  }
  if (voice.localService) {
    score += 5
  }

  return score
}

function selectVoice(voices: SpeechSynthesisVoice[]) {
  return voices
    .filter((voice) => voice.lang.toLowerCase().startsWith('zh'))
    .sort((left, right) => scoreVoice(right) - scoreVoice(left))[0]
}

export function useSpeechSynthesis() {
  const synthesis = useMemo(() => {
    if (typeof window === 'undefined') {
      return undefined
    }
    return window.speechSynthesis
  }, [])
  const [isSpeaking, setIsSpeaking] = useState(false)
  const [voices, setVoices] = useState<SpeechSynthesisVoice[]>([])

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
      const utterance = new SpeechSynthesisUtterance(normalizeTextForSpeech(text))
      const preferredVoice = selectVoice(voices)
      utterance.lang = 'zh-CN'
      if (preferredVoice) {
        utterance.voice = preferredVoice
        utterance.lang = preferredVoice.lang
      }
      utterance.rate = 0.88
      utterance.pitch = 1.04
      utterance.volume = 0.95
      utterance.onstart = () => setIsSpeaking(true)
      utterance.onend = () => setIsSpeaking(false)
      utterance.onerror = () => setIsSpeaking(false)
      synthesis.speak(utterance)
    },
    [synthesis, voices],
  )

  useEffect(() => {
    if (!synthesis) {
      return undefined
    }

    const updateVoices = () => setVoices(synthesis.getVoices())
    updateVoices()
    synthesis.addEventListener('voiceschanged', updateVoices)
    return () => synthesis.removeEventListener('voiceschanged', updateVoices)
  }, [synthesis])

  useEffect(() => stop, [stop])

  return {
    isSupported: Boolean(synthesis),
    isSpeaking,
    speak,
    stop,
  }
}
