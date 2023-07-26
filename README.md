# MiniPay

A contactless payment app based on Minima supporting:
1) pay to an address by scanning a QR code or tapping NFC
2) request a payment to your address by presenting a QR code or your phone's NFC device
3) establish a Layer 2 payment channel using QR codes or NFC and Maxima for P2P messaging.
4) pay and request payments over an existing L2 channel.
5) pay and request payments using NFC over existing L2 channel while offline

# Warning!

**THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND**

This project is currently experimental and released as early preview for initial feedback.
Please do not use it with serious value.

It is recommended to test only on either:
* test networks
* "burner nodes" - nodes with limited value in the wallet that you are prepared to lose

## Limitation

Currently only supported in WRITE MODE with vault unlocked.
Support for pending commands in READ MODE is planned as a priority.

## Roadmap

Some of the next planned features are:
* streamlined settlement
* pending commands in read mode
* transaction history

## Building

This project has dependencies on the MinimaK library.
MinimaK library snapshots are currently only published to GitHub Packages Registry.
To use it you need to have a GitHub account and create an access token with at least read:packages permission.
Put your username and token either in:
* GITHUB_ACTOR and GITHUB_TOKEN environment variables
* gpr.user and gpr.key properties in your gradle.properties file located in ~/.gradle directory

## License

[MIT License](LICENSE)