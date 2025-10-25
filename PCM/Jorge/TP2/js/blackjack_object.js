// Blackjack (lógica do jogo)
// Classe que representa o jogo e contém a lógica das regras, baralho e estados.
class Blackjack {
  // Máximo de pontos antes de estourar (configurável)
  static MAX_POINTS = 25;
  // Limite a partir do qual o dealer pára de tirar cartas (regra da casa)
  static DEALER_MAX_TURN_POINTS = 21;

  /**
   * Cria uma instância do jogo e inicializa o baralho embaralhado.
   */
  constructor() {
    this.dealerCards = []; // Cartas do dealer
    this.playerCards = []; // Cartas do jogador
    this.dealerTurn = false; // Indica se é a vez do dealer

    // Estado do jogo (flags que descrevem o resultado)
    this.state = {
      gameEnded: false,
      playerWon: false,
      dealerWon: false,
      playerBusted: false,
      dealerBusted: false,
      draw: false,
    };

    // Criar e embaralhar o baralho
    this.deck = this.shuffle(this.newDeck());
  }

  // Gera um novo baralho (52 cartas), com nomes compatíveis com as imagens
  /**
   * Cria um novo baralho de cartas.
   * @returns {Card[]} - Array com nomes das cartas, ex: 'ace_of_spades'
   */
  newDeck() {
    const naipes = ["spades", "hearts", "diamonds", "clubs"];
    const valores = [
      "ace",
      "2",
      "3",
      "4",
      "5",
      "6",
      "7",
      "8",
      "9",
      "10",
      "jack",
      "queen",
      "king",
    ];
    const deck = [];
    for (const naipe of naipes) {
      for (const valor of valores) {
        // Use the same naming convention as the image files: e.g. 'ace_of_spades'
        deck.push(`${valor}_of_${naipe}`);
      }
    }
    return deck;
  }

  // Embaralha o baralho recebido e devolve uma cópia embaralhada
  /**
   * Embaralha o baralho.
   * @param {Card[]} deck - Baralho original
   * @returns {Card[]} - Baralho embaralhado
   */
  shuffle(deck) {
    // Create array of indices
    const indices = [];
    for (let i = 0; i < deck.length; i++) indices.push(i);

    const shuffled = [];
    while (indices.length > 0) {
      const r = Math.floor(Math.random() * indices.length);
      const idx = indices.splice(r, 1)[0];
      shuffled.push(deck[idx]);
    }
    return shuffled;
  }

  /**
   * Devolve uma cópia do array de cartas do dealer (para não expor referência direta).
   * @returns {Card[]}
   */
  getDealerCards() {
    return this.dealerCards.slice(); // Return a copy of the dealer's cards
  }

  /**
   * Devolve uma cópia do array de cartas do jogador.
   * @returns {Card[]}
   */
  getPlayerCards() {
    return this.playerCards.slice();
  }

  /**
   * Define se é a vez do dealer (true) ou do jogador (false).
   * @param {boolean} val
   */
  setDealerTurn(val) {
    this.dealerTurn = val; // Update the dealer's turn status
  }

  // Calcula o valor total de um conjunto de cartas, tratando ases como 1 ou 11
  /**
   * Calcula o valor das cartas passadas.
   * @param {Card[]} cards
   * @returns {number}
   */
  getCardsValue(cards) {
    let total = 0;
    let aces = 0;
    for (const c of cards) {
      // card names are like 'ace_of_spades' or '10_of_hearts'
      const parts = c.split("_of_");
      const v = parts[0];
      if (v === "ace") {
        aces += 1;
        total += 11; // count as 11 for now
      } else if (v === "jack" || v === "queen" || v === "king") {
        total += 10;
      } else {
        // numeric value
    const n = parseInt(v, 10);
    // Usar Number.isNaN para checagem estrita de NaN (recomendado)
    total += Number.isNaN(n) ? 0 : n;
      }
    }

    // Ajusta ases de 11 para 1 conforme necessário até ficar abaixo do limite
    while (total > Blackjack.MAX_POINTS && aces > 0) {
      total -= 10; // convert one ace from 11 to 1
      aces -= 1;
    }
    return total;
  }

  // Lógica para quando o dealer tira uma carta (respeita limites e se é a vez do dealer)
  /**
   * Dealer tira uma carta (se for permitido) e devolve o estado do jogo.
   * @returns {Object}
   */
  dealerMove() {
    // Dealer draws only if there are cards and dealer hasn't busted and it's the dealer's turn
    const dValue = this.getCardsValue(this.dealerCards);
    if (
      this.deck.length > 0 && !this.state.gameEnded && this.dealerTurn && dValue < Blackjack.DEALER_MAX_TURN_POINTS) {
      const card = this.deck.pop();
      this.dealerCards.push(card);
    }
    return this.getGameState();
  }

  // Quando o jogador pede carta: adiciona ao array do jogador se permitido
  /**
   * Jogador tira uma carta (se for permitido) e devolve o estado do jogo.
   * @returns {Object}
   */
  playerMove() {
    if (this.deck.length > 0 && !this.state.gameEnded && !this.dealerTurn) {
      const card = this.deck.pop();
      this.playerCards.push(card);
    }
    return this.getGameState();
  }

  // Calcula e define o estado do jogo com base nas cartas atuais (busts, wins, empate)
  /**
   * Verifica o estado atual do jogo e atualiza as flags em this.state.
   * Retorna o objeto this.state.
   */
  getGameState() {
    // Reset state flags (preserve dealerTurn)
    this.state.gameEnded = false;
    this.state.playerWon = false;
    this.state.dealerWon = false;
    this.state.playerBusted = false;
    this.state.dealerBusted = false;
    this.state.draw = false;

    const pValue = this.getCardsValue(this.playerCards);
    const dValue = this.getCardsValue(this.dealerCards);

    // Verifica se alguém estourou imediatamente
    // Se ambos estouraram ao mesmo tempo consideramos empate
    if (pValue > Blackjack.MAX_POINTS && dValue > Blackjack.MAX_POINTS) {
      this.state.playerBusted = true;
      this.state.dealerBusted = true;
      this.state.gameEnded = true;
      this.state.draw = true;
      return this.state;
    }
    if (pValue > Blackjack.MAX_POINTS) {
      this.state.playerBusted = true;
      this.state.gameEnded = true;
      this.state.dealerWon = true;
      return this.state;
    }
    if (dValue > Blackjack.MAX_POINTS) {
      this.state.dealerBusted = true;
      this.state.gameEnded = true;
      this.state.playerWon = true;
      return this.state;
    }

    // Se alguém atingiu o máximo exato de pontos ganha imediatamente
    // Se ambos atingiram exatamente o máximo -> Empate
    if (pValue === Blackjack.MAX_POINTS && dValue === Blackjack.MAX_POINTS) {
      this.state.gameEnded = true;
      this.state.draw = true;
      return this.state;
    }

    // Se alguém atingiu o máximo exato de pontos ganha imediatamente
    if (pValue === Blackjack.MAX_POINTS) {
      this.state.gameEnded = true;
      this.state.playerWon = true;
      return this.state;
    }
    if (dValue === Blackjack.MAX_POINTS) {
      this.state.gameEnded = true;
      this.state.dealerWon = true;
      return this.state;
    }

    // Quando é a vez do dealer e ele já atingiu o ponto a partir do qual deve parar,
    // decide-se o vencedor comparando os pontos.
    if (this.dealerTurn) {
      if (dValue >= Blackjack.DEALER_MAX_TURN_POINTS) {
        this.state.gameEnded = true;
        if (dValue === pValue) {
          // Empate explícito
          this.state.draw = true;
        } else if (dValue > pValue) {
          this.state.dealerWon = true;
        } else {
          this.state.playerWon = true;
        }
        return this.state;
      }
    }
    return this.state;
  }
}
