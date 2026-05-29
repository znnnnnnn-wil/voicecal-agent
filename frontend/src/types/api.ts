export type ApiResponse<T> = {
  success?: boolean
  code?: string | number
  message?: string
  data?: T
  timestamp?: string
}
