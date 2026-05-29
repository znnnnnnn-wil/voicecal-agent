const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.trim() ?? ''

export async function downloadEventIcs(eventId: number): Promise<void> {
  const response = await fetch(buildUrl(`/api/calendar/events/${eventId}/ics`), {
    headers: {
      Accept: 'text/calendar',
    },
  }).catch(() => {
    throw new Error('无法连接后端服务，请确认 Spring Boot 已启动。')
  })

  if (!response.ok) {
    throw new Error(await readErrorMessage(response))
  }

  const blob = await response.blob()
  const objectUrl = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = objectUrl
  anchor.download = getFileName(response, eventId)
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(objectUrl)
}

function buildUrl(path: string) {
  if (!API_BASE_URL) {
    return path
  }
  return `${API_BASE_URL.replace(/\/$/, '')}/${path.replace(/^\//, '')}`
}

async function readErrorMessage(response: Response) {
  const fallback = `导出 ICS 失败，HTTP 状态码 ${response.status}`
  const text = await response.text()
  if (!text) {
    return fallback
  }

  try {
    const payload = JSON.parse(text) as { message?: string }
    return payload.message || fallback
  } catch {
    return fallback
  }
}

function getFileName(response: Response, eventId: number) {
  const disposition = response.headers.get('Content-Disposition')
  const match = disposition?.match(/filename="?([^"]+)"?/)
  return match?.[1] || `voicecal-event-${eventId}.ics`
}
