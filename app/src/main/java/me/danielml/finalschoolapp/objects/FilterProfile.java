package me.danielml.finalschoolapp.objects;

import java.util.Arrays;

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

    /**
     * Checks a test if it passes the filters set in the filter profile.
     * @param test Test to be checked
     * @return If it passes the filters or not.
     */
    public boolean doesPassFilter(Test test) {
        boolean classNumCheck = test.getClassNums().contains(-1) || test.getClassNums().contains(classNum);
        if(!classNumCheck || test.getGradeNum() != gradeNum)
            return false;

        boolean isNotaMajor = Arrays.stream(Subject.majors()).noneMatch(subject -> subject.equals(test.getSubject()));
        if(!isNotaMajor && gradeNum < 12 && test.getSubject() == Subject.SAFROT)
            isNotaMajor = true;
        if(isNotaMajor)
            return true;

        return test.getSubject().equals(majorA) || test.getSubject().equals(majorB) || test.getSubject().equals(Subject.MAGAMOT_A) || test.getSubject().equals(Subject.MAGAMOT_B);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterProfile that = (FilterProfile) o;

        if (classNum != that.classNum) return false;
        if (gradeNum != that.gradeNum) return false;
        if (majorA != that.majorA) return false;
        return majorB == that.majorB;
    }
}
