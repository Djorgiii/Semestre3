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
    const suits = ["spades", "hearts", "diamonds", "clubs"];
    const values = [
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
    for (const suit of suits) {
      for (const value of values) {
        // Use the same naming convention as the image files: e.g. 'ace_of_spades'
        deck.push(`${value}_of_${suit}`);
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
        total += isNaN(n) ? 0 : n;
      }
    }

    // Reduce aces from 11 to 1 as needed until under MAX_POINTS
    while (total > Blackjack.MAX_POINTS && aces > 0) {
      total -= 10; // convert one ace from 11 to 1
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
    // Dealer draws only if there are cards and dealer hasn't busted and it's the dealer's turn
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

  //TODO: Implement this method
  /**
   * Executes the player's move by adding a card to the player's array.
   * @returns {Object} - The game state after the player's move.
   */
  playerMove() {
    if (this.deck.length > 0 && !this.state.gameEnded && !this.dealerTurn) {
      const card = this.deck.pop();
      this.playerCards.push(card);
    }
    return this.getGameState();
  }

  //TODO: Implement this method
  /**
   * Checks the game state based on the dealer's and player's cards.
   * @returns {Object} - The updated game state.
   */
  getGameState() {
    // Reset state flags (preserve dealerTurn)
    this.state.gameEnded = false;
    this.state.playerWon = false;
    this.state.dealerWon = false;
    this.state.playerBusted = false;
    this.state.dealerBusted = false;

    const pValue = this.getCardsValue(this.playerCards);
    const dValue = this.getCardsValue(this.dealerCards);

    // Check immediate busts
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

    // If player reached max points -> player wins immediately
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

    // If dealer's turn and dealer has reached or exceeded the dealer threshold, decide winner
    if (this.dealerTurn) {
      // If dealer already has enough points to stand, compare with player
      if (dValue >= Blackjack.DEALER_MAX_TURN_POINTS) {
        this.state.gameEnded = true;
        // Dealer wins ties
        if (dValue >= pValue) {
          this.state.dealerWon = true;
        } else {
          this.state.playerWon = true;
        }
        return this.state;
      }
    }

    // No one has won yet
    return this.state;
  }
}
