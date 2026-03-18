public final class Term {

    public static final String TERM_CSI = "\u001b[";

    public static final String TERM_ALERT_FONT = TERM_CSI + "31m";
    public static final String TERM_RESET_FONT = TERM_CSI + "0m";
    public static final String TERM_SUCCESS_FONT = TERM_CSI + "94m";
    public static final String TERM_FAILURE_FONT = TERM_CSI + "93m";

    public static final String TERM_BLUE_BG = TERM_CSI + ";44m";
    public static final String TERM_GREEN_BG = TERM_CSI + ";42m";
    public static final String TERM_RED_BG = TERM_CSI + ";41m";
    public static final String TERM_YELLOW_BG = TERM_CSI + ";103m";

    private Term() {
        // Prevent instantiation
    }

    public static void cls() {
        System.out.println("".repeat(100));
    }

    public static void alert(String msg) {
        System.out.print(TERM_ALERT_FONT + msg);
    }

    public static void alertln(String msg) {
        System.out.println(TERM_ALERT_FONT + msg + TERM_RESET_FONT);
    }

    public static void success(String msg) {
        System.out.print(TERM_SUCCESS_FONT + msg + TERM_RESET_FONT);
    }

    public static void successln(String msg) {
        System.out.println(TERM_SUCCESS_FONT + msg + TERM_RESET_FONT);
    }

    public static void failure(String msg) {
        System.out.print(TERM_FAILURE_FONT + msg + TERM_RESET_FONT);
    }

    public static void failureln(String msg) {
        System.out.println(TERM_FAILURE_FONT + msg + TERM_RESET_FONT);
    }
}