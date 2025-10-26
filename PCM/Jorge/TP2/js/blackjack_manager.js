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
  // Revelar a segunda carta do dealer, se estiver escondida

  const cardBtn = document.getElementById("card");
  const standBtn = document.getElementById("stand");
  const newBtn = document.getElementById("new_game");
  if (cardBtn) cardBtn.disabled = true;
  if (standBtn) standBtn.disabled = true;
  if (newBtn) newBtn.disabled = false;
}

/**
 * Obtém os caminhos prováveis para as imagens de uma carta.
 * Coloquei preferência para PNG em "img/png" e fornecemos SVG como fallback em "img/svg" ex: 'ace_of_spades'.
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
  game = new Blackjack(); // Cria uma nova instância do jogo

  // Clear UI
  clearPage();

  // Distribui cartas iniciais: dealer duas cartas, jogador uma carta
  // Primeira carta do dealer
  if (game.deck.length > 0) {
    const c1 = game.deck.pop();
    game.dealerCards.push(c1);
  }
  // Segunda carta do dealer (escondida)
  if (game.deck.length > 0) {
    const c2 = game.deck.pop();
    game.dealerCards.push(c2);
  }
  // Primeira carta do jogador
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

// Calcula e mostra o score final do jogo
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

// Atualiza o estado do dealer no jogo.
function updateDealer(state) {
  const el = document.getElementById("dealer");
  if (!el) return;
  el.innerHTML = "";
  const cards = game.getDealerCards();
  for (let i = 0; i < cards.length; i++) {
    if (i === 1 && !game.dealerTurn && !state.gameEnded) {
      // Mostra a carta virada para baixo se for a segunda carta do dealer e não for a vez do dealer
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

  // Esconde o score do dealer com um '?' até ser a vez do dealer ou o jogo acabar
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
    // Apenas finaliza os botões quando o jogo termina
    finalizeButtons();
  }
  prevDealerCount = cards.length;
}

// Atualiza o estado do jogador no jogo.
function updatePlayer(state) {
  const el = document.getElementById("player");
  if (!el) return;
  el.innerHTML = "";
  const cards = game.getPlayerCards();
  // Atualiza o score do jogador ao vivo
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
  // Se o jogador atingiu exatamente o máximo (ex.: 25), damos ao dealer a
  // oportunidade automática de jogar a sua vez para tentar igualar/ganhar.
  if (state.playerReachedMax) {
    // inicia a vez do dealer que, ao terminar, chamará finalScore
    dealerFinish();
    return state;
  }

  if (state.gameEnded) {
    updateDealer(state);
    finalScore(state);
  }
  return state;
}


// Acaba a vez do dealer.
async function dealerFinish() {
  // Desativa ações do jogador durante a vez do dealer
  const cardBtn = document.getElementById("card");
  const standBtn = document.getElementById("stand");
  if (cardBtn) cardBtn.disabled = true;
  if (standBtn) standBtn.disabled = true;

  game.setDealerTurn(true);
  let state = game.getGameState();
  updateDealer(state); // Revela a carta escondida do dealer

  // Pausa pequena antes do dealer começar a tirar cartas
  await delay(450);

  // O dealer tira cartas até o jogo acabar
  while (!state.gameEnded) {
    state = dealerNewCard();
    // Delay entre cartas para notar animação
    await delay(550);
    if (!state) break;
  }
  updateDealer(state);
  updatePlayer(state);
  finalScore(state);
}

// Imprime a carta na interface gráfica.
function printCard(element, card, replace = false) {
  if (!element) return;
  if (replace) element.innerHTML = "";

  const { png, svg } = getCardImagePath(card);
  const img = document.createElement("img");
  img.src = png; // Preferência por PNG
  img.alt = card;
  // SVG como fallback
  img.onerror = function () {
    this.onerror = null;
    this.src = svg;
  };
  element.appendChild(img);
}
