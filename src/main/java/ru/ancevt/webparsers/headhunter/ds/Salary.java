package ru.ancevt.webparsers.headhunter.ds;

import ru.ancevt.util.string.ToStringBuilder;

/**
 *
 * @author ancevt
 */
public class Salary {

    private final static String DELIM = "\\s+";
    private final static String DASH = "-";
    private final static String FROM = "от";
    private final static String TO = "до";

    private int from;
    private int to;
    private String unit;
    private String extra;
    private final String source;

    public Salary(String hhSalaryString) {
        this.source = hhSalaryString;
        parse(hhSalaryString);
    }
    
    public final boolean isEmpty() {
        return from == 0 && to == 0;
    }
    
    private void parse(String hhSalaryString) {
        if (hhSalaryString == null || hhSalaryString.length() == 0) {
            return;
        }

        if (isSourceSalaryEmpty(hhSalaryString)) {
            return;
        }

        hhSalaryString = collapseNumbers(hhSalaryString);

        if (hhSalaryString.contains(DASH)) {
            // 170 000-220 000 руб.
            final String[] s1 = hhSalaryString.split(DELIM);
            final String digits = s1[0];

            setUnit(s1[1]);

            final String[] s2 = digits.split(DASH);

            setFrom(Integer.valueOf(s2[0]));
            setTo(Integer.valueOf(s2[1]));

        } else if (fromToExists(hhSalaryString)) {
            // 0  1     2  3     4    5
            // от 80000 до 80000 руб. на руки
            final String[] s = hhSalaryString.split(DELIM, 6);
            setFrom(Integer.valueOf(s[1]));
            setTo(Integer.valueOf(s[3]));
            if (s.length > 4) {
                setUnit(s[4]);
            }
            if (s.length > 5) {
                setExtra(s[5]);
            }

        } else if (hhSalaryString.contains(FROM)) {
            // от 250 000 руб.

            final String[] s = hhSalaryString.split(DELIM);
            setFrom(Integer.valueOf(s[1]));
            setUnit(s[2]);
        } else if (hhSalaryString.contains(TO)) {
            // до 250 000 рую.

            final String[] s = hhSalaryString.split(DELIM);
            setTo(Integer.valueOf(s[1]));
            setUnit(s[2]);
        }
    }

    private static boolean fromToExists(final String source) {
        final boolean fromToContains
                = source.contains(FROM)
                && source.contains(TO);

        final String[] s = source.split(DELIM);
        return s.length > 3 && isN(s[3]) && fromToContains;
    }

    private static String collapseNumbers(final String source) {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(source.charAt(0));

        for (int i = 1; i < source.length(); i++) {
            final char p = source.charAt(i - 1);
            final char c = source.charAt(i);
            final char n = source.charAt(i < source.length() - 1 ? i + 1 : '\0');

            if (isN(p) && c == ' ' && isN(n)) {

            } else {
                stringBuilder.append(c);
            }

        }

        return stringBuilder.toString();
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    private static boolean isN(char c) {
        return Character.isDigit(c);
    }

    private static boolean isN(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isSourceSalaryEmpty(String source) {
        for (int i = 0; i < source.length(); i++) {
            final char c = source.charAt(i);
            if (isN(c)) {
                return false;
            }
        }
        return true;
    }

    public String getSource() {
        return source;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("from", from)
                .append("to", to)
                .append("unit", unit)
                .append("extra", extra)
                .build();
    }

}
