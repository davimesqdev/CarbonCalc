package br.com.carboncalc.domain;

public enum Formato {
    PDF("pdf", "application/pdf"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final String extensao;
    private final String contentType;

    Formato(String extensao, String contentType) {
        this.extensao = extensao;
        this.contentType = contentType;
    }

    public String getExtensao() {
        return extensao;
    }

    public String getContentType() {
        return contentType;
    }
}
