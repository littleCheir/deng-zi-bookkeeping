export interface CurrentUser {
  id: number
  username: string
}

export interface LoginPayload {
  username: string
  password: string
}

export interface CsrfInfo {
  headerName: string
  parameterName: string
}
