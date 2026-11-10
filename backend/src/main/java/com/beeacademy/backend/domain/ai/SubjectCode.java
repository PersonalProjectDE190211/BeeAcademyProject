package com.beeacademy.backend.domain.ai;

/**
 * Mã môn học chuẩn hóa K-12 trên hệ thống BeeAcademy.
 */
public enum SubjectCode {
    MATH("Toán học", "toan-hoc", new String[]{"toán", "toan", "đại số", "dai so", "hình học", "hinh hoc", "giải tích", "giai tich"}),
    PHYSICS("Vật lý", "khoa-hoc-tu-nhien", new String[]{"vật lý", "vat ly", "vật lí", "vat li", "lý", "ly", "dao động", "sóng cơ"}),
    CHEMISTRY("Hóa học", "khoa-hoc-tu-nhien", new String[]{"hóa học", "hoa hoc", "hóa", "hoa", "este", "lipit", "hữu cơ", "vô cơ"}),
    BIOLOGY("Sinh học", "khoa-hoc-tu-nhien", new String[]{"sinh học", "sinh hoc", "sinh", "di truyền", "tế bào"}),
    ENGLISH("Tiếng Anh", "tieng-anh", new String[]{"tiếng anh", "tieng anh", "anh văn", "anh van", "english", "ngữ pháp"}),
    LITERATURE("Ngữ văn", "ngu-van", new String[]{"ngữ văn", "ngu van", "văn học", "van hoc", "văn", "van"}),
    IT("Tin học & Lập trình", "tin-hoc-lap-trinh", new String[]{"tin học", "tin hoc", "lập trình", "lap trinh", "python", "code", "thuật toán"});

    private final String displayName;
    private final String categorySlug;
    private final String[] keywords;

    SubjectCode(String displayName, String categorySlug, String[] keywords) {
        this.displayName = displayName;
        this.categorySlug = categorySlug;
        this.keywords = keywords;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public static SubjectCode fromString(String val) {
        if (val == null || val.isBlank()) return null;
        String s = val.trim().toUpperCase();
        for (SubjectCode code : values()) {
            if (code.name().equals(s)) return code;
        }
        String lower = val.trim().toLowerCase();
        for (SubjectCode code : values()) {
            if (code.displayName.toLowerCase().contains(lower) || lower.contains(code.displayName.toLowerCase())) {
                return code;
            }
            for (String kw : code.keywords) {
                if (lower.contains(kw)) return code;
            }
        }
        return null;
    }
}

