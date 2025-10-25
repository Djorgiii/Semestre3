# Exemplo VBAP - Plano horizontal 2D (slides pp. 43)

import numpy as np
import matplotlib.pyplot as plt

# Definir as posições dos altifalantes em coordenadas polares (raio, ângulo)
# Vamos usar um raio de 1, e os ângulos são -45, 45
r = 1 # raio
speakers = np.array([
    [r, np.pi / 4],  # Altifalante 2: a 45 graus (left)
    [r, np.pi / 4]  # Altifalante 3: a -45 graus (right)
])
# Definir a posição da fonte em coordenadas polares (raio, ângulo)
dist = 2
source = np.array([1, -np.pi/6])
#source = np.array([1, 0])

# VBAP concept
# g = p * L => [g1, g2] = [p1, p2] * [[l11, l12], [l21, l22]]
# g = p * L => [g1, g2] = [px, py] * [[l1x, l1y], [l2x, l2y]]

p1 = np.cos(source[1])/dist
p2 = np.sin(source[1])/dist
P = np.array([[p1, p2]])

l11 = np.cos(speakers[0, 1])
l12 = np.sin(speakers[0, 1])
l21 = np.cos(speakers[1, 1])
l22 = -np.sin(speakers[1, 1])
L = np.array([[l11, l12], [l21, l22]])

print('P: ', P)
print('L: ', L)

# Check if the matrix is invertible by checking if the determinant is non-zero
if np.linalg.det(L) != 0:
    print("Determinant of L:")
    print(np.linalg.det(L))
    # Compute the inverse of the matrix
    L_inv = np.linalg.inv(L)
    print("Inverse of L:")
    print(L_inv)
else:
    print("Matrix is singular and cannot be inverted.")

G = np.dot(P, L_inv)
print('G: ', G)

