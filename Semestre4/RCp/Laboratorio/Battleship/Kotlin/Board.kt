object Board {

    val BOARD_NUMBER_OF_ROWS = 10
    val BOARD_NUMBER_OF_COLUMNS = 10
    val BOARD_CELL_WIDTH = 3

    val BOARD_EMPTY_CELL = '0'
    val BOARD_CELL_SPACING = ' '
    val BOARD_FILLED_CELL = 'S'
    val BOARD_HIT_CELL = 'H'
    val BOARD_MISS_CELL = 'M'
    val BOARD_FRAME = '#'
    val BOARD_UNKNOWN_CELL = '?'
    val BOARD_SEPARATOR = "  |  "

    enum class ShipSizes(val size: Int) {
        CARRIER(5),
        BATTLESHIP(4),
        DESTROYER(3),
        SUBMARINE(3),
        PATROLBOAT(2)
    }

    enum class ErrorCodes(val msg: String) {
        SUCCESS(""),
        ADD_SHIP_INVALID_COORDINATES("The specified coordinates are invalid!"),
        ADD_SHIP_INVALID_SIZE("Ship is of the wrong size!"),
        ADD_SHIP_ALREADY_OCCUPIED("One of the cells is already occupied!")
    }

    fun draw_x_coordinates(): String {

        var rendered = "   "

        for (j in 0..(BOARD_NUMBER_OF_COLUMNS-1)) {

            for (k in 0..(BOARD_CELL_WIDTH-1)) {

                rendered += if (k == BOARD_CELL_WIDTH / 2) j.toString() else " "
            }
        }

        return rendered
    }

    fun draw_frame(): String {

        var rendered = "  " + BOARD_FRAME

        for (i in 1..BOARD_NUMBER_OF_COLUMNS * BOARD_CELL_WIDTH + 1) {

            rendered += BOARD_FRAME
        }

        return rendered
    }

    fun draw(board: Array<CharArray>): Array<String> {

        val rendered = Array(Board.BOARD_NUMBER_OF_ROWS + 3) {""}

        rendered[0] = draw_x_coordinates()
        rendered[1] = draw_frame()

        for (i in 0..(BOARD_NUMBER_OF_ROWS-1)) {

            var nextLine = ""
            nextLine = "${i} " + BOARD_FRAME
            for (j in 0..(BOARD_NUMBER_OF_COLUMNS-1)) {

                for (k in 0..(BOARD_CELL_WIDTH-1)) {

                    nextLine +=
                        if (k == BOARD_CELL_WIDTH / 2
                            || (board[i][j] != BOARD_EMPTY_CELL && board[i][j] != BOARD_UNKNOWN_CELL))
                            board[i][j].toString()
                        else BOARD_CELL_SPACING
                }
            }

            nextLine += BOARD_FRAME
            rendered[i+2] = nextLine
        }

        rendered[BOARD_NUMBER_OF_ROWS+2] = draw_frame()

        return rendered
    }

    fun println_colors(line: String) {

        for (ch in line) {

            when (ch) {

                BOARD_EMPTY_CELL -> print(Term.TERM_RESET_FONT + ch)
                BOARD_FRAME -> print(Term.TERM_BLUE_BG + ch)
                BOARD_MISS_CELL -> print(Term.TERM_YELLOW_BG + ch)
                BOARD_FILLED_CELL -> print(Term.TERM_GREEN_BG + ch)
                BOARD_HIT_CELL -> print(Term.TERM_RED_BG + ch)
                BOARD_UNKNOWN_CELL -> print(Term.TERM_RESET_FONT + ch)
                BOARD_CELL_SPACING -> print(Term.TERM_RESET_FONT + ch)
                else -> print(Term.TERM_RESET_FONT + ch)
            }
        }

        println(Term.TERM_RESET_FONT)
    }

    fun print(board: Array<CharArray>) {

        val rendered = draw(board)

        for (s in rendered) println_colors(s)
    }

    fun print(board1: Array<CharArray>, board1Label: String, board2: Array<CharArray>, board2Label: String) {

        val rendered1 = draw(board1)
        val rendered2 = draw(board2)
        //val columns = rendered1.maxBy { it -> it.length }.length;
        val columns = BOARD_NUMBER_OF_COLUMNS * BOARD_CELL_WIDTH + 4

        print(board1Label
            .padStart((board1Label.length + columns) / 2)
            .padEnd(columns))
        print(BOARD_SEPARATOR)
        println(board2Label
            .padStart((board1Label.length + columns) / 2)
            .padEnd(columns))

        for (i in 0..rendered1.size-1)
            println_colors(rendered1[i].padEnd(columns) + BOARD_SEPARATOR + rendered2[i])
    }

    fun add_ship(board: Array<CharArray>, shipSize: ShipSizes, x0: Int, x1: Int, y0: Int, y1: Int): ErrorCodes {

        if (x0 == x1) {

            if (y1 - y0 + 1 != shipSize.size) return ErrorCodes.ADD_SHIP_INVALID_SIZE
            for (i in y0..y1) if (board[i][x0] == BOARD_FILLED_CELL) return ErrorCodes.ADD_SHIP_ALREADY_OCCUPIED

            for (i in y0..y1) board[i][x0] = BOARD_FILLED_CELL
        }
        else if (y0 == y1) {

            if (x1 - x0 + 1 != shipSize.size) return ErrorCodes.ADD_SHIP_INVALID_SIZE
            for (i in x0..x1) if (board[y0][i] == BOARD_FILLED_CELL) return ErrorCodes.ADD_SHIP_ALREADY_OCCUPIED

            for (i in x0..x1) board[y0][i] = BOARD_FILLED_CELL
        }
        else return ErrorCodes.ADD_SHIP_INVALID_COORDINATES

        return ErrorCodes.SUCCESS
    }

    fun is_hit(board: Array<CharArray>, x: Int, y: Int) = board[y][x] == BOARD_FILLED_CELL

    fun mark_hit(board: Array<CharArray>, x: Int, y: Int) {

        board[y][x] = BOARD_HIT_CELL
    }

    fun mark_miss(board: Array<CharArray>, x: Int, y: Int) {

        board[y][x] = BOARD_MISS_CELL
    }

    fun gameover(board: Array<CharArray>): Boolean {

        for (i in 0..(BOARD_NUMBER_OF_ROWS-1))
            if (BOARD_FILLED_CELL in board[i]) return false

        return true
    }

    fun new(symbol: Char) = Array(Board.BOARD_NUMBER_OF_ROWS) {
        CharArray(Board.BOARD_NUMBER_OF_ROWS) { symbol }
    }
}