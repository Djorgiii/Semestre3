import java.util.Scanner;

public class Battleship {

    static char[][] myBoard = Board.newBoard(Board.BOARD_EMPTY_CELL);
    static char[][] opponentBoard = Board.newBoard(Board.BOARD_UNKNOWN_CELL);

    static Scanner scanner = new Scanner(System.in);

    public static int readCoordinate(String coordName, int coordMax) {

        while (true) {
            System.out.print("  - Enter coordinate " + coordName + ": ");
            try {
                int value = Integer.parseInt(scanner.nextLine());
                if (value < 0 || value >= coordMax) {
                    Term.alertln("Error: " + coordName +
                            " must be a positive integer less than " +
                            Board.BOARD_NUMBER_OF_COLUMNS);
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                Term.alertln("Error: " + coordName +
                        " must be a positive integer less than " +
                        Board.BOARD_NUMBER_OF_COLUMNS);
            }
        }
    }

    public static int[] readCoordinates(String xName, String yName) {

        int x = readCoordinate(xName, Board.BOARD_NUMBER_OF_COLUMNS);
        int y = readCoordinate(yName, Board.BOARD_NUMBER_OF_ROWS);

        return new int[]{x, y};
    }

    public static void readShipPositions(char[][] board) throws InterruptedException {

        for (Board.ShipSizes shipSize : Board.ShipSizes.values()) {

            while (true) {

                Term.cls();
                System.out.println("Current board configuration:");
                Board.print(board);

                System.out.println("Enter coordinates for the " +
                        shipSize.name() + " (size " + shipSize.getSize() + "): ");

                int[] c0 = readCoordinates("x0", "y0");
                int[] c1 = readCoordinates("x1", "y1");

                int x0 = c0[0];
                int y0 = c0[1];
                int x1 = c1[0];
                int y1 = c1[1];

                Board.ErrorCodes errorCode =
                        Board.add_ship(board, shipSize, x0, x1, y0, y1);

                if (errorCode != Board.ErrorCodes.SUCCESS) {

                    Term.alertln("Error: " + errorCode.getMsg());
                    Thread.sleep(2000);
                } else break;
            }
        }
    }

    public static void gameInit() throws InterruptedException {

        Term.cls();
        System.out.println("Please, place your ships on your board.");
        Thread.sleep(2000);

        readShipPositions(myBoard);

        Term.cls();
        System.out.println("Your board is:");
        Board.print(myBoard);

        Sockets.send_ready();

        System.out.println("Waiting for the other player to be ready...");
        Sockets.wait_ready();

        Term.successln("Both players are ready!");
    }

    public static boolean gamePlayMyTurn() throws InterruptedException {

        System.out.println("It's your turn!");
        System.out.println("Enter coordinates for the shot:");

        int[] coords = readCoordinates("x", "y");
        int x = coords[0];
        int y = coords[1];

        Sockets.send_shot(x, y);
        char res = Sockets.wait_result();

        if (res == Board.BOARD_HIT_CELL) {

            Term.successln("Your shot hit a ship!");
            Board.mark_hit(opponentBoard, x, y);
        } else {

            Term.failureln("Your shot missed!");
            Board.mark_miss(opponentBoard, x, y);
        }

        boolean gameOver = Sockets.wait_gameover();
        if (gameOver) Term.successln("Congratulations! You win!");

        return gameOver;
    }

    public static boolean gamePlayOpponentTurn() {

        System.out.println("Waiting for your opponent's play...");
        int[] shot = Sockets.wait_shot();

        int x = shot[0];
        int y = shot[1];

        System.out.println("The opponent shot at (" + y + ", " + x + ")");

        if (Board.is_hit(myBoard, x, y)) {

            Term.failureln("The shot hit a ship!");
            Board.mark_hit(myBoard, x, y);
            Sockets.send_result(Board.BOARD_HIT_CELL);
        } else {

            Term.successln("The shot missed!");
            Board.mark_miss(myBoard, x, y);
            Sockets.send_result(Board.BOARD_MISS_CELL);
        }

        boolean gameOver = Board.gameover(myBoard);
        Sockets.send_gameover(gameOver);

        if (gameOver) Term.failureln("Sorry, you lost!");

        return gameOver;
    }

    public static void gamePlay(boolean turn) throws InterruptedException {

        boolean gameOver = false;
        boolean myTurn = turn;

        System.out.println("Starting game!");

        while (!gameOver) {

            Term.cls();
            Board.print(myBoard, "Your Board", opponentBoard, "Your shots");

            if (myTurn)
                gameOver = gamePlayMyTurn();
            else
                gameOver = gamePlayOpponentTurn();

            Thread.sleep(2000);
            myTurn = !myTurn;
        }
    }

    public static void usage() {

        System.out.println("Use:\n\n\tBattleship -c SERVER\nor\n\tBattleship -s");
        System.exit(1);
    }

    public static void main(String[] args) throws InterruptedException {

        boolean myTurn = true;

        if (args.length < 1) usage();

        if (args[0].equals("-s")) {

            myTurn = false;

            Sockets.create_server();
            System.out.println("Waiting for a client to connect...");
            Sockets.wait_client();

            Term.successln("Client connected!");
            Thread.sleep(1000);
            Term.cls();

        } else if (args[0].equals("-c") && args.length == 2) {

            System.out.println("Trying to connect to the server...");
            Sockets.connect_server(args[1]);

            Term.successln("Connected to the server!");
            Thread.sleep(1000);
            Term.cls();

        } else usage();

        gameInit();
        gamePlay(myTurn);
    }
}