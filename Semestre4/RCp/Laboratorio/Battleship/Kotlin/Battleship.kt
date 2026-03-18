import kotlin.system.exitProcess

val myBoard = Board.new(Board.BOARD_EMPTY_CELL)
val opponentBoard = Board.new(Board.BOARD_UNKNOWN_CELL)

fun read_coordinate(coord_name: String, coord_max: Int): Int {

    while (true) {

        print("  - Enter coordinate ${coord_name}: ")
        try {

            val value = readln().toInt()
            if (value < 0 || value >= coord_max) {

                Term.alertln("Error: ${coord_name} must be a positive interger less than ${Board.BOARD_NUMBER_OF_COLUMNS}")
            } else return value
        }
        catch (e : NumberFormatException) {

            Term.alertln("Error: ${coord_name} must be a positive interger less than ${Board.BOARD_NUMBER_OF_COLUMNS}")
        }
    }
}

fun read_coordinates(x_name: String, y_name: String): Pair<Int, Int> {

    val x = read_coordinate(x_name, Board.BOARD_NUMBER_OF_COLUMNS)
    val y = read_coordinate(y_name, Board.BOARD_NUMBER_OF_ROWS)

    return Pair(x, y)
}

@OptIn(ExperimentalStdlibApi::class)
fun read_ship_positions(board: Array<CharArray>) {

    for (shipSize in Board.ShipSizes.entries) {

        while (true) {

            Term.cls()
            println("Current board configuration:")
            Board.print(board)

            println("Enter coordinates for the ${shipSize.name} (size ${shipSize.size}): ")
            val (x0, y0) = read_coordinates("x0", "y0")
            val (x1, y1) = read_coordinates("x1", "y1")

            val errorCode = Board.add_ship(board, shipSize,  x0, x1, y0, y1)
            if (errorCode != Board.ErrorCodes.SUCCESS) {

                Term.alertln("Error: ${errorCode.msg}")
                Thread.sleep(2000)
            }
            else break
        }
    }
}

fun game_init() {

    Term.cls()
    println("Please, place your ships on your board.")
    Thread.sleep(2000)
    read_ship_positions(myBoard)

    Term.cls()
    println("Your board is:")
    Board.print(myBoard)

    // Inform the other player that we are ready to begin.
    Sockets.send_ready()

    println("Waiting for the other player to be ready...")
    // Wait for the other player to tell us that we are ready to begin.
    Sockets.wait_ready()
    Term.successln("Both players are ready!")
}

fun game_play_my_turn(): Boolean {

    println("It's your turn!")
    println("Enter coordinates for the shot:")
    val (x, y) = read_coordinates("x", "y")

    Sockets.send_shot(x, y)
    val res = Sockets.wait_result()
    if (res == Board.BOARD_HIT_CELL) {

        Term.successln("Your shot hit a ship!")
        Board.mark_hit(opponentBoard, x, y)
    }
    else {

        Term.failureln("Your shot missed!")
        Board.mark_miss(opponentBoard, x, y)
    }

    val gameOver = Sockets.wait_gameover()
    if (gameOver) Term.successln("Congratulations! You win!")

    return gameOver
}

fun game_play_opponent_turn(): Boolean {

    println("Waiting for your opponent's play...")
    val (x, y) = Sockets.wait_shot()

    println("The opponent shot at (${y}, ${x})")

    if (Board.is_hit(myBoard, x, y)) {

        Term.failureln("The shot hit a ship!")
        Board.mark_hit(myBoard, x, y)
        Sockets.send_result(Board.BOARD_HIT_CELL)
    }
    else {

        Term.successln("The shot missed!")
        Board.mark_miss(myBoard, x, y)
        Sockets.send_result(Board.BOARD_MISS_CELL)
    }

    val gameOver = Board.gameover(myBoard)
    Sockets.send_gameover(gameOver)
    if (gameOver) Term.failureln("Sorry, you lost!")

    return gameOver
}

fun game_play(turn: Boolean) {

    // Play until the game is over
    var gameOver = false
    var myTurn = turn

    println("Starting game!")

    while(!gameOver) {

        Term.cls()

        Board.print(myBoard, "Your Board", opponentBoard, "Your shots")
        if (myTurn) gameOver = game_play_my_turn()
        else gameOver = game_play_opponent_turn()

        Thread.sleep(2000)

        myTurn = ! myTurn
    }
}

fun usage() {

    println("Use:\n\n\tBatalhaNaval -c SERVER\nor\n\tBatalhaNaval -s")
    exitProcess(1)
}

fun main(args: Array<String>) {

    var myTurn = true

    if (args.size < 1) usage()
    if (args[0] == "-s") {

        // First turn is always of the client
        myTurn = false

        // Create the server socket and wait for a client to connect
        Sockets.create_server()
        println("Waiting for a client to connect...")
        Sockets.wait_client()
        Term.successln("Client connected!")
        Thread.sleep(1000)
        Term.cls()
    }
    else if (args[0] == "-c" && args.size == 2) {

        // Try to connect the server
        println("Trying to connect to the server...")
        Sockets.connect_server(args[1])
        Term.successln("Connected to the server!")
        Thread.sleep(1000)
        Term.cls()
    }
    else usage()

    game_init()
    game_play(myTurn)
}
