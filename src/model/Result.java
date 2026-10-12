package model;

public class Result {
    private String examTitle;
    private int score, total;
    private String attemptedOn;

    public Result(String examTitle, int score, int total, String attemptedOn) {
        this.examTitle = examTitle;
        this.score = score;
        this.total = total;
        this.attemptedOn = attemptedOn;
    }

    public String getExamTitle() { return examTitle; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public String getAttemptedOn() { return attemptedOn; }
    public double getPercentage() { return total == 0 ? 0 : (score * 100.0) / total; }
}
