# ShopUser

Backend-tjänst för CloudStore som hanterar användarregistrering, inloggning och beställningar.

## Teknikstack

- Java 17, Spring Boot
- Spring Security med JWT (RSA, asymmetrisk signering)
- MySQL (AWS RDS i produktion)
- Docker / Docker Compose
- CI/CD med GitHub Actions, deploy till AWS EC2

## Arkitektur

ShopUser är en av två mikrotjänster i CloudStore:

- **ShopUser** (denna tjänst, port 8080) – hanterar användare, inloggning och beställningar. Är den publika ingångspunkten för frontend.
- **ProductService** (port 8081) – hanterar produkter via FakeStore API. Anropas internt av ShopUser.

ShopUser signerar JWT-tokens med en privat RSA-nyckel vid inloggning. ProductService verifierar dessa tokens med motsvarande publika nyckel vid kommunikation mellan tjänsterna.
