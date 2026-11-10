import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  MessageSquare,
  Send,
  X,
  RefreshCw,
  Maximize2,
  Minimize2,
  BookOpen,
  Star,
  ExternalLink,
  Sparkles,
} from 'lucide-react';
import { aiApi, ChatMessage } from '../api/aiApi';
import { LatexRenderer } from '@/shared/ui/LatexRenderer';

interface BeeBotAssistantProps {
  currentLessonContext?: string;
  lessonId?: string;
  courseId?: string;
}

export const BeeBotAssistant: React.FC<BeeBotAssistantProps> = ({
  currentLessonContext,
  lessonId,
  courseId,
}) => {
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const [isExpanded, setIsExpanded] = useState(false);
  const [inputMessage, setInputMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [conversationId, setConversationId] = useState<string | undefined>(undefined);

  const [suggestions, setSuggestions] = useState<string[]>([
    'Danh mục các khóa học hiện có',
    'Tư vấn khóa học môn Toán lớp 6',
    'Khóa học luyện thi vào lớp 10',
    'Bộ môn luyện thi tốt nghiệp THPT',
    'Học phí và hình thức thanh toán',
  ]);

  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      role: 'model',
      text: 'Chào Quý phụ huynh và các em học sinh. Tôi là BeeBot — Chuyên viên Tư vấn Tuyển sinh & Khóa học của BeeAcademy.\n\nTôi hỗ trợ giải đáp thông tin về các chương trình học từ lớp 6 đến lớp 12, định hướng chọn khóa học phù hợp theo mục tiêu và giải đáp thắc mắc về học phí, hình thức học tập.\n\nQuý phụ huynh hoặc học sinh vui lòng cho biết môn học hoặc khối lớp đang quan tâm.',
    },
  ]);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, isOpen]);

  const handleSendMessage = async (textToSend?: string) => {
    const text = (textToSend || inputMessage).trim();
    if (!text || loading) return;

    const newHistory: ChatMessage[] = [...messages, { role: 'user', text }];
    setMessages(newHistory);
    setInputMessage('');
    setLoading(true);

    try {
      const response = await aiApi.sendMessage({
        conversationId,
        message: text,
      });

      if (response.conversationId) {
        setConversationId(response.conversationId);
      }

      setMessages((prev) => [
        ...prev,
        {
          role: 'model',
          text: response.reply,
          courses: response.courses,
          intent: response.intent,
        },
      ]);

      if (response.suggestions && response.suggestions.length > 0) {
        setSuggestions(response.suggestions);
      }
    } catch {
      // Fallback nếu API chatbot/message gặp lỗi kết nối
      try {
        const fallbackRes = await aiApi.chatWithAi({
          message: text,
          context: currentLessonContext,
          lessonId,
          courseId,
          conversationId,
          history: newHistory.slice(-6).map((m) => ({ role: m.role, text: m.text })),
        });

        if (fallbackRes.conversationId) {
          setConversationId(fallbackRes.conversationId);
        }

        setMessages((prev) => [
          ...prev,
          {
            role: 'model',
            text: fallbackRes.reply,
            courses: fallbackRes.courses,
            intent: fallbackRes.intent,
          },
        ]);

        if (fallbackRes.suggestions && fallbackRes.suggestions.length > 0) {
          setSuggestions(fallbackRes.suggestions);
        }
      } catch {
        setMessages((prev) => [
          ...prev,
          {
            role: 'model',
            text: 'Hệ thống tư vấn đang bận xử lý hoặc kết nối bị gián đoạn. Quý khách vui lòng thử lại sau giây lát.',
          },
        ]);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const handleClearHistory = () => {
    setConversationId(undefined);
    setMessages([
      {
        role: 'model',
        text: 'Phiên tư vấn đã được làm mới. Quý phụ huynh hoặc học sinh vui lòng đặt câu hỏi để nhận thông tin hỗ trợ.',
      },
    ]);
    setSuggestions([
      'Danh mục các khóa học hiện có',
      'Tư vấn khóa học môn Toán lớp 6',
      'Khóa học luyện thi vào lớp 10',
      'Bộ môn luyện thi tốt nghiệp THPT',
      'Học phí và hình thức thanh toán',
    ]);
  };

  return (
    <>
      {/* Nút mở hộp thoại tư vấn */}
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          className="fixed bottom-6 right-6 z-40 flex items-center gap-2.5 rounded-full bg-slate-900 px-4 py-3 text-white shadow-lg border border-slate-700/60 transition-all duration-200 hover:bg-slate-800 hover:shadow-xl focus:outline-none cursor-pointer"
          title="Tư vấn khóa học BeeAcademy"
        >
          <div className="flex h-6 w-6 items-center justify-center rounded-full bg-amber-500/20 text-amber-400">
            <MessageSquare className="h-4 w-4" />
          </div>
          <span className="text-xs font-semibold tracking-wide">Tư vấn khóa học</span>
        </button>
      )}

      {/* Cửa sổ hộp thoại tư vấn */}
      {isOpen && (
        <div
          className={`fixed z-50 flex flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-2xl transition-all duration-200 ${
            isExpanded
              ? 'bottom-4 right-4 h-[88vh] w-[92vw] max-w-4xl'
              : 'bottom-6 right-6 h-[640px] w-[94vw] max-w-[460px]'
          }`}
        >
          {/* Tiêu đề cửa sổ */}
          <div className="flex items-center justify-between border-b border-slate-200 bg-slate-900 px-4 py-3.5 text-white">
            <div className="flex items-center gap-2.5 min-w-0">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-500/20 text-amber-400">
                <MessageSquare className="h-4 w-4" />
              </div>
              <div className="min-w-0">
                <div className="flex items-center gap-1.5">
                  <h3 className="text-sm font-semibold text-white tracking-tight truncate">
                    Tư Vấn Khóa Học & Tuyển Sinh
                  </h3>
                  <span className="rounded bg-amber-500/20 text-amber-300 text-[10px] px-1.5 py-0.2 font-medium">
                    BeeBot
                  </span>
                </div>
                <p className="text-[11px] text-slate-400 truncate">
                  BeeAcademy K-12 • Trực tuyến 24/7
                </p>
              </div>
            </div>

            <div className="flex items-center gap-1">
              <button
                onClick={handleClearHistory}
                className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-800 hover:text-white transition cursor-pointer"
                title="Làm mới cuộc trò chuyện"
              >
                <RefreshCw className="h-4 w-4" />
              </button>
              <button
                onClick={() => setIsExpanded(!isExpanded)}
                className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-800 hover:text-white transition cursor-pointer"
                title={isExpanded ? 'Thu nhỏ' : 'Mở rộng'}
              >
                {isExpanded ? <Minimize2 className="h-4 w-4" /> : <Maximize2 className="h-4 w-4" />}
              </button>
              <button
                onClick={() => setIsOpen(false)}
                className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-800 hover:text-white transition cursor-pointer"
                title="Đóng cửa sổ"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
          </div>

          {/* Ngữ cảnh bài học (nếu gọi từ phòng học) */}
          {currentLessonContext && (
            <div className="flex items-center gap-2 border-b border-slate-200 bg-slate-50 px-4 py-2 text-xs text-slate-600">
              <BookOpen className="h-3.5 w-3.5 text-amber-600 shrink-0" />
              <span className="truncate font-medium">
                Ngữ cảnh: {currentLessonContext}
              </span>
            </div>
          )}

          {/* Danh sách tin nhắn */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50/60 text-sm">
            {messages.map((msg, idx) => (
              <div
                key={idx}
                className={`flex flex-col ${msg.role === 'user' ? 'items-end' : 'items-start'}`}
              >
                <span className="text-[10px] text-slate-400 mb-1 px-1 font-medium flex items-center gap-1">
                  {msg.role === 'user' ? 'Bạn' : (
                    <>
                      <span>BeeBot Tư Vấn</span>
                      <Sparkles className="h-3 w-3 text-amber-500" />
                    </>
                  )}
                </span>
                <div
                  className={`max-w-[94%] rounded-xl px-4 py-3 shadow-xs text-sm leading-relaxed ${
                    msg.role === 'user'
                      ? 'bg-slate-900 text-white'
                      : 'bg-white border border-slate-200 text-slate-800'
                  }`}
                >
                  {msg.role === 'model' ? (
                    <div>
                      <div className="text-slate-800 font-sans">
                        <LatexRenderer
                          content={msg.text}
                          onNavigate={(url) => {
                            setIsOpen(false);
                            navigate(url);
                          }}
                        />
                      </div>

                      {/* Hiển thị Course Cards riêng biệt từ dữ liệu JSON của Backend */}
                      {msg.courses && msg.courses.length > 0 && (
                        <div className="mt-3.5 pt-3 border-t border-slate-100 space-y-2.5">
                          <div className="flex items-center justify-between">
                            <span className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                              Khóa học được đề xuất ({msg.courses.length})
                            </span>
                            <span className="text-[10px] text-emerald-600 font-semibold bg-emerald-50 border border-emerald-200 rounded px-1.5 py-0.5">
                              Xác thực từ hệ thống
                            </span>
                          </div>

                          <div className="space-y-2.5">
                            {msg.courses.map((course) => (
                              <div
                                key={course.id}
                                className="group flex flex-col sm:flex-row overflow-hidden rounded-xl border border-slate-200/90 bg-white hover:border-amber-400 hover:shadow-md transition-all duration-200"
                              >
                                {course.thumbnailUrl && (
                                  <div className="sm:w-32 h-28 sm:h-auto shrink-0 bg-slate-100 overflow-hidden relative">
                                    <img
                                      src={course.thumbnailUrl}
                                      alt={course.title}
                                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                                      loading="lazy"
                                    />
                                    <span className="absolute top-1.5 left-1.5 rounded bg-slate-900/80 backdrop-blur-xs px-1.5 py-0.5 text-[9px] font-semibold text-white">
                                      {course.grade ? `Lớp ${course.grade}` : 'K-12'}
                                    </span>
                                  </div>
                                )}
                                <div className="flex-1 p-2.5 flex flex-col justify-between min-w-0">
                                  <div>
                                    <div className="flex items-center gap-1.5 mb-1 flex-wrap">
                                      <span className="rounded-full bg-amber-50 border border-amber-200/80 px-2 py-0.5 text-[10px] font-semibold text-amber-800">
                                        {course.subjectLabel}
                                      </span>
                                      <span className="flex items-center gap-0.5 text-[10px] font-bold text-amber-600">
                                        <Star className="h-3 w-3 fill-amber-500 text-amber-500" />
                                        {course.rating.toFixed(1)} ({course.reviewCount})
                                      </span>
                                    </div>
                                    <h4 className="text-xs font-semibold text-slate-900 line-clamp-2 leading-snug group-hover:text-amber-600 transition-colors">
                                      {course.title}
                                    </h4>
                                  </div>

                                  <div className="mt-2 pt-2 border-t border-slate-100 flex items-center justify-between gap-2">
                                    <div>
                                      <span className="text-[9px] text-slate-400 block leading-tight">Học phí:</span>
                                      <span className={`text-xs font-bold ${course.price === 0 ? 'text-emerald-600' : 'text-slate-900'}`}>
                                        {course.price === 0 ? 'MIỄN PHÍ (0đ)' : `${course.price.toLocaleString('vi-VN')} đ`}
                                      </span>
                                    </div>
                                    <button
                                      type="button"
                                      onClick={() => {
                                        setIsOpen(false);
                                        navigate(course.detailUrl);
                                      }}
                                      className="inline-flex items-center gap-1 rounded-lg bg-slate-900 px-2.5 py-1.5 text-[11px] font-medium text-white hover:bg-slate-800 transition cursor-pointer shadow-xs shrink-0"
                                    >
                                      <span>Xem chi tiết</span>
                                      <ExternalLink className="h-3 w-3" />
                                    </button>
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  ) : (
                    <p className="whitespace-pre-wrap">{msg.text}</p>
                  )}
                </div>
              </div>
            ))}

            {loading && (
              <div className="flex flex-col items-start text-xs text-slate-500">
                <span className="text-[10px] text-slate-400 mb-1 px-1 font-medium">BeeBot Tư Vấn</span>
                <div className="flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 shadow-xs">
                  <span className="h-1.5 w-1.5 rounded-full bg-slate-400 animate-pulse"></span>
                  <span className="h-1.5 w-1.5 rounded-full bg-slate-400 animate-pulse delay-75"></span>
                  <span className="h-1.5 w-1.5 rounded-full bg-slate-400 animate-pulse delay-150"></span>
                  <span className="text-xs text-slate-500 ml-1">Đang phân tích và tra cứu dữ liệu khóa học...</span>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Gợi ý câu hỏi */}
          {suggestions.length > 0 && (
            <div className="border-t border-slate-200 bg-white px-3.5 py-2.5">
              <p className="text-[11px] font-semibold text-slate-500 mb-1.5">
                Gợi ý câu hỏi phổ biến:
              </p>
              <div className="flex flex-wrap gap-1.5 max-h-24 overflow-y-auto">
                {suggestions.map((sug, i) => (
                  <button
                    key={i}
                    onClick={() => handleSendMessage(sug)}
                    disabled={loading}
                    className="rounded-lg border border-slate-200 bg-slate-50 px-2.5 py-1 text-xs text-slate-700 hover:border-slate-300 hover:bg-slate-100 hover:text-slate-900 transition-colors text-left cursor-pointer"
                  >
                    {sug}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Ô nhập tin nhắn */}
          <div className="border-t border-slate-200 bg-white p-3">
            <div className="flex items-end gap-2 rounded-xl border border-slate-200 bg-slate-50/50 p-2 focus-within:border-slate-400 focus-within:bg-white focus-within:ring-1 focus-within:ring-slate-300 transition-all">
              <textarea
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Nhập câu hỏi cần tư vấn về khóa học, học phí, lộ trình..."
                rows={isExpanded ? 3 : 2}
                disabled={loading}
                className="flex-1 resize-none bg-transparent text-xs sm:text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none"
              />
              <button
                type="button"
                onClick={() => handleSendMessage()}
                disabled={!inputMessage.trim() || loading}
                className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-slate-900 text-white shadow-xs transition hover:bg-slate-800 disabled:opacity-30 cursor-pointer"
                title="Gửi câu hỏi"
              >
                <Send className="h-4 w-4" />
              </button>
            </div>
            <div className="mt-1 flex items-center justify-between px-1 text-[10px] text-slate-400">
              <span>Hỗ trợ tư vấn trực tuyến 24/7</span>
              <span>Nhấn Enter để gửi, Shift + Enter để xuống dòng</span>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
