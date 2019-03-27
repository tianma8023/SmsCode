package com.github.tianma8023.smscode.app.faq;

public class FaqItem {
    private String question;
    private String answer;

    FaqItem(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    String getQuestion() {
        return question;
    }

    String getAnswer() {
        return answer;
    }

    @Override
    public String toString() {
        return "FaqItem{" +
                "question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                '}';
    }
}