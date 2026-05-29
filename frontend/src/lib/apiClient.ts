import type { ApiResponse } from '../types/api'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.trim() ?? ''

type RequestOptions = {
  method: 'GET' | 'POST'
  body?: unknown
}

export function apiGet<T>(path: string): Promise<T> {
  return request<T>(path, { method: 'GET' })
}

export function apiPost<TRequest, TResponse>(path: string, body: TRequest): Promise<TResponse> {
  return request<TResponse>(path, { method: 'POST', body })
}

async function request<T>(path: string, options: RequestOptions): Promise<T> {
  const response = await fetch(buildUrl(path), {
    method: options.method,
    headers: {
      Accept: 'application/json',
      ...(options.body === undefined ? {} : { 'Content-Type': 'application/json' }),
    },
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  }).catch(() => {
    throw new Error('无法连接后端服务，请确认 Spring Boot 已启动。')
  })

  const payload = await parseJson(response)
  const apiResponse = isRecord(payload) ? (payload as ApiResponse<T>) : undefined

  if (!response.ok) {
    throw new Error(apiResponse?.message || `请求失败，HTTP 状态码 ${response.status}`)
  }

  if (apiResponse && isApiResponse(apiResponse)) {
    if (apiResponse.success === false || isFailureCode(apiResponse.code)) {
      throw new Error(apiResponse.message || '后端返回业务错误')
    }
    return apiResponse.data as T
  }

  return payload as T
}

function buildUrl(path: string) {
  if (!API_BASE_URL) {
    return path
  }
  return `${API_BASE_URL.replace(/\/$/, '')}/${path.replace(/^\//, '')}`
}

async function parseJson(response: Response): Promise<unknown> {
  const text = await response.text()
  if (!text) {
    return undefined
  }

  try {
    return JSON.parse(text)
  } catch {
    throw new Error('后端响应不是有效 JSON')
  }
}

function isApiResponse<T>(value: ApiResponse<T>) {
  return value !== null && typeof value === 'object' && ('data' in value || 'success' in value || 'code' in value)
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === 'object'
}

function isFailureCode(code: ApiResponse<unknown>['code']) {
  if (code === undefined || code === null || code === 'OK' || code === 200 || code === '200') {
    return false
  }
  return true
}
