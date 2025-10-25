import numpy as np
import scipy.signal as sp
import matplotlib.pyplot as plt
import scipy.io.wavfile as wav

path_sons = './Sons/'
path_HRTF = './HRTF/Elev0/'
filenameSom = 'fala1.wav'
fs, xSom = wav.read(path_sons + filenameSom)

xSom = xSom.astype('int32')

#H0e025a.wav
posOuv = np.array([0, 0])
posFont = np.array([0, 1])

# Calc da distância
d = np.sqrt((posFont[0] - posOuv[0])**2 + (posFont[1] - posOuv[1])**2)

theta = int(np.arctan((posFont[1] - posOuv[1])/(posFont[0] - posOuv[0]))/np.pi*180)
print('d: ', d)
print('theta: ', theta)

#filenameHRTF = np.str('H0e0' + theta + 'a.wav')

filenameHRTF = f'H0e{theta:03.0f}a.wav'
print('filenameHRTF: ', filenameHRTF)

fs, xHRTF = wav.read(path_HRTF + filenameHRTF)
xHRTF = xHRTF.astype('int32')
print('lenxHRTF: ', len(xHRTF))
print('fs2: ', fs)

#sp.resample() Se s freq amostragem não forem iguais

yL = np.convolve(xSom, xHRTF[:, 0])
yR = np.convolve(xSom, xHRTF[:, 1])
print(np.shape(yL))

yOut = np.vstack([yL, yR])
yOut = np.transpose(yOut)
print(np.shape(yOut))

plt.subplot(3, 1, 1)
plt.plot(xSom)
plt.ylabel('Ampli')
plt.xlabel('Time [sec]')

plt.subplot(3, 1, 2)
plt.plot(yL)
plt.ylabel('Ampli')
plt.xlabel('Time [sec]')
plt.subplot(3, 1, 3)
plt.plot(yR)
plt.ylabel('Ampli')
plt.xlabel('Time [sec]')
plt.show()

plt.subplot(2, 1, 1)
plt.plot(xHRTF[:, 0])
plt.subplot(2, 1, 2)
plt.plot(xHRTF[:, 1])
plt.ylabel('Ampli')
plt.xlabel('Time [sec]')
plt.show()

yOut = yOut/np.max(np.abs(yOut))
yOut = yOut * 2**15
yOut = yOut.astype('int16')

wav.write('xSomHRTF.wav', fs, yOut)

