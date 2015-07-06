# Spass

Structured illumination Pattern Analyzer and Stripe Slicer.

Version 0.020150706

**Status**: In development.


## Features so far

This tool helps finding the parameters of the structured illumination pattern in a SIM-image.

It calculates fourier- or hartly transform in realtime, showing immediately the results of changes in the SI-parameters.

It is possible to view the transforms in logarithmic scale and to mask the (0, 0)-point.

In multiplication-mode, the result of an element-wise multiplication of the SIM-image and the SI-pattern is shown. Additionally it shows the sum of all pixel values in the multiplication (this will be used for the optimization algorithm to find matching parameters).

TODO:

* auto-optimization of the SI-parameters
* cutting and composing stripes of an image sequence to an resolution enhanced image


## Usage:

Change SI-parameters by typing them into the boxes 'angle', 'phase' and 'wvlen', or by using the mouse wheel when the cursor is behind the digit you want to change.

Change size: Click left image (SI-pattern) and press [*] or [/].  Do this only if no image has been loaded!

Load SIM-image: Drag and drop SIM image into left window (only, if size matches!).  Works only with Grayscale-GIF.

Switch between image, SI-pattern and multiplication: Click with right mouse button on left image; or set focus to left image and press [1], [2] or [3].

Change transform algorithm: Use the dropbox (DHT, FFT, ...).

Mask the (0, 0)-Point in the spectrum: Click the check box 'Mask (0, 0)'.

Switch between linear and logarithmic view of the spectrum: Click the check box 'log'.