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

  //TODO: Implement this method
  /**
   * Creates a new deck of cards.
   * @returns {Card[]} - An array of cards.
   */
  newDeck() {
    // Values 1..13 for each suit: 1 (Ace), 11 (Jack), 12 (Queen), 13 (King)
    const suits = ["clubs", "diamonds", "hearts", "spades"];
    const deck = [];
    for (const suit of suits) {
      for (let value = 1; value <= 13; value++) {
        deck.push({ suit, value });
      }
    }
    return deck;
  }

  //TODO: Implement this method
  /**
   * Shuffles the deck of cards.
   * @param {Card[]} deck - The deck of cards to be shuffled.
   * @returns {Card[]} - The shuffled deck.
   */
  shuffle(deck) {
    // Create an array of indices 1..52, then randomly pick and remove
    const indices = [];
    for (let i = 1; i <= 52; i++) indices.push(i);

    const shuffled = [];
    for (let i = 0; i < 52; i++) {
      const idxPos = Math.floor(Math.random() * indices.length); // sorteio do índice
      const index = indices[idxPos];
      // index is 1..52 -> convert to zero-based to fetch card
      shuffled.push(deck[index - 1]);
      // remove chosen index
      indices.splice(idxPos, 1);
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

  //TODO: Implement this method
  /**
   * Calculates the total value of the provided cards.
   * @param {Card[]} cards - Array of cards to be evaluated.
   * @returns {number} - The total value of the cards.
   */
  getCardsValue(cards) {
    // Count aces as 11 first; downgrade to 1 if we bust over MAX_POINTS
    let total = 0;
    let aces = 0;
    for (const c of cards) {
      if (!c) continue;
      if (c.value === 1) {
        aces += 1;
        total += 11; // initially count Ace as 11
      } else if (c.value >= 11 && c.value <= 13) {
        total += 10; // J, Q, K
      } else {
        total += c.value; // 2..10
      }
    }
    // Adjust aces from 11 to 1 as needed
    while (total > Blackjack.MAX_POINTS && aces > 0) {
      total -= 10; // convert one Ace from 11 to 1
      aces -= 1;
    }
    return total;
  }

  //TODO: Implement this method
  /**
   * Executes the dealer's move by adding a card to the dealer's array.
   * @returns {Object} - The game state after the dealer's move.
   */
  dealerMove() {
    if (this.deck.length === 0) {
      // If the deck is empty, recreate and shuffle a new one (defensive)
      this.deck = this.shuffle(this.newDeck());
    }
    const card = this.deck.pop();
    this.dealerCards.push(card);
    return this.getGameState();
  }

  //TODO: Implement this method
  /**
   * Executes the player's move by adding a card to the player's array.
   * @returns {Object} - The game state after the player's move.
   */
  playerMove() {
    if (this.deck.length === 0) {
      this.deck = this.shuffle(this.newDeck());
    }
    const card = this.deck.pop();
    this.playerCards.push(card);
    return this.getGameState();
  }

  //TODO: Implement this method
  /**
   * Checks the game state based on the dealer's and player's cards.
   * @returns {Object} - The updated game state.
   */
  getGameState() {
    const dealerPoints = this.getCardsValue(this.dealerCards);
    const playerPoints = this.getCardsValue(this.playerCards);

    // Reset state
    this.state.gameEnded = false;
    this.state.playerWon = false;
    this.state.dealerWon = false;
    this.state.playerBusted = false;
    this.state.dealerBusted = false;

    // Busts
    if (playerPoints > Blackjack.MAX_POINTS) {
      this.state.playerBusted = true;
      this.state.dealerWon = true;
      this.state.gameEnded = true;
      return this.state;
    }
    if (dealerPoints > Blackjack.MAX_POINTS) {
      this.state.dealerBusted = true;
      this.state.playerWon = true;
      this.state.gameEnded = true;
      return this.state;
    }

    // Exact target (25)
    if (playerPoints === Blackjack.MAX_POINTS) {
      this.state.playerWon = true;
      this.state.gameEnded = true;
      return this.state;
    }
    if (dealerPoints === Blackjack.MAX_POINTS) {
      this.state.dealerWon = true;
      this.state.gameEnded = true;
      return this.state;
    }

    // If it's dealer's turn to finish the round, end when dealer reaches stand threshold
    if (this.dealerTurn && dealerPoints >= Blackjack.DEALER_MAX_TURN_POINTS) {
      const diffDealer = Blackjack.MAX_POINTS - dealerPoints;
      const diffPlayer = Blackjack.MAX_POINTS - playerPoints;
      if (diffPlayer < diffDealer) {
        this.state.playerWon = true;
      } else if (diffDealer < diffPlayer) {
        this.state.dealerWon = true;
      } else {
        // tie: no winner set; game still ends
      }
      this.state.gameEnded = true;
    }

    return this.state;
  }
}
