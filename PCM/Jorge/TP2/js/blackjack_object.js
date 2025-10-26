// Blackjack (lógica do jogo)
// Classe que representa o jogo e contém a lógica das regras, baralho e estados.
class Blackjack {
  // Máximo de pontos antes de estourar
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
      // Flags que indicam se alguém atingiu exatamente o máximo (ex.: 25)
      playerReachedMax: false,
      dealerReachedMax: false,
    };

    // Criar e embaralhar o baralho
    this.deck = this.shuffle(this.newDeck());
  }

  // Gera um novo baralho (52 cartas), com nomes compatíveis com as imagens
  // Cria um novo baralho de cartas com os nomes corretos exemplo: 'ace_of_spades'

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

  // Embaralha o baralho recebido e devolve uma cópia embaralhada
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

  // Devolve uma cópia do array de cartas do dealer.
  getDealerCards() {
    return this.dealerCards.slice();
  }

  // Devolve uma cópia do array de cartas do jogador.
  getPlayerCards() {
    return this.playerCards.slice();
  }

  // Define se é a vez do dealer (true) ou do jogador (false).
  setDealerTurn(val) {
    this.dealerTurn = val; // Update the dealer's turn status
  }
  // Calcula o valor total de um conjunto de cartas, tratando ases como 1 ou 11
  // Devolve o valor total das cartas fornecidas, tratando ases como 1 ou 11 conforme necessário.
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
    // Usar Number.isNaN para checagem estrita de NaN
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

  // Lógica para quando o dealer tira uma carta (respeita limites e se é a vez do dealer)
  // Devolve o estado do jogo após a jogada do dealer.
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

  // Quando o jogador pede carta: adiciona ao array do jogador se permitido
  // Devolve o estado do jogo após a jogada do jogador.
  playerMove() {
    if (this.deck.length > 0 && !this.state.gameEnded && !this.dealerTurn) {
      const card = this.deck.pop();
      this.playerCards.push(card);
    }
    return this.getGameState();
  }

  // Calcula e define o estado do jogo com base nas cartas atuais 
  // Calcula e define o estado do jogo com base nas cartas atuais (estouro, vitoria, empate)
  /**
   * Verifica o estado atual do jogo e atualiza as flags em this.state.
   * Retorna o objeto this.state.
   */
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
    // Marcar se alguém atingiu exatamente o máximo (ex.: 25)
    this.state.playerReachedMax = pValue === Blackjack.MAX_POINTS;
    this.state.dealerReachedMax = dValue === Blackjack.MAX_POINTS;

    // Se ambos atingiram exatamente o máximo -> Empate e fim imediato
    if (this.state.playerReachedMax && this.state.dealerReachedMax) {
      this.state.gameEnded = true;
      this.state.draw = true;
      return this.state;
    }

    // Se só o dealer atingiu o máximo durante a sua vez ou jogada, o dealer ganha
    // (se o jogador já não tiver o máximo). Isto será decidido aqui para o fluxo
    // quando o dealer atinge 25 enquanto joga.
    if (this.state.dealerReachedMax && !this.state.playerReachedMax) {
      this.state.gameEnded = true;
      this.state.dealerWon = true;
      return this.state;
    }

    // Se só o jogador atingiu o máximo, não terminamos o jogo aqui 
    // dará ao dealer a oportunidade de jogar e tentar também alcançar 25.

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
