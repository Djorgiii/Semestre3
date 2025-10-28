import numpy as np
import matplotlib.pyplot as plt

import scipy as sp
import scipy.io.wavfile as wav
from scipy import signal


fs, xSom = wav.read('./TestAulaCalVoz.wav')
#xSom = xSom/2**(16-1)
xSom = xSom.astype('int32')
print('fs: ', fs)

iniCal = 900000
endCal = 1000000
endSom = 800000

xCal = xSom[iniCal:endCal]
xSom = xSom[:endSom]/2
#xSom = xSom

Pref = 20*10**(-6)
calSPL = 94

dur = 20*10**(-3)
#BLOCK = int(dur*fs)
#print('BLOCK: ', BLOCK)

BLOCK = 10000

xCal_rms = np.sqrt(np.mean(xCal**2))
print('xCal_rms: ', xCal_rms)
xCal_SPL = 20*np.log10(xCal_rms/Pref)
print('xCal_SPL: ', xCal_SPL)

factSPL = calSPL - xCal_SPL

'''
xSom_rms = np.sqrt(np.mean(xSom**2))
print('xSom_rms: ', xSom_rms)
xSom_SPL = 20*np.log10(xSom_rms/Pref)
print('xSom_SPL: ', xSom_SPL)

xSom_real_SPL = xSom_SPL + factSPL
print('xSom_real_SPL: ', xSom_real_SPL)
'''

def calSPL(x, fctSPL, Pref):
    x_rms = np.sqrt(np.mean(x ** 2))
    x_SPL = 20 * np.log10(x_rms / Pref)

    x_real_SPL = x_SPL + fctSPL

    return x_real_SPL

print('factSPL: ', factSPL)

xSom_realTotal_SPL  = calSPL(xSom, factSPL, Pref)
print('xSom_real_SPL: ', xSom_realTotal_SPL)

xSom_realTotBlck_SPL = []

NBlock = int(len(xSom)/BLOCK)
print('NBlock: ', NBlock)

for i in range(NBlock):
    xSomAux = xSom[i*BLOCK : (i+1)*BLOCK]
    xSom_real_SPL = calSPL(xSomAux, factSPL, Pref)

    xSom_realTotBlck_SPL.append(xSom_real_SPL)
    #xSom_realTotBlck_SPL = np.hstack(xSom_real_SPL)


plt.plot(xSom)
plt.ylabel('Frequency [Hz]')
plt.xlabel('Time [sec]')
plt.show()

plt.plot(xSom_realTotBlck_SPL)
plt.ylabel('Frequency [Hz]')
plt.xlabel('Time [sec]')
plt.show()