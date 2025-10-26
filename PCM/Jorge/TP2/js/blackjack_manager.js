// UI controller: liga a classe Blackjack à interface HTML

let game = null; // Instância atual do jogo
let prevDealerCount = 0; // Contador anterior de cartas do dealer (para animações)
let prevPlayerCount = 0; // Contador anterior de cartas do jogador (para animações)

// Pausa usada em animações
function delay(ms) {
  return new Promise((res) => setTimeout(res, ms));
}

// Inicializa estados dos botões (Carta, Parar, Novo Jogo)
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

// Atualiza botões ao terminar a ronda
function finalizeButtons() {

  const cardBtn = document.getElementById("card");
  const standBtn = document.getElementById("stand");
  const newBtn = document.getElementById("new_game");
  if (cardBtn) cardBtn.disabled = true;
  if (standBtn) standBtn.disabled = true;
  if (newBtn) newBtn.disabled = false;
}

// Retorna caminhos (PNG e SVG) para a imagem da carta
function getCardImagePath(card) {
  const png = `img/png/${card}.png`;
  const svg = `img/svg/${card}.svg`;
  return { png, svg };
}

// Limpa a UI para iniciar nova ronda
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

  // Inicia jogo e distribui cartas: jogador, dealer, jogador, dealer
  // A 2ª carta do dealer fica escondida
  if (game.deck.length > 0) {
    const p1 = game.deck.pop();
    game.playerCards.push(p1);
  }
  // 1ª carta dealer
  if (game.deck.length > 0) {
    const d1 = game.deck.pop();
    game.dealerCards.push(d1);
  }
  // 2ª carta jogador
  if (game.deck.length > 0) {
    const p2 = game.deck.pop();
    game.playerCards.push(p2);
  }
  // 2ª carta dealer (escondida)
  if (game.deck.length > 0) {
    const d2 = game.deck.pop();
    game.dealerCards.push(d2);
  }

  // Update UI
  const state = game.getGameState();
  updateDealer(state);
  updatePlayer(state);
  buttonsInitialization();
}

// Mostra o score final e mensagem de resultado
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

// Atualiza a área do dealer na UI
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

  // Mostra '?' até ser a vez do dealer ou o jogo terminar
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

// Atualiza a área do jogador na UI
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

// Pede nova carta para o dealer e atualiza UI
function dealerNewCard() {
  const state = game.dealerMove();
  updateDealer(state);
  return state;
}


function playerNewCard() {
  const state = game.playerMove();
  updatePlayer(state);
  // Se o jogo terminou, revela a carta do dealer e mostra o resultado
  if (state.gameEnded) {
    updateDealer(state);
    finalScore(state);
  }
  return state;
}


// Executa a vez completa do dealer (tira cartas até terminar)
async function dealerFinish() {
  // Desativa ações do jogador durante a vez do dealer
  const cardBtn = document.getElementById("card");
  const standBtn = document.getElementById("stand");
  if (cardBtn) cardBtn.disabled = true;
  if (standBtn) standBtn.disabled = true;

  game.setDealerTurn(true);
  let state = game.getGameState();
  updateDealer(state); // Revela a carta escondida do dealer

  // Pequena pausa antes do dealer tirar cartas
  await delay(450);

  // Dealer tira cartas até o estado indicar fim
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

// Renderiza imagem da carta na UI
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
