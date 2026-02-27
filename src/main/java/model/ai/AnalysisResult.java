package model.ai;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResult {
    private String globalComment;
    private List<ScoreSuggestion> scoreSuggestions = new ArrayList<>();

    public AnalysisResult() {}

    public AnalysisResult(String globalComment, List<ScoreSuggestion> scoreSuggestions) {
        this.globalComment = globalComment;
        if (scoreSuggestions != null) {
            this.scoreSuggestions = scoreSuggestions;
        }
    }

    public String getGlobalComment() {
        return globalComment;
    }

    public void setGlobalComment(String globalComment) {
        this.globalComment = globalComment;
    }

    public List<ScoreSuggestion> getScoreSuggestions() {
        return scoreSuggestions;
    }

    public void setScoreSuggestions(List<ScoreSuggestion> scoreSuggestions) {
        this.scoreSuggestions = scoreSuggestions != null ? scoreSuggestions : new ArrayList<>();
    }
}

