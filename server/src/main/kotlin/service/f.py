import sys
import chess

def san_to_uci(pgn_moves):
    board = chess.Board()
    uci_moves = []

    for san_move in pgn_moves:
        try:
            move = board.parse_san(san_move)  # Convert SAN to a Move object
            uci_moves.append(move.uci())  # Convert to UCI notation
            board.push(move)  # Update board position
        except ValueError:
            print(f"Invalid move: {san_move}", file=sys.stderr)
            return []  # Return empty list if there's an error

    return uci_moves


if __name__ == "__main__":
    if len(sys.argv) > 1:
        pgn_moves = sys.argv[1:]  # Get PGN moves as a list
        uci_moves = san_to_uci(pgn_moves)
        print(" ".join(uci_moves))  # Print UCI moves as space-separated string
    else:
        print("No PGN moves provided.", file=sys.stderr)
