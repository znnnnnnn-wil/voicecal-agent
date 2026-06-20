const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.trim() ?? ''

export async function downloadEventIcs(eventId: number): Promise<void> {
  await downloadIcs(`/api/calendar/events/${eventId}/ics`, `voicecal-event-${eventId}.ics`)
}

export async function downloadCalendarIcs(path: string): Promise<void> {
  await downloadIcs(path, 'voicecal-events.ics')
}

async function downloadIcs(path: string, fallbackFileName: string): Promise<void> {
  const response = await fetch(buildUrl(path), {
    headers: { Accept: 'text/calendar, application/json' },
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
  anchor.download = getFileName(response, fallbackFileName)
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

function getFileName(response: Response, fallbackFileName: string) {
  const disposition = response.headers.get('Content-Disposition')
  const match = disposition?.match(/filename="?([^"]+)"?/)
  return match?.[1] || fallbackFileName
}
