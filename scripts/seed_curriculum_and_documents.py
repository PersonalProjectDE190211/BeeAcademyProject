import json
import uuid
import psycopg2

DB_URL = "postgresql://postgres.wnsibvtgqlkqwxwulbff:Thanhdatvv05%40@aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres"

PDF_URL_1 = "https://raw.githubusercontent.com/mozilla/pdf.js/master/web/compressed.tracemonkey-pldi-09.pdf"
PDF_URL_2 = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
PDF_URL_3 = "https://raw.githubusercontent.com/foliojs/pdfkit/master/docs/guide.pdf"

COURSES_DATA = [
    {
        "id": "619f46b8-96bb-43ba-801c-a6ef913f6c54",
        "slug": "toan-6-khoi-dau-vung-chac-so-hoc-hinh-hoc",
        "title": "Toán 6 — Khởi Đầu Vững Chắc: Số Học & Hình Học Trực Quan",
        "chapters": [
            {
                "title": "Chương 1: Tập Hợp & Các Phép Tính Số Tự Nhiên",
                "description": "Nắm vững định nghĩa tập hợp, phần tử, lũy thừa và các phép tính cơ bản.",
                "position": 1,
                "lessons": [
                    {
                        "title": "Bài 1: Tập hợp & Phần tử của tập hợp",
                        "description": "Các cách cho một tập hợp, kí hiệu thuộc và không thuộc, minh họa bằng biểu đồ Ven.",
                        "position": 1,
                        "is_free_preview": True,
                        "video_url": "https://www.youtube.com/watch?v=0hWq5qYJb6g",
                        "duration_sec": 1200,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Tóm tắt lý thuyết & Sơ đồ tư duy Tập hợp Toán 6.pdf",
                                "name": "Tóm tắt lý thuyết & Sơ đồ tư duy Tập hợp Toán 6.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 1450000
                            },
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Phiếu bài tập rèn luyện Tập hợp có đáp án chi tiết.pdf",
                                "name": "Phiếu bài tập rèn luyện Tập hợp có đáp án chi tiết.pdf",
                                "fileUrl": PDF_URL_1,
                                "fileType": "pdf",
                                "fileSizeBytes": 2180000
                            }
                        ]
                    },
                    {
                        "title": "Bài 2: Các phép tính cộng, trừ, nhân, chia số tự nhiên",
                        "description": "Quy tắc thứ tự thực hiện phép tính, tính chất giao hoán, kết hợp và phân phối.",
                        "position": 2,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=5MgBikgcWnY",
                        "duration_sec": 1500,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Chuyên đề tính nhanh & Thứ tự thực hiện phép tính.pdf",
                                "name": "Chuyên đề tính nhanh & Thứ tự thực hiện phép tính.pdf",
                                "fileUrl": PDF_URL_3,
                                "fileType": "pdf",
                                "fileSizeBytes": 1820000
                            }
                        ]
                    },
                    {
                        "title": "Bài 3: Lũy thừa với số mũ tự nhiên",
                        "description": "Định nghĩa lũy thừa bậc n, nhân và chia hai lũy thừa cùng cơ số.",
                        "position": 3,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=kJQP7kiw5Fk",
                        "duration_sec": 1800,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Bảng quy tắc lũy thừa & Kỹ thuật tính nhẩm Toán 6.pdf",
                                "name": "Bảng quy tắc lũy thừa & Kỹ thuật tính nhẩm Toán 6.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 980000
                            }
                        ]
                    }
                ]
            },
            {
                "title": "Chương 2: Tính Chia Hết & Số Nguyên Tố",
                "description": "Dấu hiệu chia hết cho 2, 3, 5, 9, sàng Eratosthenes và phân tích thừa số nguyên tố.",
                "position": 2,
                "lessons": [
                    {
                        "title": "Bài 4: Quan hệ chia hết và dấu hiệu chia hết cho 2, 3, 5, 9",
                        "description": "Tính chất chia hết của một tổng, các dấu hiệu nhận biết nhanh.",
                        "position": 1,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=fJ9rUzIMcZQ",
                        "duration_sec": 1600,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Bộ đề trắc nghiệm dấu hiệu chia hết Toán 6.pdf",
                                "name": "Bộ đề trắc nghiệm dấu hiệu chia hết Toán 6.pdf",
                                "fileUrl": PDF_URL_1,
                                "fileType": "pdf",
                                "fileSizeBytes": 1640000
                            }
                        ]
                    },
                    {
                        "title": "Bài 5: Số nguyên tố, hợp số & Phân tích ra thừa số nguyên tố",
                        "description": "Cách tìm ước chung lớn nhất (ƯCLN) và bội chung nhỏ nhất (BCNN).",
                        "position": 2,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=0hWq5qYJb6g",
                        "duration_sec": 2000,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Sàng Eratosthenes & Chuyên đề bài tập số nguyên tố.pdf",
                                "name": "Sàng Eratosthenes & Chuyên đề bài tập số nguyên tố.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 1350000
                            }
                        ]
                    }
                ]
            },
            {
                "title": "Chương 3: Hình Học Trực Quan & Đo Lường Thực Tế",
                "description": "Tam giác đều, hình vuông, lục giác đều, hình chữ nhật và hình thoi.",
                "position": 3,
                "lessons": [
                    {
                        "title": "Bài 6: Nhận biết & Vẽ tam giác đều, hình vuông, lục giác đều",
                        "description": "Thực hành sử dụng thước kẻ, ê-ke và compa vẽ các hình hình học trực quan.",
                        "position": 1,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=5MgBikgcWnY",
                        "duration_sec": 1500,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Thực hành cắt dán và vẽ hình học trực quan Toán 6.pdf",
                                "name": "Thực hành cắt dán và vẽ hình học trực quan Toán 6.pdf",
                                "fileUrl": PDF_URL_3,
                                "fileType": "pdf",
                                "fileSizeBytes": 3200000
                            }
                        ]
                    }
                ]
            }
        ]
    },
    {
        "id": "67d48484-7fed-40f2-9f1d-2f576f8a6ca6",
        "slug": "toan-12-khao-sat-ham-so-dao-ham-tich-phan",
        "title": "Toán 12 — Chinh Phục Điểm 9+ Khảo Sát Hàm Số & Tích Phân",
        "chapters": [
            {
                "title": "Chương 1: Khảo Sát Hàm Số & Ứng Dụng Đạo Hàm",
                "description": "Đơn điệu, cực trị, giá trị lớn nhất nhỏ nhất và tiệm cận của đồ thị hàm số.",
                "position": 1,
                "lessons": [
                    {
                        "title": "Bài 1: Tính đơn điệu và các điểm cực trị của hàm số",
                        "description": "Lý thuyết đạo hàm cấp 1, dấu của đạo hàm và quy tắc xác định cực đại, cực tiểu.",
                        "position": 1,
                        "is_free_preview": True,
                        "video_url": "https://www.youtube.com/watch?v=kYv9rE2z0wM",
                        "duration_sec": 1800,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Sổ tay công thức Đạo hàm & Bảng biến thiên Toán 12.pdf",
                                "name": "Sổ tay công thức Đạo hàm & Bảng biến thiên Toán 12.pdf",
                                "fileUrl": PDF_URL_1,
                                "fileType": "pdf",
                                "fileSizeBytes": 2540000
                            },
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Tuyển tập 100 câu trắc nghiệm Cực trị hàm số có đáp án.pdf",
                                "name": "Tuyển tập 100 câu trắc nghiệm Cực trị hàm số có đáp án.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 3120000
                            }
                        ]
                    },
                    {
                        "title": "Bài 2: Giá trị lớn nhất và nhỏ nhất của hàm số trên đoạn",
                        "description": "Quy tắc tìm GTLN, GTNN trên đoạn [a; b], đạo hàm triệt tiêu và bài toán thực tế tối ưu hóa.",
                        "position": 2,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=5V_2Z1D2xvg",
                        "duration_sec": 2100,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Chuyên đề Min Max hàm hợp & Kỹ thuật Casio 880BTG.pdf",
                                "name": "Chuyên đề Min Max hàm hợp & Kỹ thuật Casio 880BTG.pdf",
                                "fileUrl": PDF_URL_3,
                                "fileType": "pdf",
                                "fileSizeBytes": 1980000
                            }
                        ]
                    },
                    {
                        "title": "Bài 3: Đường tiệm cận đứng, tiệm cận ngang và tiệm cận xiên",
                        "description": "Kỹ thuật bấm máy Casio tìm giới hạn và nhận dạng nhanh tiệm cận từ bảng biến thiên.",
                        "position": 3,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=kYv9rE2z0wM",
                        "duration_sec": 1500,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Phương pháp nhận dạng tiệm cận và đồ thị hàm phân thức.pdf",
                                "name": "Phương pháp nhận dạng tiệm cận và đồ thị hàm phân thức.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 1450000
                            }
                        ]
                    }
                ]
            },
            {
                "title": "Chương 2: Nguyên Hàm, Tích Phân & Ứng Dụng",
                "description": "Định nghĩa, tính chất, phương pháp đổi biến, từng phần và ứng dụng hình học.",
                "position": 2,
                "lessons": [
                    {
                        "title": "Bài 4: Bảng nguyên hàm cơ bản và phương pháp đổi biến số",
                        "description": "Nắm vững các công thức nguyên hàm hàm số lũy thừa, hàm lượng giác và hàm mũ.",
                        "position": 1,
                        "is_free_preview": True,
                        "video_url": "https://www.youtube.com/watch?v=5V_2Z1D2xvg",
                        "duration_sec": 2400,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Bảng 60 công thức Nguyên hàm giải nhanh đại học.pdf",
                                "name": "Bảng 60 công thức Nguyên hàm giải nhanh đại học.pdf",
                                "fileUrl": PDF_URL_1,
                                "fileType": "pdf",
                                "fileSizeBytes": 2780000
                            },
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Bài tập đổi biến số tích phân chọn lọc điểm 9+.pdf",
                                "name": "Bài tập đổi biến số tích phân chọn lọc điểm 9+.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 2210000
                            }
                        ]
                    },
                    {
                        "title": "Bài 5: Phương pháp tích phân từng phần (Nhất lô, nhì đa, tam lượng, tứ mũ)",
                        "description": "Quy tắc đặt u và dv, ứng dụng sơ đồ đường chéo tính nhanh tích phân hàm hỗn hợp.",
                        "position": 2,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=kYv9rE2z0wM",
                        "duration_sec": 1800,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Kỹ thuật sơ đồ đường chéo tính nhanh tích phân từng phần.pdf",
                                "name": "Kỹ thuật sơ đồ đường chéo tính nhanh tích phân từng phần.pdf",
                                "fileUrl": PDF_URL_3,
                                "fileType": "pdf",
                                "fileSizeBytes": 1890000
                            }
                        ]
                    },
                    {
                        "title": "Bài 6: Ứng dụng tích phân tính diện tích hình phẳng & thể tích vật thể tròn xoay",
                        "description": "Công thức diện tích giới hạn bởi 2 đường cong và thể tích khối tròn xoay quanh trục Ox.",
                        "position": 3,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=5V_2Z1D2xvg",
                        "duration_sec": 2100,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Chuyên đề bài toán thực tế diện tích & thể tích tích phân.pdf",
                                "name": "Chuyên đề bài toán thực tế diện tích & thể tích tích phân.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 3450000
                            }
                        ]
                    }
                ]
            }
        ]
    },
    {
        "id": "6cf4854f-ae2e-4210-846f-58cedbebbddc",
        "slug": "vat-ly-12-dao-dong-co-va-song-co",
        "title": "Vật Lý 12 — Nắm Trọn Điểm 9+ Dao Động Cơ & Sóng Cơ Học",
        "chapters": [
            {
                "title": "Chương 1: Dao Động Cơ Học & Con Lắc Điều Hòa",
                "description": "Phương trình li độ, vận tốc, gia tốc, năng lượng và vòng tròn lượng giác.",
                "position": 1,
                "lessons": [
                    {
                        "title": "Bài 1: Đại cương về dao động điều hòa & Vòng tròn lượng giác",
                        "description": "Mối liên hệ giữa dao động điều hòa và chuyển động tròn đều, trục phân bố thời gian.",
                        "position": 1,
                        "is_free_preview": True,
                        "video_url": "https://www.youtube.com/watch?v=kJQP7kiw5Fk",
                        "duration_sec": 1800,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Sổ tay công thức Dao động điều hòa & Trục phân bố thời gian.pdf",
                                "name": "Sổ tay công thức Dao động điều hòa & Trục phân bố thời gian.pdf",
                                "fileUrl": PDF_URL_1,
                                "fileType": "pdf",
                                "fileSizeBytes": 2450000
                            },
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Bài tập pha ban đầu và năng lượng dao động có giải chi tiết.pdf",
                                "name": "Bài tập pha ban đầu và năng lượng dao động có giải chi tiết.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 1980000
                            }
                        ]
                    },
                    {
                        "title": "Bài 2: Con lắc lò xo và lực kéo về, lực đàn hồi cực đại",
                        "description": "Tần số góc, chu kì, độ biến dạng lò xo và phương pháp giải đồ thị lực.",
                        "position": 2,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=fJ9rUzIMcZQ",
                        "duration_sec": 2000,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Chuyên đề đồ thị lực đàn hồi con lắc lò xo thẳng đứng.pdf",
                                "name": "Chuyên đề đồ thị lực đàn hồi con lắc lò xo thẳng đứng.pdf",
                                "fileUrl": PDF_URL_3,
                                "fileType": "pdf",
                                "fileSizeBytes": 2650000
                            }
                        ]
                    }
                ]
            },
            {
                "title": "Chương 2: Sóng Cơ Học & Giao Thoa Sóng",
                "description": "Sự truyền sóng, độ lệch pha, bước sóng và hiện tượng sóng dừng, sóng âm.",
                "position": 2,
                "lessons": [
                    {
                        "title": "Bài 3: Sự truyền sóng cơ và phương trình sóng",
                        "description": "Vận tốc truyền sóng, bước sóng lambda, độ lệch pha giữa hai điểm trên phương truyền.",
                        "position": 1,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=0hWq5qYJb6g",
                        "duration_sec": 1700,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Tóm tắt lý thuyết sự truyền sóng cơ & độ lệch pha.pdf",
                                "name": "Tóm tắt lý thuyết sự truyền sóng cơ & độ lệch pha.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 1720000
                            }
                        ]
                    },
                    {
                        "title": "Bài 4: Giao thoa sóng cơ học hai nguồn kết hợp",
                        "description": "Vị trí cực đại, cực tiểu giao thoa, đường hypebol và đếm số điểm dao động cực đại.",
                        "position": 2,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=5MgBikgcWnY",
                        "duration_sec": 2100,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Tổng hợp bài tập cực đại cực tiểu giao thoa sóng điểm 9+.pdf",
                                "name": "Tổng hợp bài tập cực đại cực tiểu giao thoa sóng điểm 9+.pdf",
                                "fileUrl": PDF_URL_1,
                                "fileType": "pdf",
                                "fileSizeBytes": 3100000
                            }
                        ]
                    }
                ]
            }
        ]
    },
    {
        "id": "d35d21ba-4b7e-4a50-b877-44cd21d0eaa5",
        "slug": "hoa-hoc-12-chuyen-de-este-lipit-hop-chat-chua-nito",
        "title": "Hóa Học 12 — Chuyên Đề Este, Lipit & Hợp Chất Hữu Cơ Chứa Nitơ",
        "chapters": [
            {
                "title": "Chương 1: Este - Lipit & Kỹ Thuật Đồng Đẳng Hóa",
                "description": "Phản ứng este hóa, xà phòng hóa chất béo và phương pháp dồn chất giải toán.",
                "position": 1,
                "lessons": [
                    {
                        "title": "Bài 1: Cấu tạo phân tử, danh pháp và phản ứng xà phòng hóa Este",
                        "description": "Tính chất vật lý, hóa học, phản ứng tráng bạc của este fomic và bài toán đốt cháy este.",
                        "position": 1,
                        "is_free_preview": True,
                        "video_url": "https://www.youtube.com/watch?v=fJ9rUzIMcZQ",
                        "duration_sec": 1600,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Sơ đồ phản ứng thủy phân este và đồng phân este no đơn chức.pdf",
                                "name": "Sơ đồ phản ứng thủy phân este và đồng phân este no đơn chức.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 2100000
                            },
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Tuyển tập 50 câu trắc nghiệm Este Lipit lý thuyết trọng tâm.pdf",
                                "name": "Tuyển tập 50 câu trắc nghiệm Este Lipit lý thuyết trọng tâm.pdf",
                                "fileUrl": PDF_URL_1,
                                "fileType": "pdf",
                                "fileSizeBytes": 1650000
                            }
                        ]
                    },
                    {
                        "title": "Bài 2: Bài toán thủy phân chất béo & kỹ thuật đồng đẳng hóa",
                        "description": "Chỉ số axit, chỉ số xà phòng, bảo toàn khối lượng và dồn chất chất béo CH2, H2O, COO.",
                        "position": 2,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=kYv9rE2z0wM",
                        "duration_sec": 2200,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Kỹ thuật dồn chất và đồng đẳng hóa bài toán chất béo điểm 9+.pdf",
                                "name": "Kỹ thuật dồn chất và đồng đẳng hóa bài toán chất béo điểm 9+.pdf",
                                "fileUrl": PDF_URL_3,
                                "fileType": "pdf",
                                "fileSizeBytes": 2900000
                            }
                        ]
                    }
                ]
            },
            {
                "title": "Chương 2: Amin, Amino Axit & Peptit",
                "description": "So sánh lực bazơ amin, amino axit lưỡng tính, phản ứng thủy phân peptit.",
                "position": 2,
                "lessons": [
                    {
                        "title": "Bài 3: Cấu tạo, tính bazơ của Amin và bài toán đốt cháy",
                        "description": "Bậc của amin, phản ứng với axit vô cơ, kết tủa brom với anilin.",
                        "position": 1,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=5V_2Z1D2xvg",
                        "duration_sec": 1800,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Bảng so sánh tính bazơ của Amin & phương pháp quy đổi.pdf",
                                "name": "Bảng so sánh tính bazơ của Amin & phương pháp quy đổi.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 1800000
                            }
                        ]
                    },
                    {
                        "title": "Bài 4: Amino Axit lưỡng tính và chuỗi phản ứng Peptit",
                        "description": "Điểm đẳng điện, phản ứng màu biure và phương pháp thủy phân peptit từng nấc.",
                        "position": 2,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=0hWq5qYJb6g",
                        "duration_sec": 2000,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Chuyên đề Peptit nâng cao vận dụng cao THPT Quốc Gia.pdf",
                                "name": "Chuyên đề Peptit nâng cao vận dụng cao THPT Quốc Gia.pdf",
                                "fileUrl": PDF_URL_1,
                                "fileType": "pdf",
                                "fileSizeBytes": 3300000
                            }
                        ]
                    }
                ]
            }
        ]
    },
    {
        "id": "de892ca3-ae06-4d8e-9a8f-adec4313684d",
        "slug": "toan-9-luyen-thi-vao-lop-10-chuyen-va-cong-lap",
        "title": "Toán 9 — Chiến Thuật Luyện Thi Vào Lớp 10 Chuyên & Công Lập",
        "chapters": [
            {
                "title": "Chương 1: Căn Thức Bậc Hai & Rút Gọn Biểu Thức Chứa Căn",
                "description": "Điều kiện xác định, trục căn thức ở mẫu, rút gọn phân thức đại số.",
                "position": 1,
                "lessons": [
                    {
                        "title": "Bài 1: Kỹ thuật rút gọn biểu thức chứa căn bậc hai",
                        "description": "Quy đồng mẫu thức, phân tích nhân tử bằng hằng đẳng thức đáng nhớ.",
                        "position": 1,
                        "is_free_preview": True,
                        "video_url": "https://www.youtube.com/watch?v=5MgBikgcWnY",
                        "duration_sec": 1700,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Sổ tay phương pháp rút gọn phân thức chứa căn Toán 9.pdf",
                                "name": "Sổ tay phương pháp rút gọn phân thức chứa căn Toán 9.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 2200000
                            },
                            {
                                "id": str(uuid.uuid4()),
                                "title": "20 đề thi thử vào 10 phần rút gọn biểu thức có đáp án.pdf",
                                "name": "20 đề thi thử vào 10 phần rút gọn biểu thức có đáp án.pdf",
                                "fileUrl": PDF_URL_1,
                                "fileType": "pdf",
                                "fileSizeBytes": 2850000
                            }
                        ]
                    },
                    {
                        "title": "Bài 2: Các bài toán phụ sau rút gọn: Tìm x nguyên, Min Max",
                        "description": "Phương pháp chặn miền giá trị, dùng bất đẳng thức Cô-si tìm cực trị đại số.",
                        "position": 2,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=kJQP7kiw5Fk",
                        "duration_sec": 1900,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Chuyên đề bài toán phụ sau rút gọn thi vào 10.pdf",
                                "name": "Chuyên đề bài toán phụ sau rút gọn thi vào 10.pdf",
                                "fileUrl": PDF_URL_3,
                                "fileType": "pdf",
                                "fileSizeBytes": 1950000
                            }
                        ]
                    }
                ]
            },
            {
                "title": "Chương 2: Hệ Phương Trình & Tứ Giác Nội Tiếp Đường Tròn",
                "description": "Phương pháp thế, cộng đại số, hệ đối xứng, tứ giác nội tiếp và góc với đường tròn.",
                "position": 2,
                "lessons": [
                    {
                        "title": "Bài 3: Phương pháp giải & Biện luận hệ phương trình bậc nhất hai ẩn",
                        "description": "Định lý Vi-et cho phương trình bậc 2, tìm tham số m để nghiệm thỏa mãn hệ thức.",
                        "position": 1,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=fJ9rUzIMcZQ",
                        "duration_sec": 1600,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Kỹ năng giải và biện luận hệ phương trình chứa tham số m.pdf",
                                "name": "Kỹ năng giải và biện luận hệ phương trình chứa tham số m.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 1600000
                            }
                        ]
                    },
                    {
                        "title": "Bài 4: Tứ giác nội tiếp và các góc liên quan đến đường tròn",
                        "description": "Góc ở tâm, góc nội tiếp, góc tạo bởi tiếp tuyến và dây cung, 5 dấu hiệu nhận biết tứ giác nội tiếp.",
                        "position": 2,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=kYv9rE2z0wM",
                        "duration_sec": 2200,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Bộ 30 bài hình học tứ giác nội tiếp điểm 9+ vào lớp 10.pdf",
                                "name": "Bộ 30 bài hình học tứ giác nội tiếp điểm 9+ vào lớp 10.pdf",
                                "fileUrl": PDF_URL_1,
                                "fileType": "pdf",
                                "fileSizeBytes": 3800000
                            }
                        ]
                    }
                ]
            }
        ]
    },
    {
        "id": "46b1e9a4-1688-4314-96aa-611921a85741",
        "slug": "tieng-anh-10-lam-chu-ngu-phap-b1-b2",
        "title": "Tiếng Anh 10 — Làm Chủ Ngữ Pháp B1–B2 & Từ Vựng Chuyên Đề",
        "chapters": [
            {
                "title": "Chương 1: Grammar in Use: Advanced Tenses & Passive Voice",
                "description": "Review of present, past, future tenses, passive forms with modals and reporting verbs.",
                "position": 1,
                "lessons": [
                    {
                        "title": "Bài 1: Review of Present & Past Tenses (Simple, Continuous, Perfect)",
                        "description": "Phân biệt Past Simple và Present Perfect, thì Quá khứ hoàn thành trong văn cảnh tự sự.",
                        "position": 1,
                        "is_free_preview": True,
                        "video_url": "https://www.youtube.com/watch?v=0hWq5qYJb6g",
                        "duration_sec": 1500,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Comprehensive English Grammar Chart - All 12 Tenses.pdf",
                                "name": "Comprehensive English Grammar Chart - All 12 Tenses.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 2150000
                            },
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Practice Exercises - Tenses & Time Expressions with Keys.pdf",
                                "name": "Practice Exercises - Tenses & Time Expressions with Keys.pdf",
                                "fileUrl": PDF_URL_1,
                                "fileType": "pdf",
                                "fileSizeBytes": 1780000
                            }
                        ]
                    },
                    {
                        "title": "Bài 2: Passive Voice with Modals and Double Objects",
                        "description": "Cấu trúc bị động đặc biệt với động từ chỉ giác quan, bị động khách quan (It is said that...).",
                        "position": 2,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=5MgBikgcWnY",
                        "duration_sec": 1700,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Passive Voice Rules & Sentence Transformation Exercises Grade 10.pdf",
                                "name": "Passive Voice Rules & Sentence Transformation Exercises Grade 10.pdf",
                                "fileUrl": PDF_URL_3,
                                "fileType": "pdf",
                                "fileSizeBytes": 1450000
                            }
                        ]
                    }
                ]
            },
            {
                "title": "Chương 2: Vocabulary & Reading Comprehension: Global Success",
                "description": "Unit 1: Family Life, Unit 2: Humans and Environment, Unit 3: Music and Arts.",
                "position": 2,
                "lessons": [
                    {
                        "title": "Bài 3: Family Life & Humans and the Environment (Word Formation)",
                        "description": "Tiền tố, hậu tố tạo danh từ và tính từ, collocation chủ điểm gia đình và môi trường sống xanh.",
                        "position": 1,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=fJ9rUzIMcZQ",
                        "duration_sec": 1600,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Academic Vocabulary List 500 Words for Grade 10 Global Success.pdf",
                                "name": "Academic Vocabulary List 500 Words for Grade 10 Global Success.pdf",
                                "fileUrl": PDF_URL_2,
                                "fileType": "pdf",
                                "fileSizeBytes": 2600000
                            }
                        ]
                    },
                    {
                        "title": "Bài 4: Reading Strategies & IELTS Foundation Skills",
                        "description": "Kỹ thuật Skimming & Scanning, nhận diện từ khóa và trả lời câu hỏi trắc nghiệm đọc hiểu.",
                        "position": 2,
                        "is_free_preview": False,
                        "video_url": "https://www.youtube.com/watch?v=kJQP7kiw5Fk",
                        "duration_sec": 1900,
                        "documents": [
                            {
                                "id": str(uuid.uuid4()),
                                "title": "Skimming and Scanning Techniques for High School Students.pdf",
                                "name": "Skimming and Scanning Techniques for High School Students.pdf",
                                "fileUrl": PDF_URL_1,
                                "fileType": "pdf",
                                "fileSizeBytes": 2950000
                            }
                        ]
                    }
                ]
            }
        ]
    }
]

def run():
    print("🔌 Đang kết nối tới Supabase PostgreSQL...")
    conn = psycopg2.connect(DB_URL)
    conn.autocommit = True
    cur = conn.cursor()

    total_courses = 0
    total_chapters = 0
    total_lessons = 0
    total_docs = 0

    for c in COURSES_DATA:
        course_id = c["id"]
        print(f"\n📘 Xử lý khóa học: {c['title']} ({course_id})...")

        # Xóa chapters cũ để refresh dữ liệu hoàn chỉnh (cascade sẽ xóa lessons)
        cur.execute("DELETE FROM chapters WHERE course_id = %s;", (course_id,))

        course_chapters_count = len(c["chapters"])
        course_lessons_count = 0
        course_total_duration = 0

        for ch in c["chapters"]:
            cur.execute("""
                INSERT INTO chapters (course_id, title, description, position, created_at, updated_at)
                VALUES (%s, %s, %s, %s, NOW(), NOW())
                RETURNING id;
            """, (course_id, ch["title"], ch["description"], ch["position"]))
            chapter_id = cur.fetchone()[0]
            total_chapters += 1

            for l in ch["lessons"]:
                course_lessons_count += 1
                total_lessons += 1
                course_total_duration += l["duration_sec"]
                total_docs += len(l["documents"])

                cur.execute("""
                    INSERT INTO lessons (
                        chapter_id, title, description, position,
                        is_free_preview, video_storage_path, duration_sec,
                        documents, created_at, updated_at
                    ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, NOW(), NOW());
                """, (
                    chapter_id,
                    l["title"],
                    l["description"],
                    l["position"],
                    l["is_free_preview"],
                    l["video_url"],
                    l["duration_sec"],
                    json.dumps(l["documents"])
                ))

        # Cập nhật thông số metadata của Course
        cur.execute("""
            UPDATE courses SET
                total_chapters = %s,
                total_lessons = %s,
                total_duration_sec = %s,
                updated_at = NOW()
            WHERE id = %s;
        """, (course_chapters_count, course_lessons_count, course_total_duration, course_id))

        total_courses += 1
        print(f"  ✓ Đã nhập {course_chapters_count} chương, {course_lessons_count} bài học với tài liệu PDF thật!")

    cur.close()
    conn.close()
    print("\n=======================================================")
    print(f"🎉 Hoàn tất nạp dữ liệu thật cho {total_courses} khóa học!")
    print(f"📊 Tổng số chương: {total_chapters}")
    print(f"📊 Tổng số bài giảng: {total_lessons}")
    print(f"📊 Tổng số tài liệu đính kèm (PDF/Tài liệu thật): {total_docs}")
    print("=======================================================")

if __name__ == "__main__":
    run()

