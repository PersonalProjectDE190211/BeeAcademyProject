import { httpClient } from '@/shared/api/httpClient';
import { ApiResponse } from '@/shared/types/api';

export interface ChatMessage {
  role: 'user' | 'model';
  text: string;
}

export interface AiChatRequest {
  message: string;
  context?: string;
  lessonId?: string;
  courseId?: string;
  history?: ChatMessage[];
}

export interface AiChatResponse {
  reply: string;
  suggestions: string[];
}

export const aiApi = {
  chatWithAi: async (data: AiChatRequest): Promise<AiChatResponse> => {
    const res = await httpClient.post<ApiResponse<AiChatResponse>>('/ai/chat', data);
    return res.data.data;
  },
};

