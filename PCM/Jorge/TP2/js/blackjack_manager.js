// Blackjack (gestão da UI)
// Este ficheiro liga a lógica do jogo (classe Blackjack) à interface HTML.
// Contém funções para inicializar o ecrã, atualizar cartas/pontos e gerir botões.

let game = null; // Instância atual do jogo
let prevDealerCount = 0; // Contador anterior de cartas do dealer (para animações)
let prevPlayerCount = 0; // Contador anterior de cartas do jogador (para animações)

// Pausa assíncrona usada para animações (retorna uma Promise)
function delay(ms) {
  return new Promise((res) => setTimeout(res, ms));
}

/**
 * Inicializa os botões do jogo (Carta, Parar, Novo Jogo).
 * Habilita/desabilita conforme o estado inicial de uma ronda.
 */
function buttonsInitialization() {
  const cardBtn = document.getElementById("card");
  const standBtn = document.getElementById("stand");
  const newBtn = document.getElementById("new_game");
  if (cardBtn) {
    cardBtn.disabled = false; // Enables the button to draw a card
    cardBtn.innerText = "Carta";
  }
  if (standBtn) {
    standBtn.disabled = false; // Enables the button to stand
    standBtn.innerText = "Parar";
  }
  if (newBtn) newBtn.disabled = true; // Disables the button for a new game
}

/**
 * Atualiza o estado dos botões quando a ronda termina.
 * Desativa as ações do jogador e ativa o botão de "Novo Jogo".
 */
function finalizeButtons() {
  // Nota: se escondeste a 2ª carta do dealer, aqui podes revelar essa carta.

  const cardBtn = document.getElementById("card");
  const standBtn = document.getElementById("stand");
  const newBtn = document.getElementById("new_game");
  if (cardBtn) cardBtn.disabled = true;
  if (standBtn) standBtn.disabled = true;
  if (newBtn) newBtn.disabled = false;
}

/**
 * Obtém os caminhos prováveis para as imagens de uma carta.
 * Preferimos PNG em "img/png" e fornecemos SVG como fallback em "img/svg".
 * @param {string} card - nome da carta, ex: 'ace_of_spades'
 */
function getCardImagePath(card) {
  const png = `img/png/${card}.png`;
  const svg = `img/svg/${card}.svg`;
  return { png, svg };
}

/**
 * Limpa a interface para começar uma nova ronda.
 * Remove cartas, reseta badges de pontos e limpa mensagens.
 */
function clearPage() {
  const dealerEl = document.getElementById("dealer");
  const playerEl = document.getElementById("player");
  const statusEl = document.getElementById("game_status");
  const playerScoreEl = document.getElementById("player_score");
  if (dealerEl) dealerEl.innerHTML = "";
  if (playerEl) playerEl.innerHTML = "";
  if (statusEl) statusEl.innerHTML = "";
  if (playerScoreEl) playerScoreEl.textContent = "0";
  const dealerScoreEl = document.getElementById("dealer_score");
  if (dealerScoreEl) dealerScoreEl.textContent = "?";
  prevDealerCount = 0;
  prevPlayerCount = 0;
}

function newGame() {
  game = new Blackjack(); // Creates a new instance of the Blackjack game

  // Clear UI
  clearPage();

  // Initial draws: dealer two cards, player one card
  // Dealer first card
  if (game.deck.length > 0) {
    const c1 = game.deck.pop();
    game.dealerCards.push(c1);
  }
  // Dealer second card (hidden)
  if (game.deck.length > 0) {
    const c2 = game.deck.pop();
    game.dealerCards.push(c2);
  }
  // Player one card
  if (game.deck.length > 0) {
    const p1 = game.deck.pop();
    game.playerCards.push(p1);
  }

  // Update UI
  const state = game.getGameState();
  updateDealer(state);
  updatePlayer(state);
  buttonsInitialization();
}

//TODO: Implement this method.
/**
 * Calculates and displays the final score of the game.
 * @param {Object} state - The current state of the game.
 */
function finalScore(state) {
  const pValue = game.getCardsValue(game.playerCards);
  const dValue = game.getCardsValue(game.dealerCards);
  let msg = `Jogador: ${pValue} - Dealer: ${dValue}`;
  if (state.draw) msg += " - Empate!";
  if (state.playerWon) msg += " - Jogador GANHOU!";
  if (state.dealerWon) msg += " - Dealer GANHOU!";
  if (state.playerBusted) msg += " - Jogador ESTOUROU!";
  if (state.dealerBusted) msg += " - Dealer ESTOUROU!";
  const el = document.getElementById("game_status");
  if (el) el.innerText = msg;
}

//TODO: Implement this method.
/**
 * Updates the dealer's state in the game.
 * @param {Object} state - The current state of the game.
 */
function updateDealer(state) {
  const el = document.getElementById("dealer");
  if (!el) return;
  el.innerHTML = "";
  const cards = game.getDealerCards();
  for (let i = 0; i < cards.length; i++) {
    if (i === 1 && !game.dealerTurn && !state.gameEnded) {
      // show card back image
      const img = document.createElement("img");
      img.src = "img/png/card_back.png";
      img.alt = "carta escondida";
      el.appendChild(img);
    } else {
      printCard(el, cards[i]);
      // animate only new appended card (last) when hand grows
      if (i === cards.length - 1 && cards.length > prevDealerCount) {
        const last = el.lastElementChild;
        if (last) last.classList.add("card-enter");
      }
      // add a subtle reveal for second card when dealerTurn starts
      if (i === 1 && game.dealerTurn && !state.gameEnded) {
        const last = el.lastElementChild;
        if (last) last.classList.add("card-reveal");
      }
    }
  }

  // Update dealer score badge: hidden ('?') until dealer turn or game ended
  const dealerScoreEl = document.getElementById("dealer_score");
  if (dealerScoreEl) {
    if (game.dealerTurn || state.gameEnded) {
      dealerScoreEl.textContent = String(game.getCardsValue(cards));
    } else {
      dealerScoreEl.textContent = "?";
    }
  }

  if (state.gameEnded) {
    const span = document.createElement("span");
    span.style.marginLeft = "8px";
    if (state.dealerWon) span.innerText = " - Dealer GANHOU";
    if (state.playerWon) span.innerText = " - Dealer PERDEU";
    el.appendChild(span);
    // Only finalize buttons when the game ends
    finalizeButtons();
  }
  prevDealerCount = cards.length;
}

//TODO: Implement this method.
/**
 * Updates the player's state in the game.
 * @param {Object} state - The current state of the game.
 */
function updatePlayer(state) {
  const el = document.getElementById("player");
  if (!el) return;
  el.innerHTML = "";
  const cards = game.getPlayerCards();
  // Update live score badge
  const scoreEl = document.getElementById("player_score");
  if (scoreEl) {
    const val = game.getCardsValue(cards);
    scoreEl.textContent = String(val);
  }
  for (let i = 0; i < cards.length; i++) {
    const c = cards[i];
    printCard(el, c);
    if (i === cards.length - 1 && cards.length > prevPlayerCount) {
      const last = el.lastElementChild;
      if (last) last.classList.add("card-enter");
    }
  }

  if (state.gameEnded) {
    const span = document.createElement("span");
    span.style.marginLeft = "8px";
    if (state.playerWon) span.innerText = " - Jogador GANHOU";
    if (state.dealerWon) span.innerText = " - Jogador PERDEU";
    el.appendChild(span);
    finalizeButtons();
  }
  prevPlayerCount = cards.length;
}

function dealerNewCard() {
  const state = game.dealerMove();
  updateDealer(state);
  return state;
}


function playerNewCard() {
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
async function dealerFinish() {
  // Disable player actions during dealer turn
  const cardBtn = document.getElementById("card");
  const standBtn = document.getElementById("stand");
  if (cardBtn) cardBtn.disabled = true;
  if (standBtn) standBtn.disabled = true;

  game.setDealerTurn(true);
  let state = game.getGameState();
  updateDealer(state); // this reveals the second card (with reveal anim)

  // small pause before dealer starts drawing
  await delay(450);

  // Dealer draws until game ends with animation delays
  while (!state.gameEnded) {
    state = dealerNewCard();
    // brief delay between cards
    await delay(550);
    if (!state) break;
  }
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
  if (!element) return;
  if (replace) element.innerHTML = "";

  const { png, svg } = getCardImagePath(card);
  const img = document.createElement("img");
  img.src = png; // prefer png; if missing the browser may failover to 404 — svg can be used as fallback by trying both
  img.alt = card;
  // On error, try svg fallback
  img.onerror = function () {
    this.onerror = null;
    this.src = svg;
  };
  element.appendChild(img);
}
