package model;

public class Question {
    private int id;
    private String text, optionA, optionB, optionC, optionD;
    private char correctOption;

    public Question(int id, String text, String a, String b, String c, String d, char correct) {
        this.id = id;
        this.text = text;
        this.optionA = a;
        this.optionB = b;
        this.optionC = c;
        this.optionD = d;
        this.correctOption = correct;
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public char getCorrectOption() { return correctOption; }
}
