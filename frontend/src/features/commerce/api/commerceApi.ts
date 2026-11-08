import { httpClient } from '@/shared/api/httpClient';
import { ApiResponse } from '@/shared/types/api';

export interface OrderItemDto {
  id: string;
  courseId: string;
  teacherId: string;
  courseTitle: string;
  priceAtPurchase: number;
  platformFeeRate: number;
  platformFeeVnd: number;
  teacherAmountVnd: number;
  itemStatus: string;
}

export interface CreateOrderResultDto {
  orderId: string;
  orderCode: number;
  paymentRef: string;
  checkoutUrl?: string;
  qrCodeUrl?: string;
  totalAmountVnd: number;
  discountAmountVnd: number;
  status: string;
  expiresAt: string;
  items: OrderItemDto[];
}

export interface OrderDto {
  id: string;
  studentId: string;
  orderCode: number;
  paymentRef: string;
  paymentMethod: string;
  status: string;
  totalAmountVnd: number;
  discountAmountVnd: number;
  expiresAt: string;
  paidAt?: string;
  createdAt: string;
  updatedAt: string;
  items: OrderItemDto[];
}

export interface TeacherBankAccountDto {
  id: string;
  teacherId: string;
  bankName: string;
  accountNumberMasked: string;
  accountHolder: string;
  branch?: string;
  verifyStatus: string;
  otpVerified: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PayoutPeriodDto {
  id: string;
  teacherId: string;
  monthYear: string;
  status: string;
  totalGrossVnd: number;
  totalPlatformFeeVnd: number;
  totalTeacherVnd: number;
  transactionCount: number;
  paidAt?: string;
  paidByAdmin?: string;
  transferRef?: string;
  transferContent?: string;
  uncAttachmentUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TeacherRevenueStatsDto {
  totalGrossVnd: number;
  totalPlatformFeeVnd: number;
  totalTeacherVnd: number;
  pendingPayoutVnd: number;
  paidPayoutVnd: number;
  totalTransactions: number;
  currentPeriod: string;
}

export const commerceApi = {
  // Student Orders
  async createOrder(payload: {
    courseIds: string[];
    voucherCode?: string;
    paymentMethod?: string;
  }): Promise<CreateOrderResultDto> {
    const res = await httpClient.post<ApiResponse<CreateOrderResultDto>>(
      '/student/orders',
      payload
    );
    return res.data.data;
  },

  async getMyOrders(): Promise<OrderDto[]> {
    const res = await httpClient.get<ApiResponse<OrderDto[]>>('/student/orders');
    return res.data.data;
  },

  async cancelOrder(orderId: string): Promise<OrderDto> {
    const res = await httpClient.post<ApiResponse<OrderDto>>(
      `/student/orders/${orderId}/cancel`,
      {}
    );
    return res.data.data;
  },

  // Teacher Finance
  async getTeacherBankAccount(): Promise<TeacherBankAccountDto | null> {
    const res = await httpClient.get<ApiResponse<TeacherBankAccountDto | null>>(
      '/teacher/finance/bank-account'
    );
    return res.data.data;
  },

  async saveTeacherBankAccount(payload: {
    bankName: string;
    accountNumber: string;
    accountHolder: string;
    branch?: string;
  }): Promise<TeacherBankAccountDto> {
    const res = await httpClient.post<ApiResponse<TeacherBankAccountDto>>(
      '/teacher/finance/bank-account',
      payload
    );
    return res.data.data;
  },

  async getTeacherPayouts(): Promise<PayoutPeriodDto[]> {
    const res = await httpClient.get<ApiResponse<PayoutPeriodDto[]>>(
      '/teacher/finance/payouts'
    );
    return res.data.data;
  },

  async getTeacherRevenueStatistics(): Promise<TeacherRevenueStatsDto> {
    const res = await httpClient.get<ApiResponse<TeacherRevenueStatsDto>>(
      '/teacher/finance/statistics'
    );
    return res.data.data;
  },

  // Admin Finance & Payouts
  async getAdminPayouts(status?: string): Promise<PayoutPeriodDto[]> {
    const res = await httpClient.get<ApiResponse<PayoutPeriodDto[]>>(
      '/admin/finance/payouts',
      { params: { status } }
    );
    return res.data.data;
  },

  async confirmPayout(
    payoutId: string,
    payload: {
      transferRef: string;
      transferContent: string;
      uncAttachmentUrl?: string;
    }
  ): Promise<PayoutPeriodDto> {
    const res = await httpClient.post<ApiResponse<PayoutPeriodDto>>(
      `/admin/finance/payouts/${payoutId}/confirm`,
      payload
    );
    return res.data.data;
  },

  async getPendingBankAccounts(): Promise<TeacherBankAccountDto[]> {
    const res = await httpClient.get<ApiResponse<TeacherBankAccountDto[]>>(
      '/admin/finance/bank-accounts/pending'
    );
    return res.data.data;
  },

  async verifyBankAccount(bankAccountId: string): Promise<TeacherBankAccountDto> {
    const res = await httpClient.post<ApiResponse<TeacherBankAccountDto>>(
      `/admin/finance/bank-accounts/${bankAccountId}/verify`,
      {}
    );
    return res.data.data;
  },

  async rejectBankAccount(
    bankAccountId: string,
    reason: string
  ): Promise<TeacherBankAccountDto> {
    const res = await httpClient.post<ApiResponse<TeacherBankAccountDto>>(
      `/admin/finance/bank-accounts/${bankAccountId}/reject`,
      { reason }
    );
    return res.data.data;
  },
};
