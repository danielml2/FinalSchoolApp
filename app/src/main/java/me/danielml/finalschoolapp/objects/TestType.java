package me.danielml.finalschoolapp.objects;

public enum TestType {
    BAGROT("בגרות"), MATCONET("מתכונת"), TEST("מבחן"), QUIZ("בוחן"), SECOND_DATE("מועד ב"), NONE("");

    private final String name;

    TestType(String type) {
        this.name = type;
    }

    public static TestType from(String other) {
        if (other.contains("שכבתי"))
            return TEST;

        for (TestType type : values()){
            if (type.name().contains(other))
                return type;
            if (other.contains(type.name))
                return type;
        }

        return NONE;
    }

    public String getEnumName() {
        return this.name();
    }

    public String getName() {
        return name;
    }
}
