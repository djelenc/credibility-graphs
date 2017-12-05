package credibilitygraphs;

import org.jgrapht.graph.DefaultEdge;

public class CredibilityObject extends DefaultEdge {
    private String src;
    private String tgt;
    private String reporter;

    public CredibilityObject(String src, String tgt, String reporter) {
        this.src = src;
        this.tgt = tgt;
        this.reporter = reporter;
    }

    public String getSrc() {
        return src;
    }

    public String getTgt() {
        return tgt;
    }

    public String getReporter() {
        return reporter;
    }

    public String toString() {
        return String.format("%s (%s-%s)", reporter, src, tgt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CredibilityObject that = (CredibilityObject) o;

        if (src != null ? !src.equals(that.src) : that.src != null) return false;
        if (tgt != null ? !tgt.equals(that.tgt) : that.tgt != null) return false;
        return reporter != null ? reporter.equals(that.reporter) : that.reporter == null;
    }

    @Override
    public int hashCode() {
        int result = src != null ? src.hashCode() : 0;
        result = 31 * result + (tgt != null ? tgt.hashCode() : 0);
        result = 31 * result + (reporter != null ? reporter.hashCode() : 0);
        return result;
    }
}
