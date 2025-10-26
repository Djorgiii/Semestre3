// Classe Blackjack: lógica do jogo (baralho, regras e estado)
class Blackjack {
  // Máximo de pontos antes de estourar
  static MAX_POINTS = 25;
  // Limite a partir do qual o dealer pára de tirar cartas (regra da casa)
  static DEALER_MAX_TURN_POINTS = 21;

  /** Inicializa instância e baralho embaralhado. */
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

  // Gera baralho de 52 cartas (ex: 'ace_of_spades')
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
        // Exemplo: 'ace_of_spades'
        deck.push(`${valor}_of_${naipe}`);
      }
    }
    return deck;
  }

  // Embaralha e retorna novo array
  shuffle(deck) {
    // Cria um array de índices
    const indices = [];
    for (let i = 0; i < deck.length; i++) indices.push(i);

    // Embaralha os índices e cria o baralho embaralhado
    const shuffled = [];
    while (indices.length > 0) {
      const r = Math.floor(Math.random() * indices.length);
      const idx = indices.splice(r, 1)[0];
      shuffled.push(deck[idx]);
    }
    return shuffled;
  }

  // Retorna cópia das cartas do dealer
  getDealerCards() {
    return this.dealerCards.slice();
  }

  // Retorna cópia das cartas do jogador
  getPlayerCards() {
    return this.playerCards.slice();
  }

  // Define a vez do dealer (true) ou do jogador (false)
  setDealerTurn(val) {
    this.dealerTurn = val; // Update the dealer's turn status
  }
  // Calcula pontos de uma mão; ases valem 1 ou 11 conforme necessário
  getCardsValue(cards) {
    let total = 0;
    let aces = 0;
    for (const c of cards) {
      // Nomes das cartas 'ace_of_spades' ou '10_of_hearts'
      const parts = c.split("_of_");
      const v = parts[0];
      if (v === "ace") {
        aces += 1;
        total += 11; // Inicialmente conta o ás como 11
      } else if (v === "jack" || v === "queen" || v === "king") {
        total += 10;
      } else {
        // Números de 2 a 10
        const n = parseInt(v, 10);
        total += Number.isNaN(n) ? 0 : n;
      }
    }

    // Ajusta ases de 11 para 1 conforme necessário até ficar abaixo do limite
    while (total > Blackjack.MAX_POINTS && aces > 0) {
      total -= 10; // Converte um ás de 11 para 1
      aces -= 1;
    }
    return total;
  }

  // Dealer tira carta quando for a sua vez e abaixo do limite
  dealerMove() {
    // O dealer tira carta apenas se houver cartas, não estourou e for a vez do dealer
    const dValue = this.getCardsValue(this.dealerCards);
    if (
      this.deck.length > 0 && !this.state.gameEnded && this.dealerTurn && dValue < Blackjack.DEALER_MAX_TURN_POINTS) {
      const card = this.deck.pop();
      this.dealerCards.push(card);
    }
    return this.getGameState();
  }

  // Jogador pede carta se for a sua vez e houver baralho
  playerMove() {
    if (this.deck.length > 0 && !this.state.gameEnded && !this.dealerTurn) {
      const card = this.deck.pop();
      this.playerCards.push(card);
    }
    return this.getGameState();
  }

  // Atualiza e retorna o estado do jogo (bust, vitória, empate, flags)
  getGameState() {
    // Reseta as flags de estado (preserva dealerTurn)
    this.state.gameEnded = false;
    this.state.playerWon = false;
    this.state.dealerWon = false;
    this.state.playerBusted = false;
    this.state.dealerBusted = false;
    this.state.draw = false;

    const pValue = this.getCardsValue(this.playerCards);
    const dValue = this.getCardsValue(this.dealerCards);

  // Verifica estouro (bust)
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
    // Jogador com exatamente MAX_POINTS -> vitória imediata
    if (pValue === Blackjack.MAX_POINTS) {
      this.state.gameEnded = true;
      this.state.playerWon = true;
      return this.state;
    }
    if (dValue > Blackjack.MAX_POINTS) {
      this.state.dealerBusted = true;
      this.state.gameEnded = true;
      this.state.playerWon = true;
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
