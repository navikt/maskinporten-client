# maskinporten-client
This library provides a pre-built solution for fetching tokens from Maskinporten.

---
### Instructions
Create a MaskinportenConfig instance. This takes following parameters
* Maskinporten base URL (https://maskinporten.no in prod).
* The application's client-id.
* The application's private RSAKey.
* The number of seconds for the token to be valid.
* (Optional) Proxy to use when contacting Maskinporten.
* (Optional) JTI for the token.
* (Optional) Resource for the token.

Then create a MaskinportenClient instance using your config object.

To generate a token, use the getToken or getTokenString method of the client object. This takes the desired token scope(s) as parameter.

---

For questions, contact [Mathias Sand Jahren](https://teamkatalog.nais.adeo.no/resource/J156788?source=slackprofile).
