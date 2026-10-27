# Dartboard Automatic Calculator

<p align="center">
  <img src=".github/logo.png" alt="dartboard-automatic-calculator logo" width="128" />
</p>

A Java desktop program that scores darts automatically from photos of a
dartboard.

## What it does

- `CalibratorUI` lets you calibrate the board geometry on an image by
  dragging points.
- `BoardMapper` maps image positions to dartboard regions.
- `DartboardAnalyzer` compares images to find where darts landed,
  using edge and blob detection.
- `ScoreCalculator` works out the score.

Sample photos and the intermediate images (calibration, difference and
region bitmaps) are in the root of the repository.

## Tech

- Java with AWT and Swing
- Image filters from the JH Labs library

## Building

Import the Eclipse project and run the classes in the
`dartboardextreme` package.

## License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE).
