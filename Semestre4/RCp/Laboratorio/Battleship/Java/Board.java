public final class Board {

    public static final int BOARD_NUMBER_OF_ROWS = 10;
    public static final int BOARD_NUMBER_OF_COLUMNS = 10;
    public static final int BOARD_CELL_WIDTH = 3;

    public static final char BOARD_EMPTY_CELL = '0';
    public static final char BOARD_CELL_SPACING = ' ';
    public static final char BOARD_FILLED_CELL = 'S';
    public static final char BOARD_HIT_CELL = 'H';
    public static final char BOARD_MISS_CELL = 'M';
    public static final char BOARD_FRAME = '#';
    public static final char BOARD_UNKNOWN_CELL = '?';
    public static final String BOARD_SEPARATOR = "  |  ";

    private Board() {} // Prevent instantiation

    // =========================
    // ENUMS
    // =========================

    public enum ShipSizes {
        CARRIER(5),
        BATTLESHIP(4),
        DESTROYER(3),
        SUBMARINE(3),
        PATROLBOAT(2);

        private final int size;

        ShipSizes(int size) {
            this.size = size;
        }

        public int getSize() {
            return size;
        }
    }

    public enum ErrorCodes {
        SUCCESS(""),
        ADD_SHIP_INVALID_COORDINATES("The specified coordinates are invalid!"),
        ADD_SHIP_INVALID_SIZE("Ship is of the wrong size!"),
        ADD_SHIP_ALREADY_OCCUPIED("One of the cells is already occupied!");

        private final String msg;

        ErrorCodes(String msg) {
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }
    }

    // =========================
    // DRAW METHODS
    // =========================

    public static String drawXCoordinates() {

        StringBuilder rendered = new StringBuilder("   ");

        for (int j = 0; j < BOARD_NUMBER_OF_COLUMNS; j++) {
            for (int k = 0; k < BOARD_CELL_WIDTH; k++) {
                if (k == BOARD_CELL_WIDTH / 2)
                    rendered.append(j);
                else
                    rendered.append(" ");
            }
        }

        return rendered.toString();
    }

    public static String drawFrame() {

        StringBuilder rendered = new StringBuilder("  ");
        rendered.append(BOARD_FRAME);

        for (int i = 1; i <= BOARD_NUMBER_OF_COLUMNS * BOARD_CELL_WIDTH + 1; i++) {
            rendered.append(BOARD_FRAME);
        }

        return rendered.toString();
    }

    public static String[] draw(char[][] board) {

        String[] rendered = new String[BOARD_NUMBER_OF_ROWS + 3];

        rendered[0] = drawXCoordinates();
        rendered[1] = drawFrame();

        for (int i = 0; i < BOARD_NUMBER_OF_ROWS; i++) {

            StringBuilder nextLine = new StringBuilder();
            nextLine.append(i).append(" ").append(BOARD_FRAME);

            for (int j = 0; j < BOARD_NUMBER_OF_COLUMNS; j++) {
                for (int k = 0; k < BOARD_CELL_WIDTH; k++) {

                    if (k == BOARD_CELL_WIDTH / 2 ||
                            (board[i][j] != BOARD_EMPTY_CELL &&
                                    board[i][j] != BOARD_UNKNOWN_CELL))
                        nextLine.append(board[i][j]);
                    else
                        nextLine.append(BOARD_CELL_SPACING);
                }
            }

            nextLine.append(BOARD_FRAME);
            rendered[i + 2] = nextLine.toString();
        }

        rendered[BOARD_NUMBER_OF_ROWS + 2] = drawFrame();

        return rendered;
    }

    // =========================
    // PRINT METHODS
    // =========================

    public static void printlnColors(String line) {

        for (char ch : line.toCharArray()) {

            switch (ch) {
                case BOARD_EMPTY_CELL:
                case BOARD_UNKNOWN_CELL:
                case BOARD_CELL_SPACING:
                    System.out.print(Term.TERM_RESET_FONT + ch);
                    break;

                case BOARD_FRAME:
                    System.out.print(Term.TERM_BLUE_BG + ch);
                    break;

                case BOARD_MISS_CELL:
                    System.out.print(Term.TERM_YELLOW_BG + ch);
                    break;

                case BOARD_FILLED_CELL:
                    System.out.print(Term.TERM_GREEN_BG + ch);
                    break;

                case BOARD_HIT_CELL:
                    System.out.print(Term.TERM_RED_BG + ch);
                    break;

                default:
                    System.out.print(Term.TERM_RESET_FONT + ch);
            }
        }

        System.out.println(Term.TERM_RESET_FONT);
    }

    public static void print(char[][] board) {

        String[] rendered = draw(board);
        for (String s : rendered)
            printlnColors(s);
    }

    public static void print(char[][] board1, String board1Label,
                             char[][] board2, String board2Label) {

        String[] rendered1 = draw(board1);
        String[] rendered2 = draw(board2);

        int columns = BOARD_NUMBER_OF_COLUMNS * BOARD_CELL_WIDTH + 4;

        System.out.print(center(board1Label, columns));
        System.out.print(BOARD_SEPARATOR);
        System.out.println(center(board2Label, columns));

        for (int i = 0; i < rendered1.length; i++) {
            printlnColors(
                    padEnd(rendered1[i], columns) +
                            BOARD_SEPARATOR +
                            rendered2[i]
            );
        }
    }

    // =========================
    // GAME LOGIC
    // =========================

    public static ErrorCodes add_ship(char[][] board,
                                      ShipSizes shipSize,
                                      int x0, int x1,
                                      int y0, int y1) {

        if (x0 == x1) {

            if (y1 - y0 + 1 != shipSize.getSize())
                return ErrorCodes.ADD_SHIP_INVALID_SIZE;

            for (int i = y0; i <= y1; i++)
                if (board[i][x0] == BOARD_FILLED_CELL)
                    return ErrorCodes.ADD_SHIP_ALREADY_OCCUPIED;

            for (int i = y0; i <= y1; i++)
                board[i][x0] = BOARD_FILLED_CELL;

        } else if (y0 == y1) {

            if (x1 - x0 + 1 != shipSize.getSize())
                return ErrorCodes.ADD_SHIP_INVALID_SIZE;

            for (int i = x0; i <= x1; i++)
                if (board[y0][i] == BOARD_FILLED_CELL)
                    return ErrorCodes.ADD_SHIP_ALREADY_OCCUPIED;

            for (int i = x0; i <= x1; i++)
                board[y0][i] = BOARD_FILLED_CELL;

        } else {
            return ErrorCodes.ADD_SHIP_INVALID_COORDINATES;
        }

        return ErrorCodes.SUCCESS;
    }

    public static boolean is_hit(char[][] board, int x, int y) {
        return board[y][x] == BOARD_FILLED_CELL;
    }

    public static void mark_hit(char[][] board, int x, int y) {
        board[y][x] = BOARD_HIT_CELL;
    }

    public static void mark_miss(char[][] board, int x, int y) {
        board[y][x] = BOARD_MISS_CELL;
    }

    public static boolean gameover(char[][] board) {

        for (int i = 0; i < BOARD_NUMBER_OF_ROWS; i++)
            for (int j = 0; j < BOARD_NUMBER_OF_COLUMNS; j++)
                if (board[i][j] == BOARD_FILLED_CELL)
                    return false;

        return true;
    }

    public static char[][] newBoard(char symbol) {

        char[][] board = new char[BOARD_NUMBER_OF_ROWS][BOARD_NUMBER_OF_COLUMNS];

        for (int i = 0; i < BOARD_NUMBER_OF_ROWS; i++)
            for (int j = 0; j < BOARD_NUMBER_OF_COLUMNS; j++)
                board[i][j] = symbol;

        return board;
    }

    // =========================
    // STRING HELPERS
    // =========================

    private static String padEnd(String s, int length) {
        return String.format("%-" + length + "s", s);
    }

    private static String center(String s, int width) {
        int padding = (width - s.length()) / 2;
        return padEnd(" ".repeat(Math.max(0, padding)) + s, width);
    }
}