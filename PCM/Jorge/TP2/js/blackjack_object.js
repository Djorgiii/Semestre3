class Blackjack {
  static MAXPOINTS = 25;
  static DEALER_MAX_TURN_POINTS = 21;

  constructor() {
    this.dealerCards = [];
    this.playerCards = [];
    this.dealerTurn = false;

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

  // Gerar baralho de cartas
  newDeck() {
    const naipes = ["hearts", "spades", "diamonds", "clubs"];
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
        deck.push(`${valor}_of_${naipe}`);
      }
    }
    return deck;
  }

  // Embaralha e retorna novo array
  shuffle(deck) {
    const indices = [];
    for (let i = 0; i < deck.length; i++) indices.push(i);

    const baralhado = [];
    while (indices.length > 0) {
      const r = Math.floor(Math.random() * indices.length);
      const idx = indices.splice(r, 1)[0];
      baralhado.push(deck[idx]);
    }
    return baralhado;
  }

  getDealerCards() {
    return this.dealerCards.slice();
  }

  getPlayerCards() {
    return this.playerCards.slice();
  }

  setDealerTurn(val) {
    this.dealerTurn = val;
  }

  getCardsValue(cards) {
    let total = 0;
    let ases = 0;
    for (const c of cards) {
      const parts = c.split("_of_");
      const v = parts[0];
      if (v === "ace") {
        ases += 1;
        total += 11;
      } else if (v === "jack" || v === "queen" || v === "king") {
        total += 10;
      } else {
        const n = parseInt(v, 10);
        total += Number.isNaN(n) ? 0 : n;
      }
    }

    while (total > Blackjack.MAXPOINTS && ases > 0) {
      total -= 10;
      ases -= 1;
    }
    return total;
  }

  dealerMove() {
    const dValue = this.getCardsValue(this.dealerCards);
    if (
      this.deck.length > 0 &&
      !this.state.gameEnded &&
      this.dealerTurn &&
      dValue < Blackjack.DEALER_MAX_TURN_POINTS
    ) {
      const card = this.deck.pop();
      this.dealerCards.push(card);
    }
    return this.getGameState();
  }

  playerMove() {
    if (this.deck.length > 0 && !this.state.gameEnded && !this.dealerTurn) {
      const card = this.deck.pop();
      this.playerCards.push(card);
    }
    return this.getGameState();
  }

  getGameState() {
    this.state.gameEnded = false;
    this.state.playerWon = false;
    this.state.dealerWon = false;
    this.state.playerBusted = false;
    this.state.dealerBusted = false;
    this.state.draw = false;

    const pValue = this.getCardsValue(this.playerCards);
    const dValue = this.getCardsValue(this.dealerCards);

    // Verifica estouro (bust)
    if (pValue > Blackjack.MAXPOINTS && dValue > Blackjack.MAXPOINTS) {
      this.state.playerBusted = true;
      this.state.dealerBusted = true;
      this.state.gameEnded = true;
      this.state.draw = true;
      return this.state;
    }
    if (pValue > Blackjack.MAXPOINTS) {
      this.state.playerBusted = true;
      this.state.gameEnded = true;
      this.state.dealerWon = true;
      return this.state;
    }
    if (pValue === Blackjack.MAXPOINTS) {
      this.state.gameEnded = true;
      this.state.playerWon = true;
      return this.state;
    }
    if (dValue > Blackjack.MAXPOINTS) {
      this.state.dealerBusted = true;
      this.state.gameEnded = true;
      this.state.playerWon = true;
      return this.state;
    }

    // Decisão do vencedor por pontos
    if (this.dealerTurn) {
      if (dValue >= Blackjack.DEALER_MAX_TURN_POINTS) {
        this.state.gameEnded = true;
        if (dValue === pValue) {
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
