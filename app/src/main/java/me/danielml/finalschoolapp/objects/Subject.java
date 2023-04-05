package me.danielml.finalschoolapp.objects;


public enum Subject {

    PHYSICS(new String[]{"פיזיקה","פיסיקה"}),MEKRA(new String[]{"תנך","מקרא"}),MATH(new String[]{"מתמטיקה"}),CS(new String[]{"מדעי המחשב"}),BIOLOGY(new String[]{"בילוגיה"}),HEBREW(new String[]{"עברית"})
    ,HISTORY(new String[]{"היסטוריה","הסטוריה"}),TAVURA(new String[]{"תיאוריה"}), CHEMISTRY(new String[]{"כימיה"}),PHILOSOPHY(new String[]{"פילוסופיה"}),
    MAGAMOT_B(new String[]{"אשכול ב","אשכולב","מגמות ב"}),MAGAMOT_A(new String[]{"אשכול א","אשכולא","מגמות א"}),SAFROT(new String[]{"ספרות"}),ENGLISH(new String[]{"אנגלית"}),
    CITIZENSHIP(new String[]{"אזרחות"}),SPORTS(new String[]{"חדר כושר","ספורט"}),THEATER(new String[]{"תיאטרון"}),ARABIC(new String[]{"ערבית"}),
    GEOGRAPHY(new String[]{"גיאוגרפיה","גאוגרפיה"}),
    PSYCHOLOGY(new String[]{"פסיכולוגיה"}),FILM(new String[]{"תקשורת"}),SCIENCE(new String[]{"מדעים"}),TARBUT_ISRAEL(new String[]{"תרבות ישראל"}),
    ANATOMY(new String[]{"אנטומיה"}),OTHER(new String[]{"אחר"});

    private final String[] names;

    public static Subject from(String other) {
        if(other == null)
            return null;

        for(Subject s : values())
        {
            if(other.contains(s.name()))
                return s;
            for(String subName : s.names)
                if(subName.equalsIgnoreCase(other) || other.contains(subName))
                    return s;
        }

        return OTHER;
    }

    public String getDefaultName() {
        return this.names[0];
    }

    Subject(String[] names) {
        this.names = names;
    }

    public static Subject[] majors() {
        return new Subject[]{Subject.PHYSICS, Subject.CS, Subject.BIOLOGY, Subject.CHEMISTRY, Subject.PHILOSOPHY, Subject.SAFROT, Subject.THEATER, Subject.ARABIC, Subject.GEOGRAPHY, Subject.PSYCHOLOGY, Subject.FILM};
    }
}
