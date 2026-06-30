export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface ErrorResponse {
  status: number;
  message: string;
  timestamp: string;
  path: string;
}
