// Blackjack OOP

let game = null; // Stores the current instance of the game

// debug utilities removed

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
  document.getElementById("card").disabled = true; // Disables the button to draw a card
  document.getElementById("stand").disabled = true; // Disables the button to stand
  document.getElementById("new_game").disabled = false; // Enables the button for a new game
}

//TODO: Implement this method.
/**
 * Clears the page to start a new game.
 */
function clearPage() {
  document.getElementById("dealer").innerHTML = "";
  document.getElementById("player").innerHTML = "";
  document.getElementById("game_status").innerHTML = "";
}

//TODO: Complete this method.
/**
 * Starts a new game of Blackjack.
 */
function newGame() {
  game = new Blackjack(); // Creates a new instance of the Blackjack game
  // debug removed

  clearPage();

  // Initial deals: dealer, player, dealer (second dealer card hidden)
  let state = game.dealerMove();
  updateDealer(state);

  state = game.playerMove();
  updatePlayer(state);

  state = game.dealerMove();
  updateDealer(state); // Will render with hidden second card

  buttonsInitialization();
}

//TODO: Implement this method.
/**
 * Calculates and displays the final score of the game.
 * @param {Object} state - The current state of the game.
 */
function finalScore(state) {
  if (!game) return;
  const dealerPts = game.getCardsValue(game.getDealerCards());
  const playerPts = game.getCardsValue(game.getPlayerCards());

  if (state.playerBusted) msg += "Player rebentou. Dealer ganhou.";
  else if (state.dealerBusted) msg += "Dealer rebentou. Player ganhou!";
  else if (state.playerWon && !state.dealerWon) msg += "Player ganhou!";
  else if (state.dealerWon && !state.playerWon) msg += "Dealer ganhou!";
  else msg += "Empate!";

  document.getElementById("game_status").innerText = msg;
}

//TODO: Implement this method.
/**
 * Updates the dealer's state in the game.
 * @param {Object} state - The current state of the game.
 */
function updateDealer(state) {
  if (!game) return;
  const el = document.getElementById("dealer");
  const cards = game.getDealerCards();
  // Clear element and render images
  el.innerHTML = "";
  // Compute visible score (hide 2nd card while it's face down)
  let visibleCards = cards;
  if (state && !state.gameEnded && cards.length >= 2 && !game.dealerTurn) {
    visibleCards = [cards[0]];
    if (cards.length > 2) {
      visibleCards = visibleCards.concat(cards.slice(2));
    }
  }
  const dealerScore = game.getCardsValue(visibleCards);
  const dealerTitle = document.getElementById("dealer_title");
  if (dealerTitle) dealerTitle.textContent = `Dealer (${dealerScore})`;

  for (let i = 0; i < cards.length; i++) {
    const card = cards[i];
    if (state && !state.gameEnded && i === 1 && !game.dealerTurn) {
      // Hide second card as back image
      printCardBack(el);
    } else {
      printCard(el, card);
    }
  }

  if (state && state.gameEnded) {
    const res = document.createElement("div");
    if (state.dealerWon) res.textContent = "— Dealer ganhou!";
    else if (state.playerWon) res.textContent = "— Dealer perdeu!";
    else res.textContent = "— Empate!";
    res.style.marginTop = "8px";
    el.appendChild(res);
    finalizeButtons();
  }

  // debug removed
}

//TODO: Implement this method.
/**
 * Updates the player's state in the game.
 * @param {Object} state - The current state of the game.
 */
function updatePlayer(state) {
  if (!game) return;
  const el = document.getElementById("player");
  const cards = game.getPlayerCards();
  // Clear and render images
  el.innerHTML = "";
  const playerScore = game.getCardsValue(cards);
  const playerTitle = document.getElementById("player_title");
  if (playerTitle) playerTitle.textContent = `Player (${playerScore})`;

  for (const card of cards) {
    printCard(el, card);
  }

  if (state && state.gameEnded) {
    const res = document.createElement("div");
    if (state.playerWon) res.textContent = "— Ganhou!";
    else if (state.dealerWon) res.textContent = "— Perdeu!";
    else res.textContent = "— Empate!";
    res.style.marginTop = "8px";
    el.appendChild(res);
    finalizeButtons();
  }
}

//TODO: Implement this method.
/**
 * Causes the dealer to draw a new card.
 * @returns {Object} - The game state after the dealer's move.
 */
function dealerNewCard() {
  if (!game) return null;
  const state = game.dealerMove();
  updateDealer(state);
  if (state.gameEnded) {
    // Ensure player view also updated and show final score
    updatePlayer(state);
    finalScore(state);
  }
  return state;
}

//TODO: Implement this method.
/**
 * Causes the player to draw a new card.
 * @returns {Object} - The game state after the player's move.
 */
function playerNewCard() {
  if (!game) return null;
  const state = game.playerMove();
  updatePlayer(state);
  if (state.gameEnded) {
    updateDealer(state);
    finalScore(state);
  }
  return state;
}

//TODO: Implement this method.
/**
 * Finishes the dealer's turn.
 */
function dealerFinish() {
  if (!game) return;
  // Declare dealer's turn and evaluate current state
  let state = game.getGameState();
  game.setDealerTurn(true);

  while (!state.gameEnded) {
    updateDealer(state);
    state = game.dealerMove();
  }

  // Final updates when the game ends
  updateDealer(state);
  updatePlayer(state);
  finalScore(state);
}

//TODO: Implement this method.
/**
 * Prints the card in the graphical interface.
 * @param {HTMLElement} element - The element where the card will be displayed.
 * @param {Card} card - The card to be displayed.
 * @param {boolean} [replace=false] - Indicates whether to replace the existing image.
 */
function printCard(element, card, replace = false) {
  if (replace) {
    element.innerHTML = "";
  }
  if (!card) return;
  const img = document.createElement("img");
  const name = valueToName(card.value);
  img.alt = `${name} of ${card.suit}`;
  img.src = `img/png/${name}_of_${card.suit}.png`;
  img.classList.add('card', 'img-fluid', 'me-2');
  img.tabIndex = 0; // make focusable for keyboard users
  element.appendChild(img);

  // Stagger reveal based on current number of .card elements in this container
  const cardsInContainer = Array.from(element.querySelectorAll('.card')).length;
  const delay = Math.min(400, (cardsInContainer - 1) * 120);
  // Allow the browser to register the insert, then add visible class
  setTimeout(() => img.classList.add('card--visible'), 20 + delay);
}

function printCardBack(element) {
  const img = document.createElement('img');
  img.alt = 'Card back';
  img.src = `img/png/card_back.png`;
  img.classList.add('card', 'card-back', 'img-fluid', 'me-2');
  img.tabIndex = 0;
  element.appendChild(img);
  const cardsInContainer = Array.from(element.querySelectorAll('.card')).length;
  const delay = Math.min(400, (cardsInContainer - 1) * 120);
  setTimeout(() => img.classList.add('card--visible'), 20 + delay);
}

// Helpers
function valueToName(value) {
  if (value === 1) return "ace";
  if (value >= 2 && value <= 10) return String(value);
  if (value === 11) return "jack";
  if (value === 12) return "queen";
  return "king";
}

function toCardString(card) {
  const suitMap = { clubs: "♣", diamonds: "♦", hearts: "♥", spades: "♠" };
  const val = card.value;
  const v =
    val === 1
      ? "A"
      : val === 11
      ? "J"
      : val === 12
      ? "Q"
      : val === 13
      ? "K"
      : String(val);
  return `${v}${suitMap[card.suit]}`;
}
