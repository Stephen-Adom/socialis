package com.alaska.socialis.config;

public class TranscodeConfig {
    private String poster;
    private String tsSeconds;
    private String cutStart;
    private String cutEnd;

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getTsSeconds() {
        return tsSeconds;
    }

    public void setTsSeconds(String tsSeconds) {
        this.tsSeconds = tsSeconds;
    }

    public String getCutStart() {
        return cutStart;
    }

    public void setCutStart(String cutStart) {
        this.cutStart = cutStart;
    }

    public String getCutEnd() {
        return cutEnd;
    }

    public void setCutEnd(String cutEnd) {
        this.cutEnd = cutEnd;
    }

    @Override
    public String toString() {
        return "TranscodeConfig [poster=" + poster + ", tsSeconds=" + tsSeconds + ", cutStart=" + cutStart + ", cutEnd="
                + cutEnd + "]";
    }
}
