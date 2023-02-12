package me.danielml.finalschoolapp.objects;

public class FilterProfile {

    public static final FilterProfile NULL_FALLBACK = new FilterProfile(1, 7, null, null);

    private final int classNum;
    private final int gradeNum;
    private final Subject majorA;
    private final Subject majorB;

    public FilterProfile(int classNum, int gradeNum, Subject majorA, Subject majorB) {
        this.classNum = classNum;
        this.gradeNum = gradeNum;
        this.majorA = majorA;
        this.majorB = majorB;
    }

    public int getClassNum() {
        return classNum;
    }

    public int getGradeNum() {
        return gradeNum;
    }

    public Subject getMajorA() {
        return majorA;
    }

    public Subject getMajorB() {
        return majorB;
    }
}
