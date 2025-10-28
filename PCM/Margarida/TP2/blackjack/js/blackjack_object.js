// Blackjack object

/**
 * Class that represents the Blackjack game.
 */
class Blackjack {
  // Constant that defines the maximum points to avoid busting in Blackjack
  static MAX_POINTS = 25;
  // Constant that defines the point threshold at which the dealer must stand
  static DEALER_MAX_TURN_POINTS = 21;

  /**
   * Creates an instance of Blackjack and initializes the deck.
   */
  constructor() {
    this.dealerCards = []; // Array to hold the dealer's cards
    this.playerCards = []; // Array to hold the player's cards
    this.dealerTurn = false; // Flag to indicate if it's the dealer's turn to play

    // State of the game with information about the outcome
    this.state = {
      gameEnded: false, // Indicates whether the game has ended
      playerWon: false, // Indicates if the player has won
      dealerWon: false, // Indicates if the dealer has won
      playerBusted: false, // Indicates if the player has exceeded MAX_POINTS
      dealerBusted: false, // Indicates if the dealer has exceeded MAX_POINTS
    };

    // Initialize the deck of cards
    this.deck = this.shuffle(this.newDeck()); // Create and shuffle a new deck
  }

  /**
   * Creates a new deck of cards.
   * @returns {Card[]} - An array of cards.
   */
  newDeck() {
    const deck = [];
    // naipes: 0 = paus, 1 = ouros, 2 = copas, 3 = espadas
    for (let suit = 0; suit < 4; suit++) {
      for (let value = 1; value <= 13; value++) {
        deck.push({ value, suit });
      }
    }
    return deck;
  }

  /**
   * Shuffles the deck of cards.
   * @param {Card[]} deck - The deck of cards to be shuffled.
   * @returns {Card[]} - The shuffled deck.
   */
  shuffle(deck) {
    // array de índices 0..deck.length-1
    const indices = [];
    for (let i = 0; i < deck.length; i++) {
      indices.push(i);
    }

    // novo array baralhado
    const shuffled = [];
    for (let i = 0; i < deck.length; i++) {
      // sorteia posição no array de índices
      const r = Math.floor(Math.random() * indices.length);
      const idx = indices[r];

      // adiciona carta correspondente
      shuffled.push(deck[idx]);

      // remove o índice já usado
      indices.splice(r, 1);
    }

    return shuffled;
  }

  /**
   * Returns the dealer's cards.
   * @returns {Card[]} - An array containing the dealer's cards.
   */
  getDealerCards() {
    return this.dealerCards.slice(); // Return a copy of the dealer's cards
  }

  /**
   * Returns the player's cards.
   * @returns {Card[]} - An array containing the player's cards.
   */
  getPlayerCards() {
    return this.playerCards.slice(); // Return a copy of the player's cards
  }

  /**
   * Sets whether it is the dealer's turn to play.
   * @param {boolean} val - Value indicating if it's the dealer's turn.
   */
  setDealerTurn(val) {
    this.dealerTurn = val; // Update the dealer's turn status
  }

  /**
   * Calculates the total value of the provided cards.
   * @param {Card[]} cards - Array of cards to be evaluated.
   * @returns {number} - The total value of the cards.
   */
  getCardsValue(cards) {
    let total = 0;
    let aces = 0;

    for (const { value } of cards) {
      if (value === 1) {
        // Ás
        aces += 1;
        total += 1; // conta como 1 por agora
      } else if (value >= 11) {
        // J, Q, K
        total += 10;
      } else {
        // 2..10
        total += value;
      }
    }

    // Promove ases de 1 para 11 quando não ultrapassa 21
    while (aces > 0 && total + 10 <= Blackjack.MAX_POINTS) {
      total += 10;
      aces -= 1;
    }

    return total;
  }

  /**
   * Executes the dealer's move by adding a card to the dealer's array.
   * @returns {Object} - The game state after the dealer's move.
   */
  dealerMove() {
    if (this.state.gameEnded) return this.state;
    if (this.deck.length === 0) {
      this.deck = this.shuffle(this.newDeck());
    }
    this.dealerCards.push(this.deck.pop());

    return this.getGameState();
  }

  /**
   * Executes the player's move by adding a card to the player's array.
   * @returns {Object} - The game state after the player's move.
   */
  playerMove() {
    if (this.state.gameEnded) return this.state;

    if (this.deck.length === 0) {
      this.deck = this.shuffle(this.newDeck());
    }
    this.playerCards.push(this.deck.pop());

    return this.getGameState();
  }
  setDealerTurn(v) {
    this.dealerTurn = v;
  } // ajuda o controller a passar a vez

  /**
   * Checks the game state based on the dealer's and player's cards.
   * @returns {Object} - The updated game state.
   */
  getGameState() {
    const playerTotal = this.getCardsValue(this.playerCards);
    const dealerTotal = this.getCardsValue(this.dealerCards);

    // reset flags
    this.state.playerWon = false;
    this.state.dealerWon = false;
    this.state.playerBusted = false;
    this.state.dealerBusted = false;
    this.state.gameEnded = false;

    // Condições imediatas do jogador
    if (playerTotal === Blackjack.MAX_POINTS) {
      this.state.playerWon = true;
      this.state.gameEnded = true;
      return this.state;
    }
    if (playerTotal > Blackjack.MAX_POINTS) {
      this.state.playerBusted = true;
      this.state.dealerWon = true;
      this.state.gameEnded = true;
      return this.state;
    }
    if (this.dealerTurn) {
      // Dealer rebenta
      if (dealerTotal > Blackjack.MAX_POINTS) {
        this.state.dealerBusted = true;
        this.state.playerWon = true;
        this.state.gameEnded = true;
        return this.state;
      }
      // Quando o dealer atinge o limiar (>= 21), pode encerrar o jogo
      if (dealerTotal >= Blackjack.DEALER_MAX_TURN_POINTS) {
        if (dealerTotal > playerTotal) {
          this.state.dealerWon = true;
          this.state.gameEnded = true;
        } else if (dealerTotal === playerTotal) {
          // Empate: termina o jogo sem vencedor
          this.state.gameEnded = true;
        }
      }
    }

    return this.state;
  }
}
