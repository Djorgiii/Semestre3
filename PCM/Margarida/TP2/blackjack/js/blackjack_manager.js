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
function clearPage() {
  const dealerEl = document.getElementById("dealer");
  const playerEl = document.getElementById("player");
  const debugEl = document.getElementById("debug");
  if (dealerEl) dealerEl.innerHTML = "";
  if (playerEl) playerEl.innerHTML = "";
  if (debugEl) debugEl.textContent = "";
}

/**
 * Starts a new game of Blackjack.
 */
function newGame() {
  clearPage();

  const statusEl = document.getElementById("game_status");
  if (statusEl) statusEl.textContent = "";

  game = new Blackjack();

  game.dealerMove();
  game.playerMove();
  game.dealerMove();

  // ligar botões existentes
  document.getElementById("card").onclick = () => playerNewCard();
  document.getElementById("stand").onclick = () => dealerFinish();
  document.getElementById("new_game").onclick = () => newGame();

  updateDealer();
  updatePlayer();
  buttonsInitialization(); // habilita Card/Stand para começar
}

//TODO: Implement this method.
/**
 * Calculates and displays the final score of the game.
 * @param {Object} state - The current state of the game.
 */
function finalScore(state) {
  let msg = "";
  if (
    state.playerWon &&
    game.getCardsValue(game.getPlayerCards()) === Blackjack.MAX_POINTS
  )
    msg = "BlackJack! You Won :D";
  else if (state.playerWon) msg = "You Won! :D";
  else if (state.dealerWon) msg = "Dealer Won :(";
  else if (state.playerBusted) msg = "You Lose :(";
  else if (state.dealerBusted) msg = "You Won! :D";
  else msg = "Tie! O.o";
  document.getElementById("game_status").textContent = msg;
}

/**
 * Updates the dealer's state in the game.
 * @param {Object} state - The current state of the game.
 */
function updateDealer(state) {
  if (!game) return;
  const st = game.getGameState();
  const dealerEl = document.getElementById("dealer");
  if (!dealerEl) return;

  dealerEl.innerHTML = "";
  const cards = game.getDealerCards();

  let hasHidden = false;
  cards.forEach((c, i) => {
    const hide = !(st.gameEnded || game.dealerTurn) && i === 1;
    if (hide) {
      hasHidden = true;
      const img = document.createElement("img");
      img.src = "img/png/card_back.png";
      img.alt = "?";
      img.style.height = "60px";
      img.className = "mx-1";
      dealerEl.appendChild(img);
    } else {
      printCard(dealerEl, c, false);
    }
  });

  // soma ao lado das cartas do dealer
  if (hasHidden) {
    dealerEl.innerHTML += " (= ?)";
  } else {
    dealerEl.innerHTML += ` (=${game.getCardsValue(cards)})`;
  }
  if (st.gameEnded) finalizeButtons();
}

/**
 * Updates the player's state in the game.
 * @param {Object} state - The current state of the game.
 */
function updatePlayer() {
  if (!game) return;
  const st = game.getGameState();
  const playerEl = document.getElementById("player");
  if (!playerEl) return;

  playerEl.innerHTML = " ";
  game.getPlayerCards().forEach((c) => printCard(playerEl, c, false));
  playerEl.innerHTML += ` (=${game.getCardsValue(game.getPlayerCards())})`;

  if (st.gameEnded) {
    finalScore(st);
    finalizeButtons();
  }
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
  const st = game.playerMove();
  updatePlayer();
  if (st.gameEnded) {
    updateDealer();
  }
  return st;
}

//TODO: Implement this method.
/**
 * Finishes the dealer's turn.
 */
function dealerFinish() {
  if (!game) return null;
  if (typeof game.setDealerTurn === "function") game.setDealerTurn(true);
  else game.dealerTurn = true;
  let st = game.getGameState();
  while (!st.gameEnded) {
    updateDealer();
    st = game.dealerMove();
  }
  updateDealer();
  updatePlayer();
  finalizeButtons();
  return st;
}

//TODO: Implement this method.
/**
 * Prints the card in the graphical interface.
 * @param {HTMLElement} element - The element where the card will be displayed.
 * @param {Card} card - The card to be displayed.
 * @param {boolean} [replace=false] - Indicates whether to replace the existing image.
 */
function printCard(element, card, replace = false) {
  if (!element || !card) return;
  if (replace) element.innerHTML = "";

  const v = card.value;
  const val =
    v === 1
      ? "ace"
      : v === 11
      ? "jack"
      : v === 12
      ? "queen"
      : v === 13
      ? "king"
      : String(v);
  const suitName = ["clubs", "diamonds", "hearts", "spades"][card.suit];

  // relativo ao HTML blackjack_oop.html
  const src = `img/png/${val}_of_${suitName}.png`;

  const img = document.createElement("img");
  img.src = src;
  img.alt = `${val} of ${suitName}`;
  img.style.height = "60px";
  img.className = "mx-1";
  img.onerror = function () {
    this.replaceWith(document.createTextNode(`${val} of ${suitName}`));
  };
  element.appendChild(img);
}
