<img src="./ping-logo.png" align="right"/>

# Android app for the Awala Ping Service

The [Awala Ping for Android](https://play.google.com/store/apps/details?id=tech.relaycorp.ping) is an implementation of the [Awala Ping Service](https://specs.awala.network/RS-014), which is meant to help test Awala itself.

This document is aimed at advanced users and (prospective) contributors. We aim to make the app as simple and intuitive as possible, and we're therefore not planning on publishing end-user documentation at this point. To learn more about _using_ Awala, visit [awala.network/users](https://awala.network/users).

## Security and privacy considerations

The items below summarize the security and privacy considerations specific to this app. For a more general overview of the security considerations in Awala, please refer to [RS-019](https://specs.awala.network/RS-019). This app uses the [Awala endpoint library](https://github.com/relaycorp/awala-endpoint-android) (_awaladroid_), so please refer to its documentation for more information on its security and privacy guarantees.

### No personally-identifiable information is stored

This app only stores cryptographically-generated data whose inputs are not derived in any way from personally-identifiable information.

### External communication

This app only communicates with the following:

- The [private gateway](https://play.google.com/store/apps/details?id=tech.relaycorp.gateway) via awaladroid.
- Any public endpoint that the user sends pings to. By default, the Relaycorp-operated public endpoint at `ping.awala.services` is used ([connection parameters file](https://pong-pohttp.awala.services/connection-params.der)).

This app doesn't track usage (for example, using Google Analytics), nor does it use ads.

## Development

The project requires [Android Studio](https://developer.android.com/studio/) 4+.

## Contributing

We love contributions! If you haven't contributed to a Relaycorp project before, please take a minute to [read our guidelines](https://github.com/relaycorp/.github/blob/master/CONTRIBUTING.md) first.
