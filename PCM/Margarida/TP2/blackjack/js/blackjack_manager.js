// Blackjack OOP

let game = null; // Stores the current instance of the game

/**
 * Function to debug and display the state of the game object.
 * @param {Object} obj - The object to be debugged.
 */
function debug(obj) {
  document.getElementById("debug").innerHTML = JSON.stringify(obj); // Displays the state of the object as JSON
}

/**
 * Initializes the game buttons.
 */
function buttonsInitialization() {
  document.getElementById("card").disabled = false; // Enables the button to draw a card
  document.getElementById("stand").disabled = false; // Enables the button to stand
  document.getElementById("new_game").disabled = true; // Disables the button for a new game
}

/**
 * Finalizes the buttons after the game ends.
 */
function finalizeButtons() {
  //TODO: Reveal the dealer's hidden card if you hid it like you were supposed to.

  document.getElementById("card").disabled = true; // Disables the button to draw a card
  document.getElementById("stand").disabled = true; // Disables the button to stand
  document.getElementById("new_game").disabled = false; // Enables the button for a new game
}

//TODO: Implement this method.
/**
 * Clears the page to start a new game.
 */
function clearPage() {}

/**
 * Starts a new game of Blackjack.
 */
function newGame() {
  game = new Blackjack();

  // Duas cartas ao dealer, uma ao player; 2ª do dealer fica oculta na UI
  game.dealerMove();
  game.playerMove();
  game.dealerMove();

  // Bind de botões (se existirem)
  const btnPlayer = el("player-new-card");
  const btnDealer = el("dealer-new-card");
  const btnFinish = el("dealer-finish");
  const btnNew = el("new-game");

  if (btnPlayer) btnPlayer.onclick = () => playerNewCard();
  if (btnDealer) btnDealer.onclick = () => dealerNewCard();
  if (btnFinish) btnFinish.onclick = () => dealerFinish();
  if (btnNew) btnNew.onclick = () => newGame();

  updateDealer();
  updatePlayer();
  finalizeButtons();
}

//TODO: Implement this method.
/**
 * Calculates and displays the final score of the game.
 * @param {Object} state - The current state of the game.
 */
function finalScore(state) {}

/**
 * Updates the dealer's state in the game.
 * @param {Object} state - The current state of the game.
 */
function updateDealer(state) {
  if (!game) return;
  const state = game.getGameState();
  const dealerEl = el("dealer");
  if (!dealerEl) return;

  const cards = game.getDealerCards();

  // Esconde a 2ª carta do dealer enquanto o jogo não acabou e ainda não é a vez do dealer
  let shown = cards.map((c, i) =>
    state.gameEnded || game.dealerTurn || i !== 1 ? formatCard(c) : "?"
  );

  let line = `Dealer: ${shown.join(" ")}`;

  if (state.gameEnded) {
    if (state.dealerBusted) line += " — Dealer rebentou";
    else if (state.dealerWon) line += " — Dealer ganhou";
    else if (state.playerWon) line += " — Jogador ganhou";
    else line += " — Empate";
  }
  dealerEl.textContent = line;
  finalizeButtons();
}

/**
 * Updates the player's state in the game.
 * @param {Object} state - The current state of the game.
 */
function updatePlayer(state) {
  if (!game) return;
  const state = game.getGameState();
  const playerEl = el("player");
  if (!playerEl) return;

  const cards = game.getPlayerCards();
  let line = `Player: ${cardsToString(cards)} (=${game.getCardsValue(cards)})`;

  if (state.gameEnded) {
    if (state.playerBusted) line += " — Rebentou";
    else if (state.playerWon) line += " — Ganhou";
    else if (state.dealerWon) line += " — Perdeu";
    else line += " — Empate";
  }

  playerEl.textContent = line;
  finalizeButtons();
}

/**
 * Causes the dealer to draw a new card.
 * @returns {Object} - The game state after the dealer's move.
 */
function dealerNewCard() {
  if (!game) return null;
  const state = game.dealerMove();
  updateDealer();
  return state;
}

/**
 * Causes the player to draw a new card.
 * @returns {Object} - The game state after the player's move.
 */
function playerNewCard() {
  if (!game) return null;
  const state = game.playerMove();
  updatePlayer();
  return state;
}

//TODO: Implement this method.
/**
 * Finishes the dealer's turn.
 */
function dealerFinish() {
  if (!game) return null;

  //passa a vez ao dealer e joga ate terminar
  game.setDealerTurn(true);
  let state = game.getGameState();
  while (!state.gameEnded) {
    updateDealer();
    state = game.dealerMove();
  }
  updateDealer();
  updatePlayer();
  finalizeButtons();
  return state;
}

//TODO: Implement this method.
/**
 * Prints the card in the graphical interface.
 * @param {HTMLElement} element - The element where the card will be displayed.
 * @param {Card} card - The card to be displayed.
 * @param {boolean} [replace=false] - Indicates whether to replace the existing image.
 */
function printCard(element, card, replace = false) {}
