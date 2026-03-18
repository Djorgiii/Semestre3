object Term {

    val TERM_CSI = "\u001b["
    val TERM_ALERT_FONT = TERM_CSI + "31m"
    val TERM_RESET_FONT = TERM_CSI + "0m"
    val TERM_SUCCESS_FONT = TERM_CSI + "94m"
    val TERM_FAILURE_FONT = TERM_CSI + "93m"
    val TERM_BLUE_BG = TERM_CSI + ";44m"
    val TERM_GREEN_BG = TERM_CSI + ";42m"
    val TERM_RED_BG = TERM_CSI + ";41m"
    val TERM_YELLOW_BG = TERM_CSI + ";103m"

    fun cls() = println("".padEnd(100, '\n'))

    fun alert(msg: String) = print(TERM_ALERT_FONT + msg)
    fun alertln(msg: String) = println(TERM_ALERT_FONT + msg + TERM_RESET_FONT)
    fun success(msg: String) = print(TERM_SUCCESS_FONT + msg + TERM_RESET_FONT)
    fun successln(msg: String) = println(TERM_SUCCESS_FONT + msg + TERM_RESET_FONT)
    fun failure(msg: String) = print(TERM_FAILURE_FONT + msg + TERM_RESET_FONT)
    fun failureln(msg: String) = println(TERM_FAILURE_FONT + msg + TERM_RESET_FONT)

}