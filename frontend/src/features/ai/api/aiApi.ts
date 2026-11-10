import { httpClient } from '@/shared/api/httpClient';
import { ApiResponse } from '@/shared/types/api';

export interface ChatbotCourseCard {
  id: string;
  title: string;
  subject: string;
  subjectLabel: string;
  grade?: number;
  price: number;
  originalPrice?: number;
  rating: number;
  reviewCount: number;
  totalLessons: number;
  thumbnailUrl?: string;
  detailUrl: string;
}

export interface ChatMessage {
  role: 'user' | 'model';
  text: string;
  intent?: string;
  courses?: ChatbotCourseCard[];
}

export interface ChatbotMessageRequest {
  conversationId?: string;
  message: string;
}

export interface ChatbotMessageResponse {
  conversationId: string;
  reply: string;
  intent: string;
  courses: ChatbotCourseCard[];
  suggestions: string[];
}

export interface AiChatRequest {
  message: string;
  context?: string;
  lessonId?: string;
  courseId?: string;
  history?: { role: string; text: string }[];
  conversationId?: string;
}

export interface AiChatResponse {
  reply: string;
  suggestions: string[];
  conversationId?: string;
  intent?: string;
  courses?: ChatbotCourseCard[];
}

export const aiApi = {
  /**
   * API Chatbot mới (Kiến trúc 4 giai đoạn, có quản lý ConversationContext và trả Course Card)
   */
  sendMessage: async (data: ChatbotMessageRequest): Promise<ChatbotMessageResponse> => {
    const res = await httpClient.post<ApiResponse<ChatbotMessageResponse>>('/chatbot/message', data);
    return res.data.data;
  },

  /**
   * API tương thích ngược với usecase học tập
   */
  chatWithAi: async (data: AiChatRequest): Promise<AiChatResponse> => {
    const res = await httpClient.post<ApiResponse<AiChatResponse>>('/ai/chat', data);
    return res.data.data;
  },
};
