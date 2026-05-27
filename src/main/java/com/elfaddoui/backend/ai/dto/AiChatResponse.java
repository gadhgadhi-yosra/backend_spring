package com.elfaddoui.backend.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class AiChatResponse {
    private String reply;
    private Double confidence;
    private List<Action> actions = new ArrayList<>();
    private List<String> sources = new ArrayList<>();

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public List<Action> getActions() { return actions; }
    public void setActions(List<Action> actions) { this.actions = actions; }
    public List<String> getSources() { return sources; }
    public void setSources(List<String> sources) { this.sources = sources; }

    public static class Action {
        private String type;
        private String value;

        public Action() {}
        public Action(String type, String value) {
            this.type = type;
            this.value = value;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
